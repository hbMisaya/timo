<template>
  <v-form v-model="valid" ref="UserForm">
    <v-text-field
      v-model="user.username"
      label="请输入用户名名称"
      required
    />

    <v-text-field
      v-model="user.phone"
      label="请输入手机号"
      required
    />
    <v-text-field
      v-model="user.created"
      label="创建时间"
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
    name: "user-form",
    props: {
      oldUser: {type: Object},
      isEdit: {type: Boolean, default: false},
    },
    data() {
      return {
        valid: false,
        user: {
          id: "",
          username: "",
          created: "",
          phone: "",
        },
      }
    },
    watch: {
      oldUser: {
        deep: true,
        handler(val) {
          if (val) {
            this.user = Object.deepCopy(val);
          } else {
            this.clear();
          }
        }
      }
    },
    methods: {
      // 提交表单
      submit() {
        this.user.id = this.oldUser.id;
        this.$http({
          method: this.isEdit ? 'put' : 'post',
          url: "/user/user/updateUser",
          data: this.user,
        }).then(() => {
          //关闭对话框
          this.$emit('reload');
          this.$message.success("保存成功！");
          this.clear();
        }).catch(
          () => {
            this.$message.success("保存失败！");
          });
      },
        clear()
        {
          // 重置表单
          this.$refs.UserForm.reset();
          this.oldUser = null;
        }
      ,
        closeWindow()
        {
          this.$emit("close");
        }
      },
    }
</script>

<style scoped>

</style>
