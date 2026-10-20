package com.lingjuli.iam.auth;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class LoginAccountRepository {

    private static final RowMapper<LoginAccount> ROW_MAPPER = LoginAccountRepository::map;
    private final JdbcTemplate jdbcTemplate;

    public LoginAccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<LoginAccount> findByLoginNameAndPortalTypeAndMerchantCode(
            String loginName, PortalType portalType, String merchantCode) {
        var accounts = jdbcTemplate.query("""
                SELECT id, login_name, password_hash, portal_type, merchant_code, tenant_id, display_name, status,
                       must_change_password
                FROM iam_account
                WHERE login_name = ? AND portal_type = ? AND merchant_code = ?
                """, ROW_MAPPER, loginName, portalType.name(), merchantCode);
        return accounts.stream().findFirst();
    }

    public void updatePassword(long accountId, String passwordHash) {
        jdbcTemplate.update("""
                UPDATE iam_account
                SET password_hash = ?, must_change_password = FALSE
                WHERE id = ?
                """, passwordHash, accountId);
    }

    private static LoginAccount map(ResultSet resultSet, int rowNumber) throws SQLException {
        long tenantId = resultSet.getLong("tenant_id");
        Long resolvedTenantId = resultSet.wasNull() ? null : tenantId;
        return new LoginAccount(
                resultSet.getLong("id"),
                resultSet.getString("login_name"),
                resultSet.getString("password_hash"),
                PortalType.valueOf(resultSet.getString("portal_type")),
                resultSet.getString("merchant_code"),
                resolvedTenantId,
                resultSet.getString("display_name"),
                AccountStatus.valueOf(resultSet.getString("status")),
                resultSet.getBoolean("must_change_password")
        );
    }
}
