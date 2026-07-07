package gn.odc.gestionrh.notification.service;

import gn.odc.gestionrh.common.exception.RegleMetierException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class BrevoApiEmailSender {

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.brevo.com")
            .build();

    public void envoyer(String apiKey, String fromEmail, String fromName,
                        String destinataire, String sujet, String contenu) {
        Map<String, Object> body = new LinkedHashMap<>();
        Map<String, String> sender = new LinkedHashMap<>();
        sender.put("email", fromEmail.trim());
        if (fromName != null && !fromName.isBlank()) {
            sender.put("name", fromName.trim());
        }
        body.put("sender", sender);
        body.put("to", List.of(Map.of("email", destinataire.trim())));
        body.put("subject", sujet);
        body.put("textContent", contenu);

        try {
            restClient.post()
                    .uri("/v3/smtp/email")
                    .header("api-key", apiKey.trim())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            log.info("E-mail Brevo API envoyé à {}", destinataire);
        } catch (RestClientResponseException e) {
            log.error("Brevo API {} : {}", e.getStatusCode(), e.getResponseBodyAsString());
            String detail = e.getResponseBodyAsString();
            if (detail != null && detail.length() > 200) {
                detail = detail.substring(0, 200) + "...";
            }
            throw new RegleMetierException("Échec envoi e-mail Brevo : " + detail);
        } catch (Exception e) {
            log.error("Échec envoi e-mail Brevo à {} : {}", destinataire, e.getMessage());
            throw new RegleMetierException("Échec envoi e-mail Brevo : " + e.getMessage());
        }
    }
}
