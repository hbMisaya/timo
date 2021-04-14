package com.leyou.item.service;



import com.leyou.item.mapper.ManagerMapper;
import com.leyou.item.pojo.Manager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ManagerService {

    @Autowired
    private ManagerMapper managerMapper;

    public void loginUserManager(Manager manager) {
        String username=manager.getUsername ();
        String password=manager.getPassword ();
        System.out.println (username);
        System.out.println (password);
        if (username!=null && password!=null){
            int i = this.managerMapper.selectManager(username, password);
            if (i>0){
                System.out.println ("登录成功");
            }else{
                System.out.println ("用户或密码错误");
            }
        }
    }
}
