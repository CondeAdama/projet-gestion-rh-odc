package gn.odc.gestionrh.configuration.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EntrepriseContextService {

    private static final String DEFAULT_NOM = "MINERVA GROUP";

    private final ConfigurationEntrepriseService configurationEntrepriseService;

    public String nomEntreprise() {
        try {
            String nom = configurationEntrepriseService.obtenir().getNomEntreprise();
            return nom != null && !nom.isBlank() ? nom.trim() : DEFAULT_NOM;
        } catch (Exception e) {
            return DEFAULT_NOM;
        }
    }
}
