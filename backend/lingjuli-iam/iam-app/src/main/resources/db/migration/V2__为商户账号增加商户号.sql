ALTER TABLE iam_account
    ADD COLUMN merchant_code CHAR(5) NOT NULL DEFAULT '' COMMENT '商户登录使用的五位商户号' AFTER portal_type;

ALTER TABLE iam_account
    DROP INDEX uk_iam_account_login_portal,
    ADD UNIQUE KEY uk_iam_account_login_portal_merchant (login_name, portal_type, merchant_code),
    ADD CONSTRAINT chk_iam_account_merchant_code CHECK (
        (portal_type = 'PLATFORM' AND merchant_code = '')
        OR (portal_type = 'MERCHANT' AND merchant_code REGEXP '^[0-9]{5}$')
    );
