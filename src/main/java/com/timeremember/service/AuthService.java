package com.timeremember.service;

import com.timeremember.dto.auth.LoginDTO;
import com.timeremember.dto.auth.RegisterDTO;
import com.timeremember.vo.auth.LoginVO;
import com.timeremember.vo.user.UserVO;

public interface AuthService {

    UserVO register(RegisterDTO dto);

    LoginVO login(LoginDTO dto);
}
