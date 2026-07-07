package gn.odc.gestionrh.presence.service;

import gn.odc.gestionrh.common.enums.StatutEntite;
import gn.odc.gestionrh.common.enums.StatutPresence;
import gn.odc.gestionrh.common.exception.RegleMetierException;
import gn.odc.gestionrh.common.exception.RessourceNonTrouveeException;
import gn.odc.gestionrh.common.service.ServiceSuppression;
import gn.odc.gestionrh.employe.dto.EmployeDTO;
import gn.odc.gestionrh.employe.entity.Employe;
import gn.odc.gestionrh.employe.repository.EmployeRepository;
import gn.odc.gestionrh.presence.dto.PresenceDTO;
import gn.odc.gestionrh.presence.dto.ScanReponseDTO;
import gn.odc.gestionrh.presence.entity.Presence;
import gn.odc.gestionrh.presence.repository.PresenceRepository;
import gn.odc.gestionrh.referentiel.entity.Localisation;
import gn.odc.gestionrh.referentiel.repository.LocalisationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private static final LocalTime HEURE_LIMITE = LocalTime.of(8, 30);

    private final PresenceRepository presenceRepository;
    private final EmployeRepository employeRepository;
    private final LocalisationRepository localisationRepository;

    @Transactional
    public ScanReponseDTO scanner(String matricule, Long localisationId) {
        Employe employe = employeRepository.findByMatricule(matricule)
                .filter(e -> e.getStatut() == StatutEntite.ACTIF)
                .orElseThrow(() -> new RessourceNonTrouveeException("Badge ou matricule inconnu"));

        Localisation local = localisationRepository.findById(localisationId)
                .filter(l -> l.getStatut() == StatutEntite.ACTIF)
                .orElseThrow(() -> new RessourceNonTrouveeException("Localisation invalide"));

        LocalDate aujourdHui = LocalDate.now();
        LocalTime maintenant = LocalTime.now();

        var passageOuvert = presenceRepository.findFirstByEmployeIdAndDateJourAndStatutAndHeureSortieIsNullOrderByHeureEntreeDesc(
                employe.getId(), aujourdHui, StatutEntite.ACTIF);

        if (passageOuvert.isPresent()) {
            Presence p = passageOuvert.get();
            p.setHeureSortie(maintenant);
            Presence sauvegarde = presenceRepository.save(p);
            int passage = numeroPassage(sauvegarde);
            return ScanReponseDTO.builder()
                    .success(true)
                    .message("Sortie enregistrée (passage " + passage + ") à " + formaterHeure(maintenant))
                    .typeScan("SORTIE")
                    .presence(versDTO(sauvegarde))
                    .build();
        }

        long passagesExistants = presenceRepository.countByEmployeIdAndDateJourAndStatut(
                employe.getId(), aujourdHui, StatutEntite.ACTIF);
        int numeroPassage = (int) passagesExistants + 1;

        StatutPresence statut = StatutPresence.EN_REGLE;
        if (numeroPassage == 1) {
            statut = maintenant.isAfter(HEURE_LIMITE) ? StatutPresence.RETARD : StatutPresence.EN_REGLE;
        }

        Presence nouvelle = Presence.builder()
                .employe(employe)
                .localisation(local)
                .dateJour(aujourdHui)
                .heureEntree(maintenant)
                .statutPresence(statut)
                .build();
        Presence sauvegarde = presenceRepository.save(nouvelle);

        String message = numeroPassage == 1
                ? "Arrivée enregistrée (" + statut.name() + ") à " + formaterHeure(maintenant)
                : "Ré-entrée (passage " + numeroPassage + ") enregistrée à " + formaterHeure(maintenant);

        return ScanReponseDTO.builder()
                .success(true)
                .message(message)
                .typeScan("ENTREE")
                .presence(versDTO(sauvegarde))
                .build();
    }

    @Transactional(readOnly = true)
    public List<PresenceDTO> aujourdhui() {
        return presenceRepository.findByDateJourAndStatut(LocalDate.now(), StatutEntite.ACTIF).stream()
                .sorted(Comparator.comparing(Presence::getHeureEntree).reversed())
                .map(this::versDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PresenceDTO> parEmploye(Long employeId) {
        return presenceRepository.findByEmployeIdAndStatut(employeId, StatutEntite.ACTIF).stream()
                .sorted(Comparator.comparing(Presence::getDateJour).reversed()
                        .thenComparing(Presence::getHeureEntree).reversed())
                .map(this::versDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Long> statistiquesJour() {
        LocalDate today = LocalDate.now();
        List<Presence> presences = presenceRepository.findByDateJourAndStatut(today, StatutEntite.ACTIF);
        Set<Long> employesDistincts = new HashSet<>();
        presences.forEach(p -> employesDistincts.add(p.getEmploye().getId()));
        return Map.of(
                "total", (long) presences.size(),
                "employes", (long) employesDistincts.size(),
                "enRegle", presences.stream().filter(p -> p.getStatutPresence() == StatutPresence.EN_REGLE).count(),
                "retards", presences.stream().filter(p -> p.getStatutPresence() == StatutPresence.RETARD).count(),
                "presents", presences.stream().filter(p -> p.getHeureSortie() == null).count()
        );
    }

    @Transactional(readOnly = true)
    public List<PresenceDTO> rechercher(LocalDate dateDebut, LocalDate dateFin, Long localisationId,
                                        Long departementId, String statutPresenceStr) {
        StatutPresence statutPresence = null;
        if (statutPresenceStr != null && !statutPresenceStr.isBlank()) {
            try {
                statutPresence = StatutPresence.valueOf(statutPresenceStr);
            } catch (IllegalArgumentException e) {
                throw new RegleMetierException("Statut de présence invalide");
            }
        }
        return presenceRepository.rechercher(
                StatutEntite.ACTIF, dateDebut, dateFin, localisationId, departementId, statutPresence
        ).stream().map(this::versDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<PresenceDTO> listerCorbeille() {
        return presenceRepository.findByStatut(StatutEntite.SUPPRIME).stream()
                .map(this::versDTO).toList();
    }

    @Transactional
    public void supprimer(Long id) {
        Presence p = ServiceSuppression.trouverActif(presenceRepository, id, "Présence");
        ServiceSuppression.supprimerLogique(p);
        presenceRepository.save(p);
    }

    @Transactional
    public PresenceDTO restaurer(Long id) {
        Presence p = ServiceSuppression.trouverSupprime(presenceRepository, id, "Présence");
        ServiceSuppression.restaurer(p);
        return versDTO(presenceRepository.save(p));
    }

    private int numeroPassage(Presence p) {
        if (p.getEmploye() == null) {
            return 1;
        }
        return (int) presenceRepository.countByEmployeIdAndDateJourAndStatutAndHeureEntreeLessThanEqual(
                p.getEmploye().getId(), p.getDateJour(), StatutEntite.ACTIF, p.getHeureEntree());
    }

    private String formaterHeure(LocalTime t) {
        return String.format("%02d:%02d", t.getHour(), t.getMinute());
    }

    private PresenceDTO versDTO(Presence p) {
        Employe e = p.getEmploye();
        Localisation l = p.getLocalisation();
        EmployeDTO employeDTO = null;
        if (e != null) {
            employeDTO = EmployeDTO.builder()
                    .id(e.getId())
                    .matricule(e.getMatricule())
                    .nom(e.getNom())
                    .prenom(e.getPrenom())
                    .photoUrl(e.getPhotoUrl())
                    .build();
        }
        int passage = numeroPassage(p);
        return PresenceDTO.builder()
                .id(p.getId())
                .employeId(e != null ? e.getId() : null)
                .employe(employeDTO)
                .localisationId(l != null ? l.getId() : null)
                .localisationNom(l != null ? l.getNom() : null)
                .localisationVille(l != null ? l.getVille() : null)
                .dateJour(p.getDateJour())
                .heureEntree(p.getHeureEntree())
                .heureSortie(p.getHeureSortie())
                .statutPresence(p.getStatutPresence().name())
                .typeScan(p.getHeureSortie() != null ? "SORTIE" : "ENTREE")
                .numeroPassage(passage)
                .build();
    }
}
