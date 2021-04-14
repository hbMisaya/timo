<template>
  <v-card>
    <v-card-title>
      <v-spacer/>
      <div class="order-wrap">
        <div class="order-list-choose">
          <div class="order-list-option">
            开始日期：
            <date-picker :date="startTime" :option="option" :limit="limit"></date-picker>
            结束日期：
            <date-picker :date="endTime" :option="option" :limit="limit"></date-picker>
            <v-btn @click="getDataFromServer" color="primary">查询</v-btn>
          </div>
        </div>
      </div>
    </v-card-title>
    <v-divider/>
    <v-data-table
      :headers="headers"
      :items="counts"
      :pagination.sync="pagination"
      :total-items="totalGoods"
      :loading="loading"
      class="elevation-1"
    >

      <template slot="items" slot-scope="props">
        <td class="text-xs-center">{{ props.item.id }}</td>
        <td class="text-xs-center">{{ props.item.title }}</td>
        <td class="text-xs-center">{{ props.item.count }}</td>
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
          <count-form @close="closeWindow" :oldGoods="oldGoods" :isEdit="isEdit"/>
        </v-card-text>
      </v-card>
    </v-dialog>
  </v-card>

</template>


<script>
  // 导入自定义的表单组件
  import CountForm from './CountForm';
  import Datepicker from 'vue-datepicker/vue-datepicker-es6.vue'

  export default {
    name: "count",
    components: {
      CountForm,
      'date-picker': Datepicker,
    },
    data() {
      return {
        startTime: {
          time: ''
        },
        endTime: {
          time: ''
        },
        option: {
          type: 'day',
          week: ['Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa', 'Su'],
          month: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'],
          format: 'YYYY-MM-DD',
          placeholder: 'when?',
          inputStyle: {
            'display': 'inline-block',
            'padding': '6px',
            'line-height': '22px',
            'font-size': '16px',
            'border': '2px solid #fff',
            'box-shadow': '0 1px 3px 0 rgba(0, 0, 0, 0.2)',
            'border-radius': '2px',
            'color': '#5F5F5F'
          },
          color: {
            header: '#ccc',
            headerText: '#f00'
          },
          buttons: {
            ok: 'Ok',
            cancel: 'Cancel'
          },
          overlayOpacity: 0.5, // 0.5 as default
          dismissible: true // as true as default
        },
        timeoption: {
          type: 'min',
          week: ['Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa', 'Su'],
          month: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'],
          format: 'YYYY-MM-DD HH:mm'
        },
        multiOption: {
          type: 'multi-day',
          week: ['Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa', 'Su'],
          month: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'],
          format:"YYYY-MM-DD HH:mm"
        },
        limit: [{
          type: Array,
          default:function _default(){
            return [];
          }
        }],

        totalGoods: 0, // 总条数
        counts: [], // 当前页品牌数据
        loading: true, // 是否在加载中
        pagination: {}, // 分页信息
        headers: [
          {text: '商品编号', align: 'center', value: 'id'},
          {text: '商品名称', align: 'center', sortable: false, value: 'title'},
          {text: '销售数量', align: 'center', value: 'count', sortable: false,},
          {text: '操作', align: 'center', value: 'countId', sortable: false}
        ],
        show: false,// 控制对话框的显示
        oldGoods: {}, // 即将被编辑的商品信息
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
        this.$http.get("/order/orders/countList", {
            params: {
              page: this.pagination.page,// 当前页
              rows: this.pagination.rowsPerPage,// 每页大小
              startTime: this.startTime.time,// 当前页
              endTime: this.endTime.time,// 当前页
            }
          }
        ).then(({data}) => { // 这里使用箭头函数
          this.counts = data.items;
          this.totalGoods = data.total;
          // 完成赋值后，把加载状态赋值为false
          this.loading = false;
        })
      },
      editOrder(oldGoods) {
        this.$http.get("/count/counts/CrudOrder?countId=" + oldGoods.countId)
          .then(({data}) => {
            // 修改标记
            this.isEdit = true;
            // 控制弹窗可见：
            this.show = true;
            // 获取要编辑的count
            this.oldGoods = oldGoods;
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

  }
</script>

<style scoped>

</style>
