package gn.odc.gestionrh.auth.service;

import gn.odc.gestionrh.auth.dto.*;
import gn.odc.gestionrh.auth.entity.CodeConfirmation;
import gn.odc.gestionrh.auth.entity.Utilisateur;
import gn.odc.gestionrh.auth.repository.CodeConfirmationRepository;
import gn.odc.gestionrh.auth.repository.UtilisateurRepository;
import gn.odc.gestionrh.auth.security.JwtService;
import gn.odc.gestionrh.auth.security.UtilisateurPrincipal;
import gn.odc.gestionrh.authorization.entity.Permission;
import gn.odc.gestionrh.authorization.entity.Role;
import gn.odc.gestionrh.common.enums.ModuleApplication;
import gn.odc.gestionrh.common.enums.TypeAction;
import gn.odc.gestionrh.common.enums.TypeCodeConfirmation;
import gn.odc.gestionrh.common.util.ValidateurFichier;
import gn.odc.gestionrh.common.exception.RegleMetierException;
import gn.odc.gestionrh.common.exception.RessourceNonTrouveeException;
import gn.odc.gestionrh.employe.entity.Employe;
import gn.odc.gestionrh.configuration.service.ConfigurationNotificationService;
import gn.odc.gestionrh.configuration.service.EntrepriseContextService;
import gn.odc.gestionrh.employe.repository.EmployeRepository;
import gn.odc.gestionrh.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Path UPLOAD_DIR = Paths.get("uploads/photos");

    private final AuthenticationManager authenticationManager;
    private final UtilisateurRepository utilisateurRepository;
    private final EmployeRepository employeRepository;
    private final CodeConfirmationRepository codeConfirmationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final NotificationService notificationService;
    private final CompteProvisionService compteProvisionService;
    private final EntrepriseContextService entrepriseContextService;
    private final ConfigurationNotificationService configurationNotificationService;

    @Transactional
    public AuthReponseDTO login(LoginRequeteDTO requete) {
        String email = requete.getEmail().trim().toLowerCase();
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RegleMetierException("Identifiants invalides"));

        if (!utilisateur.isConfirme()) {
            throw new RegleMetierException("Compte non activé. Utilisez le lien reçu par e-mail/SMS pour activer votre compte.");
        }
        if (!utilisateur.isActif()) {
            throw new RegleMetierException("Compte suspendu. Contactez le service RH.");
        }

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, requete.getMotDePasse()));

        UtilisateurPrincipal principal = (UtilisateurPrincipal) auth.getPrincipal();
        String token = jwtService.genererToken(principal);
        return construireReponse(token, utilisateur);
    }

    @Transactional
    public Map<String, String> inscrire(InscriptionRequeteDTO requete) {
        throw new RegleMetierException(
                "L'auto-inscription est désactivée. Votre compte doit être créé par le service RH de "
                        + entrepriseContextService.nomEntreprise() + ".");
    }

    @Transactional
    public AuthReponseDTO activerCompte(ActiverCompteRequeteDTO requete) {
        requete.setEmail(requete.getEmail().trim().toLowerCase());
        requete.setCode(requete.getCode().trim());
        CodeConfirmation code = resoudreCodeActivation(requete);

        if (code.getExpireLe().isBefore(LocalDateTime.now())) {
            throw new RegleMetierException("Le code ou le lien a expiré. Demandez un nouveau lien au service RH.");
        }
        if (!code.getCode().equals(requete.getCode())) {
            throw new RegleMetierException("Code d'activation invalide");
        }

        Utilisateur utilisateur = utilisateurRepository.findByEmail(requete.getEmail())
                .orElseThrow(() -> new RessourceNonTrouveeException("Utilisateur introuvable"));

        if (utilisateur.isConfirme()) {
            throw new RegleMetierException("Ce compte est déjà activé. Connectez-vous.");
        }

        utilisateur.setMotDePasse(passwordEncoder.encode(requete.getMotDePasse()));
        utilisateur.setConfirme(true);
        utilisateur.setActif(true);
        utilisateurRepository.save(utilisateur);

        code.setUtilise(true);
        codeConfirmationRepository.save(code);

        UtilisateurPrincipal principal = new UtilisateurPrincipal(utilisateur);
        String token = jwtService.genererToken(principal);
        return construireReponse(token, utilisateur);
    }

    @Transactional(readOnly = true)
    public Map<String, String> verifierToken(String token) {
        CodeConfirmation code = codeConfirmationRepository
                .findByTokenAndUtiliseFalseAndTypeCode(token, TypeCodeConfirmation.ACTIVATION_COMPTE)
                .orElseThrow(() -> new RegleMetierException("Lien d'activation invalide ou expiré"));

        if (code.getExpireLe().isBefore(LocalDateTime.now())) {
            throw new RegleMetierException("Ce lien d'activation a expiré");
        }

        return Map.of("email", code.getEmail(), "valide", "true");
    }

    @Transactional
    public AuthReponseDTO confirmer(ConfirmationRequeteDTO requete) {
        throw new RegleMetierException("Utilisez l'endpoint /auth/activer-compte avec votre code et mot de passe.");
    }

    @Transactional
    public Map<String, String> renvoyerCode(String email) {
        return compteProvisionService.renvoyerActivation(email.trim().toLowerCase());
    }

    @Transactional(readOnly = true)
    public ProfilDTO profil(Authentication auth) {
        UtilisateurPrincipal principal = (UtilisateurPrincipal) auth.getPrincipal();
        Utilisateur utilisateur = utilisateurRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> new RessourceNonTrouveeException("Utilisateur introuvable"));
        return construireProfil(utilisateur);
    }

    @Transactional
    public ProfilDTO modifierProfil(Authentication auth, ProfilModifierDTO dto) {
        Utilisateur utilisateur = chargerUtilisateur(auth);
        Employe employe = utilisateur.getEmploye();
        if (employe == null) {
            throw new RegleMetierException("Aucun profil employé associé");
        }
        if (dto.getPrenom() != null && !dto.getPrenom().isBlank()) {
            employe.setPrenom(dto.getPrenom().trim());
        }
        if (dto.getNom() != null && !dto.getNom().isBlank()) {
            employe.setNom(dto.getNom().trim().toUpperCase());
        }
        if (dto.getTelephone() != null && !dto.getTelephone().isBlank()) {
            employe.setTelephone(dto.getTelephone().replaceAll("\\s+", ""));
        }
        employeRepository.save(employe);
        return construireProfil(utilisateur);
    }

    @Transactional
    public Map<String, String> modifierMotDePasse(Authentication auth, MotDePasseModifierDTO dto) {
        Utilisateur utilisateur = chargerUtilisateur(auth);
        if (!passwordEncoder.matches(dto.getMotDePasseActuel(), utilisateur.getMotDePasse())) {
            throw new RegleMetierException("Mot de passe actuel incorrect");
        }
        utilisateur.setMotDePasse(passwordEncoder.encode(dto.getNouveauMotDePasse()));
        utilisateurRepository.save(utilisateur);
        return Map.of("message", "Mot de passe modifié avec succès");
    }

    @Transactional
    public Map<String, String> demanderReinitialisation(DemanderReinitialisationDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();
        utilisateurRepository.findByEmail(email).ifPresent(utilisateur -> {
            if (!utilisateur.isConfirme()) return;

            String code = String.format("%06d", new Random().nextInt(999999));
            String token = UUID.randomUUID().toString().replace("-", "");
            String telephone = utilisateur.getEmploye() != null ? utilisateur.getEmploye().getTelephone() : null;

            CodeConfirmation confirmation = CodeConfirmation.builder()
                    .email(email)
                    .telephone(telephone)
                    .code(code)
                    .token(token)
                    .typeCode(TypeCodeConfirmation.REINITIALISATION_MOT_DE_PASSE)
                    .expireLe(LocalDateTime.now().plusMinutes(30))
                    .build();
            codeConfirmationRepository.save(confirmation);

            String lien = configurationNotificationService.obtenirAppUrl() + "/reinitialiser-mot-de-passe?token=" + token + "&email=" + email;
            notificationService.envoyerReinitialisationMotDePasse(email, telephone, code, lien);
        });

        return Map.of("message", "Si un compte actif existe pour cet e-mail, un code et un lien ont été envoyés.");
    }

    @Transactional(readOnly = true)
    public Map<String, String> verifierTokenReinitialisation(String token) {
        CodeConfirmation code = codeConfirmationRepository
                .findByTokenAndUtiliseFalseAndTypeCode(token, TypeCodeConfirmation.REINITIALISATION_MOT_DE_PASSE)
                .orElseThrow(() -> new RegleMetierException("Lien de réinitialisation invalide ou expiré"));

        if (code.getExpireLe().isBefore(LocalDateTime.now())) {
            throw new RegleMetierException("Ce lien de réinitialisation a expiré");
        }

        return Map.of("email", code.getEmail(), "valide", "true");
    }

    @Transactional
    public Map<String, String> reinitialiserMotDePasse(ReinitialiserMotDePasseDTO dto) {
        CodeConfirmation code = resoudreCodeReinitialisation(dto);

        if (code.getExpireLe().isBefore(LocalDateTime.now())) {
            throw new RegleMetierException("Le code ou le lien a expiré. Demandez une nouvelle réinitialisation.");
        }
        if (!code.getCode().equals(dto.getCode())) {
            throw new RegleMetierException("Code de réinitialisation invalide");
        }

        Utilisateur utilisateur = utilisateurRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RessourceNonTrouveeException("Utilisateur introuvable"));

        if (!utilisateur.isConfirme()) {
            throw new RegleMetierException("Ce compte n'est pas encore activé");
        }

        utilisateur.setMotDePasse(passwordEncoder.encode(dto.getNouveauMotDePasse()));
        utilisateurRepository.save(utilisateur);

        code.setUtilise(true);
        codeConfirmationRepository.save(code);

        return Map.of("message", "Mot de passe réinitialisé avec succès");
    }

    @Transactional
    public ProfilDTO modifierPhotoProfil(Authentication auth, MultipartFile file) throws IOException {
        ValidateurFichier.validerImage(file);
        if (!Files.exists(UPLOAD_DIR)) {
            Files.createDirectories(UPLOAD_DIR);
        }
        String ext = ValidateurFichier.extraireExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + ext;
        Files.copy(file.getInputStream(), UPLOAD_DIR.resolve(filename));
        String photoUrl = "/uploads/photos/" + filename;

        Utilisateur utilisateur = chargerUtilisateur(auth);
        Employe employe = utilisateur.getEmploye();
        if (employe == null) {
            throw new RegleMetierException("Aucun profil employé associé");
        }
        employe.setPhotoUrl(photoUrl);
        employeRepository.save(employe);
        return construireProfil(utilisateur);
    }

    private CodeConfirmation resoudreCodeReinitialisation(ReinitialiserMotDePasseDTO requete) {
        if (requete.getToken() != null && !requete.getToken().isBlank()) {
            return codeConfirmationRepository
                    .findByTokenAndUtiliseFalseAndTypeCode(requete.getToken(), TypeCodeConfirmation.REINITIALISATION_MOT_DE_PASSE)
                    .filter(c -> c.getEmail().equalsIgnoreCase(requete.getEmail()))
                    .orElseThrow(() -> new RegleMetierException("Lien de réinitialisation invalide"));
        }
        return codeConfirmationRepository
                .findTopByEmailAndTypeCodeAndUtiliseFalseOrderByDateCreationDesc(
                        requete.getEmail(), TypeCodeConfirmation.REINITIALISATION_MOT_DE_PASSE)
                .orElseThrow(() -> new RegleMetierException("Aucun code de réinitialisation actif pour cet e-mail"));
    }

    private CodeConfirmation resoudreCodeActivation(ActiverCompteRequeteDTO requete) {
        if (requete.getToken() != null && !requete.getToken().isBlank()) {
            return codeConfirmationRepository
                    .findByTokenAndUtiliseFalseAndTypeCode(requete.getToken(), TypeCodeConfirmation.ACTIVATION_COMPTE)
                    .filter(c -> c.getEmail().equalsIgnoreCase(requete.getEmail()))
                    .orElseThrow(() -> new RegleMetierException("Lien d'activation invalide"));
        }
        return codeConfirmationRepository
                .findTopByEmailAndTypeCodeAndUtiliseFalseOrderByDateCreationDesc(
                        requete.getEmail().trim().toLowerCase(), TypeCodeConfirmation.ACTIVATION_COMPTE)
                .orElseThrow(() -> new RegleMetierException("Aucun code d'activation actif pour cet e-mail"));
    }

    private Utilisateur chargerUtilisateur(Authentication auth) {
        UtilisateurPrincipal principal = (UtilisateurPrincipal) auth.getPrincipal();
        return utilisateurRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> new RessourceNonTrouveeException("Utilisateur introuvable"));
    }

    private AuthReponseDTO construireReponse(String token, Utilisateur utilisateur) {
        AuthReponseDTO reponse = new AuthReponseDTO();
        reponse.setToken(token);
        reponse.setEmail(utilisateur.getEmail());
        reponse.setEmployeId(utilisateur.getEmploye() != null ? utilisateur.getEmploye().getId() : null);
        if (utilisateur.getEmploye() != null) {
            Employe e = utilisateur.getEmploye();
            reponse.setNomComplet(e.getPrenom() + " " + e.getNom());
            reponse.setPrenom(e.getPrenom());
            reponse.setNom(e.getNom());
            reponse.setPhotoUrl(e.getPhotoUrl());
        }
        reponse.setRoles(extraireRoles(utilisateur));
        reponse.setPermissions(extraireMatrice(utilisateur));
        return reponse;
    }

    private ProfilDTO construireProfil(Utilisateur utilisateur) {
        ProfilDTO profil = new ProfilDTO();
        profil.setId(utilisateur.getId());
        profil.setEmail(utilisateur.getEmail());
        profil.setActif(utilisateur.isActif());
        profil.setConfirme(utilisateur.isConfirme());
        profil.setEmployeId(utilisateur.getEmploye() != null ? utilisateur.getEmploye().getId() : null);
        if (utilisateur.getEmploye() != null) {
            Employe e = utilisateur.getEmploye();
            profil.setNomComplet(e.getPrenom() + " " + e.getNom());
            profil.setNom(e.getNom());
            profil.setPrenom(e.getPrenom());
            profil.setTelephone(e.getTelephone());
            profil.setMatricule(e.getMatricule());
            profil.setPhotoUrl(e.getPhotoUrl());
            profil.setDepartementLibelle(e.getDepartement() != null ? e.getDepartement().getLibelle() : null);
            profil.setPosteLibelle(e.getPoste() != null ? e.getPoste().getLibelle() : null);
        }
        profil.setRoles(extraireRoles(utilisateur));
        profil.setPermissions(extraireMatrice(utilisateur));
        return profil;
    }

    private Set<String> extraireRoles(Utilisateur utilisateur) {
        Set<String> roles = new HashSet<>();
        for (Role role : utilisateur.getRoles()) {
            roles.add(role.getCode());
        }
        return roles;
    }

    private Map<ModuleApplication, Set<TypeAction>> extraireMatrice(Utilisateur utilisateur) {
        Map<ModuleApplication, Set<TypeAction>> matrice = new EnumMap<>(ModuleApplication.class);
        for (Role role : utilisateur.getRoles()) {
            if ("ADMINISTRATEUR".equals(role.getCode())) {
                for (ModuleApplication module : ModuleApplication.values()) {
                    matrice.put(module, EnumSet.allOf(TypeAction.class));
                }
                return matrice;
            }
            for (Permission perm : role.getPermissions()) {
                matrice.computeIfAbsent(perm.getModule(), k -> new HashSet<>()).add(perm.getAction());
            }
        }
        return matrice;
    }
}
