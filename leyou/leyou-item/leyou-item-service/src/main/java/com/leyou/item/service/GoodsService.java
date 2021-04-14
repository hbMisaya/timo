package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service

public class GoodsService {
    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    //    根据条件分页查询spu
    public PageResult<SpuBo> querySpuByPage(String key, Boolean saleable, Integer page, Integer rows) {
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //添加查询条件
        if (StringUtils.isNotEmpty(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        //添加分页
        PageHelper.startPage(page, rows);
        //执行查询，获取spu集合
        List<Spu> spus = this.spuMapper.selectByExample(example);
        PageInfo<Spu> pageInfo = new PageInfo<>(spus);
        //spu集合转换成spubo集合
        List<SpuBo> spuBos = spus.stream().map(spu -> {
            SpuBo spuBo = new SpuBo();
            BeanUtils.copyProperties(spu, spuBo);
            //查询品牌名称
            Brand brand = this.brandMapper.selectByPrimaryKey(spu.getBrandId());
            spuBo.setBname(brand.getName());
            //查询分类名称
            List<String> names = categoryService.queryNameByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            spuBo.setCname(StringUtils.join(names, "-"));
            return spuBo;
        }).collect(Collectors.toList());

        //返回pageResult<spuBo>
        return new PageResult<>(pageInfo.getTotal(), spuBos);
    }

    //    新增商品
    @Transactional
    public void saveGoods(SpuBo spuBo) {
        //先新增spu
        spuBo.setId(null);
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());
        spuMapper.insertSelective(spuBo);
        //再去新增spu_detail
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());
        spuDetailMapper.insertSelective(spuDetail);
        //在新增sku
        saveSkuAndStock(spuBo);
        sendMsg ("insert",spuBo.getId ());

    }

    private void sendMsg(String type,Long id) {
        try {
            this.amqpTemplate.convertAndSend("item."+type,id);
        } catch (AmqpException e) {
            e.printStackTrace();
        }
    }


    private void saveSkuAndStock(SpuBo spuBo) {
        spuBo.getSkus().forEach(sku->{
            //新增stock
            sku.setId(null);
            sku.setSpuId(spuBo.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insertSelective(sku);
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insertSelective(stock);
        });
    }

    //根据spuid查找spu_detail
    public SpuDetail querySpuDetailById(Long spuId) {
        SpuDetail spuDetail = this.spuDetailMapper.selectByPrimaryKey(spuId);
        return spuDetail;
    }
    //根据spuid查询sku集合
//    public List<Sku> querySkusById(Long id) {
//        Sku record=new Sku();
//        record.setSpuId(id);
//        List<Sku> skus=this.skuMapper.select(record);
//        skus.forEach(sku -> {
//            Stock stock = this.stockMapper.selectByPrimaryKey(sku.getId());
//            sku.setStock(stock.getStock());
//        });
//        return skus;
//    }

    //修改商品信息
    @Transactional
    public void updateGoods(SpuBo spuBo) {
        //根据spuid查询要删除的sku
        Sku record = new Sku();
        record.setSpuId(spuBo.getId());
        List<Sku> sks =this.skuMapper.select(record);
        sks.forEach(sku -> {
            //先删除stock
            stockMapper.deleteByPrimaryKey(sku.getId());
        });
        //在删除sku
        Sku sku = new Sku();
        sku.setSpuId(spuBo.getId());
        this.skuMapper.delete(sku);
        //新增sku和stock
        this.saveSkuAndStock(spuBo);;
        //更新spu
        spuBo.setCreateTime(null);
        spuBo.setLastUpdateTime(new Date());
        spuBo.setValid(null);
        spuBo.setSaleable(null);
        this.spuMapper.updateByPrimaryKeySelective(spuBo);
        //更新spu_detail
        this.spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());
        sendMsg ("update", spuBo.getId ());
    }

    public SpuDetail querySpuDetailBySpuId(Long spuId) {
        SpuDetail spuDetail = this.spuDetailMapper.selectByPrimaryKey(spuId);
        return spuDetail;
    }

    public List<Sku> querySkusBySpuId(Long id) {
        Sku record=new Sku();
        record.setSpuId(id);
        List<Sku> skus=this.skuMapper.select(record);
        skus.forEach(sku -> {
            Stock stock = this.stockMapper.selectByPrimaryKey(sku.getId());
            sku.setStock(stock.getStock());
        });
        return skus;
    }


    public Spu querySpuById(Long id) {
        return this.spuMapper.selectByPrimaryKey(id);
    }

    public Sku querySkuBySkuId(Long skuId) {
        return  this.skuMapper.selectByPrimaryKey (skuId);
    }

    public void deleteGood(Long id) {
        int i = this.spuMapper.deleteByPrimaryKey (id);
    }

    public void recommendedGood(Long id) {
        Spu spu = this.spuMapper.selectByPrimaryKey (id);
        if (spu.getRecommended ()==0){
            spu.setRecommended (1L);
        }else{
            spu.setRecommended (0L);
        }
        this.spuMapper.updateByPrimaryKey (spu);
    }

    public List<Sku> queryRecommendedGoods() {
        List<Spu> spus = this.spuMapper.selectrecommendedGood ();
        List<Sku> skus=new ArrayList<> ();
        spus.stream ().forEach (spu -> {
            Sku sku = this.skuMapper.selectBySpuId (spu.getId ());
            sku.setSpuId (spu.getId ());
            skus.add (sku);
        });
        return skus;
    }


    public List<Sku> queryLikeGoods() {
        List<Spu> spus = this.spuMapper.selectLikeGoods ();
        List<Sku> skus=new ArrayList<> ();
        spus.stream ().forEach (spu -> {
            Sku sku = this.skuMapper.selectBySpuId (spu.getId ());
            sku.setSpuId (spu.getId ());
            skus.add (sku);
        });
        return skus;
    }
}
