package com.lingjuli.iam.auth;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final LoginAccountRepository loginAccountRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthenticationService(LoginAccountRepository loginAccountRepository) {
        this.loginAccountRepository = loginAccountRepository;
    }

    public LoginResponse login(LoginRequest request, PortalType portalType) {
        String merchantCode = resolveMerchantCode(request, portalType);
        LoginAccount account = loginAccountRepository.findByLoginNameAndPortalTypeAndMerchantCode(
                        request.loginName().trim(), portalType, merchantCode)
                .filter(candidate -> candidate.status() == AccountStatus.ENABLED)
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.passwordHash()))
                .orElseThrow(() -> new AuthException("账号或密码错误"));

        if (portalType == PortalType.MERCHANT && account.tenantId() == null) {
            throw new AuthException("商户账号缺少租户归属，无法登录");
        }
        if (portalType == PortalType.PLATFORM && account.tenantId() != null) {
            throw new AuthException("商户账号不能登录平台端");
        }

        LoginPrincipal principal = new LoginPrincipal(
                account.id(), account.loginName(), account.displayName(), account.portalType(),
                account.merchantCode(), account.tenantId(), account.mustChangePassword());
        StpUtil.login(account.id());
        StpUtil.getSession().set("principal", principal);
        return new LoginResponse(StpUtil.getTokenValue(), principal, account.mustChangePassword());
    }

    public LoginPrincipal current(PortalType portalType) {
        StpUtil.checkLogin();
        Object principal = StpUtil.getSession().get("principal");
        if (!(principal instanceof LoginPrincipal currentPrincipal) || currentPrincipal.portalType() != portalType) {
            throw new AuthException("当前会话无权访问此门户");
        }
        return currentPrincipal;
    }

    public void logout() {
        StpUtil.logout();
    }

    public void changePassword(ChangePasswordRequest request) {
        StpUtil.checkLogin();
        Object principal = StpUtil.getSession().get("principal");
        if (!(principal instanceof LoginPrincipal currentPrincipal)) {
            throw new AuthException("当前会话无效，请重新登录");
        }
        loginAccountRepository.updatePassword(currentPrincipal.accountId(), passwordEncoder.encode(request.newPassword()));
        StpUtil.getSession().set("principal", new LoginPrincipal(
                currentPrincipal.accountId(), currentPrincipal.loginName(), currentPrincipal.displayName(),
                currentPrincipal.portalType(), currentPrincipal.merchantCode(), currentPrincipal.tenantId(), false));
    }

    private String resolveMerchantCode(LoginRequest request, PortalType portalType) {
        if (portalType == PortalType.PLATFORM) {
            return "";
        }
        if (request.merchantCode() == null || !request.merchantCode().matches("\\d{5}")) {
            throw new AuthException("请输入五位数字商户号");
        }
        return request.merchantCode();
    }
}
