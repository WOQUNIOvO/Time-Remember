package com.timeremember.security;

import com.timeremember.enums.UserRole;

public record LoginUser(Long id, String username, UserRole role) {
}
