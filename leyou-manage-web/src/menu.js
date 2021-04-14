var menus = [
  {
    action: "home",
    title: "首页",
    path:"/index",
    items: [{ title: "统计", path: "/dashboard" }]
  },
  {
    action: "apps",
    title: "商品管理",
    path:"/item",
    items: [
      { title: "分类管理", path: "/category" },
      { title: "品牌管理", path: "/brand" },
      { title: "商品列表", path: "/list" },
      { title: "规格参数", path: "/specification" }
    ]
  },
  {
    action: "people",
    title: "会员管理",
    path:"/user",
    items: [
      { title: "会员管理", path: "/statistics" },
    ]
  },
  {
    action: "attach_money",
    title: "订单管理",
    path:"/order",
    items: [
      { title: "订单管理", path: "/order" },
      { title: "统计管理", path: "/count" },
    ]
  },
  // {
  //   action: "settings",
  //   title: "权限管理",
  //   path:"/authority",
  //   items: [
  //     { title: "权限管理", path: "/list" },
  //     { title: "角色管理", path: "/role" },
  //     { title: "人员管理", path: "/member" }
  //   ]
  // }
]

export default menus;
