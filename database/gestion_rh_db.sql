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
-- Current Database: `gestion_rh_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `gestion_rh_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_uca1400_ai_ci */;

USE `gestion_rh_db`;

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
-- Dumping data for table `cartes_visite`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `cartes_visite` WRITE;
/*!40000 ALTER TABLE `cartes_visite` DISABLE KEYS */;
INSERT INTO `cartes_visite` VALUES
(1,'2026-07-06 16:23:37.582543','VIS-001','DISPONIBLE'),
(2,'2026-07-06 16:23:37.583544','VIS-002','DISPONIBLE'),
(3,'2026-07-06 16:23:37.586541','VIS-003','DISPONIBLE'),
(4,'2026-07-06 16:23:37.587594','VIS-004','DISPONIBLE'),
(5,'2026-07-06 16:23:37.590565','VIS-005','DISPONIBLE');
/*!40000 ALTER TABLE `cartes_visite` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `codes_confirmation`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `codes_confirmation` WRITE;
/*!40000 ALTER TABLE `codes_confirmation` DISABLE KEYS */;
INSERT INTO `codes_confirmation` VALUES
(1,'869439','2026-07-07 14:14:23.753893','diabatekaba198@gmail.com','2026-07-09 14:14:23.749798','+224629697244','ACTIVATION_COMPTE',0x00,'9635dbdc11104c6f998b3cccd361461e'),
(2,'238148','2026-07-07 14:15:41.076232','diabatekaba198@gmail.com','2026-07-09 14:15:41.075092','+224629697244','ACTIVATION_COMPTE',0x00,'a9eb5f9fa3c942568d71d132b3873c2c'),
(3,'945614','2026-07-07 14:15:44.499697','diabatekaba198@gmail.com','2026-07-09 14:15:44.499697','+224629697244','ACTIVATION_COMPTE',0x01,'7961676a76144b39aa50854ab7b503ef'),
(4,'089041','2026-07-07 16:15:37.159500','diabatekaba198@gmail.com','2026-07-07 16:45:37.024505','+224629697244','REINITIALISATION_MOT_DE_PASSE',0x01,'46329c5c29d84d4da221cba3ce6461c9');
/*!40000 ALTER TABLE `codes_confirmation` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `configuration_entreprise`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `configuration_entreprise` WRITE;
/*!40000 ALTER TABLE `configuration_entreprise` DISABLE KEYS */;
INSERT INTO `configuration_entreprise` VALUES
(1,'Immeuble Sanana, Kaloum, Commune de Kaloum, Conakry, Guin├⌐e','2026-07-06 18:17:51.816977','GNF','contact@sanana.gn','/uploads/logos/d37a3744-e89e-4dbc-8161-0374b258d30b.png','GN-NIF-2026-B98745','MINERVA GROUP','Identit├⌐ sociale et logistique RH','+224620000000','GN-CNSS-2026-45892',5.00,10.00);
/*!40000 ALTER TABLE `configuration_entreprise` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `configuration_notifications`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `configuration_notifications` WRITE;
/*!40000 ALTER TABLE `configuration_notifications` DISABLE KEYS */;
INSERT INTO `configuration_notifications` VALUES
(1,'http://localhost:5173','2026-07-07 14:10:38.312997',0x01,'LIVE',NULL,0x00,NULL,'TWILIO',NULL,0x01,'jeandelano223@gmail.com','MINERVA GROUP','smtp-relay.brevo.com','',587,0x01,'b12de0001@smtp-brevo.com','{\"CONGE_APPROUVE\":{\"emailSujet\":\"Cong├⌐ approuv├⌐ ΓÇö {{entreprise}}\",\"emailCorps\":\"Bonjour {{prenom}} {{nom}}, votre demande de cong├⌐ ({{typeConge}}, du {{dateDebut}} au {{dateFin}}) a ├⌐t├⌐ approuv├⌐e.{{commentaireRh}}\",\"smsCorps\":\"Cong├⌐ approuv├⌐ ({{dateDebut}}-{{dateFin}}). {{entreprise}}\"},\"CODE_CONFIRMATION\":{\"emailSujet\":\"Code de confirmation - {{entreprise}}\",\"emailCorps\":\"Votre code de confirmation est : {{code}}. Valide 15 minutes.\",\"smsCorps\":\"Code {{code}} - {{entreprise}}\"},\"LICENCIEMENT\":{\"emailSujet\":\"Notification de licenciement ΓÇö {{entreprise}}\",\"emailCorps\":\"Bonjour {{prenom}} {{nom}}, nous vous informons de la fin de votre collaboration avec {{entreprise}}.\",\"smsCorps\":\"Fin de collaboration avec {{entreprise}}. Contactez le RH.\"},\"TEST_SMS\":{\"emailSujet\":\"\",\"emailCorps\":\"\",\"smsCorps\":\"Test SMS {{entreprise}} : votre configuration SMS fonctionne correctement.\"},\"REINITIALISATION_MDP\":{\"emailSujet\":\"R├⌐initialisation de mot de passe - {{entreprise}}\",\"emailCorps\":\"Vous avez demand├⌐ la r├⌐initialisation de votre mot de passe. Code : {{code}} (valide 30 min). R├⌐initialisez ici : {{lien}}\",\"smsCorps\":\"{{entreprise}} : r├⌐initialisation MDP. Code {{code}}. {{lien}}\"},\"SUSPENSION_COMPTE\":{\"emailSujet\":\"Suspension de compte ΓÇö {{entreprise}}\",\"emailCorps\":\"Bonjour {{prenom}} {{nom}}, votre compte a ├⌐t├⌐ suspendu. Contactez le service RH pour plus d\'informations.\",\"smsCorps\":\"Compte suspendu chez {{entreprise}}. Contactez le RH.\"},\"ACTIVATION_COMPTE\":{\"emailSujet\":\"Activation de votre compte - {{entreprise}}\",\"emailCorps\":\"Bienvenue chez {{entreprise}} ! Votre code d\'activation : {{code}} (valide 48h). Activez votre compte ici : {{lien}} ΓÇö D├⌐finissez ensuite votre mot de passe.\",\"smsCorps\":\"{{entreprise}} : activez votre compte. Code {{code}}. {{lien}}\"},\"CONGE_REFUSE\":{\"emailSujet\":\"Cong├⌐ refus├⌐ ΓÇö {{entreprise}}\",\"emailCorps\":\"Bonjour {{prenom}} {{nom}}, votre demande de cong├⌐ ({{typeConge}}, du {{dateDebut}} au {{dateFin}}) a ├⌐t├⌐ refus├⌐e.{{commentaireRh}}\",\"smsCorps\":\"Cong├⌐ refus├⌐. Contactez le RH ΓÇö {{entreprise}}\"},\"CREATION_CONTRAT\":{\"emailSujet\":\"Nouveau contrat ΓÇö {{entreprise}}\",\"emailCorps\":\"Bonjour {{prenom}} {{nom}}, un contrat {{typeContrat}} a ├⌐t├⌐ cr├⌐├⌐ avec un salaire de base de {{salaireBase}} GNF. Date de d├⌐but : {{dateDebut}}.\",\"smsCorps\":\"Nouveau contrat {{typeContrat}} chez {{entreprise}}. D├⌐but : {{dateDebut}}.\"},\"TEST_EMAIL\":{\"emailSujet\":\"Test e-mail - {{entreprise}}\",\"emailCorps\":\"Ceci est un e-mail de test envoy├⌐ depuis la configuration {{entreprise}}. Si vous le recevez, le SMTP est correctement param├⌐tr├⌐.\",\"smsCorps\":\"\"}}',NULL);
/*!40000 ALTER TABLE `configuration_notifications` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `conges`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `conges` WRITE;
/*!40000 ALTER TABLE `conges` DISABLE KEYS */;
INSERT INTO `conges` VALUES
(1,'2026-07-06 18:06:00.931076','2026-07-07 14:52:56.465482','ACTIF','Je confirme ton cong├⌐ mais deux jours seulement.','2026-07-07','2026-07-08','','APPROUVE','PAYE',2);
/*!40000 ALTER TABLE `conges` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `contrats`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `contrats` WRITE;
/*!40000 ALTER TABLE `contrats` DISABLE KEYS */;
INSERT INTO `contrats` VALUES
(1,'2026-07-06 17:59:15.191390','2026-07-06 17:59:15.191390','ACTIF',100000.00,'2026-05-01','2028-12-06',400000.00,500000.00,5000000.00,'ACTIF','CDI',2);
/*!40000 ALTER TABLE `contrats` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `departements`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `departements` WRITE;
/*!40000 ALTER TABLE `departements` DISABLE KEYS */;
INSERT INTO `departements` VALUES
(1,'2026-07-06 15:04:48.369953','2026-07-06 15:04:48.369953','ACTIF','RH',NULL,'Ressources Humaines'),
(2,'2026-07-06 15:04:48.372952','2026-07-06 15:04:48.372952','ACTIF','IT',NULL,'Technologies de l\'Information'),
(3,'2026-07-06 18:09:53.203846','2026-07-06 18:09:53.204845','ACTIF','FIN','Comptabilit├⌐','Finance');
/*!40000 ALTER TABLE `departements` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `employes`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `employes` WRITE;
/*!40000 ALTER TABLE `employes` DISABLE KEYS */;
INSERT INTO `employes` VALUES
(1,'2026-07-06 15:04:48.394319','2026-07-07 14:32:25.987742','ACTIF','2000-06-07','admin@minerva.group','SNG-2026-001','SYST├êME','/uploads/photos/1e5c8f56-ef3b-4e95-9ab8-9676e0ae0fa1.png','Administrateur','+224620758634',1,1,'ACTIF'),
(2,'2026-07-06 17:33:13.889494','2026-07-06 17:35:59.132081','ACTIF','2003-01-06','diabatekaba198@gmail.com','SNG-2026-002','DIOUBATE','','Kaba','+224629697244',1,2,'ACTIF');
/*!40000 ALTER TABLE `employes` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `fiches_paie`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fiches_paie` WRITE;
/*!40000 ALTER TABLE `fiches_paie` DISABLE KEYS */;
INSERT INTO `fiches_paie` VALUES
(1,'2026-07-06 17:59:35.898140','2026-07-06 17:59:35.898140','ACTIF',300000.00,'2026-07-06',600000.00,2026,6,'SANANA-PAY-9C79D646',6000000.00,5100000.00,2),
(2,'2026-07-06 18:07:14.270192','2026-07-06 18:07:14.270192','ACTIF',300000.00,'2026-07-06',600000.00,2026,5,'SANANA-PAY-416CA129',6000000.00,5100000.00,2);
/*!40000 ALTER TABLE `fiches_paie` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `localisations`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `localisations` WRITE;
/*!40000 ALTER TABLE `localisations` DISABLE KEYS */;
INSERT INTO `localisations` VALUES
(1,'2026-07-06 15:04:48.377951','2026-07-06 15:04:48.377951','ACTIF','Kaloum, Conakry','SIEGE','Si├¿ge Social','Conakry'),
(2,'2026-07-06 18:42:55.288498','2026-07-06 18:42:55.288498','ACTIF','Matoto, Conakry','SIEGE2','Si├¿ge Matoto','Conakry');
/*!40000 ALTER TABLE `localisations` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `notifications_log`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `notifications_log` WRITE;
/*!40000 ALTER TABLE `notifications_log` DISABLE KEYS */;
INSERT INTO `notifications_log` VALUES
(1,'EMAIL','Bonjour Kaba DIOUBATE, un contrat CDI a ├⌐t├⌐ cr├⌐├⌐ avec un salaire de base de 5000000 GNF. Date de d├⌐but : 2026-05-01.','2026-07-06 17:59:15.265661','diabatekaba198@gmail.com',NULL,'ENVOYE','Nouveau contrat ΓÇö SANANA GROUP','CREATION_CONTRAT'),
(2,'SMS','Bonjour Kaba DIOUBATE, un contrat CDI a ├⌐t├⌐ cr├⌐├⌐ avec un salaire de base de 5000000 GNF. Date de d├⌐but : 2026-05-01.','2026-07-06 17:59:15.290383',NULL,'+224629697244','ENVOYE','Nouveau contrat ΓÇö SANANA GROUP','CREATION_CONTRAT'),
(3,'EMAIL','Test configuration','2026-07-07 13:39:38.664089','diabatekaba198@gmail.com',NULL,'ENVOYE','Test e-mail','INSCRIPTION'),
(4,'EMAIL','Bienvenue chez MINERVA GROUP ! Votre code d\'activation : 869439 (valide 48h). Activez votre compte ici : http://localhost:5173/activer?token=9635dbdc11104c6f998b3cccd361461e&email=diabatekaba198@gmail.com ΓÇö D├⌐finissez ensuite votre mot de passe.','2026-07-07 14:14:25.397603','diabatekaba198@gmail.com',NULL,'ECHEC','Activation de votre compte - MINERVA GROUP','INSCRIPTION'),
(5,'SMS','MINERVA GROUP : activez votre compte. Code 869439. http://localhost:5173/activer?token=9635dbdc11104c6f998b3cccd361461e&email=diabatekaba198@gmail.com','2026-07-07 14:14:25.405614',NULL,'+224629697244','MOCK','Activation de votre compte - MINERVA GROUP','INSCRIPTION'),
(6,'EMAIL','Bienvenue chez MINERVA GROUP ! Votre code d\'activation : 238148 (valide 48h). Activez votre compte ici : http://localhost:5173/activer?token=a9eb5f9fa3c942568d71d132b3873c2c&email=diabatekaba198@gmail.com ΓÇö D├⌐finissez ensuite votre mot de passe.','2026-07-07 14:15:42.802530','diabatekaba198@gmail.com',NULL,'ENVOYE','Activation de votre compte - MINERVA GROUP','INSCRIPTION'),
(7,'SMS','MINERVA GROUP : activez votre compte. Code 238148. http://localhost:5173/activer?token=a9eb5f9fa3c942568d71d132b3873c2c&email=diabatekaba198@gmail.com','2026-07-07 14:15:42.806530',NULL,'+224629697244','MOCK','Activation de votre compte - MINERVA GROUP','INSCRIPTION'),
(8,'EMAIL','Bienvenue chez MINERVA GROUP ! Votre code d\'activation : 945614 (valide 48h). Activez votre compte ici : http://localhost:5173/activer?token=7961676a76144b39aa50854ab7b503ef&email=diabatekaba198@gmail.com ΓÇö D├⌐finissez ensuite votre mot de passe.','2026-07-07 14:15:46.099501','diabatekaba198@gmail.com',NULL,'ENVOYE','Activation de votre compte - MINERVA GROUP','INSCRIPTION'),
(9,'SMS','MINERVA GROUP : activez votre compte. Code 945614. http://localhost:5173/activer?token=7961676a76144b39aa50854ab7b503ef&email=diabatekaba198@gmail.com','2026-07-07 14:15:46.103476',NULL,'+224629697244','MOCK','Activation de votre compte - MINERVA GROUP','INSCRIPTION'),
(10,'EMAIL','Bonjour Kaba DIOUBATE, votre demande de cong├⌐ (PAYE, du 2026-07-07 au 2026-07-08) a ├⌐t├⌐ approuv├⌐e. Commentaire RH : Je confirme ton cong├⌐ mais deux jours seulement.','2026-07-07 14:52:56.429063','diabatekaba198@gmail.com',NULL,'ENVOYE','Cong├⌐ approuv├⌐ ΓÇö MINERVA GROUP','VALIDATION_CONGE'),
(11,'SMS','Cong├⌐ approuv├⌐ (2026-07-07-2026-07-08). MINERVA GROUP','2026-07-07 14:52:56.461504',NULL,'+224629697244','MOCK','Cong├⌐ approuv├⌐ ΓÇö MINERVA GROUP','VALIDATION_CONGE'),
(12,'EMAIL','Vous avez demand├⌐ la r├⌐initialisation de votre mot de passe. Code : 089041 (valide 30 min). R├⌐initialisez ici : http://localhost:5173/reinitialiser-mot-de-passe?token=46329c5c29d84d4da221cba3ce6461c9&email=diabatekaba198@gmail.com','2026-07-07 16:15:40.825620','diabatekaba198@gmail.com',NULL,'ENVOYE','R├⌐initialisation de mot de passe - MINERVA GROUP','REINITIALISATION'),
(13,'SMS','MINERVA GROUP : r├⌐initialisation MDP. Code 089041. http://localhost:5173/reinitialiser-mot-de-passe?token=46329c5c29d84d4da221cba3ce6461c9&email=diabatekaba198@gmail.com','2026-07-07 16:15:40.833720',NULL,'+224629697244','MOCK','R├⌐initialisation de mot de passe - MINERVA GROUP','REINITIALISATION');
/*!40000 ALTER TABLE `notifications_log` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `permissions`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `permissions` WRITE;
/*!40000 ALTER TABLE `permissions` DISABLE KEYS */;
INSERT INTO `permissions` VALUES
(1,'2026-07-06 15:04:48.050411','2026-07-06 15:04:48.050411','ACTIF','AFFICHER','EMPLOYES'),
(2,'2026-07-06 15:04:48.077143','2026-07-06 15:04:48.077143','ACTIF','AJOUTER','EMPLOYES'),
(3,'2026-07-06 15:04:48.078122','2026-07-06 15:04:48.078122','ACTIF','MODIFIER','EMPLOYES'),
(4,'2026-07-06 15:04:48.079120','2026-07-06 15:04:48.079120','ACTIF','SUPPRIMER','EMPLOYES'),
(5,'2026-07-06 15:04:48.080120','2026-07-06 15:04:48.080120','ACTIF','AFFICHER','CONTRATS'),
(6,'2026-07-06 15:04:48.081179','2026-07-06 15:04:48.081179','ACTIF','AJOUTER','CONTRATS'),
(7,'2026-07-06 15:04:48.083124','2026-07-06 15:04:48.083124','ACTIF','MODIFIER','CONTRATS'),
(8,'2026-07-06 15:04:48.085121','2026-07-06 15:04:48.085121','ACTIF','SUPPRIMER','CONTRATS'),
(9,'2026-07-06 15:04:48.087122','2026-07-06 15:04:48.087122','ACTIF','AFFICHER','CONGES'),
(10,'2026-07-06 15:04:48.088147','2026-07-06 15:04:48.088147','ACTIF','AJOUTER','CONGES'),
(11,'2026-07-06 15:04:48.089120','2026-07-06 15:04:48.089120','ACTIF','MODIFIER','CONGES'),
(12,'2026-07-06 15:04:48.090238','2026-07-06 15:04:48.090238','ACTIF','SUPPRIMER','CONGES'),
(13,'2026-07-06 15:04:48.091122','2026-07-06 15:04:48.091122','ACTIF','AFFICHER','PRESENCES'),
(14,'2026-07-06 15:04:48.092121','2026-07-06 15:04:48.092121','ACTIF','AJOUTER','PRESENCES'),
(15,'2026-07-06 15:04:48.093136','2026-07-06 15:04:48.093136','ACTIF','MODIFIER','PRESENCES'),
(16,'2026-07-06 15:04:48.094122','2026-07-06 15:04:48.095122','ACTIF','SUPPRIMER','PRESENCES'),
(17,'2026-07-06 15:04:48.096121','2026-07-06 15:04:48.096121','ACTIF','AFFICHER','PAIES'),
(18,'2026-07-06 15:04:48.097121','2026-07-06 15:04:48.097121','ACTIF','AJOUTER','PAIES'),
(19,'2026-07-06 15:04:48.098144','2026-07-06 15:04:48.098144','ACTIF','MODIFIER','PAIES'),
(20,'2026-07-06 15:04:48.099122','2026-07-06 15:04:48.099122','ACTIF','SUPPRIMER','PAIES'),
(21,'2026-07-06 15:04:48.101122','2026-07-06 15:04:48.101122','ACTIF','AFFICHER','RAPPORTS'),
(22,'2026-07-06 15:04:48.103121','2026-07-06 15:04:48.103121','ACTIF','AJOUTER','RAPPORTS'),
(23,'2026-07-06 15:04:48.104155','2026-07-06 15:04:48.104155','ACTIF','MODIFIER','RAPPORTS'),
(24,'2026-07-06 15:04:48.105123','2026-07-06 15:04:48.105123','ACTIF','SUPPRIMER','RAPPORTS'),
(25,'2026-07-06 15:04:48.106124','2026-07-06 15:04:48.106124','ACTIF','AFFICHER','UTILISATEURS'),
(26,'2026-07-06 15:04:48.107121','2026-07-06 15:04:48.108147','ACTIF','AJOUTER','UTILISATEURS'),
(27,'2026-07-06 15:04:48.109122','2026-07-06 15:04:48.109122','ACTIF','MODIFIER','UTILISATEURS'),
(28,'2026-07-06 15:04:48.110121','2026-07-06 15:04:48.110121','ACTIF','SUPPRIMER','UTILISATEURS'),
(29,'2026-07-06 15:04:48.110121','2026-07-06 15:04:48.110121','ACTIF','AFFICHER','ROLES'),
(30,'2026-07-06 15:04:48.111158','2026-07-06 15:04:48.111158','ACTIF','AJOUTER','ROLES'),
(31,'2026-07-06 15:04:48.112123','2026-07-06 15:04:48.112123','ACTIF','MODIFIER','ROLES'),
(32,'2026-07-06 15:04:48.113121','2026-07-06 15:04:48.113121','ACTIF','SUPPRIMER','ROLES'),
(33,'2026-07-06 15:04:48.114121','2026-07-06 15:04:48.114121','ACTIF','AFFICHER','REFERENTIELS'),
(34,'2026-07-06 15:04:48.116123','2026-07-06 15:04:48.116123','ACTIF','AJOUTER','REFERENTIELS'),
(35,'2026-07-06 15:04:48.117121','2026-07-06 15:04:48.117121','ACTIF','MODIFIER','REFERENTIELS'),
(36,'2026-07-06 15:04:48.118122','2026-07-06 15:04:48.118122','ACTIF','SUPPRIMER','REFERENTIELS'),
(37,'2026-07-06 15:04:48.119127','2026-07-06 15:04:48.119127','ACTIF','AFFICHER','VISITES'),
(38,'2026-07-06 15:04:48.120122','2026-07-06 15:04:48.120122','ACTIF','AJOUTER','VISITES'),
(39,'2026-07-06 15:04:48.121120','2026-07-06 15:04:48.121120','ACTIF','MODIFIER','VISITES'),
(40,'2026-07-06 15:04:48.122122','2026-07-06 15:04:48.122122','ACTIF','SUPPRIMER','VISITES'),
(41,'2026-07-06 15:04:48.123121','2026-07-06 15:04:48.123121','ACTIF','AFFICHER','CONFIGURATION'),
(42,'2026-07-06 15:04:48.124122','2026-07-06 15:04:48.124122','ACTIF','AJOUTER','CONFIGURATION'),
(43,'2026-07-06 15:04:48.125121','2026-07-06 15:04:48.125121','ACTIF','MODIFIER','CONFIGURATION'),
(44,'2026-07-06 15:04:48.126122','2026-07-06 15:04:48.126122','ACTIF','SUPPRIMER','CONFIGURATION'),
(45,'2026-07-07 14:35:41.600996','2026-07-07 14:35:41.602031','ACTIF','AFFICHER_AUTRUI','EMPLOYES'),
(46,'2026-07-07 14:35:41.640000','2026-07-07 14:35:41.640000','ACTIF','AFFICHER_AUTRUI','CONTRATS'),
(47,'2026-07-07 14:35:41.648996','2026-07-07 14:35:41.648996','ACTIF','AFFICHER_AUTRUI','CONGES'),
(48,'2026-07-07 14:35:41.657995','2026-07-07 14:35:41.657995','ACTIF','AFFICHER_AUTRUI','PRESENCES'),
(49,'2026-07-07 14:35:41.669002','2026-07-07 14:35:41.669002','ACTIF','AFFICHER_AUTRUI','PAIES'),
(50,'2026-07-07 14:35:41.681271','2026-07-07 14:35:41.681271','ACTIF','AFFICHER_AUTRUI','RAPPORTS'),
(51,'2026-07-07 14:35:41.691352','2026-07-07 14:35:41.691352','ACTIF','AFFICHER_AUTRUI','UTILISATEURS'),
(52,'2026-07-07 14:35:41.701304','2026-07-07 14:35:41.701304','ACTIF','AFFICHER_AUTRUI','ROLES'),
(53,'2026-07-07 14:35:41.712272','2026-07-07 14:35:41.712272','ACTIF','AFFICHER_AUTRUI','REFERENTIELS'),
(54,'2026-07-07 14:35:41.720275','2026-07-07 14:35:41.720275','ACTIF','AFFICHER_AUTRUI','VISITES'),
(55,'2026-07-07 14:35:41.731663','2026-07-07 14:35:41.731663','ACTIF','AFFICHER_AUTRUI','CONFIGURATION');
/*!40000 ALTER TABLE `permissions` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `postes`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `postes` WRITE;
/*!40000 ALTER TABLE `postes` DISABLE KEYS */;
INSERT INTO `postes` VALUES
(1,'2026-07-06 15:04:48.375952','2026-07-06 15:04:48.375952','ACTIF','DIR-RH',NULL,'Directeur RH',1),
(2,'2026-07-06 17:35:16.582487','2026-07-06 17:35:16.582487','ACTIF','RECEPT','Gestion des entr├⌐es et sorties.','Receptionniste',NULL);
/*!40000 ALTER TABLE `postes` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `presences`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `presences` WRITE;
/*!40000 ALTER TABLE `presences` DISABLE KEYS */;
INSERT INTO `presences` VALUES
(1,'2026-07-06 16:30:27.159715','2026-07-06 16:30:43.124979','ACTIF','2026-07-06','16:30:27.148000','16:30:43.107000','RETARD',1,1),
(2,'2026-07-06 18:01:15.524858','2026-07-06 18:01:32.488582','ACTIF','2026-07-06','18:01:15.515000','18:01:32.483000','RETARD',2,1),
(3,'2026-07-07 10:16:47.503210','2026-07-07 15:44:12.423105','ACTIF','2026-07-07','10:16:47.471000','15:44:12.248000','RETARD',1,1),
(4,'2026-07-07 10:16:55.198399','2026-07-07 15:46:05.639857','ACTIF','2026-07-07','10:16:55.191000','15:46:05.636000','RETARD',2,1),
(5,'2026-07-07 15:45:46.226191','2026-07-07 15:45:52.273891','ACTIF','2026-07-07','15:45:46.194000','15:45:52.262000','EN_REGLE',1,1),
(6,'2026-07-07 15:46:01.814810','2026-07-07 15:46:03.237626','ACTIF','2026-07-07','15:46:01.807000','15:46:03.233000','EN_REGLE',1,1),
(7,'2026-07-07 15:46:04.150600','2026-07-07 15:47:04.751417','ACTIF','2026-07-07','15:46:04.138000','15:47:04.747000','EN_REGLE',1,1),
(8,'2026-07-07 15:46:06.480857','2026-07-07 15:46:07.359738','ACTIF','2026-07-07','15:46:06.469000','15:46:07.355000','EN_REGLE',2,1),
(9,'2026-07-07 15:47:08.653541','2026-07-07 15:47:08.653541','ACTIF','2026-07-07','15:47:08.645000',NULL,'EN_REGLE',1,1);
/*!40000 ALTER TABLE `presences` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `role_permissions`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `role_permissions` WRITE;
/*!40000 ALTER TABLE `role_permissions` DISABLE KEYS */;
INSERT INTO `role_permissions` VALUES
(1,1),
(2,1),
(1,2),
(2,2),
(1,3),
(2,3),
(1,4),
(2,4),
(1,5),
(2,5),
(1,6),
(2,6),
(1,7),
(2,7),
(1,8),
(2,8),
(1,9),
(2,9),
(3,9),
(4,9),
(5,9),
(1,10),
(2,10),
(3,10),
(4,10),
(5,10),
(1,11),
(2,11),
(5,11),
(1,12),
(2,12),
(1,13),
(2,13),
(4,13),
(1,14),
(2,14),
(4,14),
(1,15),
(2,15),
(4,15),
(1,16),
(2,16),
(4,16),
(1,17),
(2,17),
(3,17),
(4,17),
(5,17),
(1,18),
(2,18),
(3,18),
(5,18),
(1,19),
(2,19),
(5,19),
(1,20),
(2,20),
(1,21),
(2,21),
(5,21),
(1,22),
(2,22),
(5,22),
(1,23),
(2,23),
(5,23),
(1,24),
(2,24),
(1,25),
(1,26),
(1,27),
(1,28),
(1,29),
(1,30),
(1,31),
(1,32),
(1,33),
(2,33),
(1,34),
(2,34),
(1,35),
(2,35),
(1,36),
(2,36),
(1,37),
(2,37),
(4,37),
(1,38),
(2,38),
(4,38),
(1,39),
(2,39),
(4,39),
(1,40),
(2,40),
(4,40),
(1,41),
(2,41),
(1,42),
(2,42),
(1,43),
(2,43),
(1,44),
(2,44),
(1,45),
(2,45),
(1,46),
(2,46),
(1,47),
(2,47),
(1,48),
(2,48),
(1,49),
(2,49),
(1,50),
(1,51),
(1,52),
(1,53),
(1,54),
(1,55);
/*!40000 ALTER TABLE `role_permissions` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `roles`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES
(1,'2026-07-06 15:04:48.172537','2026-07-07 14:35:41.812513','ACTIF','ADMINISTRATEUR','Acc├¿s total ├á toutes les fonctionnalit├⌐s','Administrateur Syst├¿me',0x01,0x00),
(2,'2026-07-06 15:04:48.309831','2026-07-07 14:35:41.861275','ACTIF','RH','Gestion RH compl├¿te','Ressources Humaines',0x01,0x00),
(3,'2026-07-06 15:04:48.332834','2026-07-07 15:06:38.267762','ACTIF','EMPLOYE','Acc├¿s self-service','Employ├⌐',0x01,0x01),
(4,'2026-07-06 15:04:48.361952','2026-07-07 14:44:22.380987','ACTIF','RECEPTION','Pointage et visites','R├⌐ception',0x01,0x00),
(5,'2026-07-07 10:24:36.537343','2026-07-07 10:24:36.537343','ACTIF','COMTABLE','Gestion des bulletins et cotisations CNSS','Comptable Paie',0x00,0x00);
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `utilisateur_roles`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `utilisateur_roles` WRITE;
/*!40000 ALTER TABLE `utilisateur_roles` DISABLE KEYS */;
INSERT INTO `utilisateur_roles` VALUES
(1,1),
(2,4);
/*!40000 ALTER TABLE `utilisateur_roles` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `utilisateurs`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `utilisateurs` WRITE;
/*!40000 ALTER TABLE `utilisateurs` DISABLE KEYS */;
INSERT INTO `utilisateurs` VALUES
(1,'2026-07-06 15:04:48.510330','2026-07-06 19:20:10.782214','ACTIF',0x01,0x01,'admin@minerva.group','$2a$10$Ez2uYBL1ldo20C28iUkWieSECGERitig8u7ehCyd4zvM8FfGKqI6W',1),
(2,'2026-07-07 14:10:38.277071','2026-07-07 16:17:01.666246','ACTIF',0x01,0x01,'diabatekaba198@gmail.com','$2a$10$8uLBok/M1xK7wLGl/YEBZujd1BdMzD4.NqUEf0cWBUl8rgvEe9B5y',2);
/*!40000 ALTER TABLE `utilisateurs` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `visites`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `visites` WRITE;
/*!40000 ALTER TABLE `visites` DISABLE KEYS */;
INSERT INTO `visites` VALUES
(1,'2026-07-06 20:01:51.692518','2026-07-06 20:01:51.691510','2026-07-06 20:02:38.536665','Entretien','TERMINEE',1,1);
/*!40000 ALTER TABLE `visites` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
-- Dumping data for table `visiteurs`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `visiteurs` WRITE;
/*!40000 ALTER TABLE `visiteurs` DISABLE KEYS */;
INSERT INTO `visiteurs` VALUES
(1,'+224612561565','2026-07-06 18:22:24.683806','Orange Guin├⌐e','DIALLO','Mamadou','ACTIF');
/*!40000 ALTER TABLE `visiteurs` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

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
