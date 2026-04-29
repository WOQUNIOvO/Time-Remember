package com.timeremember.vo.auth;

import com.timeremember.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "登录响应")
public record LoginVO(
        @Schema(description = "JWT token") String token,
        @Schema(description = "用户 ID") Long userId,
        @Schema(description = "用户名") String username,
        @Schema(description = "昵称") String nickname,
        @Schema(description = "角色") UserRole role
) {
}
