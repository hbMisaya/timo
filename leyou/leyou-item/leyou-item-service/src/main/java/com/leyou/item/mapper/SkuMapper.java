package com.leyou.item.mapper;

import com.leyou.item.pojo.Sku;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface SkuMapper extends Mapper<Sku> {

    @Select ("select * from tb_sku where spu_id=#{id} LIMIT 0,1")
    Sku selectBySpuId(Long id);

    @Select ("select spu_id from tb_sku where id=#{key}")
    Long selectSpuIdBySkuId(Object key);
}
