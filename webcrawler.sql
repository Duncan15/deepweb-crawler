-- MySQL dump 10.13  Distrib 8.0.13, for osx10.12 (x86_64)
--
-- Host: localhost    Database: webcrawler
-- ------------------------------------------------------
-- Server version	8.0.13

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
 SET NAMES utf8mb4 ;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `apiBaseConf`
--

DROP TABLE IF EXISTS `apiBaseConf`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `apiBaseConf` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `webId` bigint(20) NOT NULL DEFAULT '0',
  `prefix` varchar(1024) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `apiBaseConf`
--

LOCK TABLES `apiBaseConf` WRITE;
/*!40000 ALTER TABLE `apiBaseConf` DISABLE KEYS */;
/*!40000 ALTER TABLE `apiBaseConf` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `current`
--

DROP TABLE IF EXISTS `current`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `current` (
  `webId` int(20) unsigned NOT NULL,
  `round` varchar(256) NOT NULL DEFAULT '0',
  `M1status` varchar(256) NOT NULL DEFAULT 'inactive',
  `M2status` varchar(256) NOT NULL DEFAULT 'inactive',
  `M3status` varchar(256) NOT NULL DEFAULT 'inactive',
  `M4status` varchar(256) NOT NULL DEFAULT 'inactive',
  `SampleData_sum` int(11) NOT NULL DEFAULT '0',
  `run` bigint(20) NOT NULL DEFAULT '0',
  KEY `round` (`round`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `current`
--

LOCK TABLES `current` WRITE;
/*!40000 ALTER TABLE `current` DISABLE KEYS */;
/*!40000 ALTER TABLE `current` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `extraConf`
--

DROP TABLE IF EXISTS `extraConf`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `extraConf` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `webId` bigint(20) NOT NULL DEFAULT '0',
  `userNameXpath` varchar(1024) NOT NULL DEFAULT '',
  `passwordXpath` varchar(1024) DEFAULT '',
  `userName` varchar(1024) NOT NULL DEFAULT '',
  `password` varchar(1024) NOT NULL DEFAULT '',
  `loginUrl` varchar(1024) NOT NULL DEFAULT '',
  `submitXpath` varchar(1024) NOT NULL DEFAULT '',
  `threadNum` int(11) NOT NULL DEFAULT '5',
  `timeout` bigint(20) NOT NULL DEFAULT '3000',
  `charset` varchar(256) NOT NULL DEFAULT '',
  `databaseSize` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `extraConf`
--

LOCK TABLES `extraConf` WRITE;
/*!40000 ALTER TABLE `extraConf` DISABLE KEYS */;
INSERT INTO `extraConf` VALUES (3,116,'','','','','','',5,3000,'utf-8',0);
/*!40000 ALTER TABLE `extraConf` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pattern`
--

DROP TABLE IF EXISTS `pattern`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `pattern` (
  `webId` int(20) unsigned NOT NULL DEFAULT '0',
  `patternName` varchar(255) NOT NULL DEFAULT 'fulltext',
  `xpath` varchar(1024) NOT NULL DEFAULT '',
  `id` int(20) unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pattern`
--

LOCK TABLES `pattern` WRITE;
/*!40000 ALTER TABLE `pattern` DISABLE KEYS */;
INSERT INTO `pattern` VALUES (97,'fulltext','//body',11),(97,'half','//half',26);
/*!40000 ALTER TABLE `pattern` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `requesttable`
--

DROP TABLE IF EXISTS `requesttable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `requesttable` (
  `requestID` bigint(20) NOT NULL AUTO_INCREMENT,
  `requestName` varchar(1024) NOT NULL DEFAULT '',
  `requestDesc` varchar(2048) NOT NULL DEFAULT '',
  `createdTime` varchar(256) NOT NULL DEFAULT '',
  PRIMARY KEY (`requestID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `requesttable`
--

LOCK TABLES `requesttable` WRITE;
/*!40000 ALTER TABLE `requesttable` DISABLE KEYS */;
INSERT INTO `requesttable` VALUES (1,'搜狗中文2','搜狗中文爬取','2019-02-27 19:57:23');
/*!40000 ALTER TABLE `requesttable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `status`
--

DROP TABLE IF EXISTS `status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `status` (
  `webId` int(20) unsigned NOT NULL,
  `statusId` int(20) unsigned NOT NULL AUTO_INCREMENT,
  `round` varchar(256) NOT NULL DEFAULT '0',
  `type` varchar(24) NOT NULL DEFAULT 'query',
  `fLinkNum` int(11) unsigned NOT NULL DEFAULT '0',
  `sLinkNum` int(11) unsigned NOT NULL DEFAULT '0',
  KEY `round` (`round`),
  KEY `statusId` (`statusId`)
) ENGINE=InnoDB AUTO_INCREMENT=6552 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `status`
--

LOCK TABLES `status` WRITE;
/*!40000 ALTER TABLE `status` DISABLE KEYS */;
/*!40000 ALTER TABLE `status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `UrlBaseConf`
--

DROP TABLE IF EXISTS `UrlBaseConf`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `UrlBaseConf` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `webId` bigint(20) NOT NULL DEFAULT '0',
  `prefix` varchar(1024) NOT NULL DEFAULT '',
  `paramQuery` varchar(256) NOT NULL DEFAULT '',
  `paramPage` varchar(256) NOT NULL DEFAULT '',
  `startPageNum` varchar(256) NOT NULL DEFAULT '',
  `paramList` varchar(1024) NOT NULL DEFAULT '',
  `paramValueList` varchar(256) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `UrlBaseConf`
--

LOCK TABLES `UrlBaseConf` WRITE;
/*!40000 ALTER TABLE `UrlBaseConf` DISABLE KEYS */;
INSERT INTO `UrlBaseConf` VALUES (1,116,'http://www.zhaoan.gov.cn/cms/siteresource/search.shtml?','key','page','1,1','searchSiteId,siteId,pageName','60427348114130001,60427348114130001,quickSiteSearch'),(2,117,'http://www.caai.cn/index.php','title','p','1,1','s','/home/article/search.html');
/*!40000 ALTER TABLE `UrlBaseConf` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `website`
--

DROP TABLE IF EXISTS `website`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `website` (
  `webId` int(20) unsigned NOT NULL AUTO_INCREMENT,
  `webName` varchar(256) NOT NULL DEFAULT '',
  `indexUrl` varchar(1024) NOT NULL DEFAULT '',
  `workFile` varchar(1024) NOT NULL DEFAULT '',
  `runningMode` varchar(256) NOT NULL DEFAULT '',
  `driver` tinyint(4) NOT NULL DEFAULT '0',
  `usable` tinyint(4) NOT NULL DEFAULT '0',
  `createtime` varchar(256) NOT NULL DEFAULT '',
  `creator` varchar(256) NOT NULL DEFAULT '' COMMENT 'task creator',
  `base` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0:url based\n1:api based',
  PRIMARY KEY (`webId`)
) ENGINE=InnoDB AUTO_INCREMENT=118 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `website`
--

LOCK TABLES `website` WRITE;
/*!40000 ALTER TABLE `website` DISABLE KEYS */;
INSERT INTO `website` VALUES (116,'诏安县政府官网','http://www.zhaoan.gov.cn/cms/html/zaxrmzf/index.html','/Users/cwc/Desktop/tencent/dc/zhaoan','unstructed',0,1,'2019-03-01 20:04:04','',0),(117,'中国人工智能学会','http://www.caai.cn/index.php','/Users/cwc/Desktop/tencent/data-crawling/CAAI','unstructed',0,1,'2019-03-04 17:59:57','',0);
/*!40000 ALTER TABLE `website` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-03-10 16:03:02
