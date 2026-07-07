package gn.odc.gestionrh.visite.service;

import gn.odc.gestionrh.common.enums.StatutCarteVisite;
import gn.odc.gestionrh.common.enums.StatutVisite;
import gn.odc.gestionrh.common.exception.RegleMetierException;
import gn.odc.gestionrh.common.exception.RessourceNonTrouveeException;
import gn.odc.gestionrh.visite.dto.*;
import gn.odc.gestionrh.visite.entity.CarteVisite;
import gn.odc.gestionrh.visite.entity.Visite;
import gn.odc.gestionrh.visite.entity.Visiteur;
import gn.odc.gestionrh.visite.repository.CarteVisiteRepository;
import gn.odc.gestionrh.visite.repository.VisiteRepository;
import gn.odc.gestionrh.visite.repository.VisiteurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VisiteService {

    private final VisiteurRepository visiteurRepository;
    private final CarteVisiteRepository carteVisiteRepository;
    private final VisiteRepository visiteRepository;

    @Transactional
    public VisiteurDTO creerVisiteur(VisiteurRequeteDTO req) {
        Visiteur v = Visiteur.builder()
                .nom(req.getNom())
                .prenom(req.getPrenom())
                .contact(req.getContact())
                .entreprise(req.getEntreprise())
                .build();
        return versVisiteurDTO(visiteurRepository.save(v));
    }

    @Transactional(readOnly = true)
    public List<VisiteurDTO> listerVisiteurs() {
        return visiteurRepository.findByStatut("ACTIF").stream().map(this::versVisiteurDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<VisiteurDTO> listerVisiteursCorbeille() {
        return visiteurRepository.findByStatut("SUPPRIME").stream().map(this::versVisiteurDTO).toList();
    }

    @Transactional
    public VisiteurDTO modifierVisiteur(Long id, VisiteurRequeteDTO req) {
        Visiteur v = visiteurRepository.findById(id)
                .filter(x -> "ACTIF".equals(x.getStatut()))
                .orElseThrow(() -> new RessourceNonTrouveeException("Visiteur introuvable"));
        v.setNom(req.getNom());
        v.setPrenom(req.getPrenom());
        v.setContact(req.getContact());
        v.setEntreprise(req.getEntreprise());
        return versVisiteurDTO(visiteurRepository.save(v));
    }

    @Transactional
    public void supprimerVisiteur(Long id) {
        Visiteur v = visiteurRepository.findById(id)
                .filter(x -> "ACTIF".equals(x.getStatut()))
                .orElseThrow(() -> new RessourceNonTrouveeException("Visiteur introuvable"));
        v.setStatut("SUPPRIME");
        visiteurRepository.save(v);
    }

    @Transactional
    public VisiteurDTO restaurerVisiteur(Long id) {
        Visiteur v = visiteurRepository.findById(id)
                .filter(x -> "SUPPRIME".equals(x.getStatut()))
                .orElseThrow(() -> new RessourceNonTrouveeException("Visiteur introuvable dans la corbeille"));
        v.setStatut("ACTIF");
        return versVisiteurDTO(visiteurRepository.save(v));
    }

    @Transactional
    public CarteVisiteDTO creerCarte(CarteVisiteRequeteDTO req) {
        if (carteVisiteRepository.findByNumeroCarte(req.getNumeroCarte()).isPresent()) {
            throw new RegleMetierException("Ce numéro de carte existe déjà");
        }
        CarteVisite c = CarteVisite.builder().numeroCarte(req.getNumeroCarte()).build();
        return versCarteDTO(carteVisiteRepository.save(c));
    }

    @Transactional(readOnly = true)
    public List<CarteVisiteDTO> listerCartes() {
        return carteVisiteRepository.findAll().stream().map(this::versCarteDTO).toList();
    }

    @Transactional
    public VisiteDTO demarrerVisite(Long visiteurId, Long carteId, String motif) {
        Visiteur visiteur = visiteurRepository.findById(visiteurId)
                .filter(v -> "ACTIF".equals(v.getStatut()))
                .orElseThrow(() -> new RessourceNonTrouveeException("Visiteur introuvable"));

        CarteVisite carte = carteVisiteRepository.findById(carteId)
                .orElseThrow(() -> new RessourceNonTrouveeException("Carte introuvable"));

        if (carte.getStatut() != StatutCarteVisite.DISPONIBLE) {
            throw new RegleMetierException("La carte n'est pas disponible");
        }

        carte.setStatut(StatutCarteVisite.ASSIGNEE);
        carteVisiteRepository.save(carte);

        Visite visite = Visite.builder()
                .visiteur(visiteur)
                .carteVisite(carte)
                .motif(motif)
                .dateHeureEntree(LocalDateTime.now())
                .statut(StatutVisite.EN_COURS)
                .build();

        return versVisiteDTO(visiteRepository.save(visite));
    }

    @Transactional
    public VisiteDTO cloturerVisite(Long visiteId) {
        Visite visite = visiteRepository.findById(visiteId)
                .orElseThrow(() -> new RessourceNonTrouveeException("Visite introuvable"));

        if (visite.getStatut() == StatutVisite.TERMINEE) {
            throw new RegleMetierException("La visite est déjà terminée");
        }

        visite.setStatut(StatutVisite.TERMINEE);
        visite.setDateHeureSortie(LocalDateTime.now());

        CarteVisite carte = visite.getCarteVisite();
        carte.setStatut(StatutCarteVisite.DISPONIBLE);
        carteVisiteRepository.save(carte);

        return versVisiteDTO(visiteRepository.save(visite));
    }

    @Transactional(readOnly = true)
    public List<VisiteDTO> listerVisites() {
        return visiteRepository.findAll().stream().map(this::versVisiteDTO).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Long> statistiques() {
        return Map.of(
                "enCours", visiteRepository.countByStatut(StatutVisite.EN_COURS),
                "terminees", visiteRepository.countByStatut(StatutVisite.TERMINEE),
                "visiteurs", visiteurRepository.count(),
                "cartesDisponibles", (long) carteVisiteRepository.findByStatut(StatutCarteVisite.DISPONIBLE).size()
        );
    }

    private VisiteurDTO versVisiteurDTO(Visiteur v) {
        return VisiteurDTO.builder()
                .id(v.getId())
                .nom(v.getNom())
                .prenom(v.getPrenom())
                .contact(v.getContact())
                .entreprise(v.getEntreprise())
                .statut(v.getStatut())
                .build();
    }

    private CarteVisiteDTO versCarteDTO(CarteVisite c) {
        return CarteVisiteDTO.builder()
                .id(c.getId())
                .numeroCarte(c.getNumeroCarte())
                .statut(c.getStatut().name())
                .build();
    }

    private VisiteDTO versVisiteDTO(Visite v) {
        Visiteur vis = v.getVisiteur();
        CarteVisite carte = v.getCarteVisite();
        LocalDateTime entree = v.getDateHeureEntree();
        LocalDateTime sortie = v.getDateHeureSortie();
        return VisiteDTO.builder()
                .id(v.getId())
                .visiteurId(vis != null ? vis.getId() : null)
                .visiteur(vis != null ? versVisiteurDTO(vis) : null)
                .carteVisiteId(carte != null ? carte.getId() : null)
                .numeroCarte(carte != null ? carte.getNumeroCarte() : null)
                .motif(v.getMotif())
                .dateJour(entree != null ? entree.toLocalDate() : null)
                .dateHeureEntree(entree)
                .dateHeureSortie(sortie)
                .heureEntree(entree != null ? entree.toLocalTime() : null)
                .heureSortie(sortie != null ? sortie.toLocalTime() : null)
                .statut(v.getStatut().name())
                .build();
    }
}
