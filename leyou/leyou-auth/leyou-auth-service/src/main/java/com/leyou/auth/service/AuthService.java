package com.leyou.auth.service;

import com.leyou.auth.client.UserClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JwtUtils;
import com.leyou.user.pojo.User;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {

    //    @Autowired
//    private UserClient userClient;
//
//    @Autowired
//    private JwtProperties jwtProperties;
    private final UserClient userClient;

    private final JwtProperties jwtProperties;

    public String accredit(String username, String password) {
        //根据用户名和密码查询
        User user = userClient.queryUser (username, password);

        //判断User是否为空
        if (user == null) {
            return null;
        }
        //通过jwtUtils生成jwt类型的token
        UserInfo userInfo = new UserInfo ();
        userInfo.setId (user.getId ());
        userInfo.setUsername (user.getUsername ());
        try {
            return JwtUtils.generateToken (userInfo, this.jwtProperties.getPrivateKey (), this.jwtProperties.getExpire ());
        } catch (Exception e) {
            e.printStackTrace ();
        }
        //
        return null;
    }



}
