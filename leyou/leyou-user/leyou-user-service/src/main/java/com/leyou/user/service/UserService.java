package com.leyou.user.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;



    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String key_perfix = "USER:VERIFY:";

    public Boolean checkUser(String data, Integer type) {
        User record = new User ();
        if (type == 1) {
            record.setUsername (data);
        } else if (type == 2) {
            record.setPhone (data);
        } else {
            return null;
        }
        return this.userMapper.selectCount (record) == 0;
    }


    public void sendVerifyCode(String phone) {
        if (StringUtils.isBlank (phone)) {
            return;
        }
        //生成验证码
        String code = NumberUtils.generateCode (6);
        //发送消息到rabbitmq
        Map<String, String> msg = new HashMap<> ();
        msg.put ("phone", phone);
        msg.put ("code", code);
        this.amqpTemplate.convertAndSend ("leyou.sms.exchange", "verifycode.sms", msg);
        //把验证码保存到redis
        this.redisTemplate.opsForValue ().set (key_perfix + phone, code, 5, TimeUnit.MINUTES);
        //
    }

    public void register(User user, String code) {
        //查询redis中的验证码
        String redisCode = this.redisTemplate.opsForValue ().get (key_perfix + user.getPhone ());
        //校验验证码
        if (!StringUtils.equals (code, redisCode)) {
            return;
        }
        //生成盐
        String salt = CodecUtils.generateSalt ();
        user.setSalt (salt);
        //加盐加密
        user.setPassword (CodecUtils.md5Hex (user.getPassword (), salt));
        //新增用户
        user.setId (null);
        user.setCreated (new Date ());
        this.userMapper.insertSelective (user);
        //删除redis中验证码
        this.redisTemplate.delete (key_perfix + user.getPhone ());
    }


    public User queryUser(String username, String password) {
        User record = new User ();
        record.setUsername (username);
        User user = this.userMapper.selectOne (record);
        //判定user是否为空
        if (user==null){
            throw new RuntimeException("用户不存在");
        }
        //获取盐，对用户输入的密码加盐加密
        password=CodecUtils.md5Hex (password, user.getSalt ());
        //和数据库中数据比较
        if (!StringUtils.equals (password, user.getPassword ())){
            throw new RuntimeException("用户密码有误");
        }
        return user;
    }

    public PageResult<User> queryUserByPage(String key, Integer page, Integer rows) {
        Example example = new Example(User.class);
        Example.Criteria criteria = example.createCriteria();
        //添加查询条件
        if (StringUtils.isNotEmpty(key)) {
            criteria.andLike("username", "%" + key + "%");
        }
        //添加分页
        PageHelper.startPage(page, rows);
        //执行查询，获取spu集合
        List<User> users = this.userMapper.selectByExample(example);
        PageInfo<User> pageInfo = new PageInfo<>(users);

        //返回pageResult<spuBo>
        return new PageResult<>(pageInfo.getTotal(), users);
    }

    public User queryUserById(Long id) {
        return this.userMapper.selectByPrimaryKey (id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateUser(User user) {
        System.out.println (user.getId ());
        int i = this.userMapper.updateByPrimaryKeySelective (user);
        if (i>0){
            System.out.println ("修改成功");
        }else{
            System.out.println ("修改失败");
        }
    }

    public void deleteUser(Long id) {
        this.userMapper.deleteByPrimaryKey(id);
    }


}
