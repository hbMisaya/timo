package com.leyou.item.mapper;

import com.leyou.item.pojo.Spu;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface SpuMapper extends Mapper<Spu> {
    @Select ("select * from tb_spu where recommended=1")
    List<Spu> selectrecommendedGood();

    @Select ("select * from tb_spu order by id desc limit 0,6")
    List<Spu> selectLikeGoods();
}
