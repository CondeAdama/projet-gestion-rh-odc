package gn.odc.gestionrh.configuration.controller;

import gn.odc.gestionrh.common.enums.SmsProvider;
import gn.odc.gestionrh.common.exception.RegleMetierException;
import gn.odc.gestionrh.configuration.dto.ConfigurationNotificationDTO;
import gn.odc.gestionrh.configuration.dto.TestEmailDTO;
import gn.odc.gestionrh.configuration.dto.TestSmsDTO;
import gn.odc.gestionrh.configuration.service.ConfigurationNotificationService;
import gn.odc.gestionrh.notification.service.ConfigurableNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/configuration/notifications")
@RequiredArgsConstructor
public class ConfigurationNotificationController {

    private final ConfigurationNotificationService configurationNotificationService;
    private final ConfigurableNotificationService configurableNotificationService;

    @GetMapping
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONFIGURATION, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public ConfigurationNotificationDTO obtenir() {
        return configurationNotificationService.obtenir();
    }

    @PutMapping
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONFIGURATION, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public ConfigurationNotificationDTO mettreAJour(@Valid @RequestBody ConfigurationNotificationDTO dto) {
        return configurationNotificationService.mettreAJour(dto);
    }

    @PostMapping("/reinitialiser-modeles")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONFIGURATION, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public ConfigurationNotificationDTO reinitialiserModeles() {
        return configurationNotificationService.reinitialiserModeles();
    }

    @GetMapping("/sms-providers")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONFIGURATION, T(gn.odc.gestionrh.common.enums.TypeAction).AFFICHER)")
    public Map<String, Object> listerFournisseursSms() {
        return Map.of(
                "fournisseurs", Arrays.stream(SmsProvider.values())
                        .map(p -> Map.of("code", p.name(), "libelle", libelleFournisseur(p)))
                        .collect(Collectors.toList())
        );
    }

    private String libelleFournisseur(SmsProvider p) {
        return switch (p) {
            case TWILIO -> "Twilio";
            case BREVO -> "Brevo (Sendinblue)";
            case AFRICAS_TALKING -> "Africa's Talking";
            case TERMII -> "Termii";
            case VONAGE -> "Vonage (Nexmo)";
            case MESSAGEBIRD -> "MessageBird";
            case AWS_SNS -> "Amazon SNS";
            case CLICKSEND -> "ClickSend";
            case PLIVO -> "Plivo";
            case SINCH -> "Sinch";
            case INFOBIP -> "Infobip";
            case TEXTLOCAL -> "Textlocal";
            case TELESIGN -> "Telesign";
            case HTTP -> "API HTTP personnalisée";
        };
    }

    @PostMapping("/test-email")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONFIGURATION, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public Map<String, String> testerEmail(@Valid @RequestBody TestEmailDTO dto) {
        var config = configurationNotificationService.obtenirEntite();
        if (!"LIVE".equals(config.getModeEnvoi())) {
            throw new RegleMetierException("Passez en mode LIVE pour tester l'envoi réel");
        }
        if (!config.isEmailActif()) {
            throw new RegleMetierException("Activez les notifications e-mail avant le test");
        }
        configurableNotificationService.envoyerTestEmail(dto.getEmail());
        return Map.of("message", "E-mail de test envoyé à " + dto.getEmail());
    }

    @PostMapping("/test-sms")
    @PreAuthorize("@autorisation.aPermission(authentication, T(gn.odc.gestionrh.common.enums.ModuleApplication).CONFIGURATION, T(gn.odc.gestionrh.common.enums.TypeAction).MODIFIER)")
    public Map<String, String> testerSms(@Valid @RequestBody TestSmsDTO dto) {
        var config = configurationNotificationService.obtenirEntite();
        if (!"LIVE".equals(config.getModeEnvoi())) {
            throw new RegleMetierException("Passez en mode LIVE pour tester l'envoi réel");
        }
        if (!config.isSmsActif()) {
            throw new RegleMetierException("Activez les notifications SMS avant le test");
        }
        configurableNotificationService.envoyerTestSms(dto.getTelephone().replaceAll("\\s+", ""));
        return Map.of("message", "SMS de test envoyé à " + dto.getTelephone());
    }
}
