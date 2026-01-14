<template>
  <a-dropdown>
    <a-button type="text">
      <template #icon>
        <icon-sun v-if="currentTheme === 'light'" />
        <icon-moon v-else-if="currentTheme === 'dark'" />
        <icon-settings v-else />
      </template>
      {{ themeText }}
    </a-button>
    <template #content>
      <div class="theme-menu">
        <div class="theme-item" @click="switchToLight">
          <icon-sun class="theme-icon" />
          <span class="theme-text">浅色主题</span>
        </div>
        <div class="theme-item" @click="switchToDark">
          <icon-moon class="theme-icon" />
          <span class="theme-text">深色主题</span>
        </div>
        <div class="theme-item" @click="switchToAuto">
          <icon-settings class="theme-icon" />
          <span class="theme-text">跟随系统</span>
        </div>
      </div>
    </template>
  </a-dropdown>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { IconSun, IconMoon, IconSettings } from '@arco-design/web-vue/es/icon';

// 当前主题状态
const currentTheme = ref<string>('auto');

// 动态主题文本
const themeText = computed(() => {
  if (currentTheme.value === 'light') {
    return '浅色';
  } else if (currentTheme.value === 'dark') {
    return '深色';
  } else {
    return '跟随系统';
  }
});

// 更新主题
const updateTheme = (theme: string) => {
  console.log('Updating theme to:', theme);
  currentTheme.value = theme;
  localStorage.setItem('theme', theme);
  // 更新文档根元素的data-theme属性
  if (theme === 'dark') {
    document.documentElement.setAttribute('data-theme', 'dark');
    console.log('Set data-theme to dark');
  } else if (theme === 'light') {
    document.documentElement.setAttribute('data-theme', 'light');
    console.log('Set data-theme to light');
  } else {
    // 跟随系统
    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
      document.documentElement.setAttribute('data-theme', 'dark');
      console.log('Set data-theme to dark (system preference)');
    } else {
      document.documentElement.setAttribute('data-theme', 'light');
      console.log('Set data-theme to light (system preference)');
    }
  }
  console.log('Current data-theme:', document.documentElement.getAttribute('data-theme'));
};

// 切换到浅色主题
const switchToLight = () => {
  console.log('Switching to light theme');
  updateTheme('light');
};

// 切换到深色主题
const switchToDark = () => {
  console.log('Switching to dark theme');
  updateTheme('dark');
};

// 切换到自动主题
const switchToAuto = () => {
  console.log('Switching to auto theme');
  updateTheme('auto');
};

// 监听系统主题变化
const handleSystemThemeChange = (e: MediaQueryListEvent) => {
  if (currentTheme.value === 'auto') {
    updateTheme('auto');
  }
};

// 初始化主题
onMounted(() => {
  // 从本地存储获取主题偏好
  const savedTheme = localStorage.getItem('theme');
  if (savedTheme) {
    currentTheme.value = savedTheme;
  } else {
    currentTheme.value = 'auto';
  }
  // 更新主题
  updateTheme(currentTheme.value);
  
  // 添加系统主题变化监听
  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
  mediaQuery.addEventListener('change', handleSystemThemeChange);
  
  // 组件卸载时移除监听
  onUnmounted(() => {
    mediaQuery.removeEventListener('change', handleSystemThemeChange);
  });
});
</script>

<style scoped>
/* 主题切换按钮样式 */
:deep(.arco-dropdown-trigger) {
  border: none;
  box-shadow: none;
  background: transparent;
}

/* 确保深色主题下按钮文字颜色正确 */
:root[data-theme="dark"] :deep(.arco-dropdown-trigger) {
  color: #ffffff !important;
}

/* 主题菜单样式 */
.theme-menu {
  padding: 8px;
  background: white;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  min-width: 160px;
}

/* 深色主题下的菜单样式 */
:root[data-theme="dark"] .theme-menu {
  background: #1f1f1f !important;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.3);
  border: 1px solid #333333;
}

/* 主题菜单项样式 */
.theme-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.2s;
  color: #1d2129;
}

/* 深色主题下的菜单项样式 */
:root[data-theme="dark"] .theme-item {
  color: #ffffff !important;
}

.theme-item:hover {
  background-color: #f5f5f5;
}

/* 深色主题下的菜单项悬停样式 */
:root[data-theme="dark"] .theme-item:hover {
  background-color: #333333 !important;
}

/* 主题图标样式 */
.theme-icon {
  margin-right: 8px;
  font-size: 16px;
}

/* 深色主题下的图标样式 */
:root[data-theme="dark"] .theme-icon {
  color: #ffffff !important;
}

/* 主题文本样式 */
.theme-text {
  font-size: 14px;
}
</style>