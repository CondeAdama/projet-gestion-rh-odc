package gn.odc.gestionrh.contrat.service;

import gn.odc.gestionrh.common.enums.StatutContrat;
import gn.odc.gestionrh.common.enums.StatutEntite;
import gn.odc.gestionrh.common.exception.RegleMetierException;
import gn.odc.gestionrh.common.exception.RessourceNonTrouveeException;
import gn.odc.gestionrh.common.service.ServiceSuppression;
import gn.odc.gestionrh.contrat.dto.ContratDTO;
import gn.odc.gestionrh.contrat.dto.ContratRequeteDTO;
import gn.odc.gestionrh.contrat.entity.Contrat;
import gn.odc.gestionrh.contrat.repository.ContratRepository;
import gn.odc.gestionrh.employe.dto.EmployeDTO;
import gn.odc.gestionrh.employe.entity.Employe;
import gn.odc.gestionrh.employe.repository.EmployeRepository;
import gn.odc.gestionrh.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContratService {

    private final ContratRepository contratRepository;
    private final EmployeRepository employeRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<ContratDTO> listerActifs() {
        return contratRepository.findByStatut(StatutEntite.ACTIF).stream()
                .map(this::versDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ContratDTO> listerSupprimes() {
        return contratRepository.findByStatut(StatutEntite.SUPPRIME).stream()
                .map(this::versDTO).toList();
    }

    @Transactional(readOnly = true)
    public ContratDTO trouverParId(Long id) {
        Contrat c = contratRepository.findById(id)
                .filter(ct -> ct.getStatut() == StatutEntite.ACTIF)
                .orElseThrow(() -> new RessourceNonTrouveeException("Contrat introuvable"));
        return versDTO(c);
    }

    @Transactional(readOnly = true)
    public ContratDTO trouverContratActifEmploye(Long employeId) {
        Contrat c = contratRepository
                .findByEmployeIdAndStatutContratAndStatut(employeId, StatutContrat.ACTIF, StatutEntite.ACTIF)
                .orElseThrow(() -> new RessourceNonTrouveeException("Aucun contrat actif pour cet employé"));
        return versDTO(c);
    }

    @Transactional(readOnly = true)
    public List<ContratDTO> listerParEmploye(Long employeId) {
        return contratRepository.findByEmployeIdAndStatut(employeId, StatutEntite.ACTIF).stream()
                .map(this::versDTO).toList();
    }

    @Transactional
    public ContratDTO creer(ContratRequeteDTO req) {
        validerDatesContrat(req);
        Employe employe = employeRepository.findById(req.getEmployeId())
                .filter(e -> e.getStatut() == StatutEntite.ACTIF)
                .orElseThrow(() -> new RessourceNonTrouveeException("Employé introuvable"));

        contratRepository.findByEmployeIdAndStatutContratAndStatut(
                employe.getId(), StatutContrat.ACTIF, StatutEntite.ACTIF
        ).ifPresent(ancien -> {
            ancien.setStatutContrat(StatutContrat.ARCHIVE);
            contratRepository.save(ancien);
        });

        Contrat contrat = Contrat.builder()
                .employe(employe)
                .typeContrat(req.getTypeContrat())
                .salaireBase(req.getSalaireBase())
                .indemniteTransport(req.getIndemniteTransport() != null ? req.getIndemniteTransport() : BigDecimal.ZERO)
                .indemniteLogement(req.getIndemniteLogement() != null ? req.getIndemniteLogement() : BigDecimal.ZERO)
                .autresAvantages(req.getAutresAvantages() != null ? req.getAutresAvantages() : BigDecimal.ZERO)
                .dateDebut(req.getDateDebut())
                .dateFin(req.getDateFin())
                .statutContrat(StatutContrat.ACTIF)
                .build();

        Contrat sauvegarde = contratRepository.save(contrat);

        notificationService.envoyerCreationContrat(
                employe,
                String.valueOf(req.getTypeContrat()),
                String.valueOf(req.getSalaireBase()),
                String.valueOf(req.getDateDebut()));

        return versDTO(sauvegarde);
    }

    @Transactional
    public ContratDTO modifier(Long id, ContratRequeteDTO req) {
        validerDatesContrat(req);
        Contrat contrat = contratRepository.findById(id)
                .filter(c -> c.getStatut() == StatutEntite.ACTIF)
                .orElseThrow(() -> new RessourceNonTrouveeException("Contrat introuvable"));

        if (contrat.getStatutContrat() == StatutContrat.RESILIE) {
            throw new RegleMetierException("Impossible de modifier un contrat résilié");
        }

        contrat.setTypeContrat(req.getTypeContrat());
        contrat.setSalaireBase(req.getSalaireBase());
        contrat.setIndemniteTransport(req.getIndemniteTransport() != null ? req.getIndemniteTransport() : BigDecimal.ZERO);
        contrat.setIndemniteLogement(req.getIndemniteLogement() != null ? req.getIndemniteLogement() : BigDecimal.ZERO);
        contrat.setAutresAvantages(req.getAutresAvantages() != null ? req.getAutresAvantages() : BigDecimal.ZERO);
        contrat.setDateDebut(req.getDateDebut());
        contrat.setDateFin(req.getDateFin());

        return versDTO(contratRepository.save(contrat));
    }

    @Transactional
    public ContratDTO resilier(Long id) {
        Contrat contrat = contratRepository.findById(id)
                .filter(c -> c.getStatut() == StatutEntite.ACTIF)
                .orElseThrow(() -> new RessourceNonTrouveeException("Contrat introuvable"));
        contrat.setStatutContrat(StatutContrat.RESILIE);
        return versDTO(contratRepository.save(contrat));
    }

    @Transactional
    public void supprimerLogique(Long id) {
        Contrat contrat = ServiceSuppression.trouverActif(contratRepository, id, "Contrat");
        ServiceSuppression.supprimerLogique(contrat);
        contratRepository.save(contrat);
    }

    @Transactional
    public ContratDTO restaurer(Long id) {
        Contrat contrat = ServiceSuppression.trouverSupprime(contratRepository, id, "Contrat");
        ServiceSuppression.restaurer(contrat);
        return versDTO(contratRepository.save(contrat));
    }

    @Transactional
    public void supprimerDefinitif(Long id) {
        Contrat contrat = ServiceSuppression.trouverSupprime(contratRepository, id, "Contrat");
        contratRepository.delete(contrat);
    }

    private ContratDTO versDTO(Contrat c) {
        Employe e = c.getEmploye();
        BigDecimal brut = c.getSalaireBase()
                .add(c.getIndemniteTransport() != null ? c.getIndemniteTransport() : BigDecimal.ZERO)
                .add(c.getIndemniteLogement() != null ? c.getIndemniteLogement() : BigDecimal.ZERO)
                .add(c.getAutresAvantages() != null ? c.getAutresAvantages() : BigDecimal.ZERO);

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

        return ContratDTO.builder()
                .id(c.getId())
                .employeId(e != null ? e.getId() : null)
                .employe(employeDTO)
                .typeContrat(c.getTypeContrat())
                .salaireBase(c.getSalaireBase())
                .indemniteTransport(c.getIndemniteTransport())
                .indemniteLogement(c.getIndemniteLogement())
                .autresAvantages(c.getAutresAvantages())
                .salaireBrut(brut)
                .dateDebut(c.getDateDebut())
                .dateFin(c.getDateFin())
                .statutContrat(c.getStatutContrat().name())
                .statut(c.getStatut().name())
                .build();
    }

    private void validerDatesContrat(ContratRequeteDTO req) {
        if ("CDD".equals(req.getTypeContrat()) && req.getDateFin() == null) {
            throw new RegleMetierException("La date de fin est obligatoire pour un CDD");
        }
        if (req.getDateFin() != null && req.getDateFin().isBefore(req.getDateDebut())) {
            throw new RegleMetierException("La date de fin doit être postérieure à la date de début");
        }
    }
}
