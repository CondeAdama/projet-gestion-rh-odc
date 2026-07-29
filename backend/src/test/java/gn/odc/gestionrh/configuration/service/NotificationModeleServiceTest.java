package gn.odc.gestionrh.configuration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gn.odc.gestionrh.configuration.dto.ModeleMessageDTO;
import gn.odc.gestionrh.configuration.entity.ConfigurationNotification;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static gn.odc.gestionrh.configuration.service.NotificationModeleDefaults.ACTIVATION_COMPTE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationModeleServiceTest {

    private final NotificationModeleService service = new NotificationModeleService(new ObjectMapper());

    @Test
    void serialiseEtRelitDefinissez() {
        ModeleMessageDTO custom = ModeleMessageDTO.builder()
                .emailCorps("Définissez ensuite votre mot de passe.")
                .build();
        String json = service.serialiser(Map.of(ACTIVATION_COMPTE, custom));

        assertTrue(json.contains("Définissez"), () -> "JSON: " + json);

        ConfigurationNotification config = ConfigurationNotification.builder()
                .modelesMessages(json)
                .build();
        String corps = service.lireModeles(config).get(ACTIVATION_COMPTE).getEmailCorps();
        assertEquals("Définissez ensuite votre mot de passe.", corps);
    }

    @Test
    void ignoreChampsCorrompusEnBase() {
        String json = """
                {"ACTIVATION_COMPTE":{"emailCorps":"Activez ici : {{lien}} ??? D??finissez ensuite votre mot de passe."}}
                """;
        ConfigurationNotification config = ConfigurationNotification.builder()
                .modelesMessages(json)
                .build();
        String corps = service.lireModeles(config).get(ACTIVATION_COMPTE).getEmailCorps();
        assertFalse(corps.contains("??"));
        assertTrue(corps.contains("Définissez"));
    }
}
