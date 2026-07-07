package gn.odc.gestionrh.notification.service;

import gn.odc.gestionrh.common.enums.TypeNotification;
import gn.odc.gestionrh.employe.entity.Employe;

public interface NotificationService {

    void envoyer(String email, String telephone, TypeNotification type, String sujet, String contenuEmail, String contenuSms);

    default void envoyer(String email, String telephone, TypeNotification type, String sujet, String contenu) {
        envoyer(email, telephone, type, sujet, contenu, contenu);
    }

    void envoyerCodeConfirmation(String email, String telephone, String code);

    void envoyerActivationCompte(String email, String telephone, String code, String lienActivation);

    void envoyerReinitialisationMotDePasse(String email, String telephone, String code, String lienReinitialisation);

    void envoyerValidationConge(Employe employe, String typeConge, String dateDebut, String dateFin,
                                boolean approuve, String commentaireRh);

    void envoyerCreationContrat(Employe employe, String typeContrat, String salaireBase, String dateDebut);

    void envoyerLicenciement(Employe employe);

    void envoyerSuspension(Employe employe);
}
