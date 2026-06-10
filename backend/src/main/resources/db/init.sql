-- ============================================
-- Social Media Operation System - Database Init
-- ============================================

CREATE DATABASE IF NOT EXISTS news_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE news_db;

-- 1. User table
DROP TABLE IF EXISTS `post_like`;
DROP TABLE IF EXISTS `message`;
DROP TABLE IF EXISTS `friendship`;
DROP TABLE IF EXISTS `friend_request`;
DROP TABLE IF EXISTS `report`;
DROP TABLE IF EXISTS `comment_like`;
DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS `favorite`;
DROP TABLE IF EXISTS `post`;
DROP TABLE IF EXISTS `category`;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50) UNIQUE NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `nickname` VARCHAR(50) NOT NULL,
    `avatar_url` VARCHAR(500) DEFAULT '/default-avatar.png',
    `bio` VARCHAR(255) DEFAULT NULL,
    `background_url` VARCHAR(500) DEFAULT NULL,
    `role` ENUM('user','admin') DEFAULT 'user',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Category table
CREATE TABLE `category` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `sort_order` INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Post table
CREATE TABLE `post` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL,
    `content` TEXT NOT NULL,
    `image_urls` VARCHAR(1000) COMMENT 'JSON array of image URLs',
    `category_id` INT,
    `user_id` BIGINT,
    `status` ENUM('pending','approved','rejected') DEFAULT 'pending',
    `reject_reason` VARCHAR(255),
    `is_recommended` BOOLEAN DEFAULT FALSE,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    FOREIGN KEY (`category_id`) REFERENCES `category`(`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Favorite table
CREATE TABLE `favorite` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT,
    `post_id` BIGINT,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`post_id`) REFERENCES `post`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Comment table
CREATE TABLE `comment` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `post_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `content` VARCHAR(500) NOT NULL,
    `like_count` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`post_id`) REFERENCES `post`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Comment Like table
CREATE TABLE `comment_like` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `comment_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
    FOREIGN KEY (`comment_id`) REFERENCES `comment`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Report table
CREATE TABLE `report` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `post_id` BIGINT NOT NULL,
    `reporter_id` BIGINT NOT NULL,
    `reason` VARCHAR(50) NOT NULL,
    `description` VARCHAR(500) DEFAULT NULL,
    `status` VARCHAR(20) DEFAULT 'pending',
    `admin_id` BIGINT DEFAULT NULL,
    `handle_note` VARCHAR(500) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`post_id`) REFERENCES `post`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`reporter_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_reporter_post` (`reporter_id`, `post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Friend Request table
CREATE TABLE `friend_request` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `from_user_id` BIGINT NOT NULL,
    `to_user_id` BIGINT NOT NULL,
    `status` VARCHAR(20) DEFAULT 'pending',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`from_user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`to_user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_from_to` (`from_user_id`, `to_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. Friendship table
CREATE TABLE `friendship` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `friend_id` BIGINT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`friend_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. Post Like table
CREATE TABLE `post_like` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `post_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`post_id`) REFERENCES `post`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. Message table
CREATE TABLE `message` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `from_user_id` BIGINT NOT NULL,
    `to_user_id` BIGINT NOT NULL,
    `content` VARCHAR(500) NOT NULL,
    `is_read` TINYINT(1) DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`from_user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`to_user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    KEY `idx_from_to` (`from_user_id`, `to_user_id`),
    KEY `idx_to_read` (`to_user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Initial Data
-- ============================================

-- Default admin (password: 123456, BCrypt encoded)
-- Default admin + 6 users (password: 123456)
INSERT INTO `user` (`username`, `password`, `nickname`, `role`) VALUES
('admin', '$2a$10$35gxY.mmYI3jzs03z8a6I.5MYjMwJ48yoqa.EmLKGmzLOqiFPuPUe', 'Administrator', 'admin'),
('User1', '$2a$10$35gxY.mmYI3jzs03z8a6I.5MYjMwJ48yoqa.EmLKGmzLOqiFPuPUe', '小明', 'user'),
('User2', '$2a$10$35gxY.mmYI3jzs03z8a6I.5MYjMwJ48yoqa.EmLKGmzLOqiFPuPUe', '小红', 'user'),
('User3', '$2a$10$35gxY.mmYI3jzs03z8a6I.5MYjMwJ48yoqa.EmLKGmzLOqiFPuPUe', '大伟', 'user'),
('User4', '$2a$10$35gxY.mmYI3jzs03z8a6I.5MYjMwJ48yoqa.EmLKGmzLOqiFPuPUe', '莉莉', 'user'),
('User5', '$2a$10$35gxY.mmYI3jzs03z8a6I.5MYjMwJ48yoqa.EmLKGmzLOqiFPuPUe', '阿强', 'user'),
('User6', '$2a$10$35gxY.mmYI3jzs03z8a6I.5MYjMwJ48yoqa.EmLKGmzLOqiFPuPUe', '小美', 'user');

-- Categories
INSERT INTO `category` (`name`, `sort_order`) VALUES
('科技', 1), ('生活', 2), ('美食', 3), ('旅行', 4), ('娱乐', 5), ('教育', 6);

-- User1 发布15条帖子 (approved + recommended)
INSERT INTO `post` (`title`, `content`, `image_urls`, `category_id`, `user_id`, `status`, `is_recommended`) VALUES
('探索人工智能的未来发展趋势', '人工智能正在深刻改变我们的生活方式。从自动驾驶到智能医疗，AI的应用场景越来越广泛。本文将深入探讨2026年AI领域的最新突破和未来方向。', '["https://images.unsplash.com/photo-1677442136019-21780ecad995?w=400&h=300&fit=crop","https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=400&h=300&fit=crop"]', 1, 2, 'approved', TRUE),
('周末美食日记：自制意大利面', '用最简单的方式打造高级餐厅的味道。只需要番茄、罗勒、大蒜和优质橄榄油，你也能在家做出令人惊艳的意面。', '["https://images.unsplash.com/photo-1473093295043-cdd812d0e601?w=400&h=300&fit=crop"]', 3, 2, 'approved', TRUE),
('户外徒步必备装备清单', '无论是短途还是多日徒步，正确的装备能让你的旅程更加安全舒适。本清单涵盖了从鞋子到帐篷的所有必需品。', '["https://images.unsplash.com/photo-1551632811-561732d1e306?w=400&h=300&fit=crop","https://images.unsplash.com/photo-1501555088652-021faa106b9b?w=400&h=300&fit=crop"]', 4, 2, 'approved', TRUE),
('2026年必看的五部大片', '今年暑期档大片云集，科幻、动作、动画应有尽有。我们为你精选了最值得期待的五部电影，不要错过！', '["https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=400&h=300&fit=crop"]', 5, 2, 'approved', TRUE),
('如何高效学习一门新语言', '学习新语言并不需要天赋，而是需要正确的方法和持续的练习。本文分享五个实用的语言学习技巧。', '["https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=400&h=300&fit=crop","https://images.unsplash.com/photo-1503676260728-1c00da094a0b?w=400&h=300&fit=crop"]', 6, 2, 'approved', TRUE),
('智能家居系统的搭建指南', '从智能灯泡到语音助手，打造一个真正的智能家居并不复杂。本文将手把手教你如何开始。', '["https://images.unsplash.com/photo-1558002038-1055907df827?w=400&h=300&fit=crop"]', 1, 2, 'approved', TRUE),
('生活中的小确幸：一杯手冲咖啡', '在这个快节奏的时代，给自己一杯手冲咖啡的时间，感受生活的慢节奏和咖啡的香气。', '["https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400&h=300&fit=crop","https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400&h=300&fit=crop"]', 2, 2, 'approved', TRUE),
('探索中国最美乡村公路', '从云南到四川，这些隐藏在山间的公路不仅风景如画，更是自驾游爱好者的天堂。', '["https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=400&h=300&fit=crop"]', 4, 2, 'approved', TRUE),
('科技改变教育：在线学习的崛起', '在线教育平台让知识触手可及。本文将分析在线教育如何改变传统学习模式。', '["https://images.unsplash.com/photo-1501504905252-473c47e087f8?w=400&h=300&fit=crop","https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=400&h=300&fit=crop"]', 6, 2, 'approved', TRUE),
('夏日必试的三款冰镇饮品', '炎炎夏日，一杯冰凉的饮品是最好的解暑方式。教你制作三款简单又美味的夏日特饮。', '["https://images.unsplash.com/photo-1544145945-f90425340c7e?w=400&h=300&fit=crop"]', 3, 2, 'approved', TRUE),
('最新手机摄影技巧大全', '不需要专业相机，你的手机就能拍出大片效果。掌握这些构图和光线技巧，人人都能成为摄影师。', '["https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=400&h=300&fit=crop","https://images.unsplash.com/photo-1542038784456-1ea8e935640e?w=400&h=300&fit=crop"]', 1, 2, 'approved', TRUE),
('旅行中的那些暖心故事', '每一次旅行，都有一些令人难忘的瞬间。分享我在旅途中遇到的暖心小故事。', '["https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=400&h=300&fit=crop"]', 4, 2, 'approved', TRUE),
('打造舒适的家庭办公环境', '远程办公已成为新常态，如何在家中打造一个高效舒适的办公空间？这里有五个建议。', '["https://images.unsplash.com/photo-1486946255434-2466348c2166?w=400&h=300&fit=crop","https://images.unsplash.com/photo-1499951360447-b19be8fe80f5?w=400&h=300&fit=crop"]', 2, 2, 'approved', TRUE),
('游戏产业的2026：趋势与展望', '从云游戏到元宇宙，游戏产业正经历前所未有的变革。来看看2026年有哪些值得关注的趋势。', '["https://images.unsplash.com/photo-1511512578047-dfb367046420?w=400&h=300&fit=crop"]', 5, 2, 'approved', TRUE),
('传统美食的现代演绎：分子料理入门', '分子料理不再是高档餐厅的专利。了解基本原理后，你也可以在家尝试这种创新的烹饪方式。', '["https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=400&h=300&fit=crop","https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400&h=300&fit=crop"]', 3, 2, 'approved', TRUE);

-- 其他5个用户各发1条帖子 (approved + recommended)
INSERT INTO `post` (`title`, `content`, `image_urls`, `category_id`, `user_id`, `status`, `is_recommended`) VALUES
('城市骑行：发现身边的风景', '不需要去远方，骑上单车探索你所在的城市，你会发现许多被忽视的美好角落。', '["https://images.unsplash.com/photo-1517649763962-0c623066013b?w=400&h=300&fit=crop"]', 2, 3, 'approved', TRUE),
('编程入门：从零开始学Python', 'Python是世界上最流行的编程语言之一。本文为零基础的你提供清晰的学习路线图。', '["https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=400&h=300&fit=crop","https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=400&h=300&fit=crop"]', 1, 4, 'approved', TRUE),
('带你品尝成都最地道的火锅', '成都火锅名扬天下，但你知道本地人最爱去哪几家吗？跟着我的脚步来一探究竟。', '["https://images.unsplash.com/photo-1555126634-323283e090fa?w=400&h=300&fit=crop"]', 3, 5, 'approved', TRUE),
('读书笔记：《人类简史》的启示', '尤瓦尔·赫拉利的经典之作带给我们对人类文明的深刻反思。分享我的读书心得。', '["https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400&h=300&fit=crop","https://images.unsplash.com/photo-1495446815901-a7297e633e8d?w=400&h=300&fit=crop"]', 6, 6, 'approved', TRUE),
('摄影日记：记录日常之美', '生活中的美无处不在，一杯茶、一片落叶、一缕阳光，用镜头记录这些瞬间。', '["https://images.unsplash.com/photo-1452587925148-ce544e77e70d?w=400&h=300&fit=crop"]', 2, 7, 'approved', TRUE);
