package gn.odc.gestionrh.notification.service;

import gn.odc.gestionrh.common.enums.TypeNotification;
import gn.odc.gestionrh.configuration.entity.ConfigurationNotification;
import gn.odc.gestionrh.configuration.service.ConfigurationNotificationService;
import gn.odc.gestionrh.configuration.service.EntrepriseContextService;
import gn.odc.gestionrh.configuration.service.NotificationModeleDefaults;
import gn.odc.gestionrh.configuration.service.NotificationModeleService;
import gn.odc.gestionrh.employe.entity.Employe;
import gn.odc.gestionrh.notification.entity.NotificationLog;
import gn.odc.gestionrh.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class ConfigurableNotificationService implements NotificationService {

    private final NotificationLogRepository notificationLogRepository;
    private final EntrepriseContextService entrepriseContextService;
    private final ConfigurationNotificationService configurationNotificationService;
    private final NotificationModeleService notificationModeleService;
    private final ExpediteurEmailService expediteurEmailService;
    private final ExpediteurSmsService expediteurSmsService;

    @Override
    public void envoyer(String email, String telephone, TypeNotification type, String sujet,
                        String contenuEmail, String contenuSms) {
        ConfigurationNotification config = configurationNotificationService.obtenirEntite();
        boolean live = "LIVE".equals(config.getModeEnvoi());

        if (email != null && !email.isBlank()) {
            if (live && config.isEmailActif()) {
                try {
                    expediteurEmailService.envoyer(config, email, sujet, contenuEmail);
                    sauvegarderLog(email, null, "EMAIL", type, sujet, contenuEmail, "ENVOYE");
                } catch (Exception e) {
                    sauvegarderLog(email, null, "EMAIL", type, sujet, contenuEmail, "ECHEC");
                    log.warn("E-mail non envoyé (mode {}): {}", config.getModeEnvoi(), e.getMessage());
                }
            } else {
                logMock("EMAIL", email, sujet, contenuEmail);
                sauvegarderLog(email, null, "EMAIL", type, sujet, contenuEmail, "MOCK");
            }
        }

        String sms = contenuSms != null && !contenuSms.isBlank() ? contenuSms : contenuEmail;
        if (telephone != null && !telephone.isBlank()) {
            if (live && config.isSmsActif()) {
                try {
                    expediteurSmsService.envoyer(config, telephone, sms);
                    sauvegarderLog(null, telephone, "SMS", type, sujet, sms, "ENVOYE");
                } catch (Exception e) {
                    sauvegarderLog(null, telephone, "SMS", type, sujet, sms, "ECHEC");
                    log.warn("SMS non envoyé (mode {}): {}", config.getModeEnvoi(), e.getMessage());
                }
            } else {
                logMock("SMS", telephone, sujet, sms);
                sauvegarderLog(null, telephone, "SMS", type, sujet, sms, "MOCK");
            }
        }
    }

    @Override
    public void envoyerCodeConfirmation(String email, String telephone, String code) {
        ConfigurationNotification config = configurationNotificationService.obtenirEntite();
        var msg = notificationModeleService.rendre(config, NotificationModeleDefaults.CODE_CONFIRMATION,
                varsBase(Map.of("code", code)));
        envoyer(email, telephone, TypeNotification.INSCRIPTION, msg.sujet(), msg.email(), msg.sms());
    }

    @Override
    public void envoyerActivationCompte(String email, String telephone, String code, String lienActivation) {
        ConfigurationNotification config = configurationNotificationService.obtenirEntite();
        var msg = notificationModeleService.rendre(config, NotificationModeleDefaults.ACTIVATION_COMPTE,
                varsBase(Map.of("code", code, "lien", lienActivation)));
        envoyer(email, telephone, TypeNotification.INSCRIPTION, msg.sujet(), msg.email(), msg.sms());
    }

    @Override
    public void envoyerReinitialisationMotDePasse(String email, String telephone, String code, String lienReinitialisation) {
        ConfigurationNotification config = configurationNotificationService.obtenirEntite();
        var msg = notificationModeleService.rendre(config, NotificationModeleDefaults.REINITIALISATION_MDP,
                varsBase(Map.of("code", code, "lien", lienReinitialisation)));
        envoyer(email, telephone, TypeNotification.REINITIALISATION, msg.sujet(), msg.email(), msg.sms());
    }

    @Override
    public void envoyerValidationConge(Employe employe, String typeConge, String dateDebut, String dateFin,
                                       boolean approuve, String commentaireRh) {
        ConfigurationNotification config = configurationNotificationService.obtenirEntite();
        String cle = approuve ? NotificationModeleDefaults.CONGE_APPROUVE : NotificationModeleDefaults.CONGE_REFUSE;
        Map<String, String> vars = varsEmploye(employe);
        vars.put("typeConge", typeConge);
        vars.put("dateDebut", dateDebut);
        vars.put("dateFin", dateFin);
        vars.put("commentaireRh", commentaireRh != null && !commentaireRh.isBlank()
                ? " Commentaire RH : " + commentaireRh : "");
        var msg = notificationModeleService.rendre(config, cle, vars);
        envoyer(employe.getEmail(), employe.getTelephone(), TypeNotification.VALIDATION_CONGE,
                msg.sujet(), msg.email(), msg.sms());
    }

    @Override
    public void envoyerCreationContrat(Employe employe, String typeContrat, String salaireBase, String dateDebut) {
        ConfigurationNotification config = configurationNotificationService.obtenirEntite();
        Map<String, String> vars = varsEmploye(employe);
        vars.put("typeContrat", typeContrat);
        vars.put("salaireBase", salaireBase);
        vars.put("dateDebut", dateDebut);
        var msg = notificationModeleService.rendre(config, NotificationModeleDefaults.CREATION_CONTRAT, vars);
        envoyer(employe.getEmail(), employe.getTelephone(), TypeNotification.CREATION_CONTRAT,
                msg.sujet(), msg.email(), msg.sms());
    }

    @Override
    public void envoyerLicenciement(Employe employe) {
        ConfigurationNotification config = configurationNotificationService.obtenirEntite();
        var msg = notificationModeleService.rendre(config, NotificationModeleDefaults.LICENCIEMENT, varsEmploye(employe));
        envoyer(employe.getEmail(), employe.getTelephone(), TypeNotification.LICENCIEMENT,
                msg.sujet(), msg.email(), msg.sms());
    }

    @Override
    public void envoyerSuspension(Employe employe) {
        ConfigurationNotification config = configurationNotificationService.obtenirEntite();
        var msg = notificationModeleService.rendre(config, NotificationModeleDefaults.SUSPENSION_COMPTE, varsEmploye(employe));
        envoyer(employe.getEmail(), employe.getTelephone(), TypeNotification.SUSPENSION_COMPTE,
                msg.sujet(), msg.email(), msg.sms());
    }

    public void envoyerTestEmail(String email) {
        ConfigurationNotification config = configurationNotificationService.obtenirEntite();
        var msg = notificationModeleService.rendre(config, NotificationModeleDefaults.TEST_EMAIL, varsBase(Map.of()));
        expediteurEmailService.envoyer(config, email, msg.sujet(), msg.email());
        sauvegarderLog(email, null, "EMAIL", TypeNotification.INSCRIPTION, msg.sujet(), msg.email(), "ENVOYE");
    }

    public void envoyerTestSms(String telephone) {
        ConfigurationNotification config = configurationNotificationService.obtenirEntite();
        var msg = notificationModeleService.rendre(config, NotificationModeleDefaults.TEST_SMS, varsBase(Map.of()));
        expediteurSmsService.envoyer(config, telephone, msg.sms());
        sauvegarderLog(null, telephone, "SMS", TypeNotification.INSCRIPTION, "Test SMS", msg.sms(), "ENVOYE");
    }

    private Map<String, String> varsBase(Map<String, String> extra) {
        Map<String, String> vars = new HashMap<>(extra);
        vars.put("entreprise", entrepriseContextService.nomEntreprise());
        return vars;
    }

    private Map<String, String> varsEmploye(Employe employe) {
        Map<String, String> vars = varsBase(Map.of());
        vars.put("prenom", employe.getPrenom());
        vars.put("nom", employe.getNom());
        return vars;
    }

    private void logMock(String canal, String destinataire, String sujet, String contenu) {
        log.info("=== NOTIFICATION MOCK [{}] ===", canal);
        log.info("Destinataire: {}", destinataire);
        log.info("Sujet: {}", sujet);
        log.info("Contenu: {}", contenu);
    }

    private void sauvegarderLog(String email, String tel, String canal, TypeNotification type,
                                String sujet, String contenu, String statut) {
        notificationLogRepository.save(NotificationLog.builder()
                .destinataireEmail(email)
                .destinataireTelephone(tel)
                .canal(canal)
                .typeNotification(type)
                .sujet(sujet)
                .contenu(contenu)
                .statutEnvoi(statut)
                .build());
    }
}
