-- MySQL dump 10.13  Distrib 8.0.15, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: dad
-- ------------------------------------------------------
-- Server version	8.0.15

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
 SET NAMES utf8 ;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `buzzers`
--

DROP TABLE IF EXISTS `buzzers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `buzzers` (
  `idBuzzer` int(11) NOT NULL AUTO_INCREMENT,
  `idPIR` int(11) NOT NULL,
  `tempBuzz` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idBuzzer`),
  KEY `idPIR idPIR_idx` (`idPIR`),
  CONSTRAINT `idPIR idPIR` FOREIGN KEY (`idPIR`) REFERENCES `sensorespir` (`idPIR`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `buzzers`
--

LOCK TABLES `buzzers` WRITE;
/*!40000 ALTER TABLE `buzzers` DISABLE KEYS */;
INSERT INTO `buzzers` VALUES (1,1,1554391765),(2,2,1556838841);
/*!40000 ALTER TABLE `buzzers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `finalescarrera`
--

DROP TABLE IF EXISTS `finalescarrera`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `finalescarrera` (
  `idFC` int(11) NOT NULL AUTO_INCREMENT,
  `idServo` int(11) DEFAULT NULL,
  `cerrado` tinyint(1) NOT NULL,
  `tempFC` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idFC`),
  UNIQUE KEY `idServo_UNIQUE` (`idServo`),
  KEY `idServo idServo_idx` (`idServo`),
  CONSTRAINT `idServo idServo` FOREIGN KEY (`idServo`) REFERENCES `servos` (`idServo`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `finalescarrera`
--

LOCK TABLES `finalescarrera` WRITE;
/*!40000 ALTER TABLE `finalescarrera` DISABLE KEYS */;
INSERT INTO `finalescarrera` VALUES (1,1,1,1554391765);
/*!40000 ALTER TABLE `finalescarrera` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sensorespir`
--

DROP TABLE IF EXISTS `sensorespir`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `sensorespir` (
  `idPIR` int(11) NOT NULL AUTO_INCREMENT,
  `nombrePIR` varchar(45) DEFAULT NULL,
  `tempPir` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idPIR`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sensorespir`
--

LOCK TABLES `sensorespir` WRITE;
/*!40000 ALTER TABLE `sensorespir` DISABLE KEYS */;
INSERT INTO `sensorespir` VALUES (1,'puerta principal',1554391765),(2,'puerta principal',1556838841);
/*!40000 ALTER TABLE `sensorespir` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `servos`
--

DROP TABLE IF EXISTS `servos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `servos` (
  `idServo` int(11) NOT NULL AUTO_INCREMENT,
  `idUsuario` int(11) NOT NULL,
  `nombreServo` varchar(45) DEFAULT NULL,
  `tempServo` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`idServo`),
  KEY `idUsuario idUsuario_idx` (`idUsuario`),
  CONSTRAINT `idUsuario idUsuario` FOREIGN KEY (`idUsuario`) REFERENCES `usuarios` (`idUsuario`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `servos`
--

LOCK TABLES `servos` WRITE;
/*!40000 ALTER TABLE `servos` DISABLE KEYS */;
INSERT INTO `servos` VALUES (1,1,'puerta',1554391765);
/*!40000 ALTER TABLE `servos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tecladosnumericos`
--

DROP TABLE IF EXISTS `tecladosnumericos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `tecladosnumericos` (
  `idTN` int(11) NOT NULL AUTO_INCREMENT,
  `idUsuario` int(11) NOT NULL,
  `passTN` int(30) NOT NULL,
  `tempTN` bigint(20) DEFAULT NULL,
  `cont` int(11) DEFAULT NULL,
  `acierto` tinyint(1) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`idTN`),
  UNIQUE KEY `idTN_UNIQUE` (`idTN`),
  KEY `idUsuarioTN idUsuario_idx` (`idUsuario`),
  CONSTRAINT `idUsuarioTN idUsuario` FOREIGN KEY (`idUsuario`) REFERENCES `usuarios` (`idUsuario`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tecladosnumericos`
--

LOCK TABLES `tecladosnumericos` WRITE;
/*!40000 ALTER TABLE `tecladosnumericos` DISABLE KEYS */;
INSERT INTO `tecladosnumericos` VALUES (1,1,1111,1554391765,1,0);
/*!40000 ALTER TABLE `tecladosnumericos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `usuarios` (
  `idUsuario` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(45) DEFAULT NULL,
  `passUsuario` int(30) NOT NULL,
  `dentro` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`idUsuario`),
  UNIQUE KEY `pass_UNIQUE` (`passUsuario`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` VALUES (1,'alberto',11413,0),(2,'simon',232112,1);
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-05-06 22:02:32
