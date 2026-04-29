package com.timeremember.service;

import com.timeremember.entity.User;

public interface UserService {

    boolean existsByUsername(String username);

    User getByUsername(String username);

    User createUser(String username, String rawPassword, String nickname);
}
