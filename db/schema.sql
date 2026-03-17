-- 微信匹配聊天系统数据库表结构
-- 版本: 1.0.0
-- 创建数据库
CREATE DATABASE IF NOT EXISTS `wechat_match` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `wechat_match`;

-- 用户表 (user)
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `open_id` varchar(128) NOT NULL COMMENT '微信OpenID',
  `union_id` varchar(128) DEFAULT NULL COMMENT '微信UnionID',
  `nickname` varchar(100) DEFAULT NULL COMMENT '微信昵称',
  `avatar_url` varchar(500) DEFAULT NULL COMMENT '头像URL',
  `gender` tinyint(1) DEFAULT NULL COMMENT '性别：0-未知，1-男，2-女',
  `city` varchar(50) DEFAULT NULL COMMENT '城市',
  `province` varchar(50) DEFAULT NULL COMMENT '省份',
  `country` varchar(50) DEFAULT NULL COMMENT '国家',
  `language` varchar(10) DEFAULT 'zh_CN' COMMENT '语言',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态：0-禁用，1-正常，2-匹配中',
  `match_count` int(11) DEFAULT 0 COMMENT '今日已匹配次数',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_open_id` (`open_id`),
  KEY `idx_status` (`status`),
  KEY `idx_last_login` (`last_login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 匹配记录表 (match_record)
CREATE TABLE IF NOT EXISTS `match_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `match_id` varchar(64) NOT NULL COMMENT '匹配会话ID',
  `user_id1` bigint(20) NOT NULL COMMENT '用户1ID',
  `user_id2` bigint(20) NOT NULL COMMENT '用户2ID',
  `match_time` datetime NOT NULL COMMENT '匹配时间',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：1-匹配中，2-聊天中，3-已满意，4-已删除，5-已过期',
  `satisfaction_user1` tinyint(1) DEFAULT NULL COMMENT '用户1满意度：1-满意，0-不满意',
  `satisfaction_user2` tinyint(1) DEFAULT NULL COMMENT '用户2满意度：1-满意，0-不满意',
  `wechat_exchanged` tinyint(1) DEFAULT 0 COMMENT '是否已交换微信：0-否，1-是',
  `expire_time` datetime NOT NULL COMMENT '过期时间（匹配后7天）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_match_id` (`match_id`),
  KEY `idx_user1` (`user_id1`, `status`),
  KEY `idx_user2` (`user_id2`, `status`),
  KEY `idx_expire` (`expire_time`, `status`),
  CONSTRAINT `fk_match_user1` FOREIGN KEY (`user_id1`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_match_user2` FOREIGN KEY (`user_id2`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='匹配记录表';

-- 聊天消息表 (chat_message)
CREATE TABLE IF NOT EXISTS `chat_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `match_id` varchar(64) NOT NULL COMMENT '匹配会话ID',
  `sender_id` bigint(20) NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint(20) NOT NULL COMMENT '接收者ID',
  `message_type` tinyint(1) NOT NULL DEFAULT 1 COMMENT '消息类型：1-文本，2-图片，3-语音',
  `content` text NOT NULL COMMENT '消息内容',
  `is_read` tinyint(1) DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  `send_time` datetime NOT NULL COMMENT '发送时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_match` (`match_id`, `send_time`),
  KEY `idx_sender` (`sender_id`, `send_time`),
  KEY `idx_receiver` (`receiver_id`, `is_read`, `send_time`),
  CONSTRAINT `fk_chat_match` FOREIGN KEY (`match_id`) REFERENCES `match_record` (`match_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- 微信关系表 (wechat_relation)
CREATE TABLE IF NOT EXISTS `wechat_relation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `match_id` varchar(64) NOT NULL COMMENT '匹配会话ID',
  `user_id1` bigint(20) NOT NULL COMMENT '用户1ID',
  `user_id2` bigint(20) NOT NULL COMMENT '用户2ID',
  `wechat_id1` varchar(100) DEFAULT NULL COMMENT '用户1微信ID',
  `wechat_id2` varchar(100) DEFAULT NULL COMMENT '用户2微信ID',
  `exchange_time` datetime NOT NULL COMMENT '交换时间',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态：1-有效，0-已删除',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_match` (`match_id`),
  KEY `idx_user1` (`user_id1`),
  KEY `idx_user2` (`user_id2`),
  CONSTRAINT `fk_wechat_match` FOREIGN KEY (`match_id`) REFERENCES `match_record` (`match_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信关系表';

-- 通知记录表 (notification)
CREATE TABLE IF NOT EXISTS `notification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `type` tinyint(1) NOT NULL COMMENT '通知类型：1-新消息，2-匹配成功，3-微信交换，4-系统通知',
  `title` varchar(200) NOT NULL COMMENT '通知标题',
  `content` text NOT NULL COMMENT '通知内容',
  `related_id` varchar(100) DEFAULT NULL COMMENT '关联ID（如match_id）',
  `is_sent` tinyint(1) DEFAULT 0 COMMENT '是否已发送：0-未发送，1-已发送',
  `send_time` datetime DEFAULT NULL COMMENT '发送时间',
  `is_read` tinyint(1) DEFAULT 0 COMMENT '用户是否已读：0-未读，1-已读',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`, `is_sent`, `created_at`),
  KEY `idx_expire` (`expire_time`, `is_sent`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知记录表';

-- 初始化数据（可选）
-- INSERT INTO `user` (open_id, nickname, status) VALUES ('test_openid_1', '测试用户1', 1);
-- INSERT INTO `user` (open_id, nickname, status) VALUES ('test_openid_2', '测试用户2', 1);

-- 标签表 (tag)
CREATE TABLE IF NOT EXISTS `tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` varchar(50) NOT NULL COMMENT '标签名称',
  `description` varchar(200) DEFAULT NULL COMMENT '标签描述',
  `category` varchar(50) DEFAULT NULL COMMENT '标签分类',
  `type` tinyint(1) DEFAULT 1 COMMENT '标签类型：1-兴趣标签，2-职业标签，3-个性标签',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_category` (`category`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- 用户标签关联表 (user_tag)
CREATE TABLE IF NOT EXISTS `user_tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `tag_id` bigint(20) NOT NULL COMMENT '标签ID',
  `weight` tinyint(1) DEFAULT 1 COMMENT '权重：1-低，2-中，3-高',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_tag` (`user_id`, `tag_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_tag` (`tag_id`),
  CONSTRAINT `fk_user_tag_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_tag_tag` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户标签关联表';

-- 初始化一些常用标签
INSERT INTO `tag` (`name`, `description`, `category`, `type`) VALUES
('音乐', '喜欢各种音乐类型', '兴趣', 1),
('电影', '喜欢看电影和影视剧', '兴趣', 1),
('读书', '喜欢阅读各类书籍', '兴趣', 1),
('运动', '喜欢体育运动', '兴趣', 1),
('美食', '喜欢品尝各种美食', '兴趣', 1),
('旅行', '喜欢旅行和探索', '兴趣', 1),
('游戏', '喜欢玩电子游戏', '兴趣', 1),
('摄影', '喜欢摄影和拍照', '兴趣', 1),
('编程', '对编程和技术感兴趣', '职业', 2),
('设计', '对设计和创意感兴趣', '职业', 2),
('教育', '从事或对教育感兴趣', '职业', 2),
('医疗', '从事或对医疗健康感兴趣', '职业', 2),
('创业', '对创业和商业感兴趣', '职业', 2),
('外向', '性格外向，喜欢社交', '个性', 3),
('内向', '性格内向，喜欢独处', '个性', 3),
('幽默', '幽默风趣，喜欢开玩笑', '个性', 3),
('稳重', '成熟稳重，做事靠谱', '个性', 3),
('热情', '热情开朗，充满活力', '个性', 3),
('理性', '理性思考，逻辑性强', '个性', 3);