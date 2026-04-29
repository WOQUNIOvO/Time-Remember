package com.timeremember.vo.user;

import com.timeremember.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户信息")
public record UserVO(
        @Schema(description = "用户 ID") Long id,
        @Schema(description = "用户名") String username,
        @Schema(description = "昵称") String nickname,
        @Schema(description = "角色") UserRole role
) {
}
