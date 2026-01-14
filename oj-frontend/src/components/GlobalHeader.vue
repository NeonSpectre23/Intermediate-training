<template>
  <a-row id="globalHeader" align="center" :wrap="false">
    <a-col flex="auto">
      <a-menu
        mode="horizontal"
        :selected-keys="selectedKeys"
        @menu-item-click="doMenuClick"
      >
        <a-menu-item
          key="0"
          :style="{ padding: 0, marginRight: '38px' }"
          disabled
        >
          <div class="title-bar">
            <img class="logo" src="../assets/oj-logo.png" alt="OJ平台Logo" />
            <div class="title">CodeJudge</div>
          </div>
        </a-menu-item>
        <a-menu-item v-for="item in visibleRoutes" :key="item.path">
          {{ item.name }}
        </a-menu-item>
      </a-menu>
    </a-col>
    <a-col flex="150px" style="display: flex; align-items: center; gap: 8px; justify-content: flex-end;">
      <!-- 主题切换 -->
      <ThemeSwitcher />
      
      <template
        v-if="
          loginUser?.userName &&
          loginUser.userName !== '未登录'
        "
      >
        <!-- 已登录状态显示头像 -->
        <a-avatar
            :size="32"
            :src="getAvatarUrl()"
            :alt="loginUser.userName"
            class="login-status"
            @click="handleLoginStatusClick"
            :style="{ cursor: 'pointer' }"
          />
      </template>
      <template v-else>
        <!-- 未登录状态 -->
        <div
          class="login-status"
          @click="handleLoginStatusClick"
          :style="{ cursor: 'pointer', textDecoration: 'underline' }"
        >
          未登录
        </div>
      </template>
    </a-col>
  </a-row>
</template>

<script setup lang="ts">
import { routes } from "@/router/routes";
import { useRoute, useRouter } from "vue-router";
import { computed, ref, watch } from "vue";
import { useStore } from "vuex";
import checkAccess from "@/access/checkAccess";
import ACCESS_ENUM from "@/access/accessEnum";
import ThemeSwitcher from "./ThemeSwitcher.vue";

const router = useRouter();
const store = useStore();

// 计算属性：登录用户信息
const loginUser = computed(() => {
  try {
    // 检查store对象是否存在
    if (!store) {
      console.error("Store对象未定义");
      return { userName: '未登录' };
    }
    // 安全地访问store.state.user
    const userModule = store.state?.user;
    if (!userModule) {
      return { userName: '未登录' };
    }
    const user = userModule.loginUser || { userName: '未登录' };
    console.log('GlobalHeader loginUser:', user);
    console.log('GlobalHeader userAvatar:', user.userAvatar);
    console.log('GlobalHeader processed avatar:', (user.userAvatar || '').replace(/[`]/g, '').trim());
    return user;
  } catch (error) {
    console.error('Error accessing loginUser:', error);
    return { userName: '未登录' };
  }
});

// 展示在菜单的路由数组
const visibleRoutes = computed(() => {
  return routes.filter((item, index) => {
    if (item.meta?.hideInMenu) {
      return false;
    }
    // todo 根据权限过滤菜单
    const access = item.meta?.access;
    if (!checkAccess(loginUser.value, access)) {
      return false;
    }
    return true;
  });
});

// 默认主页
const selectedKeys = ref<string[]>([]);

// 路由跳转后，更新选中的菜单项
watch(
  () => router.currentRoute.value,
  (route) => {
    selectedKeys.value = [route.path];
  },
  { immediate: true }
);

// 初始加载时获取登录用户信息
store.dispatch("user/getLoginUser");

const doMenuClick = (key: string) => {
  router.push({
    path: key,
  });
};

// 获取头像URL，添加时间戳防止缓存
const getAvatarUrl = () => {
  if (!loginUser.value?.userAvatar) {
    return '';
  }
  // 直接使用简单可靠的方法处理头像URL
  const rawAvatar = String(loginUser.value.userAvatar);
  console.log('Raw avatar URL:', rawAvatar);
  
  // 直接截取URL部分，去除可能的反引号和其他字符
  // 寻找http开头，直到字符串结束
  const httpIndex = rawAvatar.indexOf('http');
  if (httpIndex === -1) {
    console.log('No valid http URL found');
    return '';
  }
  
  // 提取完整URL
  let cleanUrl = rawAvatar.substring(httpIndex);
  console.log('Extracted URL:', cleanUrl);
  
  // 去除末尾可能的反引号
  if (cleanUrl.endsWith('`')) {
    cleanUrl = cleanUrl.slice(0, -1);
  }
  
  // 添加时间戳防止缓存
  const finalUrl = cleanUrl + '?t=' + Date.now();
  console.log('Final avatar URL:', finalUrl);
  return finalUrl;
};

// 处理登录状态点击事件
const handleLoginStatusClick = () => {
  if (!loginUser.value || !loginUser.value.userName || loginUser.value.userName === "未登录") {
    // 未登录状态，跳转到登录页面
    router.push({
      path: "/user/login",
    });
  } else {
    // 已登录状态，跳转到用户信息主页
    router.push({
      path: "/user/profile",
    });
  }
};
</script>

<style scoped>
.title-bar {
  display: flex;
  align-items: center;
}
.logo {
  width: 64px;
  height: 64px;
  margin-right: 8px;
  object-fit: contain;
}
.title {
  font-size: 18px;
  font-weight: bold;
}
</style>
