package com.lingjuli.iam.auth;

public record LoginAccount(
        long id,
        String loginName,
        String passwordHash,
        PortalType portalType,
        String merchantCode,
        Long tenantId,
        String displayName,
        AccountStatus status,
        boolean mustChangePassword
) {
}
