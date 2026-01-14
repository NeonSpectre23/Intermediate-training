import { createStore } from "vuex";
import user from "./user";

// 创建Vuex store实例
const store = createStore({
  state: () => ({}), // 确保root state始终存在
  getters: {},
  mutations: {},
  actions: {},
  modules: {
    user,
  },
});

export default store;
