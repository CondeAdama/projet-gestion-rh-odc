package gn.odc.gestionrh.referentiel.service;

import gn.odc.gestionrh.common.enums.StatutEntite;
import gn.odc.gestionrh.common.exception.RegleMetierException;
import gn.odc.gestionrh.common.exception.RessourceNonTrouveeException;
import gn.odc.gestionrh.common.service.ServiceSuppression;
import gn.odc.gestionrh.referentiel.dto.ReferentielDTO;
import gn.odc.gestionrh.referentiel.dto.ReferentielRequeteDTO;
import gn.odc.gestionrh.referentiel.entity.Departement;
import gn.odc.gestionrh.referentiel.entity.Localisation;
import gn.odc.gestionrh.referentiel.entity.Poste;
import gn.odc.gestionrh.referentiel.repository.DepartementRepository;
import gn.odc.gestionrh.referentiel.repository.LocalisationRepository;
import gn.odc.gestionrh.referentiel.repository.PosteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReferentielService {

    private final DepartementRepository departementRepository;
    private final PosteRepository posteRepository;
    private final LocalisationRepository localisationRepository;

    // --- Départements ---
    @Transactional(readOnly = true)
    public List<ReferentielDTO> listerDepartements() {
        return departementRepository.findByStatut(StatutEntite.ACTIF).stream()
                .map(this::versDepartementDTO).toList();
    }

    @Transactional
    public ReferentielDTO creerDepartement(ReferentielRequeteDTO req) {
        if (req.getLibelle() == null || req.getLibelle().isBlank()) {
            throw new RegleMetierException("Le libellé est obligatoire");
        }
        if (departementRepository.existsByCode(req.getCode())) {
            throw new RegleMetierException("Code département déjà utilisé");
        }
        Departement d = Departement.builder()
                .code(req.getCode().toUpperCase())
                .libelle(req.getLibelle())
                .description(req.getDescription())
                .build();
        return versDepartementDTO(departementRepository.save(d));
    }

    @Transactional
    public ReferentielDTO modifierDepartement(Long id, ReferentielRequeteDTO req) {
        Departement d = ServiceSuppression.trouverActif(departementRepository, id, "Département");
        d.setLibelle(req.getLibelle());
        d.setDescription(req.getDescription());
        return versDepartementDTO(departementRepository.save(d));
    }

    @Transactional
    public void supprimerDepartement(Long id) {
        Departement d = ServiceSuppression.trouverActif(departementRepository, id, "Département");
        ServiceSuppression.supprimerLogique(d);
        departementRepository.save(d);
    }

    @Transactional(readOnly = true)
    public List<ReferentielDTO> listerDepartementsCorbeille() {
        return departementRepository.findByStatut(StatutEntite.SUPPRIME).stream()
                .map(this::versDepartementDTO).toList();
    }

    @Transactional
    public ReferentielDTO restaurerDepartement(Long id) {
        Departement d = ServiceSuppression.trouverSupprime(departementRepository, id, "Département");
        ServiceSuppression.restaurer(d);
        return versDepartementDTO(departementRepository.save(d));
    }

    // --- Postes ---
    @Transactional(readOnly = true)
    public List<ReferentielDTO> listerPostes() {
        return posteRepository.findByStatut(StatutEntite.ACTIF).stream()
                .map(this::versPosteDTO).toList();
    }

    @Transactional
    public ReferentielDTO creerPoste(ReferentielRequeteDTO req) {
        if (req.getLibelle() == null || req.getLibelle().isBlank()) {
            throw new RegleMetierException("Le libellé est obligatoire");
        }
        if (posteRepository.existsByCode(req.getCode())) {
            throw new RegleMetierException("Code poste déjà utilisé");
        }
        Departement dept = null;
        if (req.getDepartementId() != null) {
            dept = departementRepository.findById(req.getDepartementId())
                    .orElseThrow(() -> new RessourceNonTrouveeException("Département introuvable"));
        }
        Poste p = Poste.builder()
                .code(req.getCode().toUpperCase())
                .libelle(req.getLibelle())
                .description(req.getDescription())
                .departement(dept)
                .build();
        return versPosteDTO(posteRepository.save(p));
    }

    @Transactional
    public ReferentielDTO modifierPoste(Long id, ReferentielRequeteDTO req) {
        Poste p = ServiceSuppression.trouverActif(posteRepository, id, "Poste");
        p.setLibelle(req.getLibelle());
        p.setDescription(req.getDescription());
        if (req.getDepartementId() != null) {
            Departement dept = departementRepository.findById(req.getDepartementId())
                    .orElseThrow(() -> new RessourceNonTrouveeException("Département introuvable"));
            p.setDepartement(dept);
        }
        return versPosteDTO(posteRepository.save(p));
    }

    @Transactional
    public void supprimerPoste(Long id) {
        Poste p = ServiceSuppression.trouverActif(posteRepository, id, "Poste");
        ServiceSuppression.supprimerLogique(p);
        posteRepository.save(p);
    }

    @Transactional(readOnly = true)
    public List<ReferentielDTO> listerPostesCorbeille() {
        return posteRepository.findByStatut(StatutEntite.SUPPRIME).stream()
                .map(this::versPosteDTO).toList();
    }

    @Transactional
    public ReferentielDTO restaurerPoste(Long id) {
        Poste p = ServiceSuppression.trouverSupprime(posteRepository, id, "Poste");
        ServiceSuppression.restaurer(p);
        return versPosteDTO(posteRepository.save(p));
    }

    // --- Localisations ---
    @Transactional(readOnly = true)
    public List<ReferentielDTO> listerLocalisations() {
        return localisationRepository.findByStatut(StatutEntite.ACTIF).stream()
                .map(this::versLocalisationDTO).toList();
    }

    @Transactional
    public ReferentielDTO creerLocalisation(ReferentielRequeteDTO req) {
        if (req.getNom() == null || req.getNom().isBlank()) {
            throw new RegleMetierException("Le nom est obligatoire");
        }
        if (localisationRepository.existsByCode(req.getCode())) {
            throw new RegleMetierException("Code localisation déjà utilisé");
        }
        Localisation l = Localisation.builder()
                .code(req.getCode().toUpperCase())
                .nom(req.getNom())
                .adresse(req.getAdresse())
                .ville(req.getVille() != null ? req.getVille() : "Conakry")
                .build();
        return versLocalisationDTO(localisationRepository.save(l));
    }

    @Transactional
    public ReferentielDTO modifierLocalisation(Long id, ReferentielRequeteDTO req) {
        Localisation l = ServiceSuppression.trouverActif(localisationRepository, id, "Localisation");
        l.setNom(req.getNom());
        l.setAdresse(req.getAdresse());
        if (req.getVille() != null) l.setVille(req.getVille());
        return versLocalisationDTO(localisationRepository.save(l));
    }

    @Transactional
    public void supprimerLocalisation(Long id) {
        Localisation l = ServiceSuppression.trouverActif(localisationRepository, id, "Localisation");
        ServiceSuppression.supprimerLogique(l);
        localisationRepository.save(l);
    }

    @Transactional(readOnly = true)
    public List<ReferentielDTO> listerLocalisationsCorbeille() {
        return localisationRepository.findByStatut(StatutEntite.SUPPRIME).stream()
                .map(this::versLocalisationDTO).toList();
    }

    @Transactional
    public ReferentielDTO restaurerLocalisation(Long id) {
        Localisation l = ServiceSuppression.trouverSupprime(localisationRepository, id, "Localisation");
        ServiceSuppression.restaurer(l);
        return versLocalisationDTO(localisationRepository.save(l));
    }

    private ReferentielDTO versDepartementDTO(Departement d) {
        return ReferentielDTO.builder()
                .id(d.getId()).code(d.getCode()).libelle(d.getLibelle())
                .description(d.getDescription()).statut(d.getStatut().name()).build();
    }

    private ReferentielDTO versPosteDTO(Poste p) {
        return ReferentielDTO.builder()
                .id(p.getId()).code(p.getCode()).libelle(p.getLibelle())
                .description(p.getDescription()).statut(p.getStatut().name())
                .departementId(p.getDepartement() != null ? p.getDepartement().getId() : null)
                .departementLibelle(p.getDepartement() != null ? p.getDepartement().getLibelle() : null)
                .build();
    }

    private ReferentielDTO versLocalisationDTO(Localisation l) {
        return ReferentielDTO.builder()
                .id(l.getId()).code(l.getCode()).nom(l.getNom())
                .adresse(l.getAdresse()).ville(l.getVille()).statut(l.getStatut().name()).build();
    }
}
