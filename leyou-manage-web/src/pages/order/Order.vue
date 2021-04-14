<template>
  <v-card>
    <v-card-title>
      <!--搜索框，与search属性关联-->
      <v-spacer/>
    </v-card-title>
    <v-divider/>
    <v-data-table
      :headers="headers"
      :items="orders"
      :pagination.sync="pagination"
      :total-items="totalOrders"
      :loading="loading"
      class="elevation-1"
    >
      <template slot="items" slot-scope="props">
        <td class="text-xs-center">{{ props.item.orderId }}</td>
        <td class="text-xs-center">{{ props.item.buyerNick }}</td>
        <td class="text-xs-center">{{ props.item.createTime }}</td>
        <td class="text-xs-center">{{ props.item.actualPay }}</td>
        <td class="justify-center layout px-0">
          <v-btn icon @click="editOrder(props.item)">
            <i class="el-icon-edit"/>
          </v-btn>
        </td>
      </template>
    </v-data-table>
    <!--弹出的对话框-->
    <v-dialog max-width="500" v-model="show" persistent scrollable>
      <v-card>
        <!--对话框的标题-->
        <v-toolbar dense dark color="primary">
          <v-toolbar-title>{{isEdit ? '修改' : '新增'}}订单</v-toolbar-title>
          <v-spacer/>
          <!--关闭窗口的按钮-->
          <v-btn icon @click="closeWindow">
            <v-icon>close</v-icon>
          </v-btn>
        </v-toolbar>
        <!--对话框的内容，表单-->
        <v-card-text class="px-5" style="height:400px">
          <order-form @close="closeWindow" :oldOrder="oldOrder" :isEdit="isEdit"/>
        </v-card-text>
      </v-card>
    </v-dialog>
  </v-card>
</template>


<script>
  // 导入自定义的表单组件
  import OrderForm from './OrderForm'

  export default {
    name: "order",
    data() {
      return {
        search: '', // 搜索过滤字段
        totalOrders: 0, // 总条数
        orders: [], // 当前页品牌数据
        loading: true, // 是否在加载中
        pagination: {}, // 分页信息
        headers: [
          {text: '订单编号', align: 'center', value: 'orderId'},
          {text: '购买人', align: 'center', sortable: false, value: 'buyerNick'},
          {text: '订单创建时间', align: 'center', sortable: false, value: 'createTime'},
          {text: '实付金额', align: 'center', value: 'actualPay', sortable: false,},
          {text: '操作', align: 'center', value: 'orderId', sortable: false}
        ],
        show: false,// 控制对话框的显示
        oldOrder: {}, // 即将被编辑的品牌数据
        isEdit: false, // 是否是编辑
        step: 1, // 子组件中的步骤线索引，默认为1
      }
    },
    mounted() { // 渲染后执行
      // 查询数据
      this.getDataFromServer();
    },
    watch: {
      pagination: { // 监视pagination属性的变化
        deep: true, // deep为true，会监视pagination的属性及属性中的对象属性变化
        handler() {
          // 变化后的回调函数，这里我们再次调用getDataFromServer即可
          this.getDataFromServer();
        }
      },
      search: { // 监视搜索字段
        handler() {
          this.getDataFromServer();
        }
      }
    },
    methods: {
      getDataFromServer() { // 从服务的加载数的方法。
        // 发起请求
        this.$http.get("/order/orders/orderList", {
            params: {
              page: this.pagination.page,// 当前页
              rows: this.pagination.rowsPerPage,// 每页大小
            }
          }
        ).then(({data}) => { // 这里使用箭头函数
          this.orders = data.items;
          this.totalOrders = data.total;
          // 完成赋值后，把加载状态赋值为false
          this.loading = false;
        })
      },
      editOrder(oldOrder) {
        this.$http.get("/order/orders/CrudOrder?orderId=" + oldOrder.orderId)
          .then(({data}) => {
            // 修改标记
            this.isEdit = true;
            // 控制弹窗可见：
            this.show = true;
            // 获取要编辑的order
            this.oldOrder = oldOrder;
            // 回显商品分类
          })
      },
      closeWindow() {
        // 重新加载数据
        this.getDataFromServer();
        // 关闭窗口
        this.show = false;
      },

    },
    components: {
      OrderForm
    }
  }
</script>

<style scoped>

</style>
