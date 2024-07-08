-- MySQL dump 10.13  Distrib 8.0.29, for Linux (x86_64)
--
-- Host: 127.0.0.1    Database: tiwproject
-- ------------------------------------------------------
-- Server version	8.0.32-0ubuntu0.22.04.2

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `article`
--

DROP TABLE IF EXISTS `article`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article` (
  `code` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `description` varchar(450) NOT NULL,
  `minimumPrice` float NOT NULL,
  `keyWord` varchar(45) NOT NULL,
  `creatorId` int NOT NULL,
  `winningAuctionId` int DEFAULT NULL,
  `imagePath` varchar(255) CHARACTER SET armscii8 COLLATE armscii8_general_ci DEFAULT NULL,
  PRIMARY KEY (`code`),
  KEY `fk_article_1_idx` (`winningAuctionId`),
  KEY `fk_article_2_idx` (`creatorId`),
  CONSTRAINT `fk_article_1` FOREIGN KEY (`winningAuctionId`) REFERENCES `auction` (`id`),
  CONSTRAINT `fk_article_2` FOREIGN KEY (`creatorId`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `article`
--

LOCK TABLES `article` WRITE;
/*!40000 ALTER TABLE `article` DISABLE KEYS */;
INSERT INTO `article` VALUES (33,'Valigia da viaggio','Una comoda valigia da viaggio nera, di dimensioni medie e classica.\r\nPerfetta per i viaggi di piacere.',46.352,'borsa valigia viaggio',1,NULL,'local_filename_20230505225741406'),(35,'Zaino da viaggio','Un comodo e capiente zaino da viaggio grigio.',95.15,'zaino viaggio borsa',1,NULL,'local_filename_20230505232703976'),(36,'Televisore','Un televisore HD con ottima risoluzione.\r\nDimensione 26 pollici',150.99,'televisore elettronica hd',1,NULL,'local_filename_20230505233234751'),(37,'Chitarra acustica','Uno strumento musicale a corda fatto a mano.',222.3,'strumento musicale chitarra',1,NULL,'local_filename_20230505234404467'),(38,'Borraccia','Una borraccia di alluminio',15,'borraccia bottiglia alluminio',1,NULL,'local_filename_20230505234707767'),(39,'Bicicletta','Una bicicletta per adulti',150.1,'bicicletta bici',1,NULL,'local_filename_20230505234852403'),(40,'Martello','Un martello classico di 1kg lungo 45cm',15,'martello utensile',7,NULL,'local_filename_20230505235311025'),(41,'Quadro','Un quadro raffigurante un paesaggio naturalistico.\r\nDimensioni 70cm x 50cm',15,'quadro tela cornice',7,NULL,'local_filename_20230505235443904'),(42,'Felpa Adidas','Una felpa Adidas nera taglia M',68.99,'felpa adidas',7,NULL,'local_filename_20230505235548109'),(43,'Vaso di fiori','Un vaso di fiori di vetro per fiori',10.99,'vaso vetro',7,NULL,'local_filename_20230505235654213'),(44,'Fornello da campo','Un comodo fornello da campo a gas, per viaggi',55,'fornello da campo a metano',7,NULL,'local_filename_20230505235819412'),(45,'Cassa Bluetooth JBL','Una cassa Bluetooth JBL resitente all\'acqua',95,'cassa bluetooth jbl',7,NULL,'local_filename_20230505235947232'),(46,'Computer Lenovo','Un computer laptop Lenovo con processore Intel',150,'laptop computer pc lenovo',3,NULL,'local_filename_20230506000520386'),(47,'Sedia','Una sedia di legno',10,'sedia arredamento',3,NULL,'local_filename_20230506000555830'),(48,'Mocassini','Un paio di mocassini ricamati eleganti, taglia 42',35,'mocassini scarpe abbigliamento ',3,NULL,'local_filename_20230506000720328'),(49,'Tastiera computer','Una tastiera per computer, con attacco USB',14,'computer tastiera',3,NULL,'local_filename_20230506000830486'),(50,'Motorino','Un motorino a benzina del 2010',1110,'motorino benzina',3,NULL,'local_filename_20230506000953853'),(51,'Lampada da tavolo','Una lampada da tavolo per studio',30,'lampada da tavolo arredamento',3,NULL,'local_filename_20230506001106090'),(52,'Fiat Panda','Una Fiat Panda usata del 2005',2000,'automobile fiat panda',3,NULL,'local_filename_20230506001213234'),(53,'Pallone da calcio','Un pallone da calcio',40,'pallone calcio ',3,NULL,'local_filename_20230506001333085'),(54,'Armadio','Un armadio di legno per camera matrimoniale',200,'armadio mobile arredamento',3,NULL,'local_filename_20230506001428713'),(55,'Occhiali da sole','Degli occhiali da sole stravaganti',5,'occhiali da sole abbigliamento',1,NULL,'local_filename_20230506001756925');
/*!40000 ALTER TABLE `article` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auction`
--

DROP TABLE IF EXISTS `auction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auction` (
  `id` int NOT NULL AUTO_INCREMENT,
  `startPrice` float NOT NULL,
  `minimumRise` int NOT NULL,
  `startDate` datetime NOT NULL,
  `userId` int NOT NULL,
  `endDate` datetime NOT NULL,
  `isClosed` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_auction_1_idx` (`userId`),
  CONSTRAINT `fk_auction_1` FOREIGN KEY (`userId`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auction`
--

LOCK TABLES `auction` WRITE;
/*!40000 ALTER TABLE `auction` DISABLE KEYS */;
INSERT INTO `auction` VALUES (37,100.15,5,'2023-05-06 14:48:00',1,'2023-05-06 15:48:00',0),(38,165.1,231,'2023-05-06 14:52:00',1,'2023-05-06 14:54:00',0);
/*!40000 ALTER TABLE `auction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `includes`
--

DROP TABLE IF EXISTS `includes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `includes` (
  `articleCode` int NOT NULL,
  `auctionId` int NOT NULL,
  PRIMARY KEY (`articleCode`,`auctionId`),
  KEY `fk_includes_2_idx` (`auctionId`),
  CONSTRAINT `fk_includes_1` FOREIGN KEY (`articleCode`) REFERENCES `article` (`code`),
  CONSTRAINT `fk_includes_2` FOREIGN KEY (`auctionId`) REFERENCES `auction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `includes`
--

LOCK TABLES `includes` WRITE;
/*!40000 ALTER TABLE `includes` DISABLE KEYS */;
INSERT INTO `includes` VALUES (35,37),(55,37),(38,38),(39,38);
/*!40000 ALTER TABLE `includes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `offer`
--

DROP TABLE IF EXISTS `offer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `offer` (
  `auctionOfferId` int NOT NULL AUTO_INCREMENT,
  `clientId` int NOT NULL,
  `auctionId` int NOT NULL,
  `price` float NOT NULL,
  `dateTime` datetime NOT NULL,
  PRIMARY KEY (`auctionOfferId`,`clientId`,`auctionId`),
  KEY `fk_new_table_1_idx` (`clientId`),
  KEY `fk_new_table_2_idx` (`auctionId`),
  CONSTRAINT `fk_new_table_1` FOREIGN KEY (`clientId`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_new_table_2` FOREIGN KEY (`auctionId`) REFERENCES `auction` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `offer`
--

LOCK TABLES `offer` WRITE;
/*!40000 ALTER TABLE `offer` DISABLE KEYS */;
INSERT INTO `offer` VALUES (25,1,38,397,'2023-05-06 12:08:18'),(26,7,38,700,'2023-05-06 12:13:03');
/*!40000 ALTER TABLE `offer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(45) NOT NULL,
  `password` varchar(45) NOT NULL,
  `name` varchar(45) NOT NULL,
  `surname` varchar(45) NOT NULL,
  `address` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username_UNIQUE` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'FraLo','a','Francesco','Lo Mastro','Nova Milanese'),(2,'AleFo','passwordAlessandro','Alessandro','Fornara','Busnago'),(3,'AndFe','a','Andrea','Ferrini','Pescara'),(4,'SamGa','passwordSamuele','Samuele','Galli','Busto Arsizio'),(5,'DonFio','passwordDonato','Donato','Fiore','Foggia'),(6,'EdoGen','passwordEdoardo','Edoardo','Gennaretti','Pescara'),(7,'RicFig','p','Riccardo','Figini','Cesano');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-05-06 13:07:59
