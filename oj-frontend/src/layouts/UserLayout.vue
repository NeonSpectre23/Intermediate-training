<template>
  <div id="userLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header">
        <a-space>
          <img src="../assets/oj-logo.png" class="logo" alt="OJ平台Logo" />
          <div>CodeJudge</div>
        </a-space>
      </a-layout-header>
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
    </a-layout>
  </div>
</template>

<style scoped>
#userLayout {
  text-align: center;
  background: url("@/assets/background.png") 0% 0% / 100% 100% no-repeat;
  background-size: cover;
  background-position: center;
}

#userLayout .logo {
  width: 64px;
  height: 64px;
}

#userLayout .header {
  margin-top: 16px;
}

#userLayout .content {
  margin-bottom: 16px;
  padding: 20px;
}
</style>
<script></script>
