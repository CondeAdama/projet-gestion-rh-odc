package gn.odc.gestionrh.notification.service;

import gn.odc.gestionrh.common.exception.RegleMetierException;
import gn.odc.gestionrh.configuration.entity.ConfigurationNotification;
import gn.odc.gestionrh.configuration.service.BrevoSmtpProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExpediteurEmailService {

    private final BrevoSmtpProperties brevoSmtpProperties;
    private final BrevoApiEmailSender brevoApiEmailSender;

    public void envoyer(ConfigurationNotification config, String destinataire, String sujet, String contenu) {
        if (destinataire == null || destinataire.isBlank()) {
            throw new RegleMetierException("Destinataire e-mail manquant");
        }
        ConfigurationNotification cfg = brevoSmtpProperties.appliquer(config);
        validerExpediteur(cfg);

        if (brevoSmtpProperties.isUseApi()) {
            if (!brevoSmtpProperties.preferApi()) {
                throw new RegleMetierException(
                        "Clé API Brevo manquante (variable BREVO_API_KEY). "
                                + "Sur Render, le SMTP est bloqué — créez une clé API dans Brevo → SMTP et API → Clés API.");
            }
            brevoApiEmailSender.envoyer(
                    brevoSmtpProperties.cleApiEffective(),
                    cfg.getSmtpFromEmail(),
                    cfg.getSmtpFromName(),
                    destinataire,
                    sujet,
                    contenu
            );
            return;
        }

        validerIdentifiantsSmtp(cfg);
        try {
            JavaMailSenderImpl mailSender = construireMailSender(cfg);
            var message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            String from = cfg.getSmtpFromEmail();
            String fromName = cfg.getSmtpFromName();
            if (fromName != null && !fromName.isBlank()) {
                helper.setFrom(from, fromName);
            } else {
                helper.setFrom(from);
            }
            helper.setTo(destinataire.trim());
            helper.setSubject(sujet);
            helper.setText(contenu, false);
            mailSender.send(message);
            log.info("E-mail SMTP envoyé à {}", destinataire);
        } catch (RegleMetierException e) {
            throw e;
        } catch (Exception e) {
            log.error("Échec envoi e-mail SMTP à {} : {}", destinataire, e.getMessage());
            String msg = e.getMessage() != null ? e.getMessage() : "erreur inconnue";
            if (msg.toLowerCase().contains("authentication")) {
                throw new RegleMetierException(
                        "Authentification SMTP refusée. Vérifiez le login Brevo (xxx@smtp-brevo.com) "
                                + "et régénérez une clé SMTP dans Brevo → SMTP et API.");
            }
            throw new RegleMetierException("Échec envoi e-mail : " + msg);
        }
    }

    private void validerExpediteur(ConfigurationNotification cfg) {
        if (cfg.getSmtpFromEmail() == null || cfg.getSmtpFromEmail().isBlank()) {
            throw new RegleMetierException("E-mail expéditeur manquant (doit être vérifié dans Brevo)");
        }
        if (brevoSmtpProperties.preferApi()) {
            return;
        }
        validerIdentifiantsSmtp(cfg);
    }

    private void validerIdentifiantsSmtp(ConfigurationNotification cfg) {
        if (cfg.getSmtpUsername() == null || cfg.getSmtpUsername().isBlank()) {
            throw new RegleMetierException("Utilisateur SMTP manquant (login Brevo @smtp-brevo.com)");
        }
        if (cfg.getSmtpPassword() == null || cfg.getSmtpPassword().isBlank()) {
            throw new RegleMetierException("Clé SMTP manquante. Collez la clé Brevo dans Configuration ou application-local.properties");
        }
    }

    private JavaMailSenderImpl construireMailSender(ConfigurationNotification config) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(config.getSmtpHost().trim());
        mailSender.setPort(config.getSmtpPort() != null ? config.getSmtpPort() : 587);
        mailSender.setUsername(config.getSmtpUsername().trim());
        mailSender.setPassword(config.getSmtpPassword().trim());
        mailSender.setDefaultEncoding(StandardCharsets.UTF_8.name());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        props.put("mail.smtp.ssl.trust", config.getSmtpHost().trim());
        props.put("mail.smtp.connectiontimeout", "15000");
        props.put("mail.smtp.timeout", "15000");
        props.put("mail.smtp.writetimeout", "15000");
        return mailSender;
    }
}
