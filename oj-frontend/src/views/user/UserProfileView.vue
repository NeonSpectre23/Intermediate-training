<template>
  <div id="userProfileView">
    <h2>用户信息</h2>
    <a-card style="max-width: 600px; margin: 0 auto">
      <a-descriptions :column="2" bordered>
        <a-descriptions-item label="用户头像">
          <a-avatar
            :size="64"
            :src="
              userInfo.userAvatar && userInfo.userAvatar.trim() !== ''
                ? userInfo.userAvatar.replace(/[`]/g, '').trim()
                : 'https://picsum.photos/200'
            "
            :alt="userInfo.userName"
          />
        </a-descriptions-item>
        <a-descriptions-item label="用户名">
          {{ userInfo.userName }}
        </a-descriptions-item>
        <a-descriptions-item label="用户简介" :span="2">
          {{ userInfo.userProfile || "未设置" }}
        </a-descriptions-item>
        <a-descriptions-item label="注册时间" :span="2">
          {{ userInfo.createTime || "未知" }}
        </a-descriptions-item>
        <a-descriptions-item label="用户角色" :span="2">
          {{ userInfo.userRole === "admin" ? "管理员" : "普通用户" }}
        </a-descriptions-item>
      </a-descriptions>

      <div style="margin-top: 24px">
        <a-button type="primary" @click="showUpdateModal = true">
          修改信息
        </a-button>
        <a-button style="margin-left: 16px" @click="router.push('/')">
          返回主页
        </a-button>
        <a-button style="margin-left: 16px" @click="handleLogout">
          退出登录
        </a-button>
      </div>
    </a-card>

    <!-- 修改信息弹窗 -->
    <a-modal
      v-model:visible="showUpdateModal"
      title="修改个人信息"
      @ok="handleUpdateSubmit"
      @cancel="handleUpdateCancel"
      width="600px"
    >
      <a-form :model="updateForm" label-align="left" auto-label-width>
        <a-form-item field="userName" label="用户名">
          <a-input v-model="updateForm.userName" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item field="userAvatar" label="用户头像">
          <div style="display: flex; align-items: center; gap: 16px">
            <div>
              <a-avatar
                :size="80"
                :src="
                  updateForm.userAvatar && updateForm.userAvatar.trim() !== ''
                    ? updateForm.userAvatar.replace(/[`]/g, '').trim()
                    : userInfo.userAvatar && userInfo.userAvatar.trim() !== ''
                    ? userInfo.userAvatar.replace(/[`]/g, '').trim()
                    : 'https://picsum.photos/200'
                "
              />
            </div>
            <div>
              <a-button @click="fileInputRef?.click()">选择本地图片</a-button>
              <input
                ref="fileInputRef"
                type="file"
                accept="image/*"
                style="display: none"
                @change="handleFileChange"
              />
              <div style="margin-top: 8px; font-size: 12px; color: #666">
                或
              </div>
              <a-input
                v-model="updateForm.userAvatar"
                placeholder="请输入头像URL"
                style="margin-top: 8px; width: 300px"
              />
            </div>
          </div>
        </a-form-item>
        <a-form-item field="userProfile" label="用户简介">
          <a-textarea
            v-model="updateForm.userProfile"
            placeholder="请输入简介"
            :rows="4"
          />
        </a-form-item>
        <a-form-item field="oldPassword" label="旧密码">
          <a-input-password
            v-model="updateForm.oldPassword"
            placeholder="请输入旧密码"
          />
        </a-form-item>
        <a-form-item field="newPassword" label="新密码">
          <a-input-password
            v-model="updateForm.newPassword"
            placeholder="请输入新密码"
          />
        </a-form-item>
        <a-form-item field="confirmPassword" label="确认新密码">
          <a-input-password
            v-model="updateForm.confirmPassword"
            placeholder="请确认新密码"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
import message from "@arco-design/web-vue/es/message";
import ACCESS_ENUM from "@/access/accessEnum";
import {
  FileControllerService,
  UserControllerService,
  UserUpdateMyRequest,
} from "../../../generated";

const router = useRouter();
const store = useStore();

// 用户信息
const userInfo = computed(() => {
  try {
    // 检查store对象是否存在
    if (!store) {
      console.error("Store对象未定义");
      return {};
    }
    // 安全地访问store.state.user
    const userModule = store.state?.user;
    if (!userModule) {
      return {};
    }
    const info = userModule.loginUser || {};
    console.log("userInfo:", info);
    console.log("userInfo.userAvatar:", info.userAvatar);
    return info;
  } catch (error) {
    console.error('Error accessing userInfo:', error);
    return {};
  }
});

// 修改信息弹窗显示状态
const showUpdateModal = ref(false);

// 文件输入框引用
const fileInputRef = ref<HTMLInputElement | null>(null);

// 修改表单
const updateForm = reactive<UserUpdateMyRequest>({
  userName: "",
  userAvatar: "",
  userProfile: "",
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

// 监听弹窗显示状态，初始化表单
watch(showUpdateModal, (newVal) => {
  if (newVal) {
    // 初始化表单数据
    updateForm.userName = userInfo.value.userName || "";
    updateForm.userAvatar = userInfo.value.userAvatar || "";
    updateForm.userProfile = userInfo.value.userProfile || "";
    // 每次打开弹窗时清空密码字段
    updateForm.oldPassword = "";
    updateForm.newPassword = "";
    updateForm.confirmPassword = "";
  }
});

// 图片压缩函数
const compressImage = (
  file: File,
  maxWidth = 1024,
  maxHeight = 1024,
  quality = 0.7
): Promise<Blob> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = (event) => {
      const img = new Image();
      img.src = event.target?.result as string;
      img.onload = () => {
        // 计算压缩后的尺寸
        let width = img.width;
        let height = img.height;

        if (width > height) {
          if (width > maxWidth) {
            height = Math.round((height * maxWidth) / width);
            width = maxWidth;
          }
        } else {
          if (height > maxHeight) {
            width = Math.round((width * maxHeight) / height);
            height = maxHeight;
          }
        }

        // 使用Canvas绘制压缩后的图片
        const canvas = document.createElement("canvas");
        canvas.width = width;
        canvas.height = height;
        const ctx = canvas.getContext("2d");
        if (!ctx) {
          reject(new Error("Failed to get canvas context"));
          return;
        }
        ctx.drawImage(img, 0, 0, width, height);

        // 将Canvas转换为Blob对象
        canvas.toBlob(
          (blob) => {
            if (blob) {
              resolve(blob);
            } else {
              reject(new Error("Failed to create blob from canvas"));
            }
          },
          file.type || "image/jpeg",
          quality
        );
      };
      img.onerror = (error) => {
        reject(error);
      };
    };
    reader.onerror = (error) => {
      reject(error);
    };
  });
};

// 处理文件选择
const handleFileChange = async (event: Event) => {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;

  console.log("Selected file:", file);

  let uploadFile: File = file;

  // 检查文件大小，如果超过1MB则压缩
  const maxSize = 1 * 1024 * 1024; // 1MB
  if (file.size > maxSize) {
    try {
      message.loading("图片压缩中...");
      console.log("Compressing image...");

      // 压缩图片，调整质量参数以确保压缩后的文件大小不超过1MB
      let compressedBlob = await compressImage(file, 1024, 1024, 0.5);

      // 如果压缩一次后还是超过1MB，继续降低质量压缩
      if (compressedBlob.size > maxSize) {
        console.log("Compressing again with lower quality...");
        compressedBlob = await compressImage(file, 1024, 1024, 0.3);
      }

      console.log("Compressed image size:", compressedBlob.size);
      message.clear();

      // 创建一个新的File对象，保留原始文件名和类型
      uploadFile = new File([compressedBlob], file.name, {
        type: file.type || "image/jpeg",
      });
    } catch (error) {
      message.error("图片压缩失败：" + (error as Error).message);
      // 清空文件输入
      if (input) {
        input.value = "";
      }
      return;
    }
  }

  try {
    message.loading("上传中...");
    // 调用文件上传API
    console.log("Calling FileControllerService.uploadFileUsingPost...");
    console.log("Upload file info:", {
      name: uploadFile.name,
      type: uploadFile.type,
      size: uploadFile.size,
      constructor: uploadFile.constructor.name,
    });

    // 手动创建FormData对象进行测试
    const testFormData = new FormData();
    testFormData.append("file", uploadFile);
    testFormData.append("biz", "user_avatar");
    console.log("Test FormData created:", testFormData);


    const res = await FileControllerService.uploadFileUsingPost(
      uploadFile,
      "user_avatar"
    );
    console.log("Upload result:", res);
    console.log("Response data type:", typeof res);
    console.log("Response code:", res.code);
    console.log("Response message:", res.message);

    if (res.code === 0) {
      // 上传成功，处理返回的URL，去除反引号和空格
      let avatarUrl = res.data;
      console.log("Original res.data:", res.data);
      console.log("res.data type:", typeof res.data);
      console.log("res.data JSON:", JSON.stringify(res.data));

      // 确保是字符串类型
      avatarUrl = String(avatarUrl);
      console.log("After String():", avatarUrl);

      // 修复：使用更可靠的方法去除反引号
      // 方法1：使用replaceAll去除所有反引号
      // 使用正则表达式，确保匹配所有反引号（包括可能的特殊情况）
      avatarUrl = avatarUrl.replace(/[`\u0060]/g, '');
      console.log("After regex replace:", avatarUrl);

      // 去除前后空格
      avatarUrl = avatarUrl.trim();
      console.log("After trim:", avatarUrl);

      // 更新表单中的头像URL
      updateForm.userAvatar = avatarUrl;
      message.success("图片上传成功");
      console.log("Updated updateForm.userAvatar:", updateForm.userAvatar);
      console.log(
        "updateForm.userAvatar JSON:",
        JSON.stringify(updateForm.userAvatar)
      );
      
      // 直接更新Vuex全局状态，确保Header立即显示新头像
      // 安全地获取当前用户信息
      const userModule = store.state?.user;
      const currentUser = userModule?.loginUser || {};
      store.commit("user/updateUser", {
        ...currentUser,
        userAvatar: avatarUrl,
      });
      // 安全地获取更新后的用户信息
      const updatedUserModule = store.state?.user;
      console.log("直接更新Vuex state后的loginUser:", updatedUserModule?.loginUser);
    } else {
      message.error("上传失败：" + res.message);
      console.error(
        "Upload failed with code:",
        res.code,
        "message:",
        res.message
      );
    }
  } catch (error) {
    console.error("Upload error (catch block):", error);
    console.error("Error type:", typeof error);
    console.error("Error stack:", error instanceof Error ? error.stack : null);
    message.error("上传失败：" + (error as Error).message);
  } finally {
    // 清空文件输入
    if (input) {
      input.value = "";
    }
    message.clear();
  }
};

// 提交修改
const handleUpdateSubmit = async () => {
  try {
    // 创建要提交的数据对象，只包含有修改的字段
    const submitData = { ...updateForm };

    // 如果userAvatar是空字符串或只包含空格，则不提交该字段
    if (!submitData.userAvatar || !submitData.userAvatar.trim()) {
      delete submitData.userAvatar;
    }

    // 调用更新用户信息API
    const res = await UserControllerService.updateMyUserUsingPost(submitData);
    if (res.code === 0) {
      // 更新成功，重新获取用户信息
      await store.dispatch("user/getLoginUser");
      message.success("信息更新成功");
      showUpdateModal.value = false;
    } else {
      message.error("更新失败：" + res.message);
    }
  } catch (error) {
    message.error("更新失败：" + (error as Error).message);
  }
};

// 取消修改
const handleUpdateCancel = () => {
  showUpdateModal.value = false;
};

// 退出登录
const handleLogout = async () => {
  try {
    // 调用退出登录API
    const res = await UserControllerService.userLogoutUsingPost();
    if (res.code === 0) {
      // 清除用户状态
      store.commit("user/updateUser", {
        userName: "未登录",
        userRole: ACCESS_ENUM.NOT_Login,
      });
      message.success("退出登录成功");
      // 跳转到登录页面
      router.push({
        path: "/user/login",
        replace: true,
      });
    } else {
      message.error("退出登录失败：" + res.message);
    }
  } catch (error) {
    message.error("退出登录失败：" + (error as Error).message);
  }
};
</script>

<style scoped>
#userProfileView {
  padding: 24px;
}
</style>
