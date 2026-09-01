-- 个人网站后端建表脚本（MySQL 5.7+）
-- 使用前请先创建数据库：CREATE DATABASE interview CHARACTER SET utf8mb4;
-- 然后在 application.yml 中把 spring.sql.init.mode 改为 always 启动一次（建好后改回 never）。

CREATE TABLE IF NOT EXISTS `user` (
    id         VARCHAR(64)  NOT NULL PRIMARY KEY,
    username   VARCHAR(64)  NOT NULL,
    password   VARCHAR(100) NOT NULL,
    nickname   VARCHAR(64),
    email      VARCHAR(128),
    avatar     VARCHAR(10),
    bio        VARCHAR(500),
    created_at DATETIME,
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `note` (
    id         VARCHAR(64)  NOT NULL PRIMARY KEY,
    title      VARCHAR(200) NOT NULL,
    summary    VARCHAR(500),
    content    LONGTEXT,
    category   VARCHAR(32),
    tags       TEXT,
    pinned     TINYINT(1)   DEFAULT 0,
    views      INT          DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `resume` (
    id         VARCHAR(64)  NOT NULL PRIMARY KEY,
    label      VARCHAR(100),
    path       VARCHAR(200),
    icon       VARCHAR(10),
    content    LONGTEXT,
    created_at DATETIME,
    updated_at DATETIME
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
