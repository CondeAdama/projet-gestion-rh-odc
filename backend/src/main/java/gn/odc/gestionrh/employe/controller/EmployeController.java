package gn.odc.gestionrh.employe.controller;

import gn.odc.gestionrh.authorization.service.AccesEmployeHelper;
import gn.odc.gestionrh.common.enums.ModuleApplication;
import gn.odc.gestionrh.common.util.ValidateurFichier;
import gn.odc.gestionrh.employe.dto.EmployeDTO;
import gn.odc.gestionrh.employe.dto.EmployeRequeteDTO;
import gn.odc.gestionrh.employe.service.EmployeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/employes")
@RequiredArgsConstructor
public class EmployeController {

    private final EmployeService employeService;
    private final AccesEmployeHelper accesEmployeHelper;
    private static final Path UPLOAD_DIR = Paths.get("uploads/photos");

    @GetMapping
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).EMPLOYES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public List<EmployeDTO> lister(Authentication auth) {
        if (accesEmployeHelper.peutVoirAutrui(auth, ModuleApplication.EMPLOYES)) {
            return employeService.listerActifs();
        }
        return List.of(employeService.trouverParId(accesEmployeHelper.requireEmployeId(auth)));
    }

    @GetMapping("/moi")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).EMPLOYES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public EmployeDTO moi(Authentication auth) {
        return employeService.trouverParId(accesEmployeHelper.requireEmployeId(auth));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).EMPLOYES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public EmployeDTO trouver(@PathVariable Long id, Authentication auth) {
        accesEmployeHelper.verifierPropreEmployeOuAutrui(auth, ModuleApplication.EMPLOYES, id);
        return employeService.trouverParId(id);
    }

    @GetMapping("/matricule/{matricule}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).EMPLOYES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public EmployeDTO trouverParMatricule(@PathVariable String matricule, Authentication auth) {
        EmployeDTO employe = employeService.trouverParMatricule(matricule);
        accesEmployeHelper.verifierPropreEmployeOuAutrui(auth, ModuleApplication.EMPLOYES, employe.getId());
        return employe;
    }

    @PostMapping
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).EMPLOYES, T(gn.odc.gestionrh.common.enums.TypeAction).AJOUTER)")
    public ResponseEntity<EmployeDTO> creer(@Valid @RequestBody EmployeRequeteDTO requete) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeService.creer(requete));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).EMPLOYES, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public EmployeDTO modifier(@PathVariable Long id, @Valid @RequestBody EmployeRequeteDTO requete) {
        return employeService.modifier(id, requete);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).EMPLOYES, T(gn.odc.gestionrh.common.enums.TypeAction).SUPPRIMER)")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        employeService.supprimerLogique(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restaurer")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).EMPLOYES, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public EmployeDTO restaurer(@PathVariable Long id) {
        return employeService.restaurer(id);
    }

    @DeleteMapping("/{id}/definitif")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).EMPLOYES, T(gn.odc.gestionrh.common.enums.TypeAction).SUPPRIMER)")
    public ResponseEntity<Void> supprimerDefinitif(@PathVariable Long id) {
        employeService.supprimerDefinitif(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/licencier")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).EMPLOYES, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public EmployeDTO licencier(@PathVariable Long id) {
        return employeService.licencier(id);
    }

    @PostMapping("/{id}/suspendre")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).EMPLOYES, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public EmployeDTO suspendre(@PathVariable Long id) {
        return employeService.suspendre(id);
    }

    @GetMapping("/corbeille")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).EMPLOYES, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER_AUTRUI)")
    public List<EmployeDTO> corbeille() {
        return employeService.listerSupprimes();
    }

    @PostMapping("/{id}/renvoyer-activation")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).EMPLOYES, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public Map<String, String> renvoyerActivation(@PathVariable Long id) {
        return employeService.renvoyerActivation(id);
    }

    @PostMapping("/upload-photo")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).EMPLOYES, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER) or @autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).EMPLOYES, T(gn.odc.gestionrh.common.enums.TypeAction).AJOUTER)")
    public Map<String, String> uploadPhoto(@RequestParam("file") MultipartFile file) throws IOException {
        ValidateurFichier.validerImage(file);
        if (!Files.exists(UPLOAD_DIR)) {
            Files.createDirectories(UPLOAD_DIR);
        }
        String ext = ValidateurFichier.extraireExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + ext;
        Files.copy(file.getInputStream(), UPLOAD_DIR.resolve(filename));
        return Map.of("url", "/uploads/photos/" + filename);
    }
}
