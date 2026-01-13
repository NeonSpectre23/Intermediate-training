// initial state
import { StoreOptions } from "vuex";
import ACCESS_ENUM from "@/access/accessEnum";
import { UserControllerService } from "../../generated";
export default {
  namespaced: true,
  state: () => ({
    loginUser: {
      userName: "未登录",
    },
  }),
  actions: {
    async getLoginUser({ commit, state }, payload) {
      try {
        // 从远程请求获取登录信息
        const res = await UserControllerService.getLoginUserUsingGet();
        if (res.code == 0 && res.data) {
          commit("updateUser", res.data);
        } else if (res.code === 40100) {
          // 明确的未登录状态
          commit("updateUser", {
            userName: "未登录",
            userRole: ACCESS_ENUM.NOT_Login,
          });
        } else {
          // 其他异常情况
          console.warn("获取用户信息失败:", res);
        }
      } catch (error) {
        // 网络错误或其他异常
        console.error("获取用户信息异常:", error);
      }
    },
  },
  mutations: {
    updateUser(state, payload) {
      state.loginUser = payload;
    },
  },
} as StoreOptions<any>;
