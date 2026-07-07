package gn.odc.gestionrh.config;

import gn.odc.gestionrh.configuration.repository.ConfigurationNotificationRepository;
import gn.odc.gestionrh.configuration.service.NotificationModeleService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Corrige les données texte corrompues par import ou charset incorrect :
 * mojibake UTF-8 (ex. Cong├⌐ → Congé) et perte d'accents (ex. Syst??me → Système).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReparationEncodageService {

    private static final String[][] REMPLACEMENTS_MOJIBAKE = {
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

    /** Perte d'accents : caractères UTF-8 remplacés par « ?? » (0x3F3F) à l'import. */
    private static final String[][] REMPLACEMENTS_QUESTION = {
            {"Syst??me", "Système"},
            {"R??ception", "Réception"},
            {"Employ??", "Employé"},
            {"Si??ge", "Siège"},
            {"D??veloppeur", "Développeur"},
            {"Op??rations", "Opérations"},
            {"Entrep??t", "Entrepôt"},
            {"Comptabilit??", "Comptabilité"},
            {"d??clarations", "déclarations"},
            {"r??initialisation", "réinitialisation"},
            {"R??initialisation", "Réinitialisation"},
            {"approuv??", "approuvé"},
            {"refus??", "refusé"},
            {"cr??ation", "création"},
            {"Cr??ation", "Création"},
            {"licenci??", "licencié"},
            {"D??finissez", "Définissez"},
            {"r??ponse", "réponse"},
    };

    private static final String[][] ROLES_DEFAUT = {
            {"ADMINISTRATEUR", "Administrateur Système", "Accès total à toutes les fonctionnalités"},
            {"RH", "Ressources Humaines", "Gestion RH complète"},
            {"EMPLOYE", "Employé", "Accès self-service"},
            {"RECEPTION", "Réception", "Pointage, visites et self-service employé"},
    };

    private static final String[][] DEPARTEMENTS_DEFAUT = {
            {"RH", "Ressources Humaines", "Gestion du personnel et paie"},
            {"IT", "Technologies de l'Information", "Développement et infrastructure"},
            {"FIN", "Finance", "Comptabilité et déclarations sociales"},
            {"OPS", "Opérations", "Logistique et exploitation"},
            {"COM", "Commercial", "Ventes et relation client"},
    };

    private static final String[][] POSTES_DEFAUT = {
            {"DIR-RH", "Directeur RH", "Pilotage des ressources humaines"},
            {"ASSIST-RH", "Assistant RH", "Administration RH et recrutement"},
            {"DEV-SR", "Développeur Senior", "Conception et développement applicatif"},
            {"DEV-JR", "Développeur Junior", "Développement et maintenance"},
            {"COMPT", "Comptable", "Paie, CNSS et déclarations fiscales"},
            {"GEST-OPS", "Gestionnaire Opérations", "Coordination opérationnelle"},
            {"COMM", "Commercial", "Prospection et suivi clients"},
    };

    private static final String[][] LOCALISATIONS_DEFAUT = {
            {"SIEGE", "Siège Social", "Immeuble Sanana, Kaloum, Conakry", "Conakry"},
            {"ANNEX", "Annexe Dixinn", "Quartier Dixinn, Conakry", "Conakry"},
            {"ENTREP", "Entrepôt Ratoma", "Zone industrielle, Ratoma", "Conakry"},
            {"KIPE", "Bureau Kipé", "Kipé, Commune de Ratoma", "Conakry"},
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

        int referentiels = reparerReferentielsParCode();
        reparerEmployeAdmin();
        reparerModelesNotifications();

        if (colonnes > 0 || referentiels > 0) {
            log.info("Réparation encodage UTF-8 : {} colonne(s) texte, {} référentiel(s) par code",
                    colonnes, referentiels);
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
        for (String[] pair : tousLesRemplacements()) {
            expr = "REPLACE(" + expr + ", '" + echapperSql(pair[0]) + "', '" + echapperSql(pair[1]) + "')";
        }
        return "UPDATE `" + table + "` SET `" + colonne + "` = " + expr
                + " WHERE " + clauseCorruption(colonne);
    }

    private String[][] tousLesRemplacements() {
        String[][] all = new String[REMPLACEMENTS_MOJIBAKE.length + REMPLACEMENTS_QUESTION.length][];
        System.arraycopy(REMPLACEMENTS_MOJIBAKE, 0, all, 0, REMPLACEMENTS_MOJIBAKE.length);
        System.arraycopy(REMPLACEMENTS_QUESTION, 0, all, REMPLACEMENTS_MOJIBAKE.length, REMPLACEMENTS_QUESTION.length);
        return all;
    }

    private String clauseCorruption(String colonne) {
        return "`" + colonne + "` LIKE '%├%' OR `" + colonne + "` LIKE '%Ã%' OR "
                + "`" + colonne + "` LIKE '%ΓÇ%' OR `" + colonne + "` LIKE '%â€%' OR "
                + "`" + colonne + "` LIKE '%??%'";
    }

    private int reparerReferentielsParCode() {
        int updated = 0;
        for (String[] role : ROLES_DEFAUT) {
            updated += entityManager.createNativeQuery(
                    "UPDATE roles SET libelle = ?, description = ? WHERE code = ? AND ("
                            + clauseCorruption("libelle") + " OR " + clauseCorruption("description") + ")")
                    .setParameter(1, role[1])
                    .setParameter(2, role[2])
                    .setParameter(3, role[0])
                    .executeUpdate();
        }
        for (String[] dept : DEPARTEMENTS_DEFAUT) {
            updated += entityManager.createNativeQuery(
                    "UPDATE departements SET libelle = ?, description = ? WHERE code = ? AND ("
                            + clauseCorruption("libelle") + " OR " + clauseCorruption("description") + ")")
                    .setParameter(1, dept[1])
                    .setParameter(2, dept[2])
                    .setParameter(3, dept[0])
                    .executeUpdate();
        }
        for (String[] poste : POSTES_DEFAUT) {
            updated += entityManager.createNativeQuery(
                    "UPDATE postes SET libelle = ?, description = ? WHERE code = ? AND ("
                            + clauseCorruption("libelle") + " OR " + clauseCorruption("description") + ")")
                    .setParameter(1, poste[1])
                    .setParameter(2, poste[2])
                    .setParameter(3, poste[0])
                    .executeUpdate();
        }
        for (String[] loc : LOCALISATIONS_DEFAUT) {
            updated += entityManager.createNativeQuery(
                    "UPDATE localisations SET nom = ?, adresse = ?, ville = ? WHERE code = ? AND ("
                            + clauseCorruption("nom") + " OR " + clauseCorruption("adresse") + ")")
                    .setParameter(1, loc[1])
                    .setParameter(2, loc[2])
                    .setParameter(3, loc[3])
                    .setParameter(4, loc[0])
                    .executeUpdate();
        }
        if (updated > 0) {
            log.info("Référentiels système réparés par code : {} ligne(s)", updated);
        }
        return updated;
    }

    private void reparerEmployeAdmin() {
        int updated = entityManager.createNativeQuery(
                "UPDATE employes SET nom = 'Système' WHERE (nom = 'SYSTEME' OR nom LIKE '%??%') "
                        + "AND (matricule = 'ADM-001' OR matricule LIKE 'SNG-%' OR email = 'admin@minerva.group')")
                .executeUpdate();
        if (updated > 0) {
            log.info("Nom employé admin corrigé : {} ligne(s)", updated);
        }
    }

    private void reparerModelesNotifications() {
        configurationNotificationRepository.findAll().forEach(config -> {
            String modeles = config.getModelesMessages();
            if (modeles == null || !contientCorruption(modeles)) {
                return;
            }
            String corriges = appliquerRemplacements(modeles);
            if (contientCorruption(corriges)) {
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
        for (String[] pair : tousLesRemplacements()) {
            result = result.replace(pair[0], pair[1]);
        }
        return result;
    }

    private boolean contientCorruption(String texte) {
        return texte.contains("├") || texte.contains("Ã") || texte.contains("ΓÇ")
                || texte.contains("â€") || texte.contains("??");
    }

    private String echapperSql(String value) {
        return value.replace("\\", "\\\\").replace("'", "''");
    }
}
