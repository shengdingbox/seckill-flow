DROP TABLE  IF EXISTS TASK_GROUP;

CREATE TABLE `TASK_GROUP` (
                              `id` VARCHAR(40) NOT NULL ,
                              `name` VARCHAR(45) NULL,
                              `description` VARCHAR(200) NULL,
                              `create_time` DATETIME NULL,
                              `update_time` DATETIME NULL,
                              PRIMARY KEY (`id`));


DROP TABLE  IF EXISTS TASK_JOB;

CREATE TABLE `TASK_JOB` (
                            `id` VARCHAR(40) NOT NULL ,
                            `name` VARCHAR(200) NULL,
                            `group_id` VARCHAR(40) NOT NULL,
                            `type` CHAR(1) NULL COMMENT '1-http 2-shell',
                            `http_url` VARCHAR(500) NULL,
                            `http_body` VARCHAR(1000) NULL,
                            `http_content_type` VARCHAR(100) NULL,
                            `http_method` VARCHAR(10) NULL,
                            `shell_command` VARCHAR(1000) NULL,
                            `create_time` DATETIME NULL,
                            `update_time` DATETIME NULL,
                            PRIMARY KEY (`id`));

DROP TABLE  IF EXISTS TASK_SCHEDULE;

CREATE TABLE `TASK_SCHEDULE` (
                                 `id` VARCHAR(40) NOT NULL,
                                 `trigger_type` CHAR(1) NULL COMMENT '1-间隔 2-cron',
                                 `cron` VARCHAR(40)  NULL,
                                 `period` INT NULL,
                                 `timeunit` VARCHAR(45) NULL,
                                 `create_time` DATETIME NULL,
                                 `update_time` DATETIME NULL,
                                 `job_id` VARCHAR(40) NULL,
                                 `status` CHAR(1)  NULL COMMENT '0-禁用 1-启用',
                                 PRIMARY KEY (`id`));

DROP TABLE  IF EXISTS TASK_LOG;

CREATE TABLE `TASK_LOG` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT,
                            `job_id` VARCHAR(40) NULL,
                            `job_name` VARCHAR(45) NULL,
                            `schedule_id` VARCHAR(40) NULL,
                            `request_body` VARCHAR(1000) NULL,
                            `response_body` VARCHAR(4000) NULL,
                            `status` VARCHAR(10) NULL,
                            `start_time` DATETIME NULL,
                            `end_time` DATETIME NULL,
                            PRIMARY KEY (`id`));

DROP TABLE  IF EXISTS TASK_USER;
CREATE TABLE `TASK_USER` (
                             `id` bigint(20) NOT NULL AUTO_INCREMENT,
                             `account` varchar(45)  NULL,
                             `address` varchar(45)  NULL,
                             `birthday` datetime  NULL,
                             `description` varchar(100)   NULL ,
                             `email` varchar(45)   NULL,
                             `id_number` varchar(45)  NULL,
                             `name` varchar(45)  NULL,
                             `password` varchar(100)  NULL,
                             `phone_number` varchar(45)  NULL,
                             `sex` char(1)  NULL ,
                             `type` char(1)  NULL ,
                             `status` char(1) NULL ,
                             `create_time` datetime  NULL ,
                             `modify_time` datetime  NULL ,
                             PRIMARY KEY (`id`)
);
INSERT INTO `TASK_USER`(`account`,`address`,`birthday`,`description`, `email`,`id_number`,`name`, `password`,`phone_number`,`sex`, `type`, `status`,`create_time`, `modify_time`)
VALUES ('admin',NULL,NULL,'管理员','admin@163.com','','管理员','$2a$10$f3.j.dzDrWEUOMAX/ZxL4eh8fT2YyL9g5V13W0AalfHpGjQt6TyrC','','0','0','1',NOW(),NOW());