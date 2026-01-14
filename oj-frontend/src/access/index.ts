import router from "@/router";
import ACCESS_ENUM from "./accessEnum";
import checkAccess from "./checkAccess";

// 从main.ts中导入store，确保它已经被正确初始化
import store from "@/store";

// 创建一个标志位，用于记录路由守卫是否已经被正确初始化
const guardInitialized = false;

// 注册路由守卫，确保在store和router安装之后执行
router.beforeEach(async (to: any, from: any, next: any) => {
  try {
    // 安全地获取用户信息，防止store或store.state为undefined
    if (!store || !store.state) {
      console.error("Store未初始化");
      next();
      return;
    }
    
    let userModule;
    let loginUser;
    
    try {
      userModule = store.state?.user;
      loginUser = userModule?.loginUser;
      console.log("登录用户信息", loginUser);
    } catch (err) {
      console.error("获取用户信息失败:", err);
      // 如果无法获取用户信息，默认允许访问
      next();
      return;
    }

    // 自动登录
    if (!loginUser || !loginUser.userRole) {
      try {
        await store.dispatch("user/getLoginUser");
        // 再次安全地获取用户信息
        userModule = store.state?.user;
        loginUser = userModule?.loginUser;
      } catch (err) {
        console.error("自动登录失败:", err);
        // 如果自动登录失败，默认允许访问
        next();
        return;
      }
    }

    const needAccess = (to.meta?.access as string) ?? ACCESS_ENUM.NOT_Login;

    // 登录的页面需要登录权限
    if (needAccess !== ACCESS_ENUM.NOT_Login) {
      if (
        !loginUser ||
        !loginUser.userRole ||
        loginUser.userRole === ACCESS_ENUM.NOT_Login
      ) {
        next(`/user/login?redirect=${to.fullPath}`);
        return;
      }

      if (!checkAccess(loginUser, needAccess)) {
        next("/noAuthority");
        return;
      }
    }
    next();
  } catch (error) {
    console.error("路由守卫错误:", error);
    // 如果发生错误，默认允许访问
    next();
  }
});
