package gn.odc.gestionrh.config;

import gn.odc.gestionrh.configuration.repository.ConfigurationNotificationRepository;
import gn.odc.gestionrh.configuration.service.NotificationModeleService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Corrige le mojibake UTF-8 des données importées (ex. Cong├⌐ → Congé, Si├¿ge → Siège).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReparationEncodageService {

    private static final String[][] REMPLACEMENTS = {
            {"├⌐", "é"},
            {"├¿", "è"},
            {"├á", "à"},
            {"├â", "â"},
            {"├ê", "È"},
            {"├ë", "É"},
            {"├®", "é"},
            {"├º", "ç"},
            {"├»", "ï"},
            {"ΓÇö", "—"},
            {"ΓÇÖ", "'"},
            {"ΓÇ£", "\""},
            {"ΓÇ¥", "\""},
            {"Ã©", "é"},
            {"Ã¨", "è"},
            {"Ã ", "à"},
            {"Ã¢", "â"},
            {"Ã§", "ç"},
            {"Ã®", "î"},
            {"Ã´", "ô"},
            {"Ã»", "û"},
            {"Ã«", "ë"},
            {"Ã‰", "É"},
            {"Ãˆ", "È"},
            {"ÃŠ", "Ê"},
            {"â€™", "'"},
            {"â€\"", "—"},
            {"â€œ", "\""},
            {"â€\u009d", "\""},
    };

    private final EntityManager entityManager;
    private final ConfigurationNotificationRepository configurationNotificationRepository;
    private final NotificationModeleService notificationModeleService;

    @Transactional
    public void reparer() {
        int colonnes = 0;
        colonnes += reparerColonnes("roles", "libelle", "description");
        colonnes += reparerColonnes("departements", "libelle", "description");
        colonnes += reparerColonnes("postes", "libelle", "description");
        colonnes += reparerColonnes("localisations", "nom", "adresse", "ville");
        colonnes += reparerColonnes("employes", "nom", "prenom");
        colonnes += reparerColonnes("configuration_entreprise", "adresse", "slogan");
        colonnes += reparerColonnes("conges", "commentaire_rh");
        colonnes += reparerColonnes("notifications_log", "sujet", "contenu");
        colonnes += reparerColonnes("visiteurs", "societe");

        reparerModelesNotifications();

        if (colonnes > 0) {
            log.info("Réparation encodage UTF-8 : {} colonne(s) texte traitée(s)", colonnes);
        } else {
            log.info("Réparation encodage UTF-8 : aucune donnée corrompue détectée");
        }
    }

    private int reparerColonnes(String table, String... colonnes) {
        int count = 0;
        for (String colonne : colonnes) {
            String sql = buildReplaceSql(table, colonne);
            if (sql == null) continue;
            int updated = entityManager.createNativeQuery(sql).executeUpdate();
            if (updated > 0) {
                log.info("Encodage corrigé : {}.{} ({} ligne(s))", table, colonne, updated);
            }
            count++;
        }
        return count;
    }

    private String buildReplaceSql(String table, String colonne) {
        String expr = "`" + colonne + "`";
        for (String[] pair : REMPLACEMENTS) {
            expr = "REPLACE(" + expr + ", '" + echapperSql(pair[0]) + "', '" + echapperSql(pair[1]) + "')";
        }
        return "UPDATE `" + table + "` SET `" + colonne + "` = " + expr
                + " WHERE `" + colonne + "` LIKE '%├%' OR `" + colonne + "` LIKE '%Ã%' OR "
                + "`" + colonne + "` LIKE '%ΓÇ%' OR `" + colonne + "` LIKE '%â€%'";
    }

    private void reparerModelesNotifications() {
        configurationNotificationRepository.findAll().forEach(config -> {
            String modeles = config.getModelesMessages();
            if (modeles == null || !contientMojibake(modeles)) {
                return;
            }
            String corriges = appliquerRemplacements(modeles);
            if (contientMojibake(corriges)) {
                config.setModelesMessages(notificationModeleService.serialiserDefauts());
                log.info("Modèles de notification réinitialisés (encodage corrompu)");
            } else {
                config.setModelesMessages(corriges);
                log.info("Modèles de notification corrigés (encodage)");
            }
            configurationNotificationRepository.save(config);
        });
    }

    private String appliquerRemplacements(String texte) {
        String result = texte;
        for (String[] pair : REMPLACEMENTS) {
            result = result.replace(pair[0], pair[1]);
        }
        return result;
    }

    private boolean contientMojibake(String texte) {
        return texte.contains("├") || texte.contains("Ã") || texte.contains("ΓÇ") || texte.contains("â€");
    }

    private String echapperSql(String value) {
        return value.replace("\\", "\\\\").replace("'", "''");
    }
}
