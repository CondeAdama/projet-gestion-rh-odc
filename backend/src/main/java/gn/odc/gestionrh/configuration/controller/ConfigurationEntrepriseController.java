package gn.odc.gestionrh.configuration.controller;

import gn.odc.gestionrh.configuration.dto.ConfigurationEntrepriseDTO;
import gn.odc.gestionrh.configuration.service.ConfigurationEntrepriseService;
import gn.odc.gestionrh.common.util.ValidateurFichier;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@RestController
@RequestMapping("/configuration")
@RequiredArgsConstructor
public class ConfigurationEntrepriseController {

    private static final Path LOGO_DIR = Path.of("uploads", "logos");

    private final ConfigurationEntrepriseService service;

    @GetMapping
    public ConfigurationEntrepriseDTO obtenir() {
        return service.obtenir();
    }

    @PutMapping
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONFIGURATION, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public ConfigurationEntrepriseDTO mettreAJour(@Valid @RequestBody ConfigurationEntrepriseDTO dto) {
        return service.mettreAJour(dto);
    }

    @PostMapping(value = "/upload-logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONFIGURATION, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public ConfigurationEntrepriseDTO uploadLogo(@RequestParam("file") MultipartFile file) throws IOException {
        ValidateurFichier.validerImage(file);
        if (!Files.exists(LOGO_DIR)) {
            Files.createDirectories(LOGO_DIR);
        }
        String ext = ValidateurFichier.extraireExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + ext;
        Files.copy(file.getInputStream(), LOGO_DIR.resolve(filename));
        return service.mettreAJourLogo("/uploads/logos/" + filename);
    }
}
