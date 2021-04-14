package com.leyou.auth.controller;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.service.AuthService;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import com.leyou.user.pojo.User;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
//@EnableConfigurationProperties(JwtProperties.class)
@AllArgsConstructor
public class AuthController {
    //    @Autowired
//    private AuthService authService;
//
//    @Autowired
//    private JwtProperties jwtProperties;
    private final AuthService authService;

    private final JwtProperties jwtProperties;

    @PostMapping("accredit")
    public ResponseEntity<Void> accredit(@RequestParam("username") String username,
                                         @RequestParam("password") String password,
                                         HttpServletRequest httpServletRequest,
                                         HttpServletResponse httpServletResponse) {
        String token = this.authService.accredit (username, password);
        if (StringUtils.isBlank (token)) {
            return ResponseEntity.status (HttpStatus.SC_UNAUTHORIZED).build ();
        }
        CookieUtils.setCookie (httpServletRequest, httpServletResponse, this.jwtProperties.getCookieName (), token, this.jwtProperties.getExpire () * 60);
        return ResponseEntity.ok (null);
    }

    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(HttpServletRequest httpServletRequest,
                                           HttpServletResponse httpServletResponse,@CookieValue("LY_TOKEN")String token){

        try {
            //通过jwt工具类使用公钥解析jwt
            UserInfo userInfo= JwtUtils.getInfoFromToken(token,this.jwtProperties.getPublicKey ());
            if (userInfo==null){
                return ResponseEntity.status (HttpStatus.SC_UNAUTHORIZED).build ();
            }
            //刷新jwt中的有效时间
            token = JwtUtils.generateToken (userInfo, this.jwtProperties.getPrivateKey (), this.jwtProperties.getExpire ());
            //属性cookie的有效时间
            CookieUtils.setCookie (httpServletRequest, httpServletResponse, this.jwtProperties.getCookieName (), token, this.jwtProperties.getExpire ()*60);
            return ResponseEntity.ok (userInfo);
        } catch (Exception e) {
            e.printStackTrace ();
        }
        return ResponseEntity.status (HttpStatus.SC_UNAUTHORIZED).build ();
    }
}
