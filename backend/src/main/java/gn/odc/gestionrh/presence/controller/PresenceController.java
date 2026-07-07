package gn.odc.gestionrh.presence.controller;

import gn.odc.gestionrh.presence.dto.PresenceDTO;
import gn.odc.gestionrh.presence.dto.ScanReponseDTO;
import gn.odc.gestionrh.presence.service.PresenceService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/presences")
@RequiredArgsConstructor
@Validated
public class PresenceController {

    private final PresenceService presenceService;

    @PostMapping("/scanner")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).PRESENCES, T(gn.odc.gestionrh.common.enums.TypeAction).AJOUTER)")
    public ScanReponseDTO scanner(
            @RequestParam @NotBlank(message = "Le matricule est obligatoire") String matricule,
            @RequestParam @NotNull(message = "La localisation est obligatoire") Long localisationId) {
        return presenceService.scanner(matricule.trim(), localisationId);
    }

    @GetMapping("/aujourdhui")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).PRESENCES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public List<PresenceDTO> aujourdhui() {
        return presenceService.aujourdhui();
    }

    @GetMapping("/employe/{employeId}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).PRESENCES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public List<PresenceDTO> parEmploye(@PathVariable Long employeId) {
        return presenceService.parEmploye(employeId);
    }

    @GetMapping("/recherche")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).PRESENCES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public List<PresenceDTO> rechercher(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) Long localisationId,
            @RequestParam(required = false) Long departementId,
            @RequestParam(required = false) String statutPresence) {
        return presenceService.rechercher(dateDebut, dateFin, localisationId, departementId, statutPresence);
    }

    @GetMapping("/corbeille")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).PRESENCES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public List<PresenceDTO> corbeille() {
        return presenceService.listerCorbeille();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).PRESENCES, T(gn.odc.gestionrh.common.enums.TypeAction).SUPPRIMER)")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        presenceService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restaurer")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).PRESENCES, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public PresenceDTO restaurer(@PathVariable Long id) {
        return presenceService.restaurer(id);
    }

    @GetMapping("/statistiques")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).PRESENCES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public Map<String, Long> statistiques() {
        return presenceService.statistiquesJour();
    }
}
