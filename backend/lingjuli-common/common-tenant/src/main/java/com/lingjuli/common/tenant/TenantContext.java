package com.lingjuli.common.tenant;

import java.util.Optional;

/** Holds the tenant resolved by the trusted service-side authentication flow. */
public final class TenantContext {

    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenantId(long tenantId) {
        if (tenantId <= 0) {
            throw new IllegalArgumentException("tenantId must be positive");
        }
        TENANT_ID.set(tenantId);
    }

    public static Optional<Long> getTenantId() {
        return Optional.ofNullable(TENANT_ID.get());
    }

    public static long requireTenantId() {
        return getTenantId().orElseThrow(() -> new IllegalStateException("Missing tenant context"));
    }

    public static void clear() {
        TENANT_ID.remove();
    }
}

