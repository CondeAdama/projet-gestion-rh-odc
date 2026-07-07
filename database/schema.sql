/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.19-12.2.2-MariaDB, for Win64 (AMD64)
--
-- Host: localhost    Database: gestion_rh_db
-- ------------------------------------------------------
-- Server version	12.2.2-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*M!100616 SET @OLD_NOTE_VERBOSITY=@@NOTE_VERBOSITY, NOTE_VERBOSITY=0 */;

--
-- Table structure for table `cartes_visite`
--

DROP TABLE IF EXISTS `cartes_visite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `cartes_visite` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date_creation` datetime(6) NOT NULL,
  `numero_carte` varchar(50) NOT NULL,
  `statut` enum('ASSIGNEE','DISPONIBLE') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKm05q71unh68ohq193mlv4hdn2` (`numero_carte`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `codes_confirmation`
--

DROP TABLE IF EXISTS `codes_confirmation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `codes_confirmation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(10) NOT NULL,
  `date_creation` datetime(6) NOT NULL,
  `email` varchar(150) NOT NULL,
  `expire_le` datetime(6) NOT NULL,
  `telephone` varchar(30) DEFAULT NULL,
  `type_code` enum('ACTIVATION_COMPTE','INSCRIPTION','REINITIALISATION_MOT_DE_PASSE') NOT NULL,
  `utilise` bit(1) NOT NULL,
  `token` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `configuration_entreprise`
--

DROP TABLE IF EXISTS `configuration_entreprise`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `configuration_entreprise` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `adresse` varchar(255) DEFAULT NULL,
  `date_modification` datetime(6) DEFAULT NULL,
  `devise` varchar(10) NOT NULL,
  `email` varchar(150) DEFAULT NULL,
  `logo_url` varchar(255) DEFAULT NULL,
  `nif` varchar(50) DEFAULT NULL,
  `nom_entreprise` varchar(150) NOT NULL,
  `slogan` varchar(255) DEFAULT NULL,
  `telephone` varchar(30) DEFAULT NULL,
  `numero_cnss` varchar(50) DEFAULT NULL,
  `taux_cnss` decimal(5,2) DEFAULT NULL,
  `taux_rts` decimal(5,2) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `configuration_notifications`
--

DROP TABLE IF EXISTS `configuration_notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `configuration_notifications` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_url` varchar(255) DEFAULT NULL,
  `date_modification` datetime(6) DEFAULT NULL,
  `email_actif` bit(1) NOT NULL,
  `mode_envoi` varchar(10) NOT NULL,
  `sms_account_sid` varchar(255) DEFAULT NULL,
  `sms_actif` bit(1) NOT NULL,
  `sms_api_secret` varchar(255) DEFAULT NULL,
  `sms_provider` varchar(40) DEFAULT NULL,
  `sms_sender_id` varchar(50) DEFAULT NULL,
  `smtp_auth` bit(1) NOT NULL,
  `smtp_from_email` varchar(150) DEFAULT NULL,
  `smtp_from_name` varchar(100) DEFAULT NULL,
  `smtp_host` varchar(150) DEFAULT NULL,
  `smtp_password` varchar(255) DEFAULT NULL,
  `smtp_port` int(11) DEFAULT NULL,
  `smtp_starttls` bit(1) NOT NULL,
  `smtp_username` varchar(150) DEFAULT NULL,
  `modeles_messages` longtext DEFAULT NULL,
  `sms_extra_config` text DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `conges`
--

DROP TABLE IF EXISTS `conges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `conges` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date_creation` datetime(6) NOT NULL,
  `date_modification` datetime(6) NOT NULL,
  `statut` enum('ACTIF','ARCHIVE','SUPPRIME') NOT NULL,
  `commentaire_rh` varchar(255) DEFAULT NULL,
  `date_debut` date NOT NULL,
  `date_fin` date NOT NULL,
  `motif` varchar(255) DEFAULT NULL,
  `statut_conge` enum('APPROUVE','EN_ATTENTE','REFUSE') NOT NULL,
  `type_conge` enum('ANNUEL','MALADIE','MATERNITE','PAYE','SANS_SOLDE') NOT NULL,
  `employe_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfcmv18ndb20r5k4e9jajd929i` (`employe_id`),
  CONSTRAINT `FKfcmv18ndb20r5k4e9jajd929i` FOREIGN KEY (`employe_id`) REFERENCES `employes` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `contrats`
--

DROP TABLE IF EXISTS `contrats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `contrats` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date_creation` datetime(6) NOT NULL,
  `date_modification` datetime(6) NOT NULL,
  `statut` enum('ACTIF','ARCHIVE','SUPPRIME') NOT NULL,
  `autres_avantages` decimal(12,2) DEFAULT NULL,
  `date_debut` date NOT NULL,
  `date_fin` date DEFAULT NULL,
  `indemnite_logement` decimal(12,2) DEFAULT NULL,
  `indemnite_transport` decimal(12,2) DEFAULT NULL,
  `salaire_base` decimal(12,2) NOT NULL,
  `statut_contrat` enum('ACTIF','ARCHIVE','RESILIE') NOT NULL,
  `type_contrat` varchar(50) NOT NULL,
  `employe_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKar44fligg64n20sqs2cjhyyn0` (`employe_id`),
  CONSTRAINT `FKar44fligg64n20sqs2cjhyyn0` FOREIGN KEY (`employe_id`) REFERENCES `employes` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `departements`
--

DROP TABLE IF EXISTS `departements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `departements` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date_creation` datetime(6) NOT NULL,
  `date_modification` datetime(6) NOT NULL,
  `statut` enum('ACTIF','ARCHIVE','SUPPRIME') NOT NULL,
  `code` varchar(20) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `libelle` varchar(150) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKcggyqk74g1naphihefb9vj1b7` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `employes`
--

DROP TABLE IF EXISTS `employes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `employes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date_creation` datetime(6) NOT NULL,
  `date_modification` datetime(6) NOT NULL,
  `statut` enum('ACTIF','ARCHIVE','SUPPRIME') NOT NULL,
  `date_naissance` date NOT NULL,
  `email` varchar(150) NOT NULL,
  `matricule` varchar(50) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `photo_url` varchar(255) DEFAULT NULL,
  `prenom` varchar(100) NOT NULL,
  `telephone` varchar(30) NOT NULL,
  `departement_id` bigint(20) DEFAULT NULL,
  `poste_id` bigint(20) DEFAULT NULL,
  `statut_emploi` enum('ACTIF','LICENCIE','SUSPENDU') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK3v0uyo0bds0i1s553pjkiewvv` (`email`),
  UNIQUE KEY `UKqiuyb191hjkemu1mnj7n5g4bc` (`matricule`),
  KEY `FKg6y4s69ena7cto8y7egtpqsi` (`departement_id`),
  KEY `FKfhafimtmjrcjj4f9hf2cmrjro` (`poste_id`),
  CONSTRAINT `FKfhafimtmjrcjj4f9hf2cmrjro` FOREIGN KEY (`poste_id`) REFERENCES `postes` (`id`),
  CONSTRAINT `FKg6y4s69ena7cto8y7egtpqsi` FOREIGN KEY (`departement_id`) REFERENCES `departements` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `fiches_paie`
--

DROP TABLE IF EXISTS `fiches_paie`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fiches_paie` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date_creation` datetime(6) NOT NULL,
  `date_modification` datetime(6) NOT NULL,
  `statut` enum('ACTIF','ARCHIVE','SUPPRIME') NOT NULL,
  `cotisation_cnss` decimal(12,2) NOT NULL,
  `date_generation` date NOT NULL,
  `impot_rts` decimal(12,2) NOT NULL,
  `periode_annee` int(11) NOT NULL,
  `periode_mois` int(11) NOT NULL,
  `qr_code_token` varchar(255) NOT NULL,
  `salaire_brut` decimal(12,2) NOT NULL,
  `salaire_net` decimal(12,2) NOT NULL,
  `employe_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKb8y0labd4giffpg1fenuquj9r` (`employe_id`,`periode_mois`,`periode_annee`),
  UNIQUE KEY `UKitlw2l6hg8w6qepie95rfkfgd` (`qr_code_token`),
  CONSTRAINT `FK64nnyof4o9fu5ixhjmh1luuns` FOREIGN KEY (`employe_id`) REFERENCES `employes` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `localisations`
--

DROP TABLE IF EXISTS `localisations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `localisations` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date_creation` datetime(6) NOT NULL,
  `date_modification` datetime(6) NOT NULL,
  `statut` enum('ACTIF','ARCHIVE','SUPPRIME') NOT NULL,
  `adresse` varchar(255) NOT NULL,
  `code` varchar(20) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `ville` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKliwlag6bxmvv80p59ddrlsfm9` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `notifications_log`
--

DROP TABLE IF EXISTS `notifications_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `canal` varchar(20) NOT NULL,
  `contenu` text DEFAULT NULL,
  `date_creation` datetime(6) NOT NULL,
  `destinataire_email` varchar(150) DEFAULT NULL,
  `destinataire_telephone` varchar(30) DEFAULT NULL,
  `statut_envoi` varchar(20) NOT NULL,
  `sujet` varchar(255) DEFAULT NULL,
  `type_notification` enum('CREATION_CONTRAT','INSCRIPTION','LICENCIEMENT','REINITIALISATION','SUSPENSION_COMPTE','VALIDATION_CONGE') NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `permissions`
--

DROP TABLE IF EXISTS `permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `permissions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date_creation` datetime(6) NOT NULL,
  `date_modification` datetime(6) NOT NULL,
  `statut` enum('ACTIF','ARCHIVE','SUPPRIME') NOT NULL,
  `action` enum('AFFICHER','AFFICHER_AUTRUI','AJOUTER','MODIFIER','SUPPRIMER') NOT NULL,
  `module` enum('CONFIGURATION','CONGES','CONTRATS','EMPLOYES','PAIES','PRESENCES','RAPPORTS','REFERENTIELS','ROLES','UTILISATEURS','VISITES') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKg6w0msto4usgebkkje1tjujeo` (`module`,`action`)
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `postes`
--

DROP TABLE IF EXISTS `postes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `postes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date_creation` datetime(6) NOT NULL,
  `date_modification` datetime(6) NOT NULL,
  `statut` enum('ACTIF','ARCHIVE','SUPPRIME') NOT NULL,
  `code` varchar(20) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `libelle` varchar(150) NOT NULL,
  `departement_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK4y26r1505fhntp8lr4pp6q4h9` (`code`),
  KEY `FKrl55d24jkusy6ej21y88lr5sv` (`departement_id`),
  CONSTRAINT `FKrl55d24jkusy6ej21y88lr5sv` FOREIGN KEY (`departement_id`) REFERENCES `departements` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `presences`
--

DROP TABLE IF EXISTS `presences`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `presences` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date_creation` datetime(6) NOT NULL,
  `date_modification` datetime(6) NOT NULL,
  `statut` enum('ACTIF','ARCHIVE','SUPPRIME') NOT NULL,
  `date_jour` date NOT NULL,
  `heure_entree` time(6) NOT NULL,
  `heure_sortie` time(6) DEFAULT NULL,
  `statut_presence` enum('EN_REGLE','REFUSE','RETARD') NOT NULL,
  `employe_id` bigint(20) NOT NULL,
  `localisation_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKhppi2n16tojxo65oanqhemen2` (`employe_id`),
  KEY `FKga8c0ea19x4hsaadv4d6l7s0u` (`localisation_id`),
  CONSTRAINT `FKga8c0ea19x4hsaadv4d6l7s0u` FOREIGN KEY (`localisation_id`) REFERENCES `localisations` (`id`),
  CONSTRAINT `FKhppi2n16tojxo65oanqhemen2` FOREIGN KEY (`employe_id`) REFERENCES `employes` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `role_permissions`
--

DROP TABLE IF EXISTS `role_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `role_permissions` (
  `role_id` bigint(20) NOT NULL,
  `permission_id` bigint(20) NOT NULL,
  PRIMARY KEY (`role_id`,`permission_id`),
  KEY `FKegdk29eiy7mdtefy5c7eirr6e` (`permission_id`),
  CONSTRAINT `FKegdk29eiy7mdtefy5c7eirr6e` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`),
  CONSTRAINT `FKn5fotdgk8d1xvo8nav9uv3muc` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date_creation` datetime(6) NOT NULL,
  `date_modification` datetime(6) NOT NULL,
  `statut` enum('ACTIF','ARCHIVE','SUPPRIME') NOT NULL,
  `code` varchar(50) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `libelle` varchar(100) NOT NULL,
  `systeme` bit(1) NOT NULL,
  `par_defaut` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKch1113horj4qr56f91omojv8` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `utilisateur_roles`
--

DROP TABLE IF EXISTS `utilisateur_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `utilisateur_roles` (
  `utilisateur_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`utilisateur_id`,`role_id`),
  KEY `FKpvb9dvcn9icrukism9we0knub` (`role_id`),
  CONSTRAINT `FK7fm9de8itma0gr6wpblfr5o0o` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateurs` (`id`),
  CONSTRAINT `FKpvb9dvcn9icrukism9we0knub` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `utilisateurs`
--

DROP TABLE IF EXISTS `utilisateurs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `utilisateurs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date_creation` datetime(6) NOT NULL,
  `date_modification` datetime(6) NOT NULL,
  `statut` enum('ACTIF','ARCHIVE','SUPPRIME') NOT NULL,
  `actif` bit(1) NOT NULL,
  `confirme` bit(1) NOT NULL,
  `email` varchar(150) NOT NULL,
  `mot_de_passe` varchar(255) NOT NULL,
  `employe_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6ldvumu3hqvnmmxy1b6lsxwqy` (`email`),
  UNIQUE KEY `UK8ochftp6dqc9en5n3h819ks8k` (`employe_id`),
  CONSTRAINT `FK2lkkuhm8nh5fbkt9egc4t2tih` FOREIGN KEY (`employe_id`) REFERENCES `employes` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `visites`
--

DROP TABLE IF EXISTS `visites`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `visites` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date_creation` datetime(6) NOT NULL,
  `date_heure_entree` datetime(6) NOT NULL,
  `date_heure_sortie` datetime(6) DEFAULT NULL,
  `motif` varchar(255) NOT NULL,
  `statut` enum('EN_COURS','TERMINEE') NOT NULL,
  `carte_visite_id` bigint(20) NOT NULL,
  `visiteur_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4q7qhrrq7pcmpm7sf7171pl9i` (`carte_visite_id`),
  KEY `FK3tb1kdjf8hxrw98e0qn2fll7u` (`visiteur_id`),
  CONSTRAINT `FK3tb1kdjf8hxrw98e0qn2fll7u` FOREIGN KEY (`visiteur_id`) REFERENCES `visiteurs` (`id`),
  CONSTRAINT `FK4q7qhrrq7pcmpm7sf7171pl9i` FOREIGN KEY (`carte_visite_id`) REFERENCES `cartes_visite` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `visiteurs`
--

DROP TABLE IF EXISTS `visiteurs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `visiteurs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `contact` varchar(50) NOT NULL,
  `date_creation` datetime(6) NOT NULL,
  `entreprise` varchar(150) DEFAULT NULL,
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL,
  `statut` varchar(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'gestion_rh_db'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*M!100616 SET NOTE_VERBOSITY=@OLD_NOTE_VERBOSITY */;

-- Dump completed on 2026-07-07 16:21:28
