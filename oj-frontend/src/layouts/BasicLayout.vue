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
  background-color: rgba(255, 255, 255, 0.3); /* 进一步降低透明度，让背景图片更清晰可见 */
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 设置所有布局组件为透明 */
.arco-layout {
  background-color: transparent !important;
}

/* 设置main元素为透明 */
main {
  background-color: transparent !important;
}

/* 设置所有div元素为透明 */
div {
  background-color: transparent !important;
}

/* 恢复特定组件的背景色 */
.header,
.content,
.arco-card,
.arco-table,
.arco-menu {
  background-color: inherit !important;
}

/* 确保#basicLayout下的元素样式正确 */
#basicLayout .header {
  background-color: rgba(255, 255, 255, 0.8) !important;
}

#basicLayout .content {
  background-color: rgba(255, 255, 255, 0.3) !important;
}

/* 深色主题下的特定组件背景色 */
[data-theme="dark"] #basicLayout .header {
  background-color: rgba(31, 31, 31, 0.8) !important;
}

[data-theme="dark"] #basicLayout .content {
  background-color: rgba(31, 31, 31, 0.3) !important;
}

/* 确保深色主题下文本颜色正确 */
:deep(.arco-layout) {
  color: var(--color-text-1);
}

/* 浅色主题下的标签文字颜色 */
:root:not([data-theme="dark"]) .arco-tag {
  color: #000000 !important;
}

/* 浅色主题下的WA状态标签文字颜色 */
:root:not([data-theme="dark"]) .arco-tag-warning {
  color: #000000 !important;
}

/* 浅色主题下的自定义颜色标签文字颜色 */
:root:not([data-theme="dark"]) .arco-tag-custom-color {
  color: #000000 !important;
}

/* 浅色主题下所有标签的文字颜色，确保覆盖所有情况 */
:root:not([data-theme="dark"]) span[class^="arco-tag"] {
  color: #000000 !important;
}

/* 确保浅色主题下内容区域文字颜色为黑色 */
:root:not([data-theme="dark"]) #basicLayout .content {
  color: #000000 !important;
}
</style>

<style>
/* 全局主题响应样式 */
body {
  transition: background-image 0.3s ease;
  background-image: url('@/assets/background.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
}

/* 深色主题样式 */
[data-theme="dark"] {
  background-image: url('@/assets/background_dark.png') !important;
  background-size: cover !important;
  background-position: center !important;
  background-repeat: no-repeat !important;
  background-attachment: fixed !important;
  color: #ffffff !important;
}

/* 深色主题下的内容区域 */
[data-theme="dark"] #basicLayout .content {
  background-color: rgba(20, 20, 20, 0.5) !important; /* 降低透明度，让背景图片更清晰可见 */
  color: #ffffff !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
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
