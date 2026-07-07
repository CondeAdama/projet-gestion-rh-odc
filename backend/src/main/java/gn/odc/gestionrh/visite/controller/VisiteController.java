package gn.odc.gestionrh.visite.controller;

import gn.odc.gestionrh.common.exception.RegleMetierException;
import gn.odc.gestionrh.visite.dto.*;
import gn.odc.gestionrh.visite.service.VisiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/visites")
@RequiredArgsConstructor
@Validated
public class VisiteController {

    private final VisiteService visiteService;

    @PostMapping("/visiteurs")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).VISITES, T(gn.odc.gestionrh.common.enums.TypeAction).AJOUTER)")
    public ResponseEntity<VisiteurDTO> creerVisiteur(@Valid @RequestBody VisiteurRequeteDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(visiteService.creerVisiteur(req));
    }

    @GetMapping("/visiteurs")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).VISITES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public List<VisiteurDTO> listerVisiteurs() {
        return visiteService.listerVisiteurs();
    }

    @GetMapping("/visiteurs/corbeille")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).VISITES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public List<VisiteurDTO> corbeilleVisiteurs() {
        return visiteService.listerVisiteursCorbeille();
    }

    @PutMapping("/visiteurs/{id}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).VISITES, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public VisiteurDTO modifierVisiteur(@PathVariable Long id, @Valid @RequestBody VisiteurRequeteDTO req) {
        return visiteService.modifierVisiteur(id, req);
    }

    @DeleteMapping("/visiteurs/{id}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).VISITES, T(gn.odc.gestionrh.common.enums.TypeAction).SUPPRIMER)")
    public ResponseEntity<Void> supprimerVisiteur(@PathVariable Long id) {
        visiteService.supprimerVisiteur(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/visiteurs/{id}/restaurer")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).VISITES, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public VisiteurDTO restaurerVisiteur(@PathVariable Long id) {
        return visiteService.restaurerVisiteur(id);
    }

    @PostMapping("/cartes")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).VISITES, T(gn.odc.gestionrh.common.enums.TypeAction).AJOUTER)")
    public ResponseEntity<CarteVisiteDTO> creerCarte(@Valid @RequestBody CarteVisiteRequeteDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(visiteService.creerCarte(req));
    }

    @GetMapping("/cartes")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).VISITES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public List<CarteVisiteDTO> listerCartes() {
        return visiteService.listerCartes();
    }

    @PostMapping("/demarrer")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).VISITES, T(gn.odc.gestionrh.common.enums.TypeAction).AJOUTER)")
    public VisiteDTO demarrer(
            @RequestBody(required = false) DemarrerVisiteRequeteDTO body,
            @RequestParam(required = false) Long visiteurId,
            @RequestParam(required = false) Long carteId,
            @RequestParam(required = false) String motif) {
        if (body != null) {
            return visiteService.demarrerVisite(body.getVisiteurId(), body.getCarteId(), body.getMotif());
        }
        if (visiteurId == null || carteId == null || motif == null || motif.isBlank()) {
            throw new RegleMetierException("Visiteur, carte et motif sont obligatoires");
        }
        return visiteService.demarrerVisite(visiteurId, carteId, motif.trim());
    }

    @PutMapping("/{visiteId}/cloturer")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).VISITES, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public VisiteDTO cloturer(@PathVariable Long visiteId) {
        return visiteService.cloturerVisite(visiteId);
    }

    @GetMapping
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).VISITES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public List<VisiteDTO> lister() {
        return visiteService.listerVisites();
    }

    @GetMapping("/statistiques")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).VISITES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public Map<String, Long> statistiques() {
        return visiteService.statistiques();
    }
}
