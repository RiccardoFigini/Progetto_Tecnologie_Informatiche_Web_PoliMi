-- MySQL dump 10.13  Distrib 8.0.32, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: newschema
-- ------------------------------------------------------
-- Server version	8.0.32

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
  `description` varchar(45) NOT NULL,
  `minimumPrice` float NOT NULL,
  `keyWord` varchar(45) NOT NULL,
  `creatorId` int NOT NULL,
  `winningAuctionId` int DEFAULT NULL,
  PRIMARY KEY (`code`),
  KEY `fk_article_1_idx` (`winningAuctionId`),
  KEY `fk_article_2_idx` (`creatorId`),
  CONSTRAINT `fk_article_1` FOREIGN KEY (`winningAuctionId`) REFERENCES `auction` (`id`),
  CONSTRAINT `fk_article_2` FOREIGN KEY (`creatorId`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `article`
--

LOCK TABLES `article` WRITE;
/*!40000 ALTER TABLE `article` DISABLE KEYS */;
INSERT INTO `article` VALUES (1,'Sedia','Sedia in vinimi',10,'Arredamento',1,NULL),(2,'Sedia','Sedia in legno',40,'Arredamento',1,NULL),(3,'Tavolo ','tavolo',50,'Arredamento',2,NULL),(4,'Tavolo','Tavolo da esterno',1000,'Arredamente',1,NULL),(5,'iPhone6','Cellulare rotto e non funzionantw',300,'Elettronica',2,NULL),(6,'Orologio','Orologio grosso d uscita',100,'Elettronica',3,NULL),(7,'Porta tovaglioli','Porta tovaglioli da cucina ',34,'Cucina',4,NULL),(8,'Coltello','Coltello molto tagliente',54,'Cucina',4,NULL),(9,'Forchetta','Set di forchette da 10',100,'Cucina',4,NULL),(10,'Tovaglia','Tovaglia di sintetico',67,'Cucina',4,NULL),(11,'Fogli','Fogli di carta spessi',10,'Carteria',5,NULL),(12,'Computer','Computer Acer usato',700,'Elettronica',6,NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auction`
--

LOCK TABLES `auction` WRITE;
/*!40000 ALTER TABLE `auction` DISABLE KEYS */;
INSERT INTO `auction` VALUES (1,100,30,'2022-01-01 00:00:00',1,'2023-10-10 00:00:00',1),(2,300,10,'2000-01-01 00:00:00',4,'2024-01-01 00:00:00',0),(3,50,5,'2000-02-02 12:23:34',5,'2002-01-01 09:12:12',0),(4,500,22,'2010-04-21 09:12:43',3,'2055-01-01 12:12:12',0),(23,100,34,'2000-02-02 12:23:34',2,'2002-01-01 09:12:12',0);
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
INSERT INTO `includes` VALUES (1,1),(7,2),(8,2),(9,2),(11,3),(6,4),(5,23);
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
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `offer`
--

LOCK TABLES `offer` WRITE;
/*!40000 ALTER TABLE `offer` DISABLE KEYS */;
INSERT INTO `offer` VALUES (1,1,1,300,'2022-02-02 00:00:00'),(2,2,1,400,'2022-03-03 00:00:00'),(3,3,1,500,'2022-04-03 00:00:00'),(4,7,2,500,'2001-12-11 00:12:45'),(5,4,3,50,'2001-05-05 00:00:00'),(6,2,1,1000,'2022-05-03 00:00:00'),(7,4,1,4658,'2023-04-24 17:12:47'),(18,7,2,1000,'2023-04-24 18:05:07'),(19,7,2,19000,'2023-04-24 20:34:41'),(20,7,2,300000,'2023-04-26 10:50:31'),(21,7,23,100000,'2023-04-26 11:22:41'),(22,7,4,1000,'2023-04-27 09:23:41');
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
INSERT INTO `user` VALUES (1,'FraLo','passwordFrancesco','Francesco','Lo Mastro','Nova Milanese'),(2,'AleFo','passwordAlessandro','Alessandro','Fornara','Busnago'),(3,'AndFe','passwordAndrea','Andrea','Ferrini','Pescara'),(4,'SamGa','passwordSamuele','Samuele','Galli','Busto Arsizio'),(5,'DonFio','passwordDonato','Donato','Fiore','Foggia'),(6,'EdoGen','passwordEdoardo','Edoardo','Gennaretti','Pescara'),(7,'RicFig','p','Riccardo','Figini','Cesano');
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

-- Dump completed on 2023-04-30 16:51:59
