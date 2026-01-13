<template>
  <div id="obfuscateView">
    <h2>代码混淆</h2>
    <div
      style="margin-bottom: 16px; display: flex; gap: 16px; align-items: center"
    >
      <div style="display: flex; gap: 8px; align-items: center">
        <span>语言：</span>
        <a-select
          v-model="selectedLanguage"
          style="width: 120px"
          placeholder="选择语言"
          @change="handleLanguageChange"
        >
          <a-option value="c">C</a-option>
          <a-option value="python">Python</a-option>
        </a-select>
      </div>
      <div style="display: flex; gap: 8px; align-items: center">
        <span>混淆方案：</span>
        <a-select
          v-model="selectedScheme"
          style="width: 120px"
          placeholder="选择方案"
        >
          <!-- C语言的混淆方案 -->
          <a-option v-if="selectedLanguage === 'c'" value="easy">
            基础混淆
          </a-option>
          <a-option v-if="selectedLanguage === 'c'" value="diff">
            强混淆
          </a-option>
          <!-- Python语言的混淆方案 -->
          <a-option v-if="selectedLanguage === 'python'" value="baseline">
            基础混淆
          </a-option>
          <a-option v-if="selectedLanguage === 'python'" value="diff">
            强混淆
          </a-option>
        </a-select>
      </div>
      <a-button
        type="primary"
        @click="handleObfuscate"
        style="margin-left: 16px"
      >
        混淆代码
      </a-button>
    </div>
    <div style="display: flex; gap: 16px; height: 600px">
      <!-- 源代码编辑器 -->
      <div style="flex: 1; display: flex; flex-direction: column">
        <div style="margin-bottom: 8px; font-weight: bold">源代码</div>
        <div style="flex: 1; border: 1px solid #d9d9d9; border-radius: 4px">
          <CodeEditor v-model="sourceCode" :language="selectedLanguage" />
        </div>
      </div>

      <!-- 混淆后代码编辑器 -->
      <div style="flex: 1; display: flex; flex-direction: column">
        <div style="margin-bottom: 8px; font-weight: bold">混淆后代码</div>
        <div style="flex: 1; border: 1px solid #d9d9d9; border-radius: 4px">
          <CodeEditor
            v-model="obfuscatedCode"
            :language="selectedLanguage"
            :read-only="true"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import CodeEditor from "@/components/CodeEditor.vue";
import message from "@arco-design/web-vue/es/message";
import { ObfuscatorControllerService } from "../../generated";

// 语言选择
const selectedLanguage = ref<string>("c");
// 混淆方案选择
const selectedScheme = ref<string>("easy");
// 源代码
const sourceCode = ref<string>(`#include <stdio.h>

int main() {
    printf("Hello, World!\n");
    return 0;
}`);
// 混淆后代码
const obfuscatedCode = ref<string>("");

// 处理语言切换
const handleLanguageChange = () => {
  // 根据语言切换默认的混淆方案和示例代码
  if (selectedLanguage.value === "c") {
    selectedScheme.value = "easy";
    sourceCode.value = `#include <stdio.h>

int main() {
    printf("Hello, World!\n");
    return 0;
}`;
  } else if (selectedLanguage.value === "python") {
    selectedScheme.value = "baseline";
    sourceCode.value = `print("Hello, World!")


def add(a, b):
    return a + b

result = add(1, 2)
print(f"Result: {result}")`;
  }
  // 清空之前的混淆结果
  obfuscatedCode.value = "";
};

// 处理混淆
const handleObfuscate = async () => {
  if (!sourceCode.value) {
    message.warning("请输入源代码");
    return;
  }

  try {
    const res = await ObfuscatorControllerService.obfuscateCodeUsingPost({
      language: selectedLanguage.value,
      scheme: selectedScheme.value,
      sourceCode: sourceCode.value,
      config: "",
    });

    if (res.code === 0) {
      obfuscatedCode.value = res.data.obfuscatedCode;
      message.success("代码混淆成功");
    } else {
      message.error("代码混淆失败：" + res.message);
    }
  } catch (error) {
    message.error("代码混淆失败：" + (error as Error).message);
  }
};
</script>

<style scoped>
#obfuscateView {
  padding: 24px;
}
</style>
