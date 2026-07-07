package gn.odc.gestionrh.configuration.service;

import gn.odc.gestionrh.configuration.dto.ModeleMessageDTO;

import java.util.LinkedHashMap;
import java.util.Map;

public final class NotificationModeleDefaults {

    public static final String ACTIVATION_COMPTE = "ACTIVATION_COMPTE";
    public static final String REINITIALISATION_MDP = "REINITIALISATION_MDP";
    public static final String CODE_CONFIRMATION = "CODE_CONFIRMATION";
    public static final String CONGE_APPROUVE = "CONGE_APPROUVE";
    public static final String CONGE_REFUSE = "CONGE_REFUSE";
    public static final String CREATION_CONTRAT = "CREATION_CONTRAT";
    public static final String LICENCIEMENT = "LICENCIEMENT";
    public static final String SUSPENSION_COMPTE = "SUSPENSION_COMPTE";
    public static final String TEST_EMAIL = "TEST_EMAIL";
    public static final String TEST_SMS = "TEST_SMS";

    private NotificationModeleDefaults() {
    }

    public static Map<String, ModeleMessageDTO> tous() {
        Map<String, ModeleMessageDTO> modeles = new LinkedHashMap<>();
        modeles.put(ACTIVATION_COMPTE, ModeleMessageDTO.builder()
                .emailSujet("Activation de votre compte - {{entreprise}}")
                .emailCorps("Bienvenue chez {{entreprise}} ! Votre code d'activation : {{code}} (valide 48h). "
                        + "Activez votre compte ici : {{lien}} — Définissez ensuite votre mot de passe.")
                .smsCorps("{{entreprise}} : activez votre compte. Code {{code}}. {{lien}}")
                .build());
        modeles.put(REINITIALISATION_MDP, ModeleMessageDTO.builder()
                .emailSujet("Réinitialisation de mot de passe - {{entreprise}}")
                .emailCorps("Vous avez demandé la réinitialisation de votre mot de passe. "
                        + "Code : {{code}} (valide 30 min). Réinitialisez ici : {{lien}}")
                .smsCorps("{{entreprise}} : réinitialisation MDP. Code {{code}}. {{lien}}")
                .build());
        modeles.put(CODE_CONFIRMATION, ModeleMessageDTO.builder()
                .emailSujet("Code de confirmation - {{entreprise}}")
                .emailCorps("Votre code de confirmation est : {{code}}. Valide 15 minutes.")
                .smsCorps("Code {{code}} - {{entreprise}}")
                .build());
        modeles.put(CONGE_APPROUVE, ModeleMessageDTO.builder()
                .emailSujet("Congé approuvé — {{entreprise}}")
                .emailCorps("Bonjour {{prenom}} {{nom}}, votre demande de congé ({{typeConge}}, "
                        + "du {{dateDebut}} au {{dateFin}}) a été approuvée.{{commentaireRh}}")
                .smsCorps("Congé approuvé ({{dateDebut}}-{{dateFin}}). {{entreprise}}")
                .build());
        modeles.put(CONGE_REFUSE, ModeleMessageDTO.builder()
                .emailSujet("Congé refusé — {{entreprise}}")
                .emailCorps("Bonjour {{prenom}} {{nom}}, votre demande de congé ({{typeConge}}, "
                        + "du {{dateDebut}} au {{dateFin}}) a été refusée.{{commentaireRh}}")
                .smsCorps("Congé refusé. Contactez le RH — {{entreprise}}")
                .build());
        modeles.put(CREATION_CONTRAT, ModeleMessageDTO.builder()
                .emailSujet("Nouveau contrat — {{entreprise}}")
                .emailCorps("Bonjour {{prenom}} {{nom}}, un contrat {{typeContrat}} a été créé "
                        + "avec un salaire de base de {{salaireBase}} GNF. Date de début : {{dateDebut}}.")
                .smsCorps("Nouveau contrat {{typeContrat}} chez {{entreprise}}. Début : {{dateDebut}}.")
                .build());
        modeles.put(LICENCIEMENT, ModeleMessageDTO.builder()
                .emailSujet("Notification de licenciement — {{entreprise}}")
                .emailCorps("Bonjour {{prenom}} {{nom}}, nous vous informons de la fin de votre collaboration avec {{entreprise}}.")
                .smsCorps("Fin de collaboration avec {{entreprise}}. Contactez le RH.")
                .build());
        modeles.put(SUSPENSION_COMPTE, ModeleMessageDTO.builder()
                .emailSujet("Suspension de compte — {{entreprise}}")
                .emailCorps("Bonjour {{prenom}} {{nom}}, votre compte a été suspendu. "
                        + "Contactez le service RH pour plus d'informations.")
                .smsCorps("Compte suspendu chez {{entreprise}}. Contactez le RH.")
                .build());
        modeles.put(TEST_EMAIL, ModeleMessageDTO.builder()
                .emailSujet("Test e-mail - {{entreprise}}")
                .emailCorps("Ceci est un e-mail de test envoyé depuis la configuration {{entreprise}}. "
                        + "Si vous le recevez, le SMTP est correctement paramétré.")
                .smsCorps("")
                .build());
        modeles.put(TEST_SMS, ModeleMessageDTO.builder()
                .emailSujet("")
                .emailCorps("")
                .smsCorps("Test SMS {{entreprise}} : votre configuration SMS fonctionne correctement.")
                .build());
        return modeles;
    }
}
