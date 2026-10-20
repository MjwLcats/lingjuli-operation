CREATE TABLE iam_account (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'IAM 账号主键',
    login_name VARCHAR(64) NOT NULL COMMENT '登录名',
    password_hash VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码哈希',
    portal_type VARCHAR(16) NOT NULL COMMENT '登录门户：PLATFORM 或 MERCHANT',
    tenant_id BIGINT NULL COMMENT '商户账号所属租户；平台账号为空',
    display_name VARCHAR(64) NOT NULL COMMENT '展示名称',
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '账号状态：ENABLED 或 DISABLED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_iam_account_login_portal (login_name, portal_type),
    KEY idx_iam_account_tenant (tenant_id),
    CONSTRAINT chk_iam_account_portal CHECK (portal_type IN ('PLATFORM', 'MERCHANT')),
    CONSTRAINT chk_iam_account_status CHECK (status IN ('ENABLED', 'DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IAM 登录账号';
