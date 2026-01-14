import { createApp } from "vue";
import App from "./App.vue";
import ArcoVue from "@arco-design/web-vue";
import "@arco-design/web-vue/dist/arco.css";
import router from "./router";
import store from "./store";
import "@/plugins/axios";
import "bytemd/dist/index.css";

// 创建Vue应用
const app = createApp(App);

// 使用ArcoVue，并配置主题
app.use(ArcoVue, {
  // 配置主题模式
  componentPrefix: 'a',
});

// 使用其他插件
app.use(store);
app.use(router);

// 导入路由守卫，确保在store和router安装之后导入
import "@/access";

// 挂载应用
app.mount("#app");
