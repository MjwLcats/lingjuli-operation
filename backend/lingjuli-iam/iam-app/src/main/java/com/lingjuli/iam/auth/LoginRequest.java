package com.lingjuli.iam.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "请输入账号") @Size(max = 64, message = "账号长度不能超过 64 个字符") String loginName,
        @NotBlank(message = "请输入密码") @Size(max = 128, message = "密码长度不能超过 128 个字符") String password,
        @Pattern(regexp = "\\d{5}", message = "商户号必须为五位数字") String merchantCode
) {
}
