package com.leyou.order.mapper;

import com.leyou.order.pojo.Address;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface AddressMapper extends Mapper<Address> {
    @Select("select * from tb_address where user_id=#{userId}")
    List<Address> selectAddressByUserId(Long userId);
}
