UPDATE iam_account
SET password_hash = '$2a$10$NKO1eYJLURgawHbAF8rPZe5KPLQ3UI74mIn7xZba.pRhLXiaurraS',
    must_change_password = TRUE
WHERE login_name = 'admin'
  AND portal_type = 'PLATFORM'
  AND merchant_code = '';
