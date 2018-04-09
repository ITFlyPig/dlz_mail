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

 Date: 03/15/2018 18:32:26 PM
*/

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `mail`
-- ----------------------------
DROP TABLE IF EXISTS `mail`;
CREATE TABLE `mail` (
  `id` int(100) unsigned NOT NULL AUTO_INCREMENT,
  `status` int(10) DEFAULT '0' COMMENT '?????????  0???  1?????  2?????  3?????  4????  5????   6?????',
  `sql` text NOT NULL COMMENT '?????sql??',
  `min` varchar(10) NOT NULL COMMENT '??  0-59',
  `hour` varchar(10) DEFAULT NULL COMMENT '?? 0-23',
  `day` varchar(10) DEFAULT NULL COMMENT '?  1-31',
  `month` varchar(10) DEFAULT NULL COMMENT '?  1-12',
  `week` varchar(10) DEFAULT NULL COMMENT '?? 0-6',
  `new_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '???????',
  `send_time` datetime DEFAULT NULL COMMENT '???????',
  `end_time` datetime DEFAULT NULL COMMENT '???????????????????',
  `receptions` text COMMENT '???????????????;???',
  `copy_to_mails` text COMMENT '?????????????????;???',
  `filePath` text COMMENT '????????????',
  `second` varchar(10) DEFAULT NULL COMMENT '? 0~59',
  `task_name` varchar(100) DEFAULT NULL COMMENT '?????',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `managerEmail` varchar(50) DEFAULT NULL COMMENT '管理员的邮件',
  `mailContent` text COMMENT '邮件的内容',
  `subject` text COMMENT '邮件的主题',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Records of `mail`
-- ----------------------------
BEGIN;
INSERT INTO `mail` VALUES ('1', '5', 'select * from mail', '3', '1', '2', '*', '*', '2018-03-07 00:00:00', '2018-03-07 00:00:00', null, 'a1659509224@163.com', '1347248229@qq.com', null, '*', '????', '2018-03-15 18:31:15', '1347248229@qq.com', '这是测试邮件', 'test mail');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
