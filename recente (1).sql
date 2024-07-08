-- MySQL dump 10.13  Distrib 8.0.32, for Linux (x86_64)
--
-- Host: 127.0.0.1    Database: tiwproject
-- ------------------------------------------------------
-- Server version	8.0.33-0ubuntu0.22.04.2

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
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `article`
--

LOCK TABLES `article` WRITE;
/*!40000 ALTER TABLE `article` DISABLE KEYS */;
INSERT INTO `article` VALUES (56,'Felpa Adidas','Una felpa nera Adidas da uomo, taglia S',50,'felpa adidas nera abbigliamento',1,NULL,'local_filename_20230627215352762.png'),(57,'Occhiali da sole','Degli stravaganti occhiali da sole',20,'occhiali sole abbigliamento',1,NULL,'local_filename_20230627215514556.png'),(58,'Mocassini','Mocassini eleganti numero 42',50,'mocassini scarpe abbigliamento',1,NULL,'local_filename_20230627220136771.png'),(59,'Zaino da viaggio','Un capiente zaino da viaggio grigio',40,'zaino viaggi',7,NULL,'local_filename_20230627220632055.png'),(60,'Valigia da viaggio','Valigia da viaggio nera',50,'valigia viaggi',7,NULL,'local_filename_20230627220723657.png'),(61,'Fiat panda','Una fiat panda verde degli anni 90',5000,'fiat panda verde',7,NULL,'local_filename_20230627220858069.png'),(62,'Bicicletta','Una bicicletta usata',75.31,'bicicletta',7,NULL,'local_filename_20230627220947120.png'),(63,'Motorino','Un motorino a benzina',5200,'motorino moto',7,NULL,'local_filename_20230627221138703.png'),(64,'Vaso da fiori','Un vaso da fiori fatto di vetro',25,'vaso fiori vetro',3,NULL,'local_filename_20230627221404924.png'),(65,'Quadro','Un quadro raffigurante un paesaggio naturalistico',24.55,'quadro arte',3,NULL,'local_filename_20230627221512046.png'),(66,'Televisore','Un televisore da 40 pollici',100.99,'televisore tv',3,NULL,'local_filename_20230627221628637.png'),(67,'Lampada','Una lampada per scrivania da ',16.54,'lampada scrivania',3,NULL,'local_filename_20230627221809136.png'),(68,'Borraccia','Una borraccia termina da viaggio',15.5,'borraccia bottaglia termica viaggi',3,NULL,'local_filename_20230627222113787.png'),(69,'Armadio','Un armadio di legno per camera da letto',140.42,'armadio mobile arredamento',1,NULL,'local_filename_20230627222215396.png'),(70,'Chitarra','Una chitarra acustica ',80,'chitarra musica strumento',1,NULL,'local_filename_20230627222307839.png');
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
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auction`
--

LOCK TABLES `auction` WRITE;
/*!40000 ALTER TABLE `auction` DISABLE KEYS */;
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

-- Dump completed on 2023-06-27 22:32:24
