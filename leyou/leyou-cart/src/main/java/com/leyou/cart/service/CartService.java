package com.leyou.cart.service;

import com.ctc.wstx.util.StringUtil;
import com.leyou.cart.client.GoodsClient;
import com.leyou.cart.interceptor.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.Sku;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.print.attribute.HashAttributeSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GoodsClient goodsClient;

    private static final String KEY_PREFIX="user:cart:";

    public void addCart(Cart cart) {
        //获取用户信息
        UserInfo userInfo =LoginInterceptor.getUserInfo ();
        //查询购物车记录
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps (KEY_PREFIX + userInfo.getId ());
        String key=cart.getSkuId ().toString ();
        Integer num=cart.getNum ();
        //判断当前商品是否在购物车中
        if(hashOperations.hasKey (key)){
            //在的话更新数量
            String cartJson = hashOperations.get (key).toString ();
            cart = JsonUtils.parse (cartJson, Cart.class);
            cart.setNum (cart.getNum ()+num);
            hashOperations.put (key, JsonUtils.serialize (cart));
        }else {
            Sku sku = this.goodsClient.querySkuBySkuId (cart.getSkuId ());

            //不在的话新增购物车
            cart.setUserId (userInfo.getId ());
            cart.setTitle (sku.getTitle ());
            cart.setOwnSpec (sku.getOwnSpec ());
            cart.setPrice (sku.getPrice ());
            cart.setImage (StringUtils.isBlank (sku.getImages ())?"":StringUtils.split (sku.getImages (),",")[0]);
            hashOperations.put (key, JsonUtils.serialize (cart));
        }
    }

    public List<Cart> queryCarts() {
        UserInfo userInfo = LoginInterceptor.getUserInfo ();
        //判断用户是否有购物车记录
        if(!this.redisTemplate.hasKey (KEY_PREFIX+userInfo.getId ())){
            return null;
        }
        //获取用户的购物车记录
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps (KEY_PREFIX + userInfo.getId ());
        //获取购物车Map中的所有cart值集合
        List<Object> cartsJson = hashOperations.values ();
        //如果购物车集合为空，直接返回NULL
        if (CollectionUtils.isEmpty (cartsJson)){
            return null;
        }
        //把List<Object>集合转化为List<Cart>集合
        return cartsJson.stream ().map
                (cartJson -> JsonUtils.parse (cartJson.toString (), Cart.class)).collect (Collectors.toList ());

    }

    public void updateNum(Cart cart) {
        //获取用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo ();
        //判断用户是否有购物车记录
        if(!this.redisTemplate.hasKey (KEY_PREFIX+userInfo.getId ())){
            return ;
        }
        Integer num=cart.getNum ();
        //获取购物车信息
        BoundHashOperations<String, Object, Object> hashOperations = this.redisTemplate.boundHashOps (KEY_PREFIX + userInfo.getId ());
        //修改购物信息
        String cartJson = hashOperations.get (cart.getSkuId ().toString ()).toString ();
        cart = JsonUtils.parse (cartJson, Cart.class);
        cart.setNum (num);
        hashOperations.put (cart.getSkuId ().toString (),JsonUtils.serialize (cart) );
    }

    public void deleteCart(String skuId) {
        UserInfo userInfo = LoginInterceptor.getUserInfo ();
        BoundHashOperations<String, Object, Object> hashOperations = this.redisTemplate.boundHashOps (KEY_PREFIX + userInfo.getId ());
        hashOperations.delete ("key",skuId);
    }
}
