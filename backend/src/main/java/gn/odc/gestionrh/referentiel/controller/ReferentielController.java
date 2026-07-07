package gn.odc.gestionrh.referentiel.controller;

import gn.odc.gestionrh.referentiel.dto.ReferentielDTO;
import gn.odc.gestionrh.referentiel.dto.ReferentielRequeteDTO;
import gn.odc.gestionrh.referentiel.service.ReferentielService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/referentiels")
@RequiredArgsConstructor
public class ReferentielController {

    private final ReferentielService referentielService;

    @GetMapping("/departements")
    public List<ReferentielDTO> listerDepartements() {
        return referentielService.listerDepartements();
    }

    @PostMapping("/departements")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).REFERENTIELS, T(gn.odc.gestionrh.common.enums.TypeAction).AJOUTER)")
    public ResponseEntity<ReferentielDTO> creerDepartement(@Valid @RequestBody ReferentielRequeteDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(referentielService.creerDepartement(req));
    }

    @PutMapping("/departements/{id}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).REFERENTIELS, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public ReferentielDTO modifierDepartement(@PathVariable Long id, @Valid @RequestBody ReferentielRequeteDTO req) {
        return referentielService.modifierDepartement(id, req);
    }

    @DeleteMapping("/departements/{id}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).REFERENTIELS, T(gn.odc.gestionrh.common.enums.TypeAction).SUPPRIMER)")
    public ResponseEntity<Void> supprimerDepartement(@PathVariable Long id) {
        referentielService.supprimerDepartement(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/departements/{id}/restaurer")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).REFERENTIELS, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public ReferentielDTO restaurerDepartement(@PathVariable Long id) {
        return referentielService.restaurerDepartement(id);
    }

    @GetMapping("/departements/corbeille")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).REFERENTIELS, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public List<ReferentielDTO> corbeilleDepartements() {
        return referentielService.listerDepartementsCorbeille();
    }

    @GetMapping("/postes")
    public List<ReferentielDTO> listerPostes() {
        return referentielService.listerPostes();
    }

    @PostMapping("/postes")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).REFERENTIELS, T(gn.odc.gestionrh.common.enums.TypeAction).AJOUTER)")
    public ResponseEntity<ReferentielDTO> creerPoste(@Valid @RequestBody ReferentielRequeteDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(referentielService.creerPoste(req));
    }

    @PutMapping("/postes/{id}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).REFERENTIELS, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public ReferentielDTO modifierPoste(@PathVariable Long id, @Valid @RequestBody ReferentielRequeteDTO req) {
        return referentielService.modifierPoste(id, req);
    }

    @DeleteMapping("/postes/{id}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).REFERENTIELS, T(gn.odc.gestionrh.common.enums.TypeAction).SUPPRIMER)")
    public ResponseEntity<Void> supprimerPoste(@PathVariable Long id) {
        referentielService.supprimerPoste(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/postes/corbeille")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).REFERENTIELS, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public List<ReferentielDTO> corbeillePostes() {
        return referentielService.listerPostesCorbeille();
    }

    @PostMapping("/postes/{id}/restaurer")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).REFERENTIELS, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public ReferentielDTO restaurerPoste(@PathVariable Long id) {
        return referentielService.restaurerPoste(id);
    }

    @GetMapping("/localisations")
    public List<ReferentielDTO> listerLocalisations() {
        return referentielService.listerLocalisations();
    }

    @PostMapping("/localisations")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).REFERENTIELS, T(gn.odc.gestionrh.common.enums.TypeAction).AJOUTER)")
    public ResponseEntity<ReferentielDTO> creerLocalisation(@Valid @RequestBody ReferentielRequeteDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(referentielService.creerLocalisation(req));
    }

    @PutMapping("/localisations/{id}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).REFERENTIELS, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public ReferentielDTO modifierLocalisation(@PathVariable Long id, @Valid @RequestBody ReferentielRequeteDTO req) {
        return referentielService.modifierLocalisation(id, req);
    }

    @DeleteMapping("/localisations/{id}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).REFERENTIELS, T(gn.odc.gestionrh.common.enums.TypeAction).SUPPRIMER)")
    public ResponseEntity<Void> supprimerLocalisation(@PathVariable Long id) {
        referentielService.supprimerLocalisation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/localisations/corbeille")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).REFERENTIELS, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public List<ReferentielDTO> corbeilleLocalisations() {
        return referentielService.listerLocalisationsCorbeille();
    }

    @PostMapping("/localisations/{id}/restaurer")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).REFERENTIELS, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public ReferentielDTO restaurerLocalisation(@PathVariable Long id) {
        return referentielService.restaurerLocalisation(id);
    }
}
