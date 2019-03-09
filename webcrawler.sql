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
INSERT INTO `current` VALUES (116,'19','done','done','active','inactive',4917,0),(117,'30','done','done','done','done',347,0);
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `extraConf`
--

LOCK TABLES `extraConf` WRITE;
/*!40000 ALTER TABLE `extraConf` DISABLE KEYS */;
INSERT INTO `extraConf` VALUES (1,117,'','','','','','',5,3000,'UTF-8',0),(2,97,'','','','','','',5,3000,'UTF-8',0);
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
) ENGINE=InnoDB AUTO_INCREMENT=6542 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `status`
--

LOCK TABLES `status` WRITE;
/*!40000 ALTER TABLE `status` DISABLE KEYS */;
INSERT INTO `status` VALUES (117,6444,'1','info',0,42),(117,6445,'1','query',0,2),(117,6446,'2','info',0,5),(117,6447,'2','query',0,1),(117,6448,'3','info',0,0),(117,6449,'3','query',0,0),(117,6450,'4','info',0,0),(117,6451,'4','query',0,0),(117,6452,'5','info',0,0),(117,6453,'5','query',0,0),(117,6454,'6','info',0,4),(117,6455,'6','query',0,1),(117,6456,'7','info',0,132),(117,6457,'7','query',0,15),(117,6458,'8','info',0,39),(117,6459,'8','query',0,7),(117,6460,'9','info',0,3),(117,6461,'9','query',0,1),(117,6462,'10','info',0,77),(117,6463,'10','query',0,12),(117,6464,'11','info',0,4),(117,6465,'11','query',0,1),(117,6466,'12','info',0,0),(117,6467,'12','query',0,1),(117,6468,'13','info',0,0),(117,6469,'13','query',0,0),(117,6470,'14','info',0,0),(117,6471,'14','query',0,0),(117,6472,'15','info',0,0),(117,6473,'15','query',0,0),(117,6474,'16','info',0,0),(117,6475,'16','query',0,0),(117,6476,'17','info',0,0),(117,6477,'17','query',0,0),(117,6478,'18','info',0,0),(117,6479,'18','query',0,0),(117,6480,'19','info',0,0),(117,6481,'19','query',0,0),(117,6482,'20','info',0,36),(117,6483,'20','query',0,15),(117,6484,'21','info',0,0),(117,6485,'21','query',0,0),(117,6486,'22','info',0,1),(117,6487,'22','query',0,1),(117,6488,'23','info',0,0),(117,6489,'23','query',0,0),(116,6490,'1','info',0,352),(116,6491,'1','query',0,24),(116,6492,'2','info',0,585),(116,6493,'2','query',0,52),(116,6494,'3','info',0,0),(116,6495,'3','query',0,0),(116,6496,'4','info',0,147),(116,6497,'4','query',0,12),(116,6498,'5','info',0,40),(116,6499,'5','query',0,3),(116,6500,'6','info',0,39),(116,6501,'6','query',0,3),(116,6502,'7','info',0,41),(116,6503,'7','query',0,3),(116,6504,'8','info',0,1070),(116,6505,'8','query',0,78),(116,6506,'9','info',0,0),(116,6507,'9','query',0,0),(116,6508,'10','info',0,0),(116,6509,'10','query',0,15),(116,6510,'11','info',0,1403),(116,6511,'11','query',0,107),(116,6512,'12','info',0,516),(116,6513,'12','query',0,39),(116,6514,'13','info',0,11),(116,6515,'13','query',0,1),(116,6516,'14','info',0,0),(116,6517,'14','query',0,0),(116,6518,'15','info',3,30),(116,6519,'15','query',0,3),(116,6520,'16','info',0,0),(116,6521,'16','query',0,0),(116,6522,'17','info',2,533),(116,6523,'17','query',0,37),(116,6524,'18','info',0,150),(116,6525,'18','query',0,11),(116,6526,'19','info',0,0),(116,6527,'19','query',0,0),(117,6528,'24','info',0,0),(117,6529,'24','query',0,1),(117,6530,'25','info',0,0),(117,6531,'25','query',0,1),(117,6532,'26','info',0,0),(117,6533,'26','query',0,0),(117,6534,'27','info',0,1),(117,6535,'27','query',0,1),(117,6536,'28','info',0,3),(117,6537,'28','query',0,3),(117,6538,'29','info',0,0),(117,6539,'29','query',0,0),(117,6540,'30','info',0,0),(117,6541,'30','query',0,0);
/*!40000 ALTER TABLE `status` ENABLE KEYS */;
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
  `prefix` varchar(1024) NOT NULL DEFAULT '',
  `paramQuery` varchar(256) NOT NULL DEFAULT '',
  `paramPage` varchar(256) NOT NULL DEFAULT '',
  `startPageNum` varchar(256) NOT NULL DEFAULT '',
  `paramList` varchar(1024) NOT NULL DEFAULT '',
  `paramValueList` varchar(1024) DEFAULT '',
  `runningMode` varchar(256) NOT NULL DEFAULT '',
  `workFile` varchar(1024) NOT NULL DEFAULT '',
  `driver` tinyint(4) NOT NULL DEFAULT '0',
  `usable` tinyint(4) NOT NULL DEFAULT '0',
  `createtime` varchar(256) NOT NULL DEFAULT '',
  `creator` varchar(256) NOT NULL DEFAULT '' COMMENT 'task creator',
  PRIMARY KEY (`webId`)
) ENGINE=InnoDB AUTO_INCREMENT=118 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `website`
--

LOCK TABLES `website` WRITE;
/*!40000 ALTER TABLE `website` DISABLE KEYS */;
INSERT INTO `website` VALUES (97,'搜狗Test爬取','http://121.194.104.120:8080/SogouT/Search.jsp','http://121.194.104.120:8080/SogouT/Search?','keyword','curpage','1,1','Submit','Search','unstructed','F:/crawler/sougou/',0,1,'2018-09-08 13:25:45',''),(116,'诏安县政府官网','http://www.zhaoan.gov.cn/cms/html/zaxrmzf/index.html','http://www.zhaoan.gov.cn/cms/siteresource/search.shtml?','key','page','1,1','searchSiteId,siteId,pageName','60427348114130001,60427348114130001,quickSiteSearch','unstructed','/Users/cwc/Desktop/tencent/dc/zhaoan',0,1,'2019-03-01 20:04:04',''),(117,'中国人工智能学会','http://www.caai.cn/index.php','http://www.caai.cn/index.php','title','p','1,1','s','/home/article/search.html','unstructed','/Users/cwc/Desktop/tencent/data-crawling/CAAI',0,1,'2019-03-04 17:59:57','');
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

-- Dump completed on 2019-03-09 15:13:31
