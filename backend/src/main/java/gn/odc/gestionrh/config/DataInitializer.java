package gn.odc.gestionrh.config;

import gn.odc.gestionrh.auth.entity.Utilisateur;
import gn.odc.gestionrh.auth.repository.UtilisateurRepository;
import gn.odc.gestionrh.authorization.entity.Permission;
import gn.odc.gestionrh.authorization.entity.Role;
import gn.odc.gestionrh.authorization.repository.PermissionRepository;
import gn.odc.gestionrh.authorization.repository.RoleRepository;
import gn.odc.gestionrh.common.enums.ModuleApplication;
import gn.odc.gestionrh.common.enums.TypeAction;
import gn.odc.gestionrh.employe.entity.Employe;
import gn.odc.gestionrh.employe.repository.EmployeRepository;
import gn.odc.gestionrh.referentiel.entity.Departement;
import gn.odc.gestionrh.referentiel.entity.Localisation;
import gn.odc.gestionrh.referentiel.entity.Poste;
import gn.odc.gestionrh.referentiel.repository.DepartementRepository;
import gn.odc.gestionrh.referentiel.repository.LocalisationRepository;
import gn.odc.gestionrh.referentiel.repository.PosteRepository;
import gn.odc.gestionrh.visite.entity.CarteVisite;
import gn.odc.gestionrh.visite.repository.CarteVisiteRepository;
import gn.odc.gestionrh.auth.service.CompteProvisionService;
import gn.odc.gestionrh.common.enums.StatutEntite;
import gn.odc.gestionrh.configuration.service.NotificationModeleService;
import gn.odc.gestionrh.configuration.service.ConfigurationEntrepriseService;
import gn.odc.gestionrh.configuration.entity.ConfigurationNotification;
import gn.odc.gestionrh.configuration.repository.ConfigurationNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EmployeRepository employeRepository;
    private final DepartementRepository departementRepository;
    private final PosteRepository posteRepository;
    private final LocalisationRepository localisationRepository;
    private final CarteVisiteRepository carteVisiteRepository;
    private final ConfigurationEntrepriseService configurationEntrepriseService;
    private final ConfigurationNotificationRepository configurationNotificationRepository;
    private final NotificationModeleService notificationModeleService;
    private final CompteProvisionService compteProvisionService;
    private final PasswordEncoder passwordEncoder;
    private final ReparationEncodageService reparationEncodageService;

    @Value("${minerva.app.url:http://localhost:5173}")
    private String defaultAppUrl;

    @Value("${brevo.smtp.host:smtp-relay.brevo.com}")
    private String brevoSmtpHost;

    @Value("${brevo.smtp.port:587}")
    private int brevoSmtpPort;

    @Value("${brevo.smtp.login:}")
    private String brevoSmtpLogin;

    @Value("${brevo.smtp.password:}")
    private String brevoSmtpPassword;

    @Value("${brevo.smtp.from-email:}")
    private String brevoFromEmail;

    @Value("${brevo.smtp.from-name:MINERVA GROUP}")
    private String brevoFromName;

    @Value("${brevo.api-key:}")
    private String brevoApiKey;

    @Value("${odc.admin.email}")
    private String adminEmail;

    @Value("${odc.admin.password}")
    private String adminPassword;

    @Value("${minerva.init.repair-accounts:false}")
    private boolean repairAccounts;

    @Override
    public void run(String... args) {
        executerSecurise("encodage", reparationEncodageService::reparer);
        executerSecurise("permissions", this::synchroniserPermissions);
        executerSecurise("roles", () -> {
            if (roleRepository.count() == 0) {
                initialiserRoles();
            } else {
                synchroniserRolesParDefaut();
            }
        });
        executerSecurise("referentiels", this::initialiserReferentiels);
        executerSecurise("configuration", this::initialiserConfiguration);
        executerSecurise("notifications", this::initialiserConfigurationNotifications);
        if (repairAccounts) {
            executerSecurise("comptes-employes", this::reparerComptesEmployes);
        }
        executerSecurise("cartes-visite", this::initialiserCartesVisite);
        executerSecurise("admin", this::initialiserAdmin);
        log.info("=== Initialisation données terminée ===");
    }

    private void executerSecurise(String etape, Runnable action) {
        try {
            log.info("Initialisation : {}...", etape);
            action.run();
            log.info("Initialisation : {} terminée", etape);
        } catch (Exception e) {
            log.error("Initialisation : {} échouée (non bloquant) : {}", etape, e.getMessage(), e);
        }
    }

    private void synchroniserPermissions() {
        int added = 0;
        for (ModuleApplication module : ModuleApplication.values()) {
            for (TypeAction action : TypeAction.values()) {
                if (!permissionRepository.existsByModuleAndAction(module, action)) {
                    permissionRepository.save(Permission.builder()
                            .module(module)
                            .action(action)
                            .build());
                    added++;
                }
            }
        }
        if (added > 0) {
            log.info("Permissions synchronisées : {} nouvelle(s)", added);
        }
    }

    private void synchroniserRolesParDefaut() {
        synchroniserRoleAdmin();
        synchroniserRoleRh();
        synchroniserRoleEmploye();
        synchroniserRoleReception();
        synchroniserRoleParDefaut();
    }

    private void synchroniserRoleParDefaut() {
        if (roleRepository.findByParDefautTrueAndStatut(StatutEntite.ACTIF).isPresent()) {
            return;
        }
        roleRepository.findByCode("EMPLOYE").ifPresent(role -> {
            role.setParDefaut(true);
            roleRepository.save(role);
            log.info("Rôle EMPLOYE défini comme rôle par défaut pour les nouveaux employés");
        });
    }

    private void synchroniserRoleAdmin() {
        roleRepository.findByCode("ADMINISTRATEUR").ifPresent(role -> {
            Set<Permission> perms = new HashSet<>(role.getPermissions());
            boolean changed = false;
            for (Permission p : permissionRepository.findAll()) {
                if (!perms.contains(p)) {
                    perms.add(p);
                    changed = true;
                }
            }
            if (changed) {
                role.setPermissions(perms);
                roleRepository.save(role);
                log.info("Rôle ADMINISTRATEUR : nouvelles permissions ajoutées");
            }
        });
    }

    private void synchroniserRoleRh() {
        roleRepository.findByCode("RH").ifPresent(role -> {
            Set<Permission> perms = new HashSet<>(role.getPermissions());
            boolean changed = false;
            for (ModuleApplication module : java.util.List.of(
                    ModuleApplication.EMPLOYES, ModuleApplication.CONTRATS,
                    ModuleApplication.CONGES, ModuleApplication.PAIES,
                    ModuleApplication.PRESENCES)) {
                changed |= ajouterPermission(perms, module, TypeAction.AFFICHER_AUTRUI);
            }
            if (changed) {
                role.setPermissions(perms);
                roleRepository.save(role);
                log.info("Rôle RH : permission AFFICHER_AUTRUI synchronisée");
            }
        });
    }

    private void synchroniserRoleEmploye() {
        roleRepository.findByCode("EMPLOYE").ifPresent(role -> {
            Set<Permission> perms = new HashSet<>(role.getPermissions());
            boolean changed = ajouterPermission(perms, ModuleApplication.PAIES, TypeAction.AFFICHER)
                    | ajouterPermission(perms, ModuleApplication.CONGES, TypeAction.AFFICHER)
                    | ajouterPermission(perms, ModuleApplication.CONGES, TypeAction.AJOUTER);
            if (changed) {
                role.setPermissions(perms);
                roleRepository.save(role);
                log.info("Rôle EMPLOYE synchronisé (PAIES:AFFICHER, CONGES:AFFICHER/AJOUTER)");
            }
        });
    }

    private void synchroniserRoleReception() {
        roleRepository.findByCode("RECEPTION").ifPresent(role -> {
            Set<Permission> perms = new HashSet<>(role.getPermissions());
            boolean changed = ajouterPermission(perms, ModuleApplication.PAIES, TypeAction.AFFICHER)
                    | ajouterPermission(perms, ModuleApplication.CONGES, TypeAction.AFFICHER)
                    | ajouterPermission(perms, ModuleApplication.CONGES, TypeAction.AJOUTER);
            if (changed) {
                role.setPermissions(perms);
                roleRepository.save(role);
                log.info("Rôle RECEPTION synchronisé (PAIES:AFFICHER, CONGES:AFFICHER/AJOUTER)");
            }
        });
    }

    private boolean ajouterPermission(Set<Permission> perms, ModuleApplication module, TypeAction action) {
        return permissionRepository.findByModuleAndAction(module, action)
                .filter(p -> !perms.contains(p))
                .map(p -> {
                    perms.add(p);
                    return true;
                })
                .orElse(false);
    }

    private void initialiserPermissions() {
        synchroniserPermissions();
    }

    private void initialiserRoles() {
        if (roleRepository.count() > 0) return;

        Set<Permission> toutes = new HashSet<>(permissionRepository.findAll());

        creerRole("ADMINISTRATEUR", "Administrateur Système",
                "Accès total à toutes les fonctionnalités", true, toutes);

        creerRole("RH", "Ressources Humaines",
                "Gestion RH complète", true, permissionsPour(
                        ModuleApplication.EMPLOYES, ModuleApplication.CONTRATS,
                        ModuleApplication.CONGES, ModuleApplication.PRESENCES,
                        ModuleApplication.PAIES, ModuleApplication.RAPPORTS,
                        ModuleApplication.REFERENTIELS, ModuleApplication.VISITES,
                        ModuleApplication.CONFIGURATION
                ));

        creerRole("EMPLOYE", "Employé",
                "Accès self-service", true, true, permissionsPour(
                        ModuleApplication.CONGES, ModuleApplication.PAIES
                ).stream().filter(p ->
                        p.getAction() == TypeAction.AFFICHER || p.getAction() == TypeAction.AJOUTER
                ).collect(java.util.stream.Collectors.toSet()));

        creerRole("RECEPTION", "Réception",
                "Pointage, visites et self-service employé", true, permissionsReception());
    }

    private Set<Permission> permissionsReception() {
        Set<Permission> perms = permissionsPour(
                ModuleApplication.PRESENCES, ModuleApplication.VISITES);
        ajouterPermission(perms, ModuleApplication.CONGES, TypeAction.AFFICHER);
        ajouterPermission(perms, ModuleApplication.CONGES, TypeAction.AJOUTER);
        ajouterPermission(perms, ModuleApplication.PAIES, TypeAction.AFFICHER);
        return perms;
    }

    private void creerRole(String code, String libelle, String desc, boolean systeme, Set<Permission> perms) {
        creerRole(code, libelle, desc, systeme, false, perms);
    }

    private void creerRole(String code, String libelle, String desc, boolean systeme, boolean parDefaut, Set<Permission> perms) {
        roleRepository.save(Role.builder()
                .code(code)
                .libelle(libelle)
                .description(desc)
                .systeme(systeme)
                .parDefaut(parDefaut)
                .permissions(perms)
                .build());
        log.info("Rôle créé : {}{}", code, parDefaut ? " (par défaut)" : "");
    }

    private Set<Permission> permissionsPour(ModuleApplication... modules) {
        Set<Permission> result = new HashSet<>();
        for (ModuleApplication module : modules) {
            for (TypeAction action : EnumSet.allOf(TypeAction.class)) {
                permissionRepository.findByModuleAndAction(module, action).ifPresent(result::add);
            }
        }
        return result;
    }

    private void initialiserReferentiels() {
        if (departementRepository.count() > 0) return;

        Departement rh = departementRepository.save(Departement.builder()
                .code("RH").libelle("Ressources Humaines").description("Gestion du personnel et paie").build());
        Departement it = departementRepository.save(Departement.builder()
                .code("IT").libelle("Technologies de l'Information").description("Développement et infrastructure").build());
        Departement fin = departementRepository.save(Departement.builder()
                .code("FIN").libelle("Finance").description("Comptabilité et déclarations sociales").build());
        Departement ops = departementRepository.save(Departement.builder()
                .code("OPS").libelle("Opérations").description("Logistique et exploitation").build());
        Departement com = departementRepository.save(Departement.builder()
                .code("COM").libelle("Commercial").description("Ventes et relation client").build());

        posteRepository.save(Poste.builder().code("DIR-RH").libelle("Directeur RH").departement(rh)
                .description("Pilotage des ressources humaines").build());
        posteRepository.save(Poste.builder().code("ASSIST-RH").libelle("Assistant RH").departement(rh)
                .description("Administration RH et recrutement").build());
        posteRepository.save(Poste.builder().code("DEV-SR").libelle("Développeur Senior").departement(it)
                .description("Conception et développement applicatif").build());
        posteRepository.save(Poste.builder().code("DEV-JR").libelle("Développeur Junior").departement(it)
                .description("Développement et maintenance").build());
        posteRepository.save(Poste.builder().code("COMPT").libelle("Comptable").departement(fin)
                .description("Paie, CNSS et déclarations fiscales").build());
        posteRepository.save(Poste.builder().code("GEST-OPS").libelle("Gestionnaire Opérations").departement(ops)
                .description("Coordination opérationnelle").build());
        posteRepository.save(Poste.builder().code("COMM").libelle("Commercial").departement(com)
                .description("Prospection et suivi clients").build());

        localisationRepository.save(Localisation.builder()
                .code("SIEGE").nom("Siège Social").adresse("Immeuble Sanana, Kaloum, Conakry").ville("Conakry").build());
        localisationRepository.save(Localisation.builder()
                .code("ANNEX").nom("Annexe Dixinn").adresse("Quartier Dixinn, Conakry").ville("Conakry").build());
        localisationRepository.save(Localisation.builder()
                .code("ENTREP").nom("Entrepôt Ratoma").adresse("Zone industrielle, Ratoma").ville("Conakry").build());
        localisationRepository.save(Localisation.builder()
                .code("KIPE").nom("Bureau Kipé").adresse("Kipé, Commune de Ratoma").ville("Conakry").build());

        log.info("Référentiels de base créés (5 départements, 7 postes, 4 localisations)");
    }

    private void initialiserConfiguration() {
        configurationEntrepriseService.obtenir();
        log.info("Configuration entreprise initialisée");
    }

    private void initialiserConfigurationNotifications() {
        ConfigurationNotification config = configurationNotificationRepository.findAll().stream()
                .findFirst()
                .orElseGet(ConfigurationNotification::new);

        config.setAppUrl(defaultAppUrl);
        config.setSmsProvider("TWILIO");

        boolean brevoActif = (brevoApiKey != null && !brevoApiKey.isBlank())
                || (brevoSmtpPassword != null && !brevoSmtpPassword.isBlank());
        if (brevoActif) {
            config.setModeEnvoi("LIVE");
            config.setEmailActif(true);
            config.setSmtpHost(brevoSmtpHost);
            config.setSmtpPort(brevoSmtpPort);
            if (brevoSmtpLogin != null && !brevoSmtpLogin.isBlank()) {
                config.setSmtpUsername(brevoSmtpLogin.trim());
            }
            if (brevoSmtpPassword != null && !brevoSmtpPassword.isBlank()) {
                config.setSmtpPassword(brevoSmtpPassword.trim());
            }
            if (brevoFromEmail != null && !brevoFromEmail.isBlank()) {
                config.setSmtpFromEmail(brevoFromEmail.trim());
            }
            config.setSmtpFromName(brevoFromName);
            config.setSmtpAuth(true);
            config.setSmtpStarttls(true);
            log.info("Configuration e-mail Brevo appliquée — expéditeur: {}", brevoFromEmail);
        } else if (config.getId() == null) {
            config.setModeEnvoi("MOCK");
            config.setSmtpHost("smtp-relay.brevo.com");
            config.setSmtpPort(587);
            config.setSmtpAuth(true);
            config.setSmtpStarttls(true);
            log.info("Configuration notifications initialisée (mode simulation)");
        }

        if (config.getModelesMessages() == null || config.getModelesMessages().isBlank()) {
            config.setModelesMessages(notificationModeleService.serialiserDefauts());
            log.info("Modèles de notification par défaut initialisés");
        }

        configurationNotificationRepository.save(config);
    }

    private void reparerComptesEmployes() {
        employeRepository.findByStatut(StatutEntite.ACTIF).forEach(employe -> {
            if (employe.getEmail() == null || employe.getEmail().isBlank()) {
                return;
            }
            if (!utilisateurRepository.existsByEmail(employe.getEmail())) {
                compteProvisionService.assurerCompteEmploye(employe, "EMPLOYE");
                log.info("Compte utilisateur créé pour {} — renvoyez l'activation depuis la fiche employé", employe.getEmail());
            }
        });
    }

    private void initialiserCartesVisite() {
        if (carteVisiteRepository.count() > 0) return;
        for (int i = 1; i <= 5; i++) {
            carteVisiteRepository.save(CarteVisite.builder()
                    .numeroCarte("VIS-" + String.format("%03d", i))
                    .build());
        }
        log.info("Cartes visiteur de démonstration créées");
    }

    private void initialiserAdmin() {
        migrerAdminLegacy();

        if (utilisateurRepository.existsByEmail(adminEmail)) {
            synchroniserMotDePasseAdmin();
            return;
        }

        Departement dept = departementRepository.findByCode("RH").orElse(null);
        Poste poste = posteRepository.findByCode("DIR-RH").orElse(null);

        Employe adminEmploye = employeRepository.save(Employe.builder()
                .matricule("ADM-001")
                .nom("Système")
                .prenom("Administrateur")
                .email(adminEmail)
                .telephone("+224620000000")
                .dateNaissance(LocalDate.of(1985, 1, 1))
                .departement(dept)
                .poste(poste)
                .build());

        Role roleAdmin = roleRepository.findByCode("ADMINISTRATEUR")
                .orElseThrow(() -> new IllegalStateException("Rôle ADMINISTRATEUR manquant"));

        Set<Role> rolesAdmin = new HashSet<>();
        rolesAdmin.add(roleAdmin);
        utilisateurRepository.save(Utilisateur.builder()
                .email(adminEmail)
                .motDePasse(passwordEncoder.encode(adminPassword))
                .employe(adminEmploye)
                .actif(true)
                .confirme(true)
                .roles(rolesAdmin)
                .build());

        log.info("Administrateur créé : {} / {}", adminEmail, adminPassword);
    }

    private void migrerAdminLegacy() {
        utilisateurRepository.findByEmail("admin@sanana.gn").ifPresent(legacy -> {
            legacy.setEmail(adminEmail);
            legacy.setMotDePasse(passwordEncoder.encode(adminPassword));
            legacy.setActif(true);
            legacy.setConfirme(true);
            if (legacy.getEmploye() != null) {
                legacy.getEmploye().setEmail(adminEmail);
                employeRepository.save(legacy.getEmploye());
            }
            utilisateurRepository.save(legacy);
            log.info("Administrateur migré de admin@sanana.gn vers {} (mdp: {})", adminEmail, adminPassword);
        });
    }

    /** Aligne le mot de passe admin sur odc.admin.password (Render / application-prod). */
    private void synchroniserMotDePasseAdmin() {
        utilisateurRepository.findByEmail(adminEmail).ifPresent(admin -> {
            admin.setMotDePasse(passwordEncoder.encode(adminPassword));
            admin.setActif(true);
            admin.setConfirme(true);
            utilisateurRepository.save(admin);
            log.info("Compte administrateur synchronisé : {}", adminEmail);
        });
    }
}
