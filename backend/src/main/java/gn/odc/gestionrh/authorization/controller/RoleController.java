package gn.odc.gestionrh.authorization.controller;

import gn.odc.gestionrh.authorization.dto.RoleDTO;
import gn.odc.gestionrh.authorization.dto.RoleRequeteDTO;
import gn.odc.gestionrh.authorization.service.RoleService;
import gn.odc.gestionrh.common.enums.ModuleApplication;
import gn.odc.gestionrh.common.enums.TypeAction;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).ROLES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public List<RoleDTO> lister() {
        return roleService.listerActifs();
    }

    @GetMapping("/par-defaut")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).ROLES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public RoleDTO parDefaut() {
        return roleService.obtenirParDefaut();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).ROLES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public RoleDTO trouver(@PathVariable Long id) {
        return roleService.trouverParId(id);
    }

    @GetMapping("/modules")
    public Map<String, Object> modulesDisponibles() {
        return Map.of(
                "modules", ModuleApplication.values(),
                "actions", TypeAction.values()
        );
    }

    @PostMapping
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).ROLES, T(gn.odc.gestionrh.common.enums.TypeAction).AJOUTER)")
    public ResponseEntity<RoleDTO> creer(@Valid @RequestBody RoleRequeteDTO requete) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.creer(requete));
    }

    @PutMapping("/{id}/par-defaut")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).ROLES, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public RoleDTO definirParDefaut(@PathVariable Long id) {
        return roleService.definirParDefaut(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).ROLES, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public RoleDTO modifier(@PathVariable Long id, @Valid @RequestBody RoleRequeteDTO requete) {
        return roleService.modifier(id, requete);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).ROLES, T(gn.odc.gestionrh.common.enums.TypeAction).SUPPRIMER)")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        roleService.supprimerLogique(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restaurer")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).ROLES, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public RoleDTO restaurer(@PathVariable Long id) {
        return roleService.restaurer(id);
    }

    @DeleteMapping("/{id}/definitif")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).ROLES, T(gn.odc.gestionrh.common.enums.TypeAction).SUPPRIMER)")
    public ResponseEntity<Void> supprimerDefinitif(@PathVariable Long id) {
        roleService.supprimerDefinitif(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/corbeille")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).ROLES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public List<RoleDTO> corbeille() {
        return roleService.listerSupprimes();
    }
}
