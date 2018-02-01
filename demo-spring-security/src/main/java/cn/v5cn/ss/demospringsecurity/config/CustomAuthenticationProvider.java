package cn.v5cn.ss.demospringsecurity.config;

import cn.v5cn.ss.demospringsecurity.domain.GrantedAuthorityImpl;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义身份认证验证组件
 */
public class CustomAuthenticationProvider implements AuthenticationProvider {
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        // 获取认证的用户名 & 密码
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        if("admin".equals(username) && "123456".equals(password)) {
            List<GrantedAuthority> authoritys = new ArrayList<>();
            authoritys.add(new GrantedAuthorityImpl("ROLE_ADMIN"));
            authoritys.add(new GrantedAuthorityImpl("AUTH_WRITE"));

            Authentication auth = new UsernamePasswordAuthenticationToken(username,password,authoritys);
            return auth;
        }

        throw new BadCredentialsException("密码错误~");
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.equals(UsernamePasswordAuthenticationToken.class);
    }
}
