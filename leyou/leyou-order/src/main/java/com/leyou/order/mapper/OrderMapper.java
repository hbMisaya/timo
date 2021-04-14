package com.leyou.order.mapper;

import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.Date;
import java.util.List;


public interface OrderMapper extends Mapper<Order> {

    List<Order> queryOrderList(
            @Param("userId") Long userId,
            @Param("status") Integer status);

    @Select ("SELECT order_id FROM tb_order where create_time BETWEEN #{startTime} and #{endTime}")
    List<Long> selectOrderIdByTime(@Param ("startTime") Date startTime, @Param ("endTime") Date endTime);

    @Select ("select sku_id from tb_order_detail where order_id=#{id} LIMIT 1")
    Long selectOrderDetailById(Long id);
}
