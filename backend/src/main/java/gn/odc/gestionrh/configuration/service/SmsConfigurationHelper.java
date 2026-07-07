package gn.odc.gestionrh.configuration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gn.odc.gestionrh.common.enums.SmsProvider;
import gn.odc.gestionrh.common.exception.RegleMetierException;
import gn.odc.gestionrh.configuration.entity.ConfigurationNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SmsConfigurationHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public void validerSms(ConfigurationNotification config) {
        if (!"LIVE".equals(config.getModeEnvoi()) || !config.isSmsActif()) {
            return;
        }
        SmsProvider provider = parseProvider(config.getSmsProvider());
        Map<String, String> extra = lireExtra(config.getSmsExtraConfig());
        String sid = config.getSmsAccountSid();
        String secret = config.getSmsApiSecret();
        String sender = config.getSmsSenderId();

        if (provider != SmsProvider.HTTP) {
            requireSecret(secret);
        } else if ((secret == null || secret.isBlank()) && (sid == null || sid.isBlank())) {
            throw new RegleMetierException("L'URL API HTTP est obligatoire");
        }

        switch (provider) {
            case BREVO, MESSAGEBIRD, TERMII, TEXTLOCAL -> requireSender(sender);
            case TWILIO, AFRICAS_TALKING, VONAGE, CLICKSEND, PLIVO, SINCH, INFOBIP -> {
                requireSid(sid, "Identifiant API");
                requireSender(sender);
            }
            case AWS_SNS -> {
                requireSid(sid, "Access Key ID");
                requireSecret(secret);
                requireExtra(extra, "region", "La région AWS est obligatoire");
            }
            case TELESIGN -> {
                requireSid(sid, "Customer ID");
                requireSecret(secret);
            }
            case HTTP -> {
                requireSid(sid, "URL de l'API");
                if (!sid.startsWith("http")) {
                    throw new RegleMetierException("L'URL API HTTP doit commencer par http:// ou https://");
                }
                requireSender(sender);
            }
        }
    }

    public Map<String, String> lireExtra(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            return MAPPER.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.warn("sms_extra_config invalide, ignoré");
            return new HashMap<>();
        }
    }

    public String serialiserExtra(Map<String, String> extra) {
        if (extra == null || extra.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(extra);
        } catch (JsonProcessingException e) {
            throw new RegleMetierException("Configuration SMS additionnelle invalide");
        }
    }

    public SmsProvider parseProvider(String code) {
        try {
            return SmsProvider.valueOf(code != null ? code.trim().toUpperCase() : "TWILIO");
        } catch (IllegalArgumentException e) {
            throw new RegleMetierException("Fournisseur SMS inconnu : " + code);
        }
    }

    public String extra(Map<String, String> extra, String key) {
        if (extra == null) {
            return "";
        }
        String v = extra.get(key);
        return v != null ? v : "";
    }

    private void requireSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new RegleMetierException("La clé / secret API SMS est obligatoire");
        }
    }

    private void requireSid(String sid, String label) {
        if (sid == null || sid.isBlank()) {
            throw new RegleMetierException(label + " SMS obligatoire pour ce fournisseur");
        }
    }

    private void requireSender(String sender) {
        if (sender == null || sender.isBlank()) {
            throw new RegleMetierException("L'expéditeur SMS est obligatoire");
        }
    }

    private void requireExtra(Map<String, String> extra, String key, String message) {
        if (extra.get(key) == null || extra.get(key).isBlank()) {
            throw new RegleMetierException(message);
        }
    }
}
