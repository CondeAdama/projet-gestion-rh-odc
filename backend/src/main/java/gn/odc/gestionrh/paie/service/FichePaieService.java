package gn.odc.gestionrh.paie.service;

import gn.odc.gestionrh.common.enums.StatutContrat;
import gn.odc.gestionrh.common.enums.StatutEntite;
import gn.odc.gestionrh.common.exception.AccesRefuseException;
import gn.odc.gestionrh.common.exception.RegleMetierException;
import gn.odc.gestionrh.common.exception.RessourceNonTrouveeException;
import gn.odc.gestionrh.common.service.ServiceSuppression;
import gn.odc.gestionrh.configuration.service.ConfigurationEntrepriseService;
import gn.odc.gestionrh.contrat.entity.Contrat;
import gn.odc.gestionrh.contrat.repository.ContratRepository;
import gn.odc.gestionrh.employe.dto.EmployeDTO;
import gn.odc.gestionrh.employe.entity.Employe;
import gn.odc.gestionrh.employe.repository.EmployeRepository;
import gn.odc.gestionrh.paie.dto.FichePaieDTO;
import gn.odc.gestionrh.paie.entity.FichePaie;
import gn.odc.gestionrh.paie.repository.FichePaieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FichePaieService {

    private static final BigDecimal TAUX_CNSS_DEFAUT = new BigDecimal("5.00");
    private static final BigDecimal TAUX_RTS_DEFAUT = new BigDecimal("10.00");
    private static final String[] MOIS = {
            "", "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
            "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
    };

    private final FichePaieRepository fichePaieRepository;
    private final EmployeRepository employeRepository;
    private final ContratRepository contratRepository;
    private final ConfigurationEntrepriseService configurationEntrepriseService;

    @Transactional(readOnly = true)
    public List<FichePaieDTO> listerToutes() {
        return fichePaieRepository.findByStatut(StatutEntite.ACTIF).stream()
                .map(this::versDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<FichePaieDTO> listerParEmploye(Long employeId) {
        return fichePaieRepository.findByEmployeIdAndStatut(employeId, StatutEntite.ACTIF).stream()
                .map(this::versDTO).toList();
    }

    @Transactional(readOnly = true)
    public FichePaieDTO trouverParId(Long id) {
        FichePaie f = fichePaieRepository.findById(id)
                .filter(fp -> fp.getStatut() == StatutEntite.ACTIF)
                .orElseThrow(() -> new RessourceNonTrouveeException("Fiche de paie introuvable"));
        return versDTO(f);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> statistiques() {
        List<FichePaie> fiches = fichePaieRepository.findByStatut(StatutEntite.ACTIF);
        BigDecimal masse = fiches.stream().map(FichePaie::getSalaireNet).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCnss = fiches.stream().map(FichePaie::getCotisationCnss).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRts = fiches.stream().map(FichePaie::getImpotRts).reduce(BigDecimal.ZERO, BigDecimal::add);
        var config = configurationEntrepriseService.obtenir();
        return Map.of(
                "totalFiches", fiches.size(),
                "masseSalariale", masse,
                "totalCnss", totalCnss,
                "totalRts", totalRts,
                "tauxCnss", config.getTauxCnss() != null ? config.getTauxCnss() : TAUX_CNSS_DEFAUT,
                "tauxRts", config.getTauxRts() != null ? config.getTauxRts() : TAUX_RTS_DEFAUT,
                "numeroCnss", config.getNumeroCnss() != null ? config.getNumeroCnss() : ""
        );
    }

    @Transactional
    public FichePaieDTO generer(Long employeId, int mois, int annee) {
        if (mois < 1 || mois > 12) {
            throw new RegleMetierException("Mois invalide (1-12)");
        }

        fichePaieRepository.findByEmployeIdAndPeriodeMoisAndPeriodeAnneeAndStatut(
                employeId, mois, annee, StatutEntite.ACTIF
        ).ifPresent(f -> {
            throw new RegleMetierException("Une fiche de paie existe déjà pour cette période");
        });

        Employe employe = employeRepository.findById(employeId)
                .filter(e -> e.getStatut() == StatutEntite.ACTIF)
                .orElseThrow(() -> new RessourceNonTrouveeException("Employé introuvable"));

        Contrat contrat = contratRepository
                .findByEmployeIdAndStatutContratAndStatut(employeId, StatutContrat.ACTIF, StatutEntite.ACTIF)
                .orElseThrow(() -> new RegleMetierException("Aucun contrat actif pour cet employé"));

        BigDecimal transport = nz(contrat.getIndemniteTransport());
        BigDecimal logement = nz(contrat.getIndemniteLogement());
        BigDecimal autres = nz(contrat.getAutresAvantages());
        BigDecimal brut = contrat.getSalaireBase().add(transport).add(logement).add(autres);
        var config = configurationEntrepriseService.obtenir();
        BigDecimal tauxCnss = nz(config.getTauxCnss(), TAUX_CNSS_DEFAUT);
        BigDecimal tauxRts = nz(config.getTauxRts(), TAUX_RTS_DEFAUT);
        BigDecimal cnss = brut.multiply(tauxCnss).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal rts = brut.multiply(tauxRts).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal net = brut.subtract(cnss).subtract(rts);

        FichePaie fiche = FichePaie.builder()
                .employe(employe)
                .periodeMois(mois)
                .periodeAnnee(annee)
                .salaireBrut(brut)
                .cotisationCnss(cnss)
                .impotRts(rts)
                .salaireNet(net)
                .qrCodeToken("MINERVA-PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .dateGeneration(LocalDate.now())
                .build();

        return versDTO(fichePaieRepository.save(fiche));
    }

    @Transactional
    public void supprimerLogique(Long id) {
        FichePaie f = ServiceSuppression.trouverActif(fichePaieRepository, id, "Fiche de paie");
        ServiceSuppression.supprimerLogique(f);
        fichePaieRepository.save(f);
    }

    @Transactional(readOnly = true)
    public void verifierAccesEmploye(Long ficheId, Long employeIdConnecte, boolean accesGlobal) {
        FichePaie f = fichePaieRepository.findById(ficheId)
                .orElseThrow(() -> new RessourceNonTrouveeException("Fiche introuvable"));
        if (!accesGlobal && !f.getEmploye().getId().equals(employeIdConnecte)) {
            throw new AccesRefuseException("Accès refusé");
        }
    }

    private BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private BigDecimal nz(BigDecimal v, BigDecimal defaut) {
        return v != null ? v : defaut;
    }

    private FichePaieDTO versDTO(FichePaie f) {
        Employe e = f.getEmploye();
        EmployeDTO employeDTO = null;
        if (e != null) {
            employeDTO = EmployeDTO.builder()
                    .id(e.getId())
                    .matricule(e.getMatricule())
                    .nom(e.getNom())
                    .prenom(e.getPrenom())
                    .photoUrl(e.getPhotoUrl())
                    .departementLibelle(e.getDepartement() != null ? e.getDepartement().getLibelle() : null)
                    .posteLibelle(e.getPoste() != null ? e.getPoste().getLibelle() : null)
                    .build();
        }
        return FichePaieDTO.builder()
                .id(f.getId())
                .employeId(e != null ? e.getId() : null)
                .employe(employeDTO)
                .periodeMois(f.getPeriodeMois())
                .periodeAnnee(f.getPeriodeAnnee())
                .periodeLibelle(MOIS[f.getPeriodeMois()] + " " + f.getPeriodeAnnee())
                .salaireBrut(f.getSalaireBrut())
                .cotisationCnss(f.getCotisationCnss())
                .impotRts(f.getImpotRts())
                .salaireNet(f.getSalaireNet())
                .qrCodeToken(f.getQrCodeToken())
                .dateGeneration(f.getDateGeneration())
                .statut(f.getStatut().name())
                .build();
    }
}
