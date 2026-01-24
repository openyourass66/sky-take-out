package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.vo.UserLoginVO;

public interface UserService {
    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    UserLoginVO wxlogin(UserLoginDTO userLoginDTO);
}
