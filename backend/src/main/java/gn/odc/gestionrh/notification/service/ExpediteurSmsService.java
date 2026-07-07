package gn.odc.gestionrh.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gn.odc.gestionrh.common.enums.SmsProvider;
import gn.odc.gestionrh.common.exception.RegleMetierException;
import gn.odc.gestionrh.configuration.entity.ConfigurationNotification;
import gn.odc.gestionrh.configuration.service.SmsConfigurationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExpediteurSmsService {

    private final SmsConfigurationHelper smsHelper;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void envoyer(ConfigurationNotification config, String telephone, String message) {
        if (telephone == null || telephone.isBlank()) {
            throw new RegleMetierException("Destinataire SMS manquant");
        }
        String numero = telephone.replaceAll("\\s+", "");
        SmsProvider provider = smsHelper.parseProvider(config.getSmsProvider());
        Map<String, String> extra = smsHelper.lireExtra(config.getSmsExtraConfig());

        switch (provider) {
            case TWILIO -> envoyerTwilio(config, numero, message);
            case BREVO -> envoyerBrevo(config, numero, message);
            case AFRICAS_TALKING -> envoyerAfricasTalking(config, numero, message);
            case TERMII -> envoyerTermii(config, numero, message, extra);
            case VONAGE -> envoyerVonage(config, numero, message);
            case MESSAGEBIRD -> envoyerMessageBird(config, numero, message);
            case AWS_SNS -> envoyerAwsSns(config, numero, message, extra);
            case CLICKSEND -> envoyerClickSend(config, numero, message);
            case PLIVO -> envoyerPlivo(config, numero, message);
            case SINCH -> envoyerSinch(config, numero, message);
            case INFOBIP -> envoyerInfobip(config, numero, message);
            case TEXTLOCAL -> envoyerTextlocal(config, numero, message);
            case TELESIGN -> envoyerTelesign(config, numero, message);
            case HTTP -> envoyerHttp(config, numero, message);
        }
    }

    private void envoyerTwilio(ConfigurationNotification config, String telephone, String message) {
        String accountSid = config.getSmsAccountSid();
        String url = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";
        HttpHeaders headers = basicAuthHeaders(accountSid, config.getSmsApiSecret());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("To", telephone);
        body.add("From", config.getSmsSenderId());
        body.add("Body", message);
        postForm(url, body, headers, "Twilio", telephone);
    }

    private void envoyerBrevo(ConfigurationNotification config, String telephone, String message) {
        String url = "https://api.brevo.com/v3/transactionalSMS/sms";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", config.getSmsApiSecret());
        Map<String, Object> payload = Map.of(
                "sender", config.getSmsSenderId(),
                "recipient", telephone,
                "content", message,
                "type", "transactional"
        );
        postJson(url, payload, headers, "Brevo", telephone);
    }

    private void envoyerAfricasTalking(ConfigurationNotification config, String telephone, String message) {
        String url = "https://api.africastalking.com/version1/messaging";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("apiKey", config.getSmsApiSecret());
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("username", config.getSmsAccountSid());
        body.add("to", telephone);
        body.add("message", message);
        body.add("from", config.getSmsSenderId());
        postForm(url, body, headers, "Africa's Talking", telephone);
    }

    private void envoyerTermii(ConfigurationNotification config, String telephone, String message, Map<String, String> extra) {
        String url = "https://api.ng.termii.com/api/sms/send";
        String channel = smsHelper.extra(extra, "channel");
        if (channel.isBlank()) {
            channel = "generic";
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("to", telephone);
        payload.put("from", config.getSmsSenderId());
        payload.put("sms", message);
        payload.put("type", "plain");
        payload.put("api_key", config.getSmsApiSecret());
        payload.put("channel", channel);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        postJson(url, payload, headers, "Termii", telephone);
    }

    private void envoyerVonage(ConfigurationNotification config, String telephone, String message) {
        String url = "https://rest.nexmo.com/sms/json";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("api_key", config.getSmsAccountSid());
        body.add("api_secret", config.getSmsApiSecret());
        body.add("to", telephone.replace("+", ""));
        body.add("from", config.getSmsSenderId());
        body.add("text", message);
        postForm(url, body, headers, "Vonage", telephone);
    }

    private void envoyerMessageBird(ConfigurationNotification config, String telephone, String message) {
        String url = "https://rest.messagebird.com/messages";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "AccessKey " + config.getSmsApiSecret());
        Map<String, Object> payload = Map.of(
                "recipients", List.of(telephone),
                "originator", config.getSmsSenderId(),
                "body", message
        );
        postJson(url, payload, headers, "MessageBird", telephone);
    }

    private void envoyerAwsSns(ConfigurationNotification config, String telephone, String message, Map<String, String> extra) {
        String region = smsHelper.extra(extra, "region");
        if (region.isBlank()) {
            region = "eu-west-1";
        }
        try (SnsClient sns = SnsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(config.getSmsAccountSid(), config.getSmsApiSecret())))
                .build()) {
            PublishRequest.Builder req = PublishRequest.builder()
                    .phoneNumber(telephone)
                    .message(message);
            if (config.getSmsSenderId() != null && !config.getSmsSenderId().isBlank()) {
                req.messageAttributes(Map.of(
                        "AWS.SNS.SMS.SenderID",
                        MessageAttributeValue.builder().dataType("String").stringValue(config.getSmsSenderId()).build()
                ));
            }
            sns.publish(req.build());
            log.info("SMS AWS SNS envoyé à {}", telephone);
        } catch (RegleMetierException e) {
            throw e;
        } catch (Exception e) {
            log.error("Échec envoi SMS AWS SNS à {} : {}", telephone, e.getMessage());
            throw new RegleMetierException("Échec envoi SMS : " + e.getMessage());
        }
    }

    private void envoyerClickSend(ConfigurationNotification config, String telephone, String message) {
        String url = "https://rest.clicksend.com/v3/sms/send";
        HttpHeaders headers = basicAuthHeaders(config.getSmsAccountSid(), config.getSmsApiSecret());
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> payload = Map.of(
                "messages", List.of(Map.of(
                        "to", telephone,
                        "body", message,
                        "from", config.getSmsSenderId()
                ))
        );
        postJson(url, payload, headers, "ClickSend", telephone);
    }

    private void envoyerPlivo(ConfigurationNotification config, String telephone, String message) {
        String authId = config.getSmsAccountSid();
        String url = "https://api.plivo.com/v1/Account/" + authId + "/Message/";
        HttpHeaders headers = basicAuthHeaders(authId, config.getSmsApiSecret());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("src", config.getSmsSenderId());
        body.add("dst", telephone);
        body.add("text", message);
        postForm(url, body, headers, "Plivo", telephone);
    }

    private void envoyerSinch(ConfigurationNotification config, String telephone, String message) {
        String planId = config.getSmsAccountSid();
        String url = "https://sms.api.sinch.com/xms/v1/" + planId + "/batches";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getSmsApiSecret());
        Map<String, Object> payload = Map.of(
                "from", config.getSmsSenderId(),
                "to", List.of(telephone),
                "body", message
        );
        postJson(url, payload, headers, "Sinch", telephone);
    }

    private void envoyerInfobip(ConfigurationNotification config, String telephone, String message) {
        String base = config.getSmsAccountSid().replaceAll("/$", "");
        String url = base + "/sms/2/text/advanced";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "App " + config.getSmsApiSecret());
        Map<String, Object> payload = Map.of(
                "messages", List.of(Map.of(
                        "from", config.getSmsSenderId(),
                        "destinations", List.of(Map.of("to", telephone)),
                        "text", message
                ))
        );
        postJson(url, payload, headers, "Infobip", telephone);
    }

    private void envoyerTextlocal(ConfigurationNotification config, String telephone, String message) {
        String url = "https://api.textlocal.in/send/";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("apikey", config.getSmsApiSecret());
        body.add("numbers", telephone.replace("+", ""));
        body.add("sender", config.getSmsSenderId());
        body.add("message", message);
        postForm(url, body, headers, "Textlocal", telephone);
    }

    private void envoyerTelesign(ConfigurationNotification config, String telephone, String message) {
        String url = "https://rest-ww.telesign.com/v1/messaging";
        HttpHeaders headers = basicAuthHeaders(config.getSmsAccountSid(), config.getSmsApiSecret());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String messageType = (config.getSmsSenderId() != null && !config.getSmsSenderId().isBlank())
                ? config.getSmsSenderId() : "ARN";
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("phone_number", telephone);
        body.add("message", message);
        body.add("message_type", messageType);
        postForm(url, body, headers, "Telesign", telephone);
    }

    private void envoyerHttp(ConfigurationNotification config, String telephone, String message) {
        String url = config.getSmsAccountSid();
        if (url == null || !url.startsWith("http")) {
            throw new RegleMetierException("URL API HTTP invalide");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (config.getSmsApiSecret() != null && !config.getSmsApiSecret().isBlank()) {
            headers.setBearerAuth(config.getSmsApiSecret());
        }
        String payload = String.format(
                "{\"to\":\"%s\",\"from\":\"%s\",\"message\":\"%s\"}",
                escapeJson(telephone), escapeJson(config.getSmsSenderId()), escapeJson(message));
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(payload, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RegleMetierException("API SMS a répondu : " + response.getStatusCode());
            }
            log.info("SMS HTTP envoyé à {}", telephone);
        } catch (RegleMetierException e) {
            throw e;
        } catch (Exception e) {
            log.error("Échec envoi SMS HTTP à {} : {}", telephone, e.getMessage());
            throw new RegleMetierException("Échec envoi SMS : " + e.getMessage());
        }
    }

    private void postForm(String url, MultiValueMap<String, String> body, HttpHeaders headers, String label, String telephone) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RegleMetierException(label + " a répondu : " + response.getStatusCode());
            }
            log.info("SMS {} envoyé à {}", label, telephone);
        } catch (RegleMetierException e) {
            throw e;
        } catch (Exception e) {
            log.error("Échec envoi SMS {} à {} : {}", label, telephone, e.getMessage());
            throw new RegleMetierException("Échec envoi SMS : " + e.getMessage());
        }
    }

    private void postJson(String url, Object payload, HttpHeaders headers, String label, String telephone) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(json, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RegleMetierException(label + " a répondu : " + response.getStatusCode());
            }
            log.info("SMS {} envoyé à {}", label, telephone);
        } catch (RegleMetierException e) {
            throw e;
        } catch (Exception e) {
            log.error("Échec envoi SMS {} à {} : {}", label, telephone, e.getMessage());
            throw new RegleMetierException("Échec envoi SMS : " + e.getMessage());
        }
    }

    private HttpHeaders basicAuthHeaders(String user, String pass) {
        HttpHeaders headers = new HttpHeaders();
        String auth = user + ":" + pass;
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8)));
        return headers;
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
