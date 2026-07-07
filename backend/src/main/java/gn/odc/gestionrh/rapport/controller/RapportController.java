package gn.odc.gestionrh.rapport.controller;

import gn.odc.gestionrh.rapport.service.RapportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/rapports")
@RequiredArgsConstructor
public class RapportController {

    private final RapportService rapportService;

    @GetMapping("/synthese")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).RAPPORTS, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public Map<String, Object> synthese() {
        return rapportService.synthese();
    }
}
