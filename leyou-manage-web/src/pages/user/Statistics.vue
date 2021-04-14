<template>
  <v-card>
    <v-toolbar class="elevation-0">
      <v-spacer/>
      <v-flex xs3>
        <v-text-field
          append-icon="search"
          label="搜索"
          single-line
          hide-details
          v-model="filter.search"
        />
      </v-flex>
    </v-toolbar>
    <v-divider/>
    <v-data-table
      :headers="headers"
      :items="userList"
      :pagination.sync="pagination"
      :total-items="totalUser"
      :loading="loading"
      class="elevation-1"
    >
      <template slot="items" slot-scope="props">
        <td class="text-xs-center">{{ props.item.id }}</td>
        <td class="text-xs-center">{{ props.item.username}}</td>
        <td class="text-xs-center">{{ props.item.phone }}</td>
        <td class="justify-center layout px-0">
          <v-btn icon @click="editUser(props.item)">
            <i class="el-icon-edit"/>
          </v-btn>
          <v-btn icon @click="deleteUser(props.item)">
            <i class="el-icon-delete"/>
          </v-btn>
        </td>
      </template>
    </v-data-table>
    <!--弹出的对话框-->
    <v-dialog max-width="500" v-model="show" persistent scrollable>
      <v-card>
        <!--对话框的标题-->
        <v-toolbar dense dark color="primary">
          <v-toolbar-title>{{isEdit ? '修改' : '新增'}}用户</v-toolbar-title>
          <v-spacer/>
          <!--关闭窗口的按钮-->
          <v-btn icon @click="closeWindow">
            <v-icon>close</v-icon>
          </v-btn>
        </v-toolbar>
        <!--对话框的内容，表单-->
        <v-card-text class="px-5" style="height:400px">
          <user-form @close="closeWindow" :oldUser="oldUser" :isEdit="isEdit"/>
        </v-card-text>
      </v-card>
    </v-dialog>
  </v-card>
</template>
<script>
  import UserForm from './UserForm'

  export default {
    name: "statistics",
    data() {
      return {
        filter: {
          search: '', // 搜索过滤字段
        },
        totalUser: 0, // 总条数
        userList: [], // 当前页品牌数据
        loading: true, // 是否在加载中
        pagination: {}, // 分页信息
        headers: [
          {text: 'id', align: 'center', sortable: false, value: 'id'},
          {text: '用户名', align: 'center', sortable: false, value: 'username'},
          {text: '电话', align: 'center', sortable: false, value: 'phone'},
          {text: '操作', align: 'center', sortable: false}
        ],
        show: false,// 控制对话框的显示
        oldUser: {}, // 即将被编辑的商品信息
        isEdit: false, // 是否是编辑
        step: 1, // 子组件中的步骤线索引，默认为1
      };
    },
    watch: {
      pagination: { // 监视pagination属性的变化
        deep: true, // deep为true，会监视pagination的属性及属性中的对象属性变化
        handler() {
          // 变化后的回调函数，这里我们再次调用getDataFromServer即可
          this.getDataFromServer();
        }
      },
      filter: {// 监视搜索字段
        handler() {
          this.getDataFromServer();
        },
        deep: true
      }
    },
    methods: {
      getDataFromServer() { // 从服务的加载数的方法。
        // 发起请求
        this.$http.get("/user/user/page", {
          params: {
            key: this.filter.search, // 搜索条件
            page: this.pagination.page,// 当前页
            rows: this.pagination.rowsPerPage,// 每页大小
          }
        }).then(resp => { // 这里使用箭头函数
          this.userList = resp.data.items;
          this.totalUser = resp.data.total;
          // 完成赋值后，把加载状态赋值为false
          this.loading = false;
        })
      },
      closeWindow() {
        console.log(1)
        // 重新加载数据
        this.getDataFromServer();
        // 关闭窗口
        this.show = false;
        // 将步骤调整到1
        this.step = 1;
      },
      previous() {
        if (this.step > 1) {
          this.step--
        }
      },
      editUser(oldUser) {
        this.$http.get("/user/user/id/" + oldUser.id)
          .then(({data}) => {
            // 修改标记
            this.isEdit = true;
            // 控制弹窗可见：
            this.show = true;
            // 获取要编辑的user
            this.oldUser = data;
            // 回显商品分类
          })
      },
      deleteUser(oldUser){
          if (oldUser.id!=null) {
            this.$message.confirm('此操作将永久删除该用户, 是否继续?').then(
              () => {
                //发起删除请求，删除单条数据
                this.$http.delete("/user/user/" + oldUser.id).then(() => {
                  this.$message.success("删除成功！");
                  this.getDataFromServer();
                }).catch(() => {
                  this.$message.error("删除失败！");
                })
              }
            ).catch(() => {
              this.$message.info("删除已取消！");
            });
          }
      },
    },
    components: {
      UserForm
    }
  }
</script>

<style scoped>

</style>
