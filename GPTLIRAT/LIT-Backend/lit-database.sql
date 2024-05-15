-- MariaDB dump 10.17  Distrib 10.5.5-MariaDB, for Linux (x86_64)
--
-- Host: localhost    Database: lit
-- ------------------------------------------------------
-- Server version	10.5.5-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `lit`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `lit` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;

USE `lit`;

--
-- Table structure for table `script`
--

DROP TABLE IF EXISTS `script`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `script` (
  `script_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `device_udid` varchar(100) COLLATE utf8_bin NOT NULL,
  `name` varchar(100) COLLATE utf8_bin DEFAULT NULL,
  `current_step` int(11) NOT NULL DEFAULT 1,
  `dirs_location` varchar(3000) COLLATE utf8_bin DEFAULT NULL,
  `create_time_millis` datetime DEFAULT NULL,
  `script_url` varchar(200) COLLATE utf8_bin DEFAULT NULL,
  `script_step_list` varchar(5000) COLLATE utf8_bin DEFAULT '',
  `app_id` varchar(100) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`script_id`)
) ENGINE=InnoDB AUTO_INCREMENT=176 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `script`
--

LOCK TABLES `script` WRITE;
/*!40000 ALTER TABLE `script` DISABLE KEYS */;
INSERT INTO `script` VALUES (22,'WBUBB18923510113','WBUBB18923510113_22',15,'scripts/22/1,scripts/22/2,scripts/22/3,scripts/22/4,scripts/22/5,scripts/22/6,scripts/22/7,scripts/22/8,scripts/22/9,scripts/22/10,scripts/22/11,scripts/22/12,scripts/22/13,scripts/22/14,scripts/22/15','2019-12-20 11:59:46','scripts/22.zip',NULL,'Wikipedia'),(23,'WBUBB18923510113','WBUBB18923510113_23',15,'scripts/23/1,scripts/23/2,scripts/23/3,scripts/23/4,scripts/23/5,scripts/23/6,scripts/23/7,scripts/23/8,scripts/23/9,scripts/23/10,scripts/23/11,scripts/23/12,scripts/23/13,scripts/23/14,scripts/23/15','2019-12-20 12:00:35','scripts/23.zip',NULL,'Wikipedia'),(25,'WBUBB18923510113','WBUBB18923510113_25',15,'scripts/25/1,scripts/25/2,scripts/25/3,scripts/25/4,scripts/25/5,scripts/25/6,scripts/25/7,scripts/25/8,scripts/25/9,scripts/25/10,scripts/25/11,scripts/25/12,scripts/25/13,scripts/25/14,scripts/25/15','2019-12-20 12:06:55','scripts/25.zip',NULL,'Wikipedia'),(35,'WBUBB18923510113','WBUBB18923510113_35',15,'scripts/35/1,scripts/35/2,scripts/35/3,scripts/35/4,scripts/35/5,scripts/35/6,scripts/35/7,scripts/35/8,scripts/35/9,scripts/35/10,scripts/35/11,scripts/35/12,scripts/35/13,scripts/35/14,scripts/35/15','2019-12-20 15:26:42','scripts/35.zip',NULL,'Wikipedia');
/*!40000 ALTER TABLE `script` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-09-03 20:39:11
