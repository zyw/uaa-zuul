package cn.v5cn.ss.demospringsecurity.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class TokenAuthenticationService {
    /**
     * 5天
     */
    public final static long EXPIRATIONTIME = 432_000_000;

    /**
     * JWT密码
     */
    public final static String SECRET = "P@ssw02d";

    /**
     * Token前缀
     */
    public final static String TOKEN_PREFIX = "Bearer";

    /**
     *  存放Token的Header Key
     */
    public final static String HEADER_STRING = "Authorization";

    /**
     * 生成JWT
     * @param response
     * @param username
     */
    public static void addAuthentication(HttpServletResponse response, String username) {
        //生成JWT
        final String jwt = Jwts.builder()
                //保存权限(角色)
                .claim("authorities", "ROLE_ADMIN,AUTH_WRITE")
                //用户名写入标题
                .setSubject(username)
                //设置有效期
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME))
                //设置签名
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();

        //将JWT写入body
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        try {
            response.getOutputStream().println(JSONResult.fillResultString(0,"",jwt));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * JWT验证方法
     * @param request
     * @return
     */
    public static Authentication getAuthentication(HttpServletRequest request) {
        //从Header中拿到token
        String token = request.getHeader(HEADER_STRING);
        if(token != null) {
            //解析Token
            Claims claims = Jwts.parser()
                    //设置签名
                    .setSigningKey(SECRET)
                    //去掉Bearer并解析
                    .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                    .getBody();
            String username = claims.getSubject();
            List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList((String) claims.get("authorities"));

            return username != null ? new UsernamePasswordAuthenticationToken(username,null,authorities) : null;
        }
        return null;
    }
}
