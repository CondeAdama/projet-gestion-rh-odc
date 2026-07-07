package gn.odc.gestionrh.configuration.service;

import gn.odc.gestionrh.configuration.dto.ConfigurationEntrepriseDTO;
import gn.odc.gestionrh.configuration.entity.ConfigurationEntreprise;
import gn.odc.gestionrh.configuration.repository.ConfigurationEntrepriseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfigurationEntrepriseService {

    private final ConfigurationEntrepriseRepository repository;

    @Transactional(readOnly = true)
    public ConfigurationEntrepriseDTO obtenir() {
        return versDTO(trouverOuCreer());
    }

    @Transactional
    public ConfigurationEntrepriseDTO mettreAJour(ConfigurationEntrepriseDTO dto) {
        ConfigurationEntreprise config = trouverOuCreer();
        config.setNomEntreprise(dto.getNomEntreprise());
        config.setAdresse(dto.getAdresse());
        config.setTelephone(dto.getTelephone());
        config.setEmail(dto.getEmail());
        config.setNif(dto.getNif());
        config.setNumeroCnss(dto.getNumeroCnss());
        config.setTauxCnss(dto.getTauxCnss() != null ? dto.getTauxCnss() : new java.math.BigDecimal("5.00"));
        config.setTauxRts(dto.getTauxRts() != null ? dto.getTauxRts() : new java.math.BigDecimal("10.00"));
        config.setSlogan(dto.getSlogan());
        config.setLogoUrl(dto.getLogoUrl());
        config.setDevise(dto.getDevise() != null ? dto.getDevise() : "GNF");
        return versDTO(repository.save(config));
    }

    @Transactional
    public ConfigurationEntrepriseDTO mettreAJourLogo(String logoUrl) {
        ConfigurationEntreprise config = trouverOuCreer();
        config.setLogoUrl(logoUrl);
        return versDTO(repository.save(config));
    }

    private ConfigurationEntreprise trouverOuCreer() {
        return repository.findAll().stream().findFirst().orElseGet(() ->
                repository.save(ConfigurationEntreprise.builder()
                        .nomEntreprise("MINERVA GROUP")
                        .adresse("Siège MINERVA, Kaloum, Conakry, Guinée")
                        .telephone("+224 620 00 00 00")
                        .email("contact@minerva.group")
                        .nif("GN-NIF-2026-B98745")
                        .numeroCnss("GN-CNSS-2026-45892")
                        .tauxCnss(new java.math.BigDecimal("5.00"))
                        .tauxRts(new java.math.BigDecimal("10.00"))
                        .slogan("Identité sociale et logistique RH")
                        .devise("GNF")
                        .build()));
    }

    private ConfigurationEntrepriseDTO versDTO(ConfigurationEntreprise c) {
        return ConfigurationEntrepriseDTO.builder()
                .id(c.getId())
                .nomEntreprise(c.getNomEntreprise())
                .adresse(c.getAdresse())
                .telephone(c.getTelephone())
                .email(c.getEmail())
                .nif(c.getNif())
                .numeroCnss(c.getNumeroCnss())
                .tauxCnss(c.getTauxCnss())
                .tauxRts(c.getTauxRts())
                .slogan(c.getSlogan())
                .logoUrl(c.getLogoUrl())
                .devise(c.getDevise())
                .build();
    }
}
