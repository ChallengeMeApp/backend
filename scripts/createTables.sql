CREATE TABLE `challenge_status` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `challenge_id` bigint NOT NULL,
  `state` int DEFAULT NULL,
  `time_stamp` datetime DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `userIndex` (`user_id`),
  KEY `challengeIndex` (`challenge_id`)
) ENGINE=MyISAM AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `challenges` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `add_to_treasure_chest` bit(1) NOT NULL,
  `category` varchar(16) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `created_by_import` bit(1) NOT NULL,
  `created_by_user_id` bigint NOT NULL,
  `deleted_at` datetime DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `description` varchar(2048) DEFAULT NULL,
  `duration_seconds` bigint DEFAULT NULL,
  `kind` varchar(16) DEFAULT NULL,
  `material` varchar(255) DEFAULT NULL,
  `points_loose` int NOT NULL,
  `points_participation` int NOT NULL,
  `points_win` int NOT NULL,
  `repeatable_after_days` int DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `dtype` varchar(31) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `challengeCategoryIndex` (`category`),
  KEY `challengeCreatedByUserIdIndex` (`created_by_user_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2183 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `ignored_challenges` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `challenge_id` bigint NOT NULL,
  `time_stamp` datetime DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `userIndex` (`user_id`),
  KEY `challengeIndex` (`challenge_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `marked_challenges` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `challenge_id` bigint NOT NULL,
  `time_stamp` datetime DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `userIndex` (`user_id`),
  KEY `challengeIndex` (`challenge_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `timer` (
  `id` bigint NOT NULL,
  `linked_id` bigint NOT NULL,
  `type` varchar(16) DEFAULT NULL,
  `valid_until` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `timerTypeIndex` (`type`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin` bit(1) NOT NULL,
  `created_at` datetime DEFAULT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `last_request_at` datetime DEFAULT NULL,
  `user_id` binary(16) DEFAULT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `userUserIdIndex` (`user_id`)
) ENGINE=MyISAM AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
