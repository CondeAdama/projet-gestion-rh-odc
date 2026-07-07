package gn.odc.gestionrh.configuration.service;

import gn.odc.gestionrh.configuration.entity.ConfigurationNotification;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class BrevoSmtpProperties {

    @Value("${brevo.smtp.host:smtp-relay.brevo.com}")
    private String host;

    @Value("${brevo.smtp.port:587}")
    private int port;

    @Value("${brevo.smtp.login:}")
    private String login;

    @Value("${brevo.smtp.password:}")
    private String password;

    @Value("${brevo.smtp.from-email:}")
    private String fromEmail;

    @Value("${brevo.smtp.from-name:MINERVA GROUP}")
    private String fromName;

    public boolean estConfigure() {
        return login != null && !login.isBlank()
                && password != null && !password.isBlank();
    }

    /** Priorité aux identifiants du fichier local (application-local.properties). */
    public ConfigurationNotification appliquer(ConfigurationNotification config) {
        if (!estConfigure()) {
            return config;
        }
        config.setModeEnvoi("LIVE");
        config.setEmailActif(true);
        config.setSmtpHost(host);
        config.setSmtpPort(port);
        config.setSmtpUsername(login.trim());
        config.setSmtpPassword(password.trim());
        config.setSmtpAuth(true);
        config.setSmtpStarttls(true);
        if (fromEmail != null && !fromEmail.isBlank()) {
            config.setSmtpFromEmail(fromEmail.trim());
        }
        if (fromName != null && !fromName.isBlank()) {
            config.setSmtpFromName(fromName.trim());
        }
        return config;
    }
}
