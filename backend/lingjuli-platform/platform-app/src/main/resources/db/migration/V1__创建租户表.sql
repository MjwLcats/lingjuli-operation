CREATE TABLE platform_tenant (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '租户主键',
    tenant_code CHAR(5) NOT NULL COMMENT '五位数字商户号',
    tenant_name VARCHAR(128) NOT NULL COMMENT '商户名称',
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '租户状态：ENABLED 或 DISABLED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_platform_tenant_code (tenant_code),
    CONSTRAINT chk_platform_tenant_code CHECK (tenant_code REGEXP '^[0-9]{5}$'),
    CONSTRAINT chk_platform_tenant_status CHECK (status IN ('ENABLED', 'DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台租户';
