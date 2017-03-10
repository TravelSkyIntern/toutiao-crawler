/*
Navicat MySQL Data Transfer

Source Server         : lql
Source Server Version : 50528
Source Host           : localhost:3306
Source Database       : ssh

Target Server Type    : MYSQL
Target Server Version : 50528
File Encoding         : 65001

Date: 2017-03-10 13:12:51
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for funny
-- ----------------------------
DROP TABLE IF EXISTS `funny`;
CREATE TABLE `funny` (
  `funnyId` int(11) NOT NULL,
  `behotTime` datetime DEFAULT NULL,
  `chineseTag` varchar(255) DEFAULT NULL,
  `group_Id` varchar(255) DEFAULT NULL,
  `hasGallery` varchar(255) DEFAULT NULL,
  `imageUrl` varchar(255) DEFAULT NULL,
  `isFeedAd` varchar(255) DEFAULT NULL,
  `mediaAvatarUrl` varchar(255) DEFAULT NULL,
  `mediaUrl` varchar(255) DEFAULT NULL,
  `moreMode` varchar(255) DEFAULT NULL,
  `singleMode` varchar(255) DEFAULT NULL,
  `source` varchar(255) DEFAULT NULL,
  `sourceUrl` varchar(255) DEFAULT NULL,
  `tag` varchar(255) DEFAULT NULL,
  `tagUrl` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `commentsCount` varchar(255) DEFAULT NULL,
  `document` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`funnyId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of funny
-- ----------------------------
