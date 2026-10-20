package com.lingjuli.iam.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "请输入新密码")
        @Size(min = 8, max = 128, message = "新密码长度应为 8 至 128 个字符")
        String newPassword
) {
}
