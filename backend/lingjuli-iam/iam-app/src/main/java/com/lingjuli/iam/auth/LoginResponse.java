package com.lingjuli.iam.auth;

public record LoginResponse(String token, LoginPrincipal user, boolean mustChangePassword) {
}
