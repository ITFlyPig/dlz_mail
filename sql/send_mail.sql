/*
 Navicat MySQL Data Transfer

 Source Server         : dlz_mail
 Source Server Type    : MySQL
 Source Server Version : 50721
 Source Host           : localhost
 Source Database       : dlz_mail

 Target Server Type    : MySQL
 Target Server Version : 50721
 File Encoding         : utf-8

 Date: 03/15/2018 18:32:35 PM
*/

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `send_mail`
-- ----------------------------
DROP TABLE IF EXISTS `send_mail`;
CREATE TABLE `send_mail` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `auth` int(11) DEFAULT '0' COMMENT '??????????',
  `protocol` varchar(50) DEFAULT NULL COMMENT '????',
  `host` varchar(50) DEFAULT NULL COMMENT '??',
  `port` varchar(50) DEFAULT NULL COMMENT '??',
  `user` varchar(50) DEFAULT NULL COMMENT '?????',
  `password` varchar(50) DEFAULT NULL COMMENT '?????(163??smtp/pop3???????????)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Records of `send_mail`
-- ----------------------------
BEGIN;
INSERT INTO `send_mail` VALUES ('1', '1', 'smtp', 'smtp.163.com', '993', 'wyl_coder@163.com', 'kjih4321');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
