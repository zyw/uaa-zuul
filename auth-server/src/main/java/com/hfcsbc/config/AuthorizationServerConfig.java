package com.hfcsbc.config;

import com.hfcsbc.security.DomainUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import javax.jws.soap.SOAPBinding;
import javax.sql.DataSource;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangyunfei on 2017/6/9.
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private RedisConnectionFactory connectionFactory;

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public RedisTokenStore tokenStore() {
        return new RedisTokenStore(connectionFactory);
    }

    /*@Bean
    public TokenStore tokenStore(){
        return new JwtTokenStore(accessTokenConverter());
    }*/


    @Bean
    public JwtAccessTokenConverter accessTokenConverter(){
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey("123");
        //自定义Token的内容
        converter.setAccessTokenConverter(new CustomerAccessTokenConverter());
        return new JwtAccessTokenConverter();
    }

    /**
     * 配置身份认证器，配置认证方式，TokenStore，TokenGranter，OAuth2RequestFactory
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
                .authenticationManager(authenticationManager)
                //用户权限获得
                .userDetailsService(userDetailsService)//若无，refresh_token会有UserDetailsService is required错误
                //token存储
                .tokenStore(tokenStore())
                .accessTokenConverter(accessTokenConverter());

       /* // 自定义token生成方式*/
        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(Arrays.asList(customerEnhancer(), accessTokenConverter()));
        endpoints.tokenEnhancer(tokenEnhancerChain);

        // 配置TokenServices参数
        DefaultTokenServices tokenServices = (DefaultTokenServices) endpoints.getDefaultAuthorizationServerTokenServices();
        tokenServices.setTokenStore(endpoints.getTokenStore());
        tokenServices.setSupportRefreshToken(true);
        tokenServices.setClientDetailsService(endpoints.getClientDetailsService());
        tokenServices.setTokenEnhancer(endpoints.getTokenEnhancer());
        tokenServices.setAccessTokenValiditySeconds((int) TimeUnit.DAYS.toSeconds(1)); // 1天
        endpoints.tokenServices(tokenServices);

        super.configure(endpoints);
    }

    /**
     * 对应于配置AuthorizationServer安全认证的相关信息，创建ClientCredentialsTokenEndpointFilter核心过滤器
     * @param security
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security
                .tokenKeyAccess("permitAll()")
                //允许表单提交
                .allowFormAuthenticationForClients()
                .checkTokenAccess("isAuthenticated()");
    }

    /**
     * 配置OAuth2的客户端相关信息
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        //可以把下面信息写入数据库中，使用 clients.withClientDetails(ClientDetailsService clientDetailsService)
        clients.inMemory()
                .withClient("android")
                .scopes("xx1")
                .secret("android")
                .authorizedGrantTypes("password", "authorization_code", "refresh_token")
            .and()
                .withClient("webapp")
                .scopes("xx2")
//                .secret("ddd1")
                .authorizedGrantTypes("password", "authorization_code", "refresh_token");
    }

    /**
     * 注入自定义token生成方式
     * @return
     */
    @Bean
    public TokenEnhancer customerEnhancer() {
        return new CustomTokenEnhancer();
    }
}
