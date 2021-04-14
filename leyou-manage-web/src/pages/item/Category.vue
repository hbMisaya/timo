<template>
  <v-card>
    <v-flex xs12 sm10>
      <v-tree url="/item/category/list"
              :isEdit="isEdit"
              @handleAdd="handleAdd"
              @handleEdit="handleEdit"
              @handleDelete="handleDelete"
              @handleClick="handleClick"
      />
    </v-flex>
  </v-card>
</template>

<script>
  export default {
    name: "category",
    data() {
      return {
        isEdit: true,
      }
    },
    methods: {
      handleAdd(node) {
        // console.log("add .... ");
        // console.log(node);
        this.$http({
          method: 'post',
          url: '/item/category',
          data: this.$qs.stringify(node)
        }).then().catch();
      },
      handleEdit(id, name) {
        console.log("-------------------------------------------------------------");
        const node = {
          id: id,
          name: name
        };
        this.$http({
          method: 'put',
          url: '/item/category',
          data: this.$qs.stringify(node)
        }).then().catch();

      },
      handleDelete(id) {
        this.$http.delete("item/category/" + id).then(() => {
          this.$message.success("删除成功");
        });
        console.log("delete ... " + id)
      },
      handleClick(node) {
        console.log(node)
      }
    }
  };
</script>

<style scoped>

</style>
