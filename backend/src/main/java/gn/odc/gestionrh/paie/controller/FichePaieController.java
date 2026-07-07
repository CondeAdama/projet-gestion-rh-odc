package gn.odc.gestionrh.paie.controller;

import gn.odc.gestionrh.authorization.service.AccesEmployeHelper;
import gn.odc.gestionrh.common.enums.ModuleApplication;
import gn.odc.gestionrh.common.enums.TypeAction;
import gn.odc.gestionrh.paie.dto.FichePaieDTO;
import gn.odc.gestionrh.paie.dto.GenererPaieRequeteDTO;
import gn.odc.gestionrh.paie.service.FichePaieService;
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
@RequestMapping("/fiches-paie")
@RequiredArgsConstructor
public class FichePaieController {

    private final FichePaieService fichePaieService;
    private final AccesEmployeHelper accesEmployeHelper;

    @GetMapping
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).PAIES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public List<FichePaieDTO> lister(Authentication auth) {
        if (accesEmployeHelper.peutVoirAutrui(auth, ModuleApplication.PAIES)) {
            return fichePaieService.listerToutes();
        }
        return fichePaieService.listerParEmploye(accesEmployeHelper.requireEmployeId(auth));
    }

    @GetMapping("/mes-fiches")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).PAIES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public List<FichePaieDTO> mesFiches(Authentication auth) {
        return fichePaieService.listerParEmploye(accesEmployeHelper.requireEmployeId(auth));
    }

    @GetMapping("/employe/{employeId}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).PAIES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public List<FichePaieDTO> parEmploye(@PathVariable Long employeId, Authentication auth) {
        accesEmployeHelper.verifierPropreEmployeOuAutrui(auth, ModuleApplication.PAIES, employeId);
        return fichePaieService.listerParEmploye(employeId);
    }

    @GetMapping("/statistiques")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).PAIES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER_AUTRUI)")
    public Map<String, Object> statistiques() {
        return fichePaieService.statistiques();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).PAIES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public FichePaieDTO trouver(@PathVariable Long id, Authentication auth) {
        fichePaieService.verifierAccesEmploye(
                id,
                accesEmployeHelper.employeId(auth),
                accesEmployeHelper.peutVoirAutrui(auth, ModuleApplication.PAIES));
        return fichePaieService.trouverParId(id);
    }

    @PostMapping("/generer")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).PAIES, T(gn.odc.gestionrh.common.enums.TypeAction).AJOUTER)")
    public ResponseEntity<FichePaieDTO> generer(@Valid @RequestBody GenererPaieRequeteDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fichePaieService.generer(req.getEmployeId(), req.getMois(), req.getAnnee()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).PAIES, T(gn.odc.gestionrh.common.enums.TypeAction).SUPPRIMER)")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        fichePaieService.supprimerLogique(id);
        return ResponseEntity.noContent().build();
    }
}
