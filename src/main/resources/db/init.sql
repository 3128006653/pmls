-- MySQL dump 10.13  Distrib 8.0.46, for Linux (x86_64)
--
-- Host: localhost    Database: pmls
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `pmls`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `pmls` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `pmls`;

--
-- Table structure for table `t_branch`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_branch` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '网点名称',
  `city` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '所属地市',
  `address` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '详细地址',
  `manager` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '负责人',
  `phone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系电话',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1=启用 0=停用（与删除无关）',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_city` (`city`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='网点表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_branch`
--

LOCK TABLES `t_branch` WRITE;
/*!40000 ALTER TABLE `t_branch` DISABLE KEYS */;
INSERT INTO `t_branch` VALUES (1,'南昌八一大道营业所','南昌市','八一大道 123 号','张伟','0791-88880001',1,0,'2026-09-20 22:27:46','2026-09-20 22:27:46'),(2,'南昌红谷滩营业所','南昌市','红谷滩新区 456 号','李娜','0791-88880002',1,0,'2026-09-20 22:27:46','2026-09-20 22:27:46'),(3,'赣州章贡营业所','赣州市','章贡区 789 号','王强','0797-88880003',1,0,'2026-09-20 22:27:46','2026-09-20 22:27:46');
/*!40000 ALTER TABLE `t_branch` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_inventory_check`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_inventory_check` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `check_no` varchar(40) COLLATE utf8mb4_general_ci NOT NULL COMMENT '盘点单号 PD+yyyyMMdd+序号',
  `branch_id` bigint NOT NULL,
  `status` varchar(20) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/SUBMITTED/AUDITED/VOID',
  `operator_id` bigint DEFAULT NULL,
  `auditor_id` bigint DEFAULT NULL,
  `total_diff` int DEFAULT '0' COMMENT '差异绝对值之和',
  `changed_count` int DEFAULT '0' COMMENT '盘点期间该网点发生的出入库次数',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `audited_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `check_no` (`check_no`),
  KEY `idx_branch` (`branch_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=2102054547738898435 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='盘点单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_inventory_check`
--

LOCK TABLES `t_inventory_check` WRITE;
/*!40000 ALTER TABLE `t_inventory_check` DISABLE KEYS */;
INSERT INTO `t_inventory_check` VALUES (2102054547738898434,'PD20260921231713115',1,'AUDITED',NULL,NULL,10,1,'月度盘点','2026-09-21 23:17:13','2026-09-21 23:17:14');
/*!40000 ALTER TABLE `t_inventory_check` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_inventory_check_item`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_inventory_check_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `check_id` bigint NOT NULL,
  `material_id` bigint NOT NULL,
  `book_qty` int NOT NULL COMMENT '创建时快照的账面数（仅用于展示对比）',
  `actual_qty` int DEFAULT NULL COMMENT '实盘数量',
  `audit_book_qty` int DEFAULT NULL COMMENT '审核时刻的真实账面数',
  `diff_qty` int DEFAULT NULL COMMENT '差异 = 实盘 - 审核时刻账面',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_check_material` (`check_id`,`material_id`),
  KEY `idx_check` (`check_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2102054547994750980 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='盘点明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_inventory_check_item`
--

LOCK TABLES `t_inventory_check_item` WRITE;
/*!40000 ALTER TABLE `t_inventory_check_item` DISABLE KEYS */;
INSERT INTO `t_inventory_check_item` VALUES (2102054547868921857,2102054547738898434,1,500,490,500,-10,'破损'),(2102054547936030721,2102054547738898434,3,60,NULL,NULL,NULL,NULL),(2102054547994750978,2102054547738898434,6,800,NULL,NULL,NULL,NULL),(2102054547994750979,2102054547738898434,8,120,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `t_inventory_check_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_material`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_material` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '物资名称',
  `category_id` bigint DEFAULT NULL COMMENT '分类 ID',
  `spec` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '规格',
  `unit` varchar(20) COLLATE utf8mb4_general_ci DEFAULT '个' COMMENT '个/箱/包/卷',
  `safe_stock` int DEFAULT '0' COMMENT '默认安全库存阈值',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1=启用 0=停用',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=2123320052711034882 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='物资表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_material`
--

LOCK TABLES `t_material` WRITE;
/*!40000 ALTER TABLE `t_material` DISABLE KEYS */;
INSERT INTO `t_material` VALUES (1,'标准信封',1,'5号 229*162mm','个',100,1,0,'2026-09-20 22:27:46','2026-09-20 22:27:46'),(2,'大号信封',1,'7号 229*324mm','个',50,1,0,'2026-09-20 22:27:46','2026-09-20 22:27:46'),(3,'纸箱 L',2,'50*40*40cm','个',30,1,0,'2026-09-20 22:27:46','2026-09-20 22:27:46'),(4,'纸箱 M',2,'40*30*30cm','个',40,1,0,'2026-09-20 22:27:46','2026-09-20 22:27:46'),(5,'胶带',2,'48mm*100m','卷',20,1,0,'2026-09-20 22:27:46','2026-09-20 22:27:46'),(6,'快递面单',3,'三联 100*150mm','张',200,1,0,'2026-09-20 22:27:46','2026-09-20 22:27:46'),(7,'A4 打印纸',4,'70g A4','包',10,1,0,'2026-09-20 22:27:46','2026-09-20 22:27:46'),(8,'签字笔',4,'0.5mm 黑色','支',50,1,0,'2026-09-20 22:27:46','2026-09-20 22:27:46'),(9,'印章印油',5,'10ml 红色','瓶',5,1,0,'2026-09-20 22:27:46','2026-09-20 22:27:46');
/*!40000 ALTER TABLE `t_material` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_material_category`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_material_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类名称',
  `sort` int DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='物资分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_material_category`
--

LOCK TABLES `t_material_category` WRITE;
/*!40000 ALTER TABLE `t_material_category` DISABLE KEYS */;
INSERT INTO `t_material_category` VALUES (1,'信封类',1,'2026-09-20 22:27:46'),(2,'包装类',2,'2026-09-20 22:27:46'),(3,'面单类',3,'2026-09-20 22:27:46'),(4,'办公用品',4,'2026-09-20 22:27:46'),(5,'其他',5,'2026-09-20 22:27:46');
/*!40000 ALTER TABLE `t_material_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_operation_log`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `branch_id` bigint NOT NULL,
  `material_id` bigint NOT NULL,
  `op_type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'IN/OUT/TRANSFER_OUT/TRANSFER_IN/CHECK_GAIN/CHECK_LOSS',
  `quantity` int NOT NULL COMMENT '变动数量，正数',
  `before_qty` int DEFAULT NULL COMMENT '变动前库存（本网点视角）',
  `after_qty` int DEFAULT NULL COMMENT '变动后库存（本网点视角）',
  `related_id` bigint DEFAULT NULL COMMENT '关联单据 ID（盘点单/调拨单）',
  `related_type` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'INVENTORY_CHECK / TRANSFER',
  `operator_id` bigint DEFAULT NULL,
  `operator_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_branch_time` (`branch_id`,`created_at`),
  KEY `idx_material` (`material_id`),
  KEY `idx_op_type` (`op_type`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=2123345128948969475 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表（不设 deleted：日志不可删）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_operation_log`
--

LOCK TABLES `t_operation_log` WRITE;
/*!40000 ALTER TABLE `t_operation_log` DISABLE KEYS */;
INSERT INTO `t_operation_log` VALUES (2101710747124420609,1,1,'OUT',30,500,470,NULL,NULL,NULL,NULL,'????','2026-09-21 00:31:04'),(2101714247564709889,1,1,'IN',100,500,600,NULL,NULL,NULL,NULL,'????','2026-09-21 00:44:59'),(2101714611030511617,1,1,'IN',100,600,700,NULL,NULL,NULL,NULL,'市局配发','2026-09-21 00:46:25'),(2101714635240034306,1,1,'OUT',30,700,670,NULL,NULL,NULL,NULL,'营业领用','2026-09-21 00:46:31'),(2101714640625520642,1,6,'TRANSFER_OUT',200,800,600,2101714640545828866,'TRANSFER',NULL,NULL,'调出至网点#3','2026-09-21 00:46:32'),(2101714640705212418,3,6,'TRANSFER_IN',200,150,350,2101714640545828866,'TRANSFER',NULL,NULL,'由网点#1调入','2026-09-21 00:46:32'),(2102054549861216258,1,1,'CHECK_LOSS',10,500,490,2102054547738898434,'INVENTORY_CHECK',NULL,NULL,'盘点差异','2026-09-21 23:17:13');
/*!40000 ALTER TABLE `t_operation_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_stock`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_stock` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `branch_id` bigint NOT NULL COMMENT '营业所ID',
  `material_id` bigint NOT NULL COMMENT '物资ID',
  `quantity` int NOT NULL COMMENT '库存数量',
  `version` int NOT NULL DEFAULT '0',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_stock`
--

LOCK TABLES `t_stock` WRITE;
/*!40000 ALTER TABLE `t_stock` DISABLE KEYS */;
INSERT INTO `t_stock` VALUES (1,1,1,490,0,'2026-11-19 17:22:57'),(2,1,3,60,0,'2026-09-21 00:27:57'),(3,1,6,800,1,'2026-09-21 00:52:23'),(4,1,8,120,0,'2026-09-21 00:27:57'),(5,2,1,300,0,'2026-09-21 00:27:57'),(6,2,5,25,0,'2026-09-21 00:27:57'),(7,2,6,600,0,'2026-09-21 00:27:57'),(8,3,1,80,0,'2026-09-21 00:27:57'),(9,3,6,150,1,'2026-09-21 00:52:23'),(10,3,9,3,0,'2026-09-21 00:27:57');
/*!40000 ALTER TABLE `t_stock` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_transfer`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_transfer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `transfer_no` varchar(40) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调拨单号 DB+yyyyMMdd+序号',
  `from_branch_id` bigint NOT NULL,
  `to_branch_id` bigint NOT NULL,
  `material_id` bigint NOT NULL,
  `quantity` int NOT NULL,
  `operator_id` bigint DEFAULT NULL,
  `status` varchar(20) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'DONE' COMMENT 'DONE=已完成/CANCELED=已冲销',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transfer_no` (`transfer_no`),
  KEY `idx_from` (`from_branch_id`),
  KEY `idx_to` (`to_branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2123344132649803778 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='调拨单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_transfer`
--

LOCK TABLES `t_transfer` WRITE;
/*!40000 ALTER TABLE `t_transfer` DISABLE KEYS */;
INSERT INTO `t_transfer` VALUES (2101714640545828866,'DB20260921004632',1,3,6,200,NULL,'DONE','网点调拨','2026-09-21 00:46:32');
/*!40000 ALTER TABLE `t_transfer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_user`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `role` varchar(20) NOT NULL COMMENT '角色',
  `branch_id` bigint DEFAULT NULL COMMENT '营业所ID',
  `status` tinyint NOT NULL DEFAULT '1',
  `deleted` tinyint NOT NULL DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_user`
--

LOCK TABLES `t_user` WRITE;
/*!40000 ALTER TABLE `t_user` DISABLE KEYS */;
INSERT INTO `t_user` VALUES (1,'admin','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','系统管理员','ADMIN',NULL,1,0,'2026-09-21 00:30:26','2026-09-21 00:30:26'),(2,'nanchang01','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','张伟','OPERATOR',1,1,0,'2026-09-21 00:30:26','2026-09-21 00:30:26'),(3,'ganzhou01','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','王强','OPERATOR',3,1,0,'2026-09-21 00:30:26','2026-09-21 00:30:26');
/*!40000 ALTER TABLE `t_user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-19 20:16:41
