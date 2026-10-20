ALTER TABLE iam_account
    ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否必须在下次登录后修改密码' AFTER status;

INSERT INTO iam_account (
    login_name, password_hash, portal_type, merchant_code, tenant_id, display_name, status, must_change_password
)
SELECT 'admin', '$2a$10$NKO1eYJLURgawHbAF8rPZe5KPLQ3UI74mIn7xZba.pRhLXiaurraS',
       'PLATFORM', '', NULL, '平台管理员', 'ENABLED', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM iam_account
    WHERE login_name = 'admin' AND portal_type = 'PLATFORM' AND merchant_code = ''
);
