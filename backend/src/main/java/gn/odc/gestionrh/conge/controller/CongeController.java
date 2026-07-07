package gn.odc.gestionrh.conge.controller;

import gn.odc.gestionrh.authorization.service.AccesEmployeHelper;
import gn.odc.gestionrh.authorization.service.EvaluateurAutorisation;
import gn.odc.gestionrh.common.enums.ModuleApplication;
import gn.odc.gestionrh.common.enums.TypeAction;
import gn.odc.gestionrh.conge.dto.CongeDTO;
import gn.odc.gestionrh.conge.dto.CongeRequeteDTO;
import gn.odc.gestionrh.conge.dto.TraitementCongeDTO;
import gn.odc.gestionrh.conge.service.CongeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/conges")
@RequiredArgsConstructor
public class CongeController {

    private final CongeService congeService;
    private final EvaluateurAutorisation evaluateurAutorisation;
    private final AccesEmployeHelper accesEmployeHelper;

    @GetMapping
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONGES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public List<CongeDTO> listerTous(Authentication auth) {
        if (accesEmployeHelper.peutVoirAutrui(auth, ModuleApplication.CONGES)) {
            return congeService.listerTous();
        }
        return congeService.listerParEmploye(accesEmployeHelper.requireEmployeId(auth));
    }

    @GetMapping("/mes-conges")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONGES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public List<CongeDTO> mesConges(Authentication auth) {
        return congeService.listerParEmploye(accesEmployeHelper.requireEmployeId(auth));
    }

    @GetMapping("/employe/{employeId}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONGES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public List<CongeDTO> parEmploye(@PathVariable Long employeId, Authentication auth) {
        accesEmployeHelper.verifierPropreEmployeOuAutrui(auth, ModuleApplication.CONGES, employeId);
        return congeService.listerParEmploye(employeId);
    }

    @GetMapping("/en-attente")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONGES, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public List<CongeDTO> enAttente() {
        return congeService.listerEnAttente();
    }

    @GetMapping("/statistiques")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONGES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER_AUTRUI)")
    public Map<String, Long> statistiques() {
        return congeService.statistiques();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONGES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public CongeDTO trouver(@PathVariable Long id, Authentication auth) {
        congeService.verifierAccesEmploye(
                id,
                accesEmployeHelper.employeId(auth),
                accesEmployeHelper.peutVoirAutrui(auth, ModuleApplication.CONGES));
        return congeService.trouverParId(id);
    }

    @PostMapping
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONGES, T(gn.odc.gestionrh.common.enums.TypeAction).AJOUTER)")
    public ResponseEntity<CongeDTO> demander(@Valid @RequestBody CongeRequeteDTO req, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(congeService.demander(req, accesEmployeHelper.requireEmployeId(auth), peutCreerPourAutrui(auth)));
    }

    @PostMapping("/employe/{employeId}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONGES, T(gn.odc.gestionrh.common.enums.TypeAction).AJOUTER)")
    public ResponseEntity<CongeDTO> demanderPourEmploye(
            @PathVariable Long employeId,
            @Valid @RequestBody CongeRequeteDTO req,
            Authentication auth) {
        accesEmployeHelper.verifierPropreEmployeOuAutrui(auth, ModuleApplication.CONGES, employeId);
        req.setEmployeId(employeId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(congeService.demander(req, accesEmployeHelper.requireEmployeId(auth), peutCreerPourAutrui(auth)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONGES, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER) or @autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONGES, T(gn.odc.gestionrh.common.enums.TypeAction).AJOUTER)")
    public CongeDTO modifier(@PathVariable Long id, @Valid @RequestBody CongeRequeteDTO req, Authentication auth) {
        boolean peutModifierAutrui = evaluateurAutorisation.aPermission(auth, ModuleApplication.CONGES, TypeAction.MODIFIER);
        return congeService.modifier(id, req, accesEmployeHelper.requireEmployeId(auth), peutModifierAutrui);
    }

    @PutMapping("/{id}/traiter")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONGES, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public CongeDTO traiter(@PathVariable Long id, @Valid @RequestBody TraitementCongeDTO req) {
        return congeService.traiter(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONGES, T(gn.odc.gestionrh.common.enums.TypeAction).SUPPRIMER)")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        congeService.supprimerLogique(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restaurer")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONGES, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public CongeDTO restaurer(@PathVariable Long id) {
        return congeService.restaurer(id);
    }

    @DeleteMapping("/{id}/definitif")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONGES, T(gn.odc.gestionrh.common.enums.TypeAction).SUPPRIMER)")
    public ResponseEntity<Void> supprimerDefinitif(@PathVariable Long id) {
        congeService.supprimerDefinitif(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/corbeille")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONGES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER_AUTRUI)")
    public List<CongeDTO> corbeille() {
        return congeService.listerSupprimes();
    }

    private boolean peutCreerPourAutrui(Authentication auth) {
        return evaluateurAutorisation.aPermission(auth, ModuleApplication.CONGES, TypeAction.AFFICHER_AUTRUI)
                || evaluateurAutorisation.aPermission(auth, ModuleApplication.CONGES, TypeAction.MODIFIER);
    }
}
