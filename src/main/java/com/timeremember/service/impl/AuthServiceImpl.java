package com.timeremember.service.impl;

import com.timeremember.common.ErrorCode;
import com.timeremember.dto.auth.LoginDTO;
import com.timeremember.dto.auth.RegisterDTO;
import com.timeremember.entity.User;
import com.timeremember.exception.BusinessException;
import com.timeremember.security.JwtTokenProvider;
import com.timeremember.security.LoginUser;
import com.timeremember.service.AuthService;
import com.timeremember.service.UserService;
import com.timeremember.vo.auth.LoginVO;
import com.timeremember.vo.user.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(UserService userService, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(RegisterDTO dto) {
        if (userService.existsByUsername(dto.getUsername())) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }
        User user = userService.createUser(dto.getUsername(), dto.getPassword(), dto.getNickname());
        log.info("User registered, username={}", user.getUsername());
        return toUserVO(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userService.getByUsername(dto.getUsername());
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        String token = jwtTokenProvider.generateToken(new LoginUser(user.getId(), user.getUsername(), user.getRole()));
        return new LoginVO(token, user.getId(), user.getUsername(), user.getNickname(), user.getRole());
    }

    private UserVO toUserVO(User user) {
        return new UserVO(user.getId(), user.getUsername(), user.getNickname(), user.getRole());
    }
}
