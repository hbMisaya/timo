<template>
  <v-form v-model="valid" ref="OrderForm">
    <v-text-field
      v-model="order.receiver"
      label="收货人名称"
      required
    />
    <v-text-field
      v-model="order.receiverMobile"
      label="收货人电话"
      required
    />
    <v-text-field
      v-model="order.receiverState"
      label="收货人省份"
      required
    />
    <v-text-field
      v-model="order.receiverCity"
      label="收货人城市"
      required
    />
    <v-text-field
      v-model="order.receiverDistrict"
      label="收货人县"
      required
    />
    <v-text-field
      v-model="order.receiverAddress"
      label="收货人地址"
      required
    />
    <v-layout class="my-4" row>
      <v-spacer/>
      <v-btn @click="submit" color="primary">提交</v-btn>
      <v-btn @click="clear">重置</v-btn>
    </v-layout>
  </v-form>

</template>

<script>

  export default {
    name: "order-form",
    props: {
      oldOrder: {type: Object},
      isEdit: {type: Boolean, default: false},
    },
    data() {
      return {
        valid: false,
        order: {
          orderId: "",// id
          totalPay: 0,// 总金额
          actualPay: 0,// 实付金额
          paymentType: 0,// 支付类型，1、在线支付，2、货到付款
          promotionIds: "",// 参与促销活动的id
          postFee: "",// 邮费
          createTime: [],// 创建时间
          shippingName: "",// 物流名称
          shippingCode: "",// 物流单号
          userId: 0,// 用户id
          buyerMessage: "",// 买家留言
          buyerNick: "",// 买家昵称
          buyerRate: true,// 买家是否已经评价
          receiver: "",// 收货人全名
          receiverMobile: "",// 移动电话
          receiverState: "", // 省份
          receiverCity: "",// 城市
          receiverDistrict: "",// 区/县
          receiverAddress: "",// 收货地址，如：xx路xx号
          receiverZip: "",// 邮政编码,如：310001
          invoiceType: "",// 发票类型，0无发票，1普通发票，2电子发票，3增值税发票
          sourceType: "",// 订单来源 1:app端，2：pc端，3：M端，4：微信端，5：手机qq端
          orderDetails: [],
        },
      }
    },
    watch:{
      oldOrder:{
        deep:true,
        handler(val){
          if(val){
            this.order=Object.deepCopy(val);
          }else{
            this.clear();
          }
        }
      }
    },

    methods: {
      // 提交表单
      submit() {
        // 1、表单校验
        if (this.$refs.OrderForm.validate()) {
          // 2、定义一个请求参数对象，通过解构表达式来获取order中的属性
          this.order.orderId = this.oldOrder.orderId;
          const param = this.order;
          // 3、数据库中只要保存分类的id即可，因此我们对categories的值进行处理,只保留id，并转为字符串
          param.orderId = this.oldOrder.orderId;
          console.log(param.orderId);
          // 4、将字母都处理为大写
          this.$http({
            method:  'put',
            url: "/order/orders/CrudOrder",
            data: param,
          }).then(() => {
            //关闭对话框
            this.$emit('reload');
            this.$message.success("保存成功！");
            this.clear();
          }).catch(
            () => {
              this.$message.success("保存失败！");
            }
          )
          ;
        }
      },
      clear() {
        // 重置表单
        this.$refs.OrderForm.reset();
        // 需要手动清空商品分类
        this.oldOrder = null;
      },
      closeWindow() {
        this.$emit("close");
      }
    },
  }
</script>

<style scoped>

</style>
