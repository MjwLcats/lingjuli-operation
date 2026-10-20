package com.lingjuli.iam.auth;

import com.lingjuli.common.core.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/api/iam/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/{portal}/login")
    public ApiResponse<LoginResponse> login(@PathVariable("portal") String portal, @Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authenticationService.login(request, toPortalType(portal)));
    }

    @GetMapping("/{portal}/session")
    public ApiResponse<LoginPrincipal> session(@PathVariable("portal") String portal) {
        return ApiResponse.ok(authenticationService.current(toPortalType(portal)));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authenticationService.logout();
        return ApiResponse.ok(null);
    }

    @PostMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authenticationService.changePassword(request);
        return ApiResponse.ok(null);
    }

    private PortalType toPortalType(String portal) {
        try {
            return PortalType.valueOf(portal.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new AuthException("不支持的登录门户");
        }
    }
}
