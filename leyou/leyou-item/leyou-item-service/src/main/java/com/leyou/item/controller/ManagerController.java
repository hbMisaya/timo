package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Manager;
import com.leyou.item.service.BrandService;
import com.leyou.item.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@RequestMapping("manager")
public class ManagerController {

    @Autowired
    private ManagerService managerService;

    @PostMapping("login")
    public ResponseEntity<Void> loginUserManager(@RequestBody Manager manager) {
        this.managerService.loginUserManager (manager);
        return ResponseEntity.status (HttpStatus.CREATED).build ();
    }
}
