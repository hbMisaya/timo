package com.leyou.item.mapper;

import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Manager;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface ManagerMapper extends Mapper<Brand> {
    @Select("select * from tb_manager where username=#{username} and password=#{password}")
    int selectManager(@Param("username") String username,@Param("password")String password);
}
