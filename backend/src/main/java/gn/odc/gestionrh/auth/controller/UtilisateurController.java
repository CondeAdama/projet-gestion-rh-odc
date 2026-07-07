package gn.odc.gestionrh.auth.controller;

import gn.odc.gestionrh.auth.dto.UtilisateurDTO;
import gn.odc.gestionrh.auth.dto.UtilisateurModifierDTO;
import gn.odc.gestionrh.auth.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @GetMapping
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).UTILISATEURS, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public List<UtilisateurDTO> lister() {
        return utilisateurService.listerActifs();
    }

    @GetMapping("/corbeille")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).UTILISATEURS, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public List<UtilisateurDTO> corbeille() {
        return utilisateurService.listerCorbeille();
    }

    @PutMapping("/{id}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).UTILISATEURS, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public UtilisateurDTO modifier(@PathVariable Long id, @Valid @RequestBody UtilisateurModifierDTO dto) {
        return utilisateurService.modifier(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).UTILISATEURS, T(gn.odc.gestionrh.common.enums.TypeAction).SUPPRIMER)")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        utilisateurService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restaurer")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).UTILISATEURS, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public UtilisateurDTO restaurer(@PathVariable Long id) {
        return utilisateurService.restaurer(id);
    }

    @PostMapping("/{id}/renvoyer-activation")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).UTILISATEURS, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public Map<String, String> renvoyerActivation(@PathVariable Long id) {
        return utilisateurService.renvoyerActivation(id);
    }
}
