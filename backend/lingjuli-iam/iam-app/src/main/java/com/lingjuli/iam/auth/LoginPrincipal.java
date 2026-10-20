package com.lingjuli.iam.auth;

import java.io.Serializable;

public record LoginPrincipal(
        long accountId, String loginName, String displayName, PortalType portalType, String merchantCode, Long tenantId,
        boolean mustChangePassword)
        implements Serializable {
}
