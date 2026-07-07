package gn.odc.gestionrh.contrat.controller;

import gn.odc.gestionrh.authorization.service.AccesEmployeHelper;
import gn.odc.gestionrh.common.enums.ModuleApplication;
import gn.odc.gestionrh.contrat.dto.ContratDTO;
import gn.odc.gestionrh.contrat.dto.ContratRequeteDTO;
import gn.odc.gestionrh.contrat.service.ContratService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contrats")
@RequiredArgsConstructor
public class ContratController {

    private final ContratService contratService;
    private final AccesEmployeHelper accesEmployeHelper;

    @GetMapping
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONTRATS, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER_AUTRUI)")
    public List<ContratDTO> lister() {
        return contratService.listerActifs();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONTRATS, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public ContratDTO trouver(@PathVariable Long id, Authentication auth) {
        ContratDTO contrat = contratService.trouverParId(id);
        accesEmployeHelper.verifierPropreEmployeOuAutrui(auth, ModuleApplication.CONTRATS, contrat.getEmployeId());
        return contrat;
    }

    @GetMapping("/employe/{employeId}/actif")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONTRATS, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public ContratDTO contratActif(@PathVariable Long employeId, Authentication auth) {
        accesEmployeHelper.verifierPropreEmployeOuAutrui(auth, ModuleApplication.CONTRATS, employeId);
        return contratService.trouverContratActifEmploye(employeId);
    }

    @GetMapping("/employe/{employeId}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONTRATS, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public List<ContratDTO> historique(@PathVariable Long employeId, Authentication auth) {
        accesEmployeHelper.verifierPropreEmployeOuAutrui(auth, ModuleApplication.CONTRATS, employeId);
        return contratService.listerParEmploye(employeId);
    }

    @PostMapping
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONTRATS, T(gn.odc.gestionrh.common.enums.TypeAction).AJOUTER)")
    public ResponseEntity<ContratDTO> creer(@Valid @RequestBody ContratRequeteDTO requete) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contratService.creer(requete));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONTRATS, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public ContratDTO modifier(@PathVariable Long id, @Valid @RequestBody ContratRequeteDTO requete) {
        return contratService.modifier(id, requete);
    }

    @PutMapping("/{id}/resilier")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONTRATS, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public ContratDTO resilier(@PathVariable Long id) {
        return contratService.resilier(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONTRATS, T(gn.odc.gestionrh.common.enums.TypeAction).SUPPRIMER)")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        contratService.supprimerLogique(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restaurer")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONTRATS, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public ContratDTO restaurer(@PathVariable Long id) {
        return contratService.restaurer(id);
    }

    @DeleteMapping("/{id}/definitif")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONTRATS, T(gn.odc.gestionrh.common.enums.TypeAction).SUPPRIMER)")
    public ResponseEntity<Void> supprimerDefinitif(@PathVariable Long id) {
        contratService.supprimerDefinitif(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/corbeille")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONTRATS, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER_AUTRUI)")
    public List<ContratDTO> corbeille() {
        return contratService.listerSupprimes();
    }
}
