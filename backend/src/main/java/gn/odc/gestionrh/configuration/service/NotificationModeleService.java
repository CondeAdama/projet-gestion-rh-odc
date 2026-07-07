package gn.odc.gestionrh.configuration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gn.odc.gestionrh.configuration.dto.ModeleMessageDTO;
import gn.odc.gestionrh.configuration.entity.ConfigurationNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationModeleService {

    public record MessageRendu(String sujet, String email, String sms) {
    }

    private final ObjectMapper objectMapper;

    public Map<String, ModeleMessageDTO> fusionnerAvecDefauts(String modelesJson) {
        Map<String, ModeleMessageDTO> defauts = NotificationModeleDefaults.tous();
        Map<String, ModeleMessageDTO> result = new HashMap<>();
        for (Map.Entry<String, ModeleMessageDTO> entry : defauts.entrySet()) {
            result.put(entry.getKey(), copier(entry.getValue()));
        }
        if (modelesJson == null || modelesJson.isBlank()) {
            return result;
        }
        try {
            Map<String, ModeleMessageDTO> personnalises = objectMapper.readValue(
                    modelesJson, new TypeReference<Map<String, ModeleMessageDTO>>() {});
            for (Map.Entry<String, ModeleMessageDTO> entry : personnalises.entrySet()) {
                String cle = entry.getKey();
                ModeleMessageDTO custom = entry.getValue();
                if (!result.containsKey(cle) || custom == null) {
                    continue;
                }
                ModeleMessageDTO base = result.get(cle);
                ModeleMessageDTO defaut = defauts.get(cle);
                base.setEmailSujet(choisir(custom.getEmailSujet(), defaut.getEmailSujet()));
                base.setEmailCorps(choisir(custom.getEmailCorps(), defaut.getEmailCorps()));
                base.setSmsCorps(choisir(custom.getSmsCorps(), defaut.getSmsCorps()));
            }
        } catch (Exception e) {
            log.warn("Modèles de notification invalides, utilisation des valeurs par défaut : {}", e.getMessage());
        }
        return result;
    }

    public Map<String, ModeleMessageDTO> lireModeles(ConfigurationNotification config) {
        return fusionnerAvecDefauts(config != null ? config.getModelesMessages() : null);
    }

    public String serialiserDefauts() {
        return serialiser(NotificationModeleDefaults.tous());
    }

    public String serialiser(Map<String, ModeleMessageDTO> modeles) {
        Map<String, ModeleMessageDTO> fusionnes = fusionnerAvecDefauts(null);
        if (modeles != null) {
            for (Map.Entry<String, ModeleMessageDTO> entry : modeles.entrySet()) {
                if (!fusionnes.containsKey(entry.getKey()) || entry.getValue() == null) {
                    continue;
                }
                ModeleMessageDTO base = fusionnes.get(entry.getKey());
                ModeleMessageDTO custom = entry.getValue();
                if (custom.getEmailSujet() != null) base.setEmailSujet(custom.getEmailSujet());
                if (custom.getEmailCorps() != null) base.setEmailCorps(custom.getEmailCorps());
                if (custom.getSmsCorps() != null) base.setSmsCorps(custom.getSmsCorps());
            }
        }
        try {
            return objectMapper.writeValueAsString(fusionnes);
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de sérialiser les modèles de notification", e);
        }
    }

    public MessageRendu rendre(ConfigurationNotification config, String cle, Map<String, String> variables) {
        ModeleMessageDTO modele = lireModeles(config).get(cle);
        ModeleMessageDTO defaut = NotificationModeleDefaults.tous().get(cle);
        if (modele == null || defaut == null) {
            throw new IllegalArgumentException("Modèle inconnu : " + cle);
        }
        String sujet = remplacer(choisir(modele.getEmailSujet(), defaut.getEmailSujet()), variables);
        String email = remplacer(choisir(modele.getEmailCorps(), defaut.getEmailCorps()), variables);
        String sms = remplacer(choisir(modele.getSmsCorps(), defaut.getSmsCorps()), variables);
        if (sms.isBlank()) {
            sms = email;
        }
        if (sujet.isBlank()) {
            sujet = "Notification - {{entreprise}}";
            sujet = remplacer(sujet, variables);
        }
        if (email.isBlank()) {
            email = sms;
        }
        return new MessageRendu(sujet, email, sms);
    }

    private String choisir(String valeur, String defaut) {
        if (valeur == null || valeur.isBlank()) {
            return defaut != null ? defaut : "";
        }
        return valeur;
    }

    private ModeleMessageDTO copier(ModeleMessageDTO source) {
        return ModeleMessageDTO.builder()
                .emailSujet(source.getEmailSujet())
                .emailCorps(source.getEmailCorps())
                .smsCorps(source.getSmsCorps())
                .build();
    }

    private String remplacer(String texte, Map<String, String> variables) {
        if (texte == null) {
            return "";
        }
        String result = texte;
        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String valeur = entry.getValue() != null ? entry.getValue() : "";
                result = result.replace("{{" + entry.getKey() + "}}", valeur);
            }
        }
        return result;
    }
}
