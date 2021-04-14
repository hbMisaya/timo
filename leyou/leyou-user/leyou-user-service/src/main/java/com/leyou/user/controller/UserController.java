package com.leyou.user.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkUser(@PathVariable("data")String data,@PathVariable("type")Integer type){
        Boolean boo=this.userService.checkUser(data,type);
        if (boo==null){
            return ResponseEntity.badRequest ().build ();
        }
        return ResponseEntity.ok (boo);
    }

    @PostMapping("code")
    public ResponseEntity<Void> sendVerifyCode(@RequestParam("phone")String phone){
        this.userService.sendVerifyCode(phone);
        return ResponseEntity.status (HttpStatus.CREATED).build ();
    }

    @PostMapping("register")
    public ResponseEntity<Void> register(@Valid User user, @RequestParam("code")String code){
         this.userService.register(user,code);
        return ResponseEntity.status (HttpStatus.CREATED).build ();
    }

    @GetMapping("query")
    public ResponseEntity<User> queryUser(@RequestParam("username") String username,@RequestParam("password")String password){
        User user=this.userService.queryUser(username,password);
        if (user==null){
            return ResponseEntity.badRequest ().build ();
        }
        return ResponseEntity.ok (user);
    }

    //    根据条件分页查询spu
    @GetMapping("/user/page")
    public ResponseEntity<PageResult<User>> querySpuByPage(
            @RequestParam(value = "key",required = false) String key,
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows
    ){
        PageResult<User> result=this.userService.queryUserByPage(key,page,rows);
        if (result==null || CollectionUtils.isEmpty(result.getItems())){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("user/id/{id}")
    public ResponseEntity<User> queryUserById(@PathVariable("id")Long id){
        User user=this.userService.queryUserById(id);
        if (user==null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("user/updateUser")
    public ResponseEntity<Void> updateBrand(@RequestBody User user){
        this.userService.updateUser(user);
        return  ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    /**
     * 删除tb_brand中的数据,单个删除、多个删除二合一
     * @param
     * @return
     */
    @DeleteMapping("user/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id){
        this.userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }



}
