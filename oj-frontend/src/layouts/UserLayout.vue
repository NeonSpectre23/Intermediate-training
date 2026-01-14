<template>
  <div id="userLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-sider :width="200" theme="light" class="sider">
        <div class="logo-wrapper">
          <img src="../assets/oj-logo.png" class="logo" alt="OJ平台Logo" />
          <div class="logo-text">用户中心</div>
        </div>
        <a-menu
          mode="inline"
          :selected-keys="selectedKeys"
          @menu-item-click="handleMenuClick"
          :style="{ height: '100%', borderRight: 0 }"
        >
          <a-menu-item key="/user/profile">
          <template #icon>
            <a-icon name="user" />
          </template>
          个人信息
        </a-menu-item>
        <a-menu-item key="/user/favorites">
          <template #icon>
            <a-icon name="star" />
          </template>
          我的收藏
        </a-menu-item>
        <a-menu-item key="/user/submissions">
          <template #icon>
            <a-icon name="file-text" />
          </template>
          我的提交
        </a-menu-item>
        </a-menu>
      </a-layout-sider>
      <a-layout>
        <a-layout-content class="content">
          <router-view />
        </a-layout-content>
      </a-layout>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const selectedKeys = ref<string[]>([]);

// 监听路由变化，更新选中的菜单项
watch(
  () => router.currentRoute.value.path,
  (path) => {
    selectedKeys.value = [path];
  },
  { immediate: true }
);

// 处理菜单点击
const handleMenuClick = (params: any) => {
  try {
    // 获取菜单的key值，根据Arco Design Vue的文档，menu-item-click事件返回的是包含key属性的对象
    const key = params.key || params;
    if (key) {
      // 使用router.push进行路由跳转，避免页面刷新
      router.push(key);
    } else {
      console.error('菜单key值未定义:', params);
    }
  } catch (error) {
    console.error('路由跳转失败:', error);
  }
};
</script>

<style scoped>
#userLayout {
  text-align: center;
  background: url("@/assets/background.png") 0% 0% / 100% 100% no-repeat;
  background-size: cover;
  background-position: center;
}

.sider {
  background: rgba(255, 255, 255, 0.9);
  border-right: 1px solid rgba(229, 230, 235, 0.5);
  backdrop-filter: blur(10px);
}

.logo-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 64px;
  border-bottom: 1px solid #e5e6eb;
  padding: 0 20px;
}

.logo {
  width: 40px;
  height: 40px;
  margin-right: 8px;
}

.logo-text {
  font-size: 16px;
  font-weight: bold;
  color: #1d2129;
}

.content {
  margin: 16px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}
</style>
