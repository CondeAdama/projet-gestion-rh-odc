package gn.odc.gestionrh.configuration.service;

import gn.odc.gestionrh.common.exception.RegleMetierException;
import gn.odc.gestionrh.configuration.dto.ConfigurationNotificationDTO;
import gn.odc.gestionrh.configuration.dto.ModeleMessageDTO;
import gn.odc.gestionrh.configuration.entity.ConfigurationNotification;
import gn.odc.gestionrh.configuration.repository.ConfigurationNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConfigurationNotificationService {

    private final ConfigurationNotificationRepository repository;
    private final NotificationModeleService notificationModeleService;
    private final SmsConfigurationHelper smsConfigurationHelper;

    @Value("${minerva.app.url:http://localhost:5173}")
    private String defaultAppUrl;

    @Transactional
    public ConfigurationNotificationDTO obtenir() {
        return versDTO(trouverOuCreer());
    }

    @Transactional
    public ConfigurationNotification obtenirEntite() {
        return trouverOuCreer();
    }

    @Transactional
    public String obtenirAppUrl() {
        ConfigurationNotification config = trouverOuCreer();
        if (config.getAppUrl() != null && !config.getAppUrl().isBlank()) {
            return config.getAppUrl().trim();
        }
        return defaultAppUrl;
    }

    @Transactional
    public ConfigurationNotificationDTO reinitialiserModeles() {
        ConfigurationNotification config = trouverOuCreer();
        config.setModelesMessages(notificationModeleService.serialiserDefauts());
        return versDTO(repository.save(config));
    }

    @Transactional
    public ConfigurationNotificationDTO mettreAJour(ConfigurationNotificationDTO dto) {
        ConfigurationNotification config = trouverOuCreer();

        if (dto.getModeEnvoi() != null) {
            config.setModeEnvoi(dto.getModeEnvoi());
        }
        if (dto.getAppUrl() != null) {
            config.setAppUrl(dto.getAppUrl().trim());
        }
        config.setEmailActif(dto.isEmailActif());
        config.setSmtpHost(dto.getSmtpHost());
        config.setSmtpPort(dto.getSmtpPort() != null ? dto.getSmtpPort() : 587);
        config.setSmtpUsername(dto.getSmtpUsername());
        if (dto.getSmtpPassword() != null && !dto.getSmtpPassword().isBlank()) {
            config.setSmtpPassword(dto.getSmtpPassword());
        }
        config.setSmtpFromEmail(dto.getSmtpFromEmail());
        config.setSmtpFromName(dto.getSmtpFromName());
        config.setSmtpAuth(dto.isSmtpAuth());
        config.setSmtpStarttls(dto.isSmtpStarttls());

        config.setSmsActif(dto.isSmsActif());
        if (dto.getSmsProvider() != null) {
            config.setSmsProvider(dto.getSmsProvider());
        }
        config.setSmsAccountSid(dto.getSmsAccountSid());
        if (dto.getSmsApiSecret() != null && !dto.getSmsApiSecret().isBlank()) {
            config.setSmsApiSecret(dto.getSmsApiSecret());
        }
        config.setSmsSenderId(dto.getSmsSenderId());
        if (dto.getSmsExtra() != null) {
            config.setSmsExtraConfig(smsConfigurationHelper.serialiserExtra(dto.getSmsExtra()));
        }

        if (dto.getModeles() != null) {
            config.setModelesMessages(notificationModeleService.serialiser(dto.getModeles()));
        }

        valider(config);
        return versDTO(repository.save(config));
    }

    private void valider(ConfigurationNotification config) {
        if ("LIVE".equals(config.getModeEnvoi()) && config.isEmailActif()) {
            if (config.getSmtpHost() == null || config.getSmtpHost().isBlank()) {
                throw new RegleMetierException("Le serveur SMTP est obligatoire lorsque l'e-mail est activé");
            }
            if (config.getSmtpFromEmail() == null || config.getSmtpFromEmail().isBlank()) {
                throw new RegleMetierException("L'adresse d'expédition e-mail est obligatoire");
            }
        }
        if ("LIVE".equals(config.getModeEnvoi()) && config.isSmsActif()) {
            smsConfigurationHelper.validerSms(config);
        }
    }

    private ConfigurationNotification trouverOuCreer() {
        return repository.findAll().stream().findFirst().orElseGet(() ->
                repository.save(ConfigurationNotification.builder()
                        .modeEnvoi("MOCK")
                        .appUrl(defaultAppUrl)
                        .smtpHost("smtp.gmail.com")
                        .smtpPort(587)
                        .smtpAuth(true)
                        .smtpStarttls(true)
                        .smsProvider("TWILIO")
                        .build()));
    }

    private ConfigurationNotificationDTO versDTO(ConfigurationNotification c) {
        return ConfigurationNotificationDTO.builder()
                .id(c.getId())
                .modeEnvoi(c.getModeEnvoi())
                .appUrl(c.getAppUrl())
                .emailActif(c.isEmailActif())
                .smtpHost(c.getSmtpHost())
                .smtpPort(c.getSmtpPort())
                .smtpUsername(c.getSmtpUsername())
                .smtpPasswordConfigure(c.getSmtpPassword() != null && !c.getSmtpPassword().isBlank())
                .smtpFromEmail(c.getSmtpFromEmail())
                .smtpFromName(c.getSmtpFromName())
                .smtpAuth(c.isSmtpAuth())
                .smtpStarttls(c.isSmtpStarttls())
                .smsActif(c.isSmsActif())
                .smsProvider(c.getSmsProvider())
                .smsAccountSid(c.getSmsAccountSid())
                .smsApiSecretConfigure(c.getSmsApiSecret() != null && !c.getSmsApiSecret().isBlank())
                .smsSenderId(c.getSmsSenderId())
                .smsExtra(smsConfigurationHelper.lireExtra(c.getSmsExtraConfig()))
                .modeles(notificationModeleService.lireModeles(c))
                .build();
    }
}
