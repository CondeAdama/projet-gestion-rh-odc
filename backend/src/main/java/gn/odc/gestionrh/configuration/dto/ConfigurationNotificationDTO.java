package gn.odc.gestionrh.configuration.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ConfigurationNotificationDTO {
    private Long id;

    @Pattern(regexp = "MOCK|LIVE", message = "Mode invalide (MOCK ou LIVE)")
    private String modeEnvoi;

    @Size(max = 255)
    private String appUrl;

    private boolean emailActif;

    @Size(max = 150)
    private String smtpHost;

    @Min(1)
    @Max(65535)
    private Integer smtpPort;

    @Size(max = 150)
    private String smtpUsername;

    /** Vide = conserver le mot de passe existant */
    @Size(max = 255)
    private String smtpPassword;

    private boolean smtpPasswordConfigure;

    @Email
    @Size(max = 150)
    private String smtpFromEmail;

    @Size(max = 100)
    private String smtpFromName;

    private boolean smtpAuth;
    private boolean smtpStarttls;

    private boolean smsActif;

    @Pattern(regexp = "TWILIO|BREVO|AFRICAS_TALKING|TERMII|VONAGE|MESSAGEBIRD|AWS_SNS|CLICKSEND|PLIVO|SINCH|INFOBIP|TEXTLOCAL|TELESIGN|HTTP",
            message = "Fournisseur SMS invalide")
    private String smsProvider;

    @Size(max = 255)
    private String smsAccountSid;

    @Size(max = 255)
    private String smsApiSecret;

    private boolean smsApiSecretConfigure;

    @Size(max = 50)
    private String smsSenderId;

    /** Champs supplémentaires selon le fournisseur (region, channel, …) */
    private Map<String, String> smsExtra;

    private Map<String, ModeleMessageDTO> modeles;
}
