-- =============================================================
-- 个人网站后端 · 数据库建表脚本（重构版 v2）
-- 适用：500 人在线笔记系统 + 多用户安全 + 未来分布式/大模型预留
-- 说明：
--   1. 本脚本为「重构/开发阶段」脚本，先 DROP 再 CREATE，便于后端启动时直接得到新结构。
--   2. 生产环境禁止直接 DROP，请改用 开发文档/4.数据库设计.md 中的增量迁移方案。
--   3. 中文全文检索依赖 ngram 分词器：需 my.cnf 配置 [mysqld] ngram_token_size=2 后重启。
--   4. 引擎 InnoDB，字符集 utf8mb4（建议 MySQL 8.0；5.7.6+ 亦支持 ngram 分词器）。
--   5. 主键统一 VARCHAR(64)：历史种子数据沿用原字符串 id，新建数据建议改用雪花 ID（见文档）。
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---------- 用户表 ----------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    id            VARCHAR(64)  NOT NULL PRIMARY KEY,
    username      VARCHAR(64)  NOT NULL,
    password      VARCHAR(100) NOT NULL,
    nickname      VARCHAR(64),
    email         VARCHAR(128),
    avatar        VARCHAR(512),            -- 改为 URL（未来走对象存储），原为占位字符 VARCHAR(10)
    bio           VARCHAR(500),
    role          VARCHAR(20)  NOT NULL DEFAULT 'user',  -- user / admin
    status        TINYINT(1)   NOT NULL DEFAULT 1,        -- 1 正常 0 禁用
    deleted       TINYINT(1)   NOT NULL DEFAULT 0,
    last_login_at DATETIME,
    created_at    DATETIME,
    updated_at    DATETIME,
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT='用户表';

-- ---------- 笔记元数据表（列表只查此表，不含正文） ----------
DROP TABLE IF EXISTS `note`;
CREATE TABLE `note` (
    id           VARCHAR(64)  NOT NULL PRIMARY KEY,
    user_id      VARCHAR(64)  NOT NULL,                     -- 数据归属（消除水平越权）
    title        VARCHAR(200) NOT NULL,
    summary      VARCHAR(500),
    category     VARCHAR(32),
    tags         VARCHAR(1000),                             -- 标签 JSON 数组（展示冗余；真相源见 note_tag_rel）
    pinned       TINYINT(1)   NOT NULL DEFAULT 0,
    views        INT          NOT NULL DEFAULT 0,
    version      INT          NOT NULL DEFAULT 0,           -- 乐观锁版本号，防并发覆盖
    content_hash VARCHAR(64),                               -- 正文 MD5，后端去重（拦截重复写库）
    deleted      TINYINT(1)   NOT NULL DEFAULT 0,           -- 软删除：回收站
    deleted_at   DATETIME,                                  -- 软删除时间，用于 30 天自动清理
    created_at   DATETIME,
    updated_at   DATETIME,
    INDEX idx_user_updated (user_id, updated_at),
    INDEX idx_user_category (user_id, category),
    INDEX idx_user_deleted  (user_id, deleted),
    FULLTEXT INDEX ft_title_summary (title, summary) WITH PARSER ngram
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT='笔记元数据表';

-- ---------- 笔记正文表（垂直拆分，仅在详情时按 id 取） ----------
DROP TABLE IF EXISTS `note_content`;
CREATE TABLE `note_content` (
    note_id VARCHAR(64) NOT NULL PRIMARY KEY,
    content LONGTEXT,
    CONSTRAINT fk_content_note FOREIGN KEY (note_id) REFERENCES `note`(id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT='笔记正文表';

-- ---------- 标签字典表（用户维度，支持重命名/合并/删除） ----------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
    id         VARCHAR(64) NOT NULL PRIMARY KEY,
    user_id    VARCHAR(64) NOT NULL,
    name       VARCHAR(50) NOT NULL,
    color      VARCHAR(20),
    created_at DATETIME,
    UNIQUE KEY uk_user_name (user_id, name),
    INDEX idx_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT='标签字典表';

-- ---------- 笔记-标签关联表 ----------
DROP TABLE IF EXISTS `note_tag_rel`;
CREATE TABLE `note_tag_rel` (
    note_id VARCHAR(64) NOT NULL,
    tag_id  VARCHAR(64) NOT NULL,
    PRIMARY KEY (note_id, tag_id),
    INDEX idx_tag (tag_id),
    CONSTRAINT fk_rel_note FOREIGN KEY (note_id) REFERENCES `note`(id) ON DELETE CASCADE,
    CONSTRAINT fk_rel_tag  FOREIGN KEY (tag_id)  REFERENCES `tag`(id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT='笔记-标签关联表';

-- ---------- 分享只读链接表 ----------
DROP TABLE IF EXISTS `note_share`;
CREATE TABLE `note_share` (
    id         VARCHAR(64) NOT NULL PRIMARY KEY,
    note_id    VARCHAR(64) NOT NULL,
    token      VARCHAR(64) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    expires_at DATETIME,                                  -- 可空 = 永久有效
    created_at DATETIME,
    UNIQUE KEY uk_token (token),
    INDEX idx_note (note_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT='笔记分享只读链接表';

-- ---------- 笔记分析表（预留：未来接入大模型做笔记分析） ----------
DROP TABLE IF EXISTS `note_analysis`;
CREATE TABLE `note_analysis` (
    id            VARCHAR(64) NOT NULL PRIMARY KEY,
    note_id       VARCHAR(64) NOT NULL,
    user_id       VARCHAR(64) NOT NULL,
    analysis_type VARCHAR(32) NOT NULL,                   -- keywords / summary / category / similar
    result        JSON,                                    -- 分析结果（结构化）
    model         VARCHAR(64),                             -- 调用的模型标识（如 gpt-4o / 自建模型）
    status        TINYINT(1) NOT NULL DEFAULT 1,           -- 1 有效 0 失效
    created_at    DATETIME,
    updated_at    DATETIME,
    INDEX idx_note_type (note_id, analysis_type)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT='笔记分析表（大模型结果预留）';

-- ---------- 异步任务表（预留：MQ / 大模型 / 导出等） ----------
DROP TABLE IF EXISTS `async_task`;
CREATE TABLE `async_task` (
    id          VARCHAR(64) NOT NULL PRIMARY KEY,
    user_id     VARCHAR(64),
    type        VARCHAR(32) NOT NULL,                     -- ANALYSIS / EXPORT / ...
    biz_id      VARCHAR(64),                              -- 关联业务主键（如 note_id）
    status      VARCHAR(16) NOT NULL,                     -- pending / processing / success / failed
    payload     JSON,
    result      JSON,
    error_msg   VARCHAR(500),
    retry_count INT NOT NULL DEFAULT 0,
    created_at  DATETIME,
    updated_at  DATETIME,
    INDEX idx_status (status),
    INDEX idx_user_type (user_id, type)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT='异步任务表（MQ/大模型预留）';

-- ---------- 文件表（预留：对象存储元信息，图片上传） ----------
DROP TABLE IF EXISTS `file`;
CREATE TABLE `file` (
    id         VARCHAR(64) NOT NULL PRIMARY KEY,
    user_id    VARCHAR(64) NOT NULL,
    note_id    VARCHAR(64),                               -- 可空，关联来源笔记
    bucket     VARCHAR(64),
    object_key VARCHAR(255) NOT NULL,                     -- 对象存储 key（禁止把图片 base64 存进正文）
    url        VARCHAR(512),                              -- 访问 URL
    name       VARCHAR(255),
    ext        VARCHAR(16),
    size       BIGINT NOT NULL DEFAULT 0,
    mime       VARCHAR(64),
    created_at DATETIME,
    INDEX idx_user (user_id),
    INDEX idx_note (note_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT='文件/附件元信息表（对象存储预留）';

-- ---------- 简历表（本次仅补齐安全字段，功能不重构） ----------
DROP TABLE IF EXISTS `resume`;
CREATE TABLE `resume` (
    id         VARCHAR(64) NOT NULL PRIMARY KEY,
    user_id    VARCHAR(64) NOT NULL DEFAULT '',           -- 数据归属（消除水平越权）
    label      VARCHAR(100),
    path       VARCHAR(200),
    icon       VARCHAR(10),
    content    LONGTEXT,                                   -- 整份 ResumeData 的 JSON 串
    version    INT NOT NULL DEFAULT 0,                     -- 乐观锁
    deleted    TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME,
    INDEX idx_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT='简历表';

SET FOREIGN_KEY_CHECKS = 1;
