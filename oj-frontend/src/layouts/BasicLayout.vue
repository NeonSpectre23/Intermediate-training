<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header">
        <GlobalHeader />
      </a-layout-header>
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
    </a-layout>
  </div>
</template>

<style scoped>
#basicLayout .header {
  margin-bottom: 16px;
  box-shadow: #eee 1px 1px 5px;
}

#basicLayout .content {
  margin-bottom: 16px;
  padding: 20px;
  min-height: 600px;
  overflow-y: auto !important;
}

/* 确保深色主题下文本颜色正确 */
:deep(.arco-layout) {
  color: var(--color-text-1);
}
</style>

<style>
/* 全局主题响应样式 */
body {
  transition: background-color 0.3s ease;
}

/* 深色主题样式 */
[data-theme="dark"] {
  background-color: #141414 !important;
  color: #ffffff !important;
}

/* 深色主题下的内容区域 */
[data-theme="dark"] #basicLayout .content {
  background-color: #141414 !important;
  color: #ffffff !important;
}

/* 深色主题下的卡片 */
[data-theme="dark"] .arco-card {
  background-color: #1f1f1f !important;
  border-color: #333333 !important;
  color: #ffffff !important;
}

/* 深色主题下的表格 */
[data-theme="dark"] .arco-table {
  background-color: #1f1f1f !important;
  border-color: #333333 !important;
}

[data-theme="dark"] .arco-table-th,
[data-theme="dark"] .arco-table-td {
  background-color: #1f1f1f !important;
  border-color: #333333 !important;
  color: #ffffff !important;
}

/* 深色主题下的横幅 */
[data-theme="dark"] .banner {
  background: linear-gradient(135deg, #4a5568 0%, #2d3748 100%) !important;
  color: white !important;
}

/* 深色主题下的导航栏 */
[data-theme="dark"] #globalHeader {
  background-color: #1f1f1f !important;
  color: #ffffff !important;
}

/* 深色主题下的菜单 */
[data-theme="dark"] .arco-menu {
  background-color: #1f1f1f !important;
  color: #ffffff !important;
}

[data-theme="dark"] .arco-menu-item {
  background-color: #1f1f1f !important;
  color: #ffffff !important;
}

[data-theme="dark"] .arco-menu-item:hover {
  background-color: #2d2d2d !important;
}

[data-theme="dark"] .arco-menu-item-active {
  background-color: #333333 !important;
  color: #409eff !important;
}

/* 深色主题下的按钮 */
[data-theme="dark"] .arco-btn {
  background-color: #1f1f1f !important;
  color: #ffffff !important;
  border-color: #333333 !important;
}

[data-theme="dark"] .arco-btn-primary {
  background-color: #409eff !important;
  color: white !important;
}

/* 深色主题下的输入框 */
[data-theme="dark"] .arco-input {
  background-color: #1f1f1f !important;
  color: #ffffff !important;
  border-color: #333333 !important;
}

/* 深色主题下的标题 */
[data-theme="dark"] .title {
  color: #ffffff !important;
}

/* 深色主题下的统计组件 */
[data-theme="dark"] .arco-statistic {
  color: #ffffff !important;
}

[data-theme="dark"] .arco-statistic-title {
  color: #cccccc !important;
}

/* 使用更具体的选择器确保数字颜色生效 */
[data-theme="dark"] .arco-statistic .arco-statistic-content {
  color: #ffffff !important;
  font-weight: bold !important;
}

/* 确保统计数字的每一部分都可见 */
[data-theme="dark"] .arco-statistic-content-value {
  color: #ffffff !important;
  font-weight: bold !important;
}

[data-theme="dark"] .arco-statistic-content-suffix {
  color: #ffffff !important;
}

/* 深色主题下的标签 */
[data-theme="dark"] .arco-tag {
  background-color: #2d2d2d !important;
  color: #ffffff !important;
  border-color: #333333 !important;
}
</style>
<script setup lang="ts">
import GlobalHeader from "@/components/GlobalHeader.vue";
</script>
