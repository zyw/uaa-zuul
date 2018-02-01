package cn.v5cn.ss.demospringsecurity.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * 设置HTTP验证规则
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        //关闭csrf(跨域攻击)验证
        http.csrf().disable()

                //多请求进行认证
                .authorizeRequests()
                //所以已“/”开始的请求都放行
                .antMatchers("/").permitAll()
                //所以/login的POST请求都放行
                .antMatchers(HttpMethod.POST,"/login").permitAll()
                //访问/hello需要AUTH_WRITE权限
                .antMatchers("/hello").hasAuthority("AUTH_WRITE")
                //访问/world需要ADMIN角色
                .antMatchers("/world").hasRole("ADMIN")
                //其他所以请求需要身份认证
                .anyRequest().authenticated()

                .and()
                //禁止服务器保存Session状态
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                //添加一个过滤器，所有访问/login的请求交给JWTLoginFilter来处理，这个类处理所有的JWT相关内容
                .addFilterBefore(new JWTLoginFilter("/login",authenticationManager()), UsernamePasswordAuthenticationFilter.class)
                //添加一个过滤器验证其他请求的Token是否合法
                .addFilterBefore(new JWTAuthenticationFilter(),UsernamePasswordAuthenticationFilter.class);
    }

    /**
     * 使用自定义身份验证组件
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(new CustomAuthenticationProvider());
    }
}
