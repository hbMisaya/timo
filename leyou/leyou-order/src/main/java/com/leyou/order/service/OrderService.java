package com.leyou.order.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.service.CategoryService;
import com.leyou.order.interceptor.LoginInterceptor;
import com.leyou.order.mapper.AddressMapper;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Address;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper detailMapper;

    @Autowired
    private OrderStatusMapper statusMapper;

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

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Transactional
    public String createOrder(Order order) {
        // 生成orderId
        String orderId = String.valueOf (idWorker.nextId());

        // 获取登录用户
        UserInfo user = LoginInterceptor.getLoginUser ();
        // 初始化数据
        order.setBuyerNick(user.getUsername());
        order.setBuyerRate(false);
        order.setCreateTime(new Date());
        order.setOrderId(orderId);
        order.setUserId(user.getId());
        // 保存数据
        this.orderMapper.insertSelective(order);

        // 保存订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setCreateTime(order.getCreateTime());
        orderStatus.setStatus(1);// 初始状态为未付款

        this.statusMapper.insertSelective(orderStatus);

        // 订单详情中添加orderId
        order.getOrderDetails().forEach(od -> od.setOrderId(orderId));
        // 保存订单详情,使用批量插入功能
        this.detailMapper.insertList(order.getOrderDetails());

        logger.debug("生成订单，订单编号：{}，用户id：{}", orderId, user.getId());

        return orderId;
    }

    public Order queryById(String id) {
        // 查询订单
        Order order = this.orderMapper.selectByPrimaryKey(id);

        // 查询订单详情
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(id);
        List<OrderDetail> details = this.detailMapper.select(detail);
        order.setOrderDetails(details);

        // 查询订单状态
        OrderStatus status = this.statusMapper.selectByPrimaryKey(order.getOrderId());
        order.setStatus(status.getStatus());
        return order;
    }

    public PageResult<Order> queryUserOrderList(Integer page, Integer rows, Integer status) {
        try {
            // 分页
            PageHelper.startPage(page, rows);
            // 获取登录用户
            UserInfo user = LoginInterceptor.getLoginUser();
            // 创建查询条件
//            Page<Order> pageInfo = (Page<Order>) this.orderMapper.queryOrderList(user.getId(), status);
            List<Order> orders = this.orderMapper.queryOrderList (user.getId (), status);
            //遍历订单集合，存入状态信息
            orders.stream ().forEach (order -> {
                OrderStatus statu = this.statusMapper.selectByPrimaryKey(order.getOrderId());
                order.setStatus(statu.getStatus());
            });
            Page<Order> pageInfo = (Page<Order>) orders;
            return new PageResult<>(pageInfo.getTotal(), pageInfo);
        } catch (Exception e) {
            logger.error("查询订单出错", e);
            return null;
        }
    }


    @Transactional
    public Boolean updateStatus(String id, Integer status) {
        OrderStatus record = new OrderStatus();
        record.setOrderId(id);
        record.setStatus(status);
        // 根据状态判断要修改的时间
        switch (status) {
            case 2:
                record.setPaymentTime(new Date());// 付款
                break;
            case 3:
                record.setConsignTime(new Date());// 发货
                break;
            case 4:
                record.setEndTime(new Date());// 确认收获，订单结束
                break;
            case 5:
                record.setCloseTime(new Date());// 交易失败，订单关闭
                break;
            case 6:
                record.setCommentTime(new Date());// 评价时间
                break;
            default:
                return null;
        }
        int count = this.statusMapper.updateByPrimaryKeySelective(record);
        return count == 1;
    }

    public PageResult<Order> queryAllOrderList(Integer page, Integer rows, Integer status) {
        try {
            // 分页
            PageHelper.startPage(page, rows);

            // 创建查询条件
            Page<Order> pageInfo = (Page<Order>) this.orderMapper.selectAll ();
            return new PageResult<>(pageInfo.getTotal(), pageInfo);
        } catch (Exception e) {
            logger.error("查询订单出错", e);
            return null;
        }
    }

    public void updateOrderByOrder(Order order) {
        this.orderMapper.updateByPrimaryKeySelective (order);
    }

    public List<Address> queryUserAddress() {
        UserInfo user = LoginInterceptor.getLoginUser();
        List<Address> addresss=this.addressMapper.selectAddressByUserId(user.getId ());
        return addresss;
    }

    public String createAddress(Address address) {
        UserInfo user = LoginInterceptor.getLoginUser ();
        address.setUserId (user.getId ());
        int i = this.addressMapper.insertSelective (address);
        return "插入成功";
    }

    //根据创建时间获取订单id集合
    public List<Long> queryOrderIdByCreateTime(Date startTime, Date endTime) {
        return this.orderMapper.selectOrderIdByTime(startTime,endTime);
    }

    //根据orderId查询orderDetail
    public Long queryCountById(Long id) {
        return this.orderMapper.selectOrderDetailById(id);
    }


    //遍历Map集合获取SpuBo
    public PageResult<SpuBo> selectSpuBo(Integer page, Integer rows, Integer status, List<Long> skuIds) {
        //定义集合存放spuId
        List<Spu> spus=new ArrayList<> ();
        List<Long> spuIds=new ArrayList<> ();
        //存放SPU
        skuIds.forEach ((Long skuId)->{
            Long spuId = this.skuMapper.selectSpuIdBySkuId (skuId);
            spuIds.add (spuId);
        });
        //统计重复的spuId
        Map<Object, Long> map = new TreeMap<Object, Long> ();
        for (Object i : spuIds) {
            if (map.get(i) == null) {
                map.put(i, 1L);
            } else {
                map.put(i, map.get(i) + 1);
            }
        }
        //遍历Map集合
        for(Map.Entry<Object, Long> entry : map.entrySet()){
            Spu spu = this.spuMapper.selectByPrimaryKey (entry.getKey ());
            spu.setCount (entry.getValue ());
            spus.add (spu);
            System.out.println("重复的："+spu.getCount ()+" 值value ："+entry.getValue());
        }

        //分页
        PageHelper.startPage(page, rows);
        PageInfo<Spu> pageInfo = new PageInfo<>(spus);

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
        return new PageResult<>(pageInfo.getTotal(), spuBos);
    }
}
