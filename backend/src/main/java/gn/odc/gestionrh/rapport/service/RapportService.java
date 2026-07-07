package gn.odc.gestionrh.rapport.service;

import gn.odc.gestionrh.common.enums.StatutConge;
import gn.odc.gestionrh.common.enums.StatutContrat;
import gn.odc.gestionrh.common.enums.StatutEmploi;
import gn.odc.gestionrh.common.enums.StatutEntite;
import gn.odc.gestionrh.common.enums.StatutVisite;
import gn.odc.gestionrh.configuration.service.ConfigurationEntrepriseService;
import gn.odc.gestionrh.conge.repository.CongeRepository;
import gn.odc.gestionrh.contrat.repository.ContratRepository;
import gn.odc.gestionrh.employe.repository.EmployeRepository;
import gn.odc.gestionrh.paie.repository.FichePaieRepository;
import gn.odc.gestionrh.presence.service.PresenceService;
import gn.odc.gestionrh.visite.repository.VisiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RapportService {

    private final EmployeRepository employeRepository;
    private final ContratRepository contratRepository;
    private final CongeRepository congeRepository;
    private final PresenceService presenceService;
    private final FichePaieRepository fichePaieRepository;
    private final VisiteRepository visiteRepository;
    private final ConfigurationEntrepriseService configurationEntrepriseService;

    @Transactional(readOnly = true)
    public Map<String, Object> synthese() {
        Map<String, Object> rapport = new HashMap<>();

        var employes = employeRepository.findByStatut(StatutEntite.ACTIF);
        long actifs = employes.stream().filter(e -> e.getStatutEmploi() == StatutEmploi.ACTIF).count();
        long suspendus = employes.stream().filter(e -> e.getStatutEmploi() == StatutEmploi.SUSPENDU).count();

        rapport.put("employes", Map.of(
                "total", employes.size(),
                "actifs", actifs,
                "suspendus", suspendus,
                "licencies", employes.stream().filter(e -> e.getStatutEmploi() == StatutEmploi.LICENCIE).count()
        ));

        rapport.put("contrats", Map.of(
                "actifs", contratRepository.countByStatutContratAndStatut(StatutContrat.ACTIF, StatutEntite.ACTIF)
        ));

        rapport.put("conges", Map.of(
                "enAttente", congeRepository.countByStatutCongeAndStatut(StatutConge.EN_ATTENTE, StatutEntite.ACTIF),
                "approuves", congeRepository.countByStatutCongeAndStatut(StatutConge.APPROUVE, StatutEntite.ACTIF),
                "refuses", congeRepository.countByStatutCongeAndStatut(StatutConge.REFUSE, StatutEntite.ACTIF)
        ));

        Map<String, Long> statsPresences = presenceService.statistiquesJour();
        rapport.put("presences", Map.of(
                "total", statsPresences.get("total"),
                "aujourdhui", statsPresences.get("total"),
                "employes", statsPresences.get("employes"),
                "enRegle", statsPresences.get("enRegle"),
                "retards", statsPresences.get("retards"),
                "presents", statsPresences.get("presents")
        ));

        var fiches = fichePaieRepository.findByStatut(StatutEntite.ACTIF);
        BigDecimal masse = fiches.stream().map(f -> f.getSalaireNet()).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCnss = fiches.stream().map(f -> f.getCotisationCnss()).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRts = fiches.stream().map(f -> f.getImpotRts()).reduce(BigDecimal.ZERO, BigDecimal::add);
        var config = configurationEntrepriseService.obtenir();
        rapport.put("paie", Map.of(
                "totalFiches", fiches.size(),
                "masseSalariale", masse,
                "totalCnss", totalCnss,
                "totalRts", totalRts,
                "tauxCnss", config.getTauxCnss() != null ? config.getTauxCnss() : BigDecimal.valueOf(5),
                "tauxRts", config.getTauxRts() != null ? config.getTauxRts() : BigDecimal.valueOf(10),
                "numeroCnss", config.getNumeroCnss() != null ? config.getNumeroCnss() : ""
        ));

        rapport.put("visites", Map.of(
                "enCours", visiteRepository.countByStatut(StatutVisite.EN_COURS),
                "terminees", visiteRepository.countByStatut(StatutVisite.TERMINEE)
        ));

        return rapport;
    }
}
