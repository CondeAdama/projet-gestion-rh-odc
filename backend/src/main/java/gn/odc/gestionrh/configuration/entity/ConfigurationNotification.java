package gn.odc.gestionrh.configuration.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "configuration_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigurationNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mode_envoi", nullable = false, length = 10)
    @Builder.Default
    private String modeEnvoi = "MOCK";

    @Column(name = "app_url", length = 255)
    private String appUrl;

    @Column(name = "email_actif", nullable = false)
    @Builder.Default
    private boolean emailActif = false;

    @Column(name = "smtp_host", length = 150)
    private String smtpHost;

    @Column(name = "smtp_port")
    @Builder.Default
    private Integer smtpPort = 587;

    @Column(name = "smtp_username", length = 150)
    private String smtpUsername;

    @Column(name = "smtp_password", length = 255)
    private String smtpPassword;

    @Column(name = "smtp_from_email", length = 150)
    private String smtpFromEmail;

    @Column(name = "smtp_from_name", length = 100)
    private String smtpFromName;

    @Column(name = "smtp_auth", nullable = false)
    @Builder.Default
    private boolean smtpAuth = true;

    @Column(name = "smtp_starttls", nullable = false)
    @Builder.Default
    private boolean smtpStarttls = true;

    @Column(name = "sms_actif", nullable = false)
    @Builder.Default
    private boolean smsActif = false;

    @Column(name = "sms_provider", length = 40)
    @Builder.Default
    private String smsProvider = "TWILIO";

    @Column(name = "sms_account_sid", length = 255)
    private String smsAccountSid;

    @Column(name = "sms_api_secret", length = 255)
    private String smsApiSecret;

    @Column(name = "sms_sender_id", length = 50)
    private String smsSenderId;

    /** Paramètres additionnels (région AWS, canal Termii, etc.) — JSON */
    @Column(name = "sms_extra_config", columnDefinition = "TEXT")
    private String smsExtraConfig;

    @Column(name = "modeles_messages", columnDefinition = "LONGTEXT")
    private String modelesMessages;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @PrePersist
    @PreUpdate
    void touch() {
        dateModification = LocalDateTime.now();
    }
}
