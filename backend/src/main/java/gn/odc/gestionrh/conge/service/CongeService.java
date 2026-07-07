package gn.odc.gestionrh.conge.service;

import gn.odc.gestionrh.common.enums.StatutConge;
import gn.odc.gestionrh.common.enums.StatutEntite;
import gn.odc.gestionrh.common.enums.TypeConge;
import gn.odc.gestionrh.common.exception.AccesRefuseException;
import gn.odc.gestionrh.common.exception.RegleMetierException;
import gn.odc.gestionrh.common.exception.RessourceNonTrouveeException;
import gn.odc.gestionrh.common.service.ServiceSuppression;
import gn.odc.gestionrh.conge.dto.CongeDTO;
import gn.odc.gestionrh.conge.dto.CongeRequeteDTO;
import gn.odc.gestionrh.conge.dto.TraitementCongeDTO;
import gn.odc.gestionrh.conge.entity.Conge;
import gn.odc.gestionrh.conge.repository.CongeRepository;
import gn.odc.gestionrh.employe.dto.EmployeDTO;
import gn.odc.gestionrh.employe.entity.Employe;
import gn.odc.gestionrh.employe.repository.EmployeRepository;
import gn.odc.gestionrh.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CongeService {

    private final CongeRepository congeRepository;
    private final EmployeRepository employeRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<CongeDTO> listerTous() {
        return congeRepository.findByStatut(StatutEntite.ACTIF).stream()
                .map(this::versDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<CongeDTO> listerParEmploye(Long employeId) {
        return congeRepository.findByEmployeIdAndStatut(employeId, StatutEntite.ACTIF).stream()
                .map(this::versDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<CongeDTO> listerEnAttente() {
        return congeRepository.findByStatutCongeAndStatut(StatutConge.EN_ATTENTE, StatutEntite.ACTIF).stream()
                .map(this::versDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<CongeDTO> listerSupprimes() {
        return congeRepository.findByStatut(StatutEntite.SUPPRIME).stream()
                .map(this::versDTO).toList();
    }

    @Transactional(readOnly = true)
    public CongeDTO trouverParId(Long id) {
        Conge c = congeRepository.findById(id)
                .filter(conge -> conge.getStatut() == StatutEntite.ACTIF)
                .orElseThrow(() -> new RessourceNonTrouveeException("Demande de congé introuvable"));
        return versDTO(c);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> statistiques() {
        return Map.of(
                "enAttente", congeRepository.countByStatutCongeAndStatut(StatutConge.EN_ATTENTE, StatutEntite.ACTIF),
                "approuves", congeRepository.countByStatutCongeAndStatut(StatutConge.APPROUVE, StatutEntite.ACTIF),
                "refuses", congeRepository.countByStatutCongeAndStatut(StatutConge.REFUSE, StatutEntite.ACTIF)
        );
    }

    @Transactional
    public CongeDTO demander(CongeRequeteDTO req, Long employeIdDemandeur, boolean peutCreerPourAutrui) {
        Long cibleId = req.getEmployeId() != null ? req.getEmployeId() : employeIdDemandeur;
        if (cibleId == null) {
            throw new RegleMetierException("Employé non identifié");
        }
        if (!cibleId.equals(employeIdDemandeur) && !peutCreerPourAutrui) {
            throw new AccesRefuseException("Vous ne pouvez créer une demande que pour vous-même");
        }

        validerDates(req.getDateDebut(), req.getDateFin());

        Employe employe = employeRepository.findById(cibleId)
                .filter(e -> e.getStatut() == StatutEntite.ACTIF)
                .orElseThrow(() -> new RessourceNonTrouveeException("Employé introuvable"));

        Conge conge = Conge.builder()
                .employe(employe)
                .typeConge(parseTypeConge(req.getTypeConge()))
                .dateDebut(req.getDateDebut())
                .dateFin(req.getDateFin())
                .motif(req.getMotif())
                .statutConge(StatutConge.EN_ATTENTE)
                .build();

        return versDTO(congeRepository.save(conge));
    }

    @Transactional
    public CongeDTO modifier(Long id, CongeRequeteDTO req, Long employeIdConnecte, boolean peutModifierAutrui) {
        Conge conge = congeRepository.findById(id)
                .filter(c -> c.getStatut() == StatutEntite.ACTIF)
                .orElseThrow(() -> new RessourceNonTrouveeException("Demande introuvable"));

        verifierAccesEmploye(conge, employeIdConnecte, peutModifierAutrui);

        if (conge.getStatutConge() != StatutConge.EN_ATTENTE) {
            throw new RegleMetierException("Impossible de modifier une demande déjà traitée");
        }

        validerDates(req.getDateDebut(), req.getDateFin());
        conge.setTypeConge(parseTypeConge(req.getTypeConge()));
        conge.setDateDebut(req.getDateDebut());
        conge.setDateFin(req.getDateFin());
        conge.setMotif(req.getMotif());

        return versDTO(congeRepository.save(conge));
    }

    @Transactional
    public CongeDTO traiter(Long id, TraitementCongeDTO req) {
        Conge conge = congeRepository.findById(id)
                .filter(c -> c.getStatut() == StatutEntite.ACTIF)
                .orElseThrow(() -> new RessourceNonTrouveeException("Demande introuvable"));

        if (conge.getStatutConge() != StatutConge.EN_ATTENTE) {
            throw new RegleMetierException("Cette demande a déjà été traitée");
        }

        StatutConge nouveauStatut;
        try {
            nouveauStatut = StatutConge.valueOf(req.getStatut());
        } catch (IllegalArgumentException e) {
            throw new RegleMetierException("Statut invalide. Utilisez APPROUVE ou REFUSE");
        }

        if (nouveauStatut != StatutConge.APPROUVE && nouveauStatut != StatutConge.REFUSE) {
            throw new RegleMetierException("Seuls APPROUVE et REFUSE sont autorisés");
        }

        conge.setStatutConge(nouveauStatut);
        conge.setCommentaireRh(req.getCommentaireRh());
        Conge sauvegarde = congeRepository.save(conge);

        Employe employe = conge.getEmploye();
        notificationService.envoyerValidationConge(
                employe,
                String.valueOf(conge.getTypeConge()),
                String.valueOf(conge.getDateDebut()),
                String.valueOf(conge.getDateFin()),
                nouveauStatut == StatutConge.APPROUVE,
                req.getCommentaireRh());

        return versDTO(sauvegarde);
    }

    @Transactional
    public void supprimerLogique(Long id) {
        Conge conge = ServiceSuppression.trouverActif(congeRepository, id, "Demande de congé");
        ServiceSuppression.supprimerLogique(conge);
        congeRepository.save(conge);
    }

    @Transactional
    public CongeDTO restaurer(Long id) {
        Conge conge = ServiceSuppression.trouverSupprime(congeRepository, id, "Demande de congé");
        ServiceSuppression.restaurer(conge);
        return versDTO(congeRepository.save(conge));
    }

    @Transactional
    public void supprimerDefinitif(Long id) {
        Conge conge = ServiceSuppression.trouverSupprime(congeRepository, id, "Demande de congé");
        congeRepository.delete(conge);
    }

    private void validerDates(LocalDate debut, LocalDate fin) {
        if (debut == null || fin == null) {
            throw new RegleMetierException("Les dates de début et de fin sont obligatoires");
        }
        if (debut.isAfter(fin)) {
            throw new RegleMetierException("La date de début doit être antérieure à la date de fin");
        }
        if (debut.isBefore(LocalDate.now())) {
            throw new RegleMetierException("La date de début ne peut pas être dans le passé");
        }
    }

    @Transactional(readOnly = true)
    public void verifierAccesEmploye(Long congeId, Long employeIdConnecte, boolean accesGlobal) {
        Conge conge = congeRepository.findById(congeId)
                .orElseThrow(() -> new RessourceNonTrouveeException("Demande de congé introuvable"));
        verifierAccesEmploye(conge, employeIdConnecte, accesGlobal);
    }

    private void verifierAccesEmploye(Conge conge, Long employeIdConnecte, boolean accesGlobal) {
        if (!accesGlobal && (employeIdConnecte == null ||
                !conge.getEmploye().getId().equals(employeIdConnecte))) {
            throw new AccesRefuseException("Accès refusé à cette demande");
        }
    }

    private CongeDTO versDTO(Conge c) {
        Employe e = c.getEmploye();
        int jours = (int) ChronoUnit.DAYS.between(c.getDateDebut(), c.getDateFin()) + 1;

        EmployeDTO employeDTO = null;
        if (e != null) {
            employeDTO = EmployeDTO.builder()
                    .id(e.getId())
                    .matricule(e.getMatricule())
                    .nom(e.getNom())
                    .prenom(e.getPrenom())
                    .email(e.getEmail())
                    .photoUrl(e.getPhotoUrl())
                    .departementLibelle(e.getDepartement() != null ? e.getDepartement().getLibelle() : null)
                    .posteLibelle(e.getPoste() != null ? e.getPoste().getLibelle() : null)
                    .build();
        }

        return CongeDTO.builder()
                .id(c.getId())
                .employeId(e != null ? e.getId() : null)
                .employe(employeDTO)
                .typeConge(c.getTypeConge().name())
                .dateDebut(c.getDateDebut())
                .dateFin(c.getDateFin())
                .nombreJours(jours)
                .motif(c.getMotif())
                .statutConge(c.getStatutConge().name())
                .commentaireRh(c.getCommentaireRh())
                .statut(c.getStatut().name())
                .dateCreation(c.getDateCreation() != null ? c.getDateCreation().toLocalDate() : null)
                .build();
    }

    private TypeConge parseTypeConge(String type) {
        try {
            return TypeConge.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new RegleMetierException("Type de congé invalide");
        }
    }
}
