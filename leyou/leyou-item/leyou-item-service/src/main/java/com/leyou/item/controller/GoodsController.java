package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

//    根据条件分页查询spu
    @GetMapping("/spu/page")
    public ResponseEntity<PageResult<SpuBo>> querySpuByPage(
            @RequestParam(value = "key",required = false) String key,
            @RequestParam(value = "saleable",required = false) Boolean saleable,
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows
    ){
        PageResult<SpuBo> result=this.goodsService.querySpuByPage(key,saleable,page,rows);
        if (result==null || CollectionUtils.isEmpty(result.getItems())){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    //    根据条件分页查询Sku
    @GetMapping("/spu/page/recommendedGood")
    public ResponseEntity<List<Sku>> getRecommendedGoods(){
        List<Sku> result=this.goodsService.queryRecommendedGoods();
        return ResponseEntity.ok(result);
    }



    //    新增商品
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuBo spuBo){
        this.goodsService.saveGoods(spuBo);
        return ResponseEntity.status(   HttpStatus.CREATED).build();
    }

    //修改商品信息
    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuBo spuBo){
        this.goodsService.updateGoods(spuBo);
        return ResponseEntity.noContent().build();
    }

    //修改商品recommendedGood信息
    @PutMapping("recommendedGood")
    public ResponseEntity<Void> recommendedGood(@RequestParam("id") Long id){
        this.goodsService.recommendedGood(id);
        return ResponseEntity.noContent().build();
    }

    //删除商品信息
    @GetMapping("delete")
    public ResponseEntity<Void> DeleteGood(@RequestParam("id") Long id){
        this.goodsService.deleteGood(id);
        return ResponseEntity.noContent().build();
    }

    //根据spuid查找spu_detail
    @GetMapping("spu/detail/{spuId}")
    public ResponseEntity<SpuDetail> querySpuDetailBySpuId(@PathVariable("spuId") Long spuId){
        SpuDetail spuDeatil=this.goodsService.querySpuDetailBySpuId(spuId);
        if (spuDeatil==null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(spuDeatil);
    }

    //根据spuid查找spu_detail
//    @GetMapping("spu/detail/{spuId}")
//    public ResponseEntity<SpuDetail> querySpuDetailById(@PathVariable("spuId") Long spuId){
//        SpuDetail spuDeatil=this.goodsService.querySpuDetailById(spuId);
//        if (spuDeatil==null){
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.ok(spuDeatil);
//    }

    //根据spuid查询sku集合
    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkusBySpuId(@RequestParam("id") Long id){
        List<Sku> skus=this.goodsService.querySkusBySpuId(id);
        if (CollectionUtils.isEmpty(skus)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(skus);
    }

    @GetMapping("{id}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("id")Long id){
        Spu spu=this.goodsService.querySpuById(id);
        if (spu==null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(spu);
    }

    @GetMapping("sku/{skuId}")
    public ResponseEntity<Sku> querySkuBySkuId(@PathVariable("skuId")Long skuId){
        Sku sku=this.goodsService.querySkuBySkuId(skuId);
        if (sku==null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sku);
    }

    //查找喜欢的商品
    @GetMapping("/spu/likes")
    public ResponseEntity<List<Sku>> getLikeGoods(){
        List<Sku> result=this.goodsService.queryLikeGoods();
        return ResponseEntity.ok(result);
    }
}
