package gn.odc.gestionrh.auth.service;

import gn.odc.gestionrh.auth.entity.CodeConfirmation;
import gn.odc.gestionrh.auth.entity.Utilisateur;
import gn.odc.gestionrh.auth.repository.CodeConfirmationRepository;
import gn.odc.gestionrh.auth.repository.UtilisateurRepository;
import gn.odc.gestionrh.authorization.entity.Role;
import gn.odc.gestionrh.authorization.repository.RoleRepository;
import gn.odc.gestionrh.authorization.repository.RoleRepository;
import gn.odc.gestionrh.common.enums.StatutEntite;
import gn.odc.gestionrh.common.enums.TypeCodeConfirmation;
import gn.odc.gestionrh.common.exception.RegleMetierException;
import gn.odc.gestionrh.employe.entity.Employe;
import gn.odc.gestionrh.employe.repository.EmployeRepository;
import gn.odc.gestionrh.configuration.service.ConfigurationNotificationService;
import gn.odc.gestionrh.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompteProvisionService {

    private static final String ROLE_DEFAUT = "EMPLOYE";

    private final UtilisateurRepository utilisateurRepository;
    private final EmployeRepository employeRepository;
    private final RoleRepository roleRepository;
    private final CodeConfirmationRepository codeConfirmationRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final ConfigurationNotificationService configurationNotificationService;

    @Transactional
    public Map<String, String> provisionnerCompte(Employe employe, String roleCode) {
        assurerCompteEmploye(employe, roleCode);
        return envoyerActivation(normaliserEmail(employe.getEmail()), employe.getTelephone());
    }

    /** Crée le compte utilisateur sans envoyer d'e-mail (réparation au démarrage). */
    @Transactional
    public void assurerCompteEmploye(Employe employe, String roleCode) {
        String email = normaliserEmail(employe.getEmail());
        if (utilisateurRepository.existsByEmail(email)) {
            return;
        }
        String codeRole = resoudreRoleCode(roleCode);
        Role role = roleRepository.findByCode(codeRole)
                .orElseThrow(() -> new RegleMetierException("Rôle introuvable : " + codeRole));
        if ("ADMINISTRATEUR".equals(codeRole)) {
            throw new RegleMetierException("Le rôle Administrateur ne peut pas être attribué via la création d'employé");
        }
        String motDePasseTemporaire = UUID.randomUUID().toString();
        utilisateurRepository.save(Utilisateur.builder()
                .email(email)
                .motDePasse(passwordEncoder.encode(motDePasseTemporaire))
                .employe(employe)
                .actif(false)
                .confirme(false)
                .roles(Set.of(role))
                .build());
    }

    @Transactional
    public Map<String, String> renvoyerActivation(String email) {
        return renvoyerActivation(email, ROLE_DEFAUT);
    }

    @Transactional
    public Map<String, String> renvoyerActivation(String email, String roleCode) {
        String normalise = normaliserEmail(email);
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByEmail(normalise);

        if (utilisateurOpt.isPresent()) {
            Utilisateur utilisateur = utilisateurOpt.get();
            if (utilisateur.isConfirme()) {
                throw new RegleMetierException("Ce compte est déjà activé");
            }
            String telephone = utilisateur.getEmploye() != null ? utilisateur.getEmploye().getTelephone() : null;
            return envoyerActivation(normalise, telephone);
        }

        Employe employe = employeRepository.findByEmail(normalise)
                .filter(e -> e.getStatut() == StatutEntite.ACTIF)
                .orElseThrow(() -> new RegleMetierException(
                        "Aucun compte associé à cet e-mail. Demandez au service RH de créer votre fiche employé."));

        assurerCompteEmploye(employe, roleCode != null ? roleCode : resoudreRoleCode(null));
        return envoyerActivation(normalise, employe.getTelephone());
    }

    @Transactional
    public void desactiverCompte(Employe employe) {
        utilisateurRepository.findByEmail(employe.getEmail()).ifPresent(u -> {
            u.setActif(false);
            utilisateurRepository.save(u);
        });
    }

    private String resoudreRoleCode(String roleCode) {
        if (roleCode != null && !roleCode.isBlank()) {
            return roleCode.trim().toUpperCase();
        }
        return roleRepository.findByParDefautTrueAndStatut(StatutEntite.ACTIF)
                .map(Role::getCode)
                .orElse(ROLE_DEFAUT);
    }

    private String normaliserEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private Map<String, String> envoyerActivation(String email, String telephone) {
        String code = String.format("%06d", new Random().nextInt(999999));
        String token = UUID.randomUUID().toString().replace("-", "");

        CodeConfirmation confirmation = CodeConfirmation.builder()
                .email(email)
                .telephone(telephone)
                .code(code)
                .token(token)
                .typeCode(TypeCodeConfirmation.ACTIVATION_COMPTE)
                .expireLe(LocalDateTime.now().plusHours(48))
                .build();
        codeConfirmationRepository.save(confirmation);

        String lien = configurationNotificationService.obtenirAppUrl() + "/activer?token=" + token + "&email=" + email;
        notificationService.envoyerActivationCompte(email, telephone, code, lien);

        return Map.of(
                "message", "Compte créé. Un code d'activation et un lien ont été envoyés par e-mail et SMS.",
                "email", email
        );
    }
}
