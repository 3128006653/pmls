package com.example1.security;

import com.example1.entity.User;
import com.example1.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                Claims claims = jwtUtil.parse(header.substring(7));
                if (claims != null) {
                    Long userId = Long.valueOf(claims.getSubject());
                    // 以数据库为准：用户被删/被禁用时 token 里的信息不可信
                    User user = userMapper.selectById(userId);
                    if (user != null && user.getDeleted() != null
                            && user.getDeleted() == 0
                            && user.getStatus() != null && user.getStatus() == 1) {

                        LoginUser loginUser = new LoginUser(
                                user.getId(), user.getUsername(),
                                user.getRole(), user.getBranchId());

                        List<GrantedAuthority> authorities =
                                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));

                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        user.getUsername(), null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(auth);

                        UserContext.set(loginUser);   // 供业务层做数据隔离
                    }
                }
            }
            chain.doFilter(request, response);
        } finally {
            UserContext.remove();   // 必须：线程池复用会串号
        }
    }
}
