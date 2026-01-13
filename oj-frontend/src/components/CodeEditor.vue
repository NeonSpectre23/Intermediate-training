<template>
  <div
    id="code-editor"
    ref="codeEditorRef"
    style="min-height: 400px; height: 70vh"
  />
</template>

<script setup lang="ts">
import {
  onMounted,
  ref,
  toRaw,
  watch,
  withDefaults,
  defineProps,
  defineEmits,
} from "vue";
import * as monaco from "monaco-editor";

/**
 * 定义组件属性类型
 */
interface Props {
  modelValue: string;
  language?: string;
  readOnly?: boolean;
}

/**
 * 给组件指定初始值
 */
const props = withDefaults(defineProps<Props>(), {
  modelValue: () => "",
  language: () => "java",
  readOnly: () => false,
});

/**
 * 定义组件事件
 */
const emit = defineEmits<{
  "update:modelValue": [value: string];
}>();

const codeEditorRef = ref();
const codeEditor = ref();

// 监听外部value变化，更新编辑器内容
watch(
  () => props.modelValue,
  (newValue) => {
    if (codeEditor.value && toRaw(codeEditor.value).getValue() !== newValue) {
      toRaw(codeEditor.value).setValue(newValue);
    }
  }
);

// 监听语言变化，更新编辑器语言
watch(
  () => props.language,
  (newLanguage) => {
    if (codeEditor.value) {
      monaco.editor.setModelLanguage(
        toRaw(codeEditor.value).getModel(),
        newLanguage
      );
    }
  }
);

// 监听只读状态变化，更新编辑器只读属性
watch(
  () => props.readOnly,
  (newReadOnly) => {
    if (codeEditor.value) {
      toRaw(codeEditor.value).updateOptions({ readOnly: newReadOnly });
    }
  }
);

onMounted(() => {
  if (!codeEditorRef.value) {
    return;
  }
  // 创建编辑器
  codeEditor.value = monaco.editor.create(codeEditorRef.value, {
    value: props.modelValue,
    language: props.language,
    automaticLayout: true,
    colorDecorators: true,
    minimap: {
      enabled: true,
    },
    readOnly: props.readOnly,
    theme: "vs-dark",
  });

  // 监听内容变化，触发update:modelValue事件
  codeEditor.value.onDidChangeModelContent(() => {
    emit("update:modelValue", toRaw(codeEditor.value).getValue());
  });
});
</script>

<style scoped></style>
