package com.example1.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example1.common.BusinessException;
import com.example1.entity.User;
import com.example1.mapper.UserMapper;
import com.example1.security.JwtUtil;
import com.example1.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public String login(String username, String password) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getDeleted, 0));
        if (user == null) {
            throw new BusinessException("账号不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        // BCrypt 自带盐，用 matches 校验，不是重新加密比对
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        LoginUser loginUser = new LoginUser(
                user.getId(), user.getUsername(), user.getRole(), user.getBranchId());
        return jwtUtil.generate(loginUser);
    }
}
