package com.leyou.item.api;

import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Manager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("manager")
public interface ManagerApi {
    @PostMapping("login")
    public void loginUserManager(@RequestBody Manager manager);
}
