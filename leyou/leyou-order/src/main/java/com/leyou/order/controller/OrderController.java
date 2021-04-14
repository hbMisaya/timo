package com.leyou.order.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.order.pojo.Address;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.service.OrderService;
import com.leyou.utils.PayHelper;
import com.leyou.utils.PayState;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("order")
@Api("订单服务接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PayHelper payHelper;



    /**
     * 创建订单
     *
     * @param order 订单对象
     * @return 订单编号
     */
    @PostMapping
    @ApiOperation(value = "创建订单接口，返回订单编号", notes = "创建订单")
    @ApiImplicitParam(name = "order", required = true, value = "订单的json对象,包含订单条目和物流信息")
    public ResponseEntity<String> createOrder(@RequestBody @Valid Order order) {
        String id = this.orderService.createOrder(order);
        return new ResponseEntity<>(id, HttpStatus.CREATED);
    }

    /**
     * 根据订单编号查询订单
     *
     * @param id
     * @return
     */
    @GetMapping("{id}")
    @ApiOperation(value = "根据订单编号查询订单，返回订单对象", notes = "查询订单")
    @ApiImplicitParam(name = "id", required = true, value = "订单的编号")
    public ResponseEntity<Order> queryOrderById(@PathVariable("id") String id) {
        Order order = this.orderService.queryById(id);
        if (order == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(order);
    }




    /**
     * 分页查询当前用户订单
     *
     * @param status 订单状态
     * @return 分页订单数据
     */
    @GetMapping("list")
    @ApiOperation(value = "分页查询当前用户订单，并且可以根据订单状态过滤", notes = "分页查询当前用户订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "当前页", defaultValue = "1", type = "Integer"),
            @ApiImplicitParam(name = "rows", value = "每页大小", defaultValue = "5", type = "Integer"),
            @ApiImplicitParam(name = "status", value = "订单状态：1未付款，2已付款未发货，3已发货未确认，4已确认未评价，5交易关闭，6交易成功，已评价", type = "Integer"),
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "订单的分页结果"),
            @ApiResponse(code = 404, message = "没有查询到结果"),
            @ApiResponse(code = 500, message = "查询失败"),
    })
    public ResponseEntity<PageResult<Order>> queryUserOrderList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "status", required = false) Integer status) {
        PageResult<Order> result = this.orderService.queryUserOrderList(page, rows, status);
        if (result == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 更新订单状态
     *
     * @param id
     * @param status
     * @return
     */
    @PutMapping("{id}/{status}")
    @ApiOperation(value = "更新订单状态", notes = "更新订单状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "订单编号", type = "Long"),
            @ApiImplicitParam(name = "status", value = "订单状态：1未付款，2已付款未发货，3已发货未确认，4已确认未评价，5交易关闭，6交易成功，已评价", type = "Integer"),
    })

    @ApiResponses({
            @ApiResponse(code = 204, message = "true：修改状态成功；false：修改状态失败"),
            @ApiResponse(code = 400, message = "请求参数有误"),
            @ApiResponse(code = 500, message = "查询失败")
    })
    public ResponseEntity<Boolean> updateStatus(@PathVariable("id") String id, @PathVariable("status") Integer status) {
        Boolean boo = this.orderService.updateStatus(id, status);
        if (boo == null) {
            // 返回400
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // 返回204
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 生成付款链接
     *
     * @param orderId
     * @return
     */
    @GetMapping("url/{id}")
    @ApiOperation(value = "生成微信扫码支付付款链接", notes = "生成付款链接")
    @ApiImplicitParam(name = "id", value = "订单编号", type = "Long")
    @ApiResponses({
            @ApiResponse(code = 200, message = "根据订单编号生成的微信支付地址"),
            @ApiResponse(code = 404, message = "生成链接失败"),
            @ApiResponse(code = 500, message = "服务器异常"),
    })
    public ResponseEntity<String> generateUrl(@PathVariable("id") String orderId) {
        // 生成付款链接
        String url = this.payHelper.createPayUrl(orderId);
        if (StringUtils.isBlank(url)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(url);
    }

    /**
     * 查询付款状态
     *
     * @param orderId
     * @return 0, 状态查询失败 1,支付成功 2,支付失败
     */
    @GetMapping("state/{id}")
    @ApiOperation(value = "查询扫码支付付款状态", notes = "查询付款状态")
    @ApiImplicitParam(name = "id", value = "订单编号", type = "Long")
    @ApiResponses({
            @ApiResponse(code = 200, message = "0, 未查询到支付信息 1,支付成功 2,支付失败"),
            @ApiResponse(code = 500, message = "服务器异常"),
    })
    public ResponseEntity<Integer> queryPayState(@PathVariable("id") String orderId) {
        PayState payState = this.payHelper.queryOrder(orderId);
        return ResponseEntity.ok(payState.getValue());
    }



    /**
     * 分页查询所有订单
     *
     * @param status 订单状态
     * @return 分页订单数据
     */
    @GetMapping("orderList")
    public ResponseEntity<PageResult<Order>> queryAllOrderList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "status", required = false) Integer status) {
        System.out.println ("------------------------------");
        PageResult<Order> result = this.orderService.queryAllOrderList(page, rows, status);
        if (result == null) {
            System.out.println ("结果为空");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("CrudOrder")
    public ResponseEntity<Order> queryOrder1ById(@RequestParam(value = "orderId", defaultValue = "0") String orderId) {

        Order order = this.orderService.queryById (orderId);
        if (order == null) {
            System.out.println ("结果为空");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(order);
    }

    @PutMapping("CrudOrder")
    public ResponseEntity<Void> updateOrderById(@RequestBody Order order){
        System.out.println ("------------------------------");
        this.orderService.updateOrderByOrder(order);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("getAddress")
    public ResponseEntity<List<Address>> queryUserAddressList() {
        List<Address> result = this.orderService.queryUserAddress();
        if (result == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("addAddress")
    public ResponseEntity<String> createAddress(@RequestBody @Valid Address address) {
        String id = this.orderService.createAddress(address);
        return new ResponseEntity<>(id, HttpStatus.CREATED);
    }

    //查询统计
    @GetMapping("countList")
    public ResponseEntity<PageResult<SpuBo>> queryAllCountList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "status", required = false) Integer status) {
        System.out.println ("进来啦----------------------------------");
        //查询一段时间内所有的订单
        String time = "2020-02-02";
        String eTime = "2022-02-02";
        if (startTime==null || startTime==""){
            startTime=time;
        }
        if (endTime==null || endTime==""){
            endTime=eTime;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date newTime =null;
        Date enTime=null;
        try {
            newTime=format.parse(startTime);
            enTime=format.parse(endTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        System.out.println ("startTime:"+startTime+"   endTime:"+endTime);
        List<Long> result = this.orderService.queryOrderIdByCreateTime(newTime,enTime);
        //存放商品信息
        List<SpuBo> spuBos=new ArrayList<> ();
        List<Long> skuIds=new ArrayList<> ();
        //遍历集合获取所有的订单
        result.forEach ((Long id)->{
            Long skuId =this.orderService.queryCountById(id);
            skuIds.add (skuId);
        });
        PageResult<SpuBo> spuBoPageResult=this.orderService.selectSpuBo(page,rows,status,skuIds);
        return ResponseEntity.ok(spuBoPageResult);
    }
}
