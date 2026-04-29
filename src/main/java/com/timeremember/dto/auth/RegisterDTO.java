package com.timeremember.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "注册请求")
public class RegisterDTO {

    @NotBlank
    @Size(min = 3, max = 32)
    @Schema(description = "用户名", example = "alice")
    private String username;

    @NotBlank
    @Size(min = 6, max = 64)
    @Schema(description = "密码", example = "123456")
    private String password;

    @Size(max = 32)
    @Schema(description = "昵称", example = "Alice")
    private String nickname;
}
