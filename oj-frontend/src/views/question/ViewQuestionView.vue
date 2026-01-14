<template>
  <div id="ViewQuestionView">
    <a-row :gutter="[24, 24]">
      <a-col :md="12" :xs="24">
        <a-tabs default-active-key="question">
          <a-tab-pane key="question" title="题目">
            <a-card v-if="question" :title="question?.title">
              <a-descriptions
                title="判题条件"
                :column="{ xs: 1, md: 2, lg: 3 }"
              >
                <a-descriptions-item label="时间限制">
                  {{ question?.judgeConfig?.timeLimit ?? 0 }}
                </a-descriptions-item>
                <a-descriptions-item label="内存限制">
                  {{ question?.judgeConfig?.memoryLimit ?? 0 }}
                </a-descriptions-item>
                <a-descriptions-item label="堆栈限制">
                  {{ question?.judgeConfig?.stackLimit ?? 0 }}
                </a-descriptions-item>
              </a-descriptions>
              <MdViewer :value="question?.content || ''" />
              <template #extra>
                <a-space wrap>
                  <a-tag
                    v-for="(tag, index) of (question?.tags || [])"
                    :key="index"
                    color="green"
                    >{{ tag }}
                  </a-tag>
                </a-space>
              </template>
            </a-card>
          </a-tab-pane>
          <a-tab-pane key="comment" title="评论" disabled>评论区</a-tab-pane>
          <a-tab-pane key="answer" title="答案">暂时无法查看答案</a-tab-pane>
        </a-tabs>
      </a-col>
      <a-col :md="12" :xs="24">
        <a-form :model="form" :layout="'inline'">
          <a-form-item field="title" label="编程语言" style="min-width: 240px">
            <a-select
              v-model="form.language"
              :style="{ width: '320px' }"
              placeholder="选择编程语言"
            >
              <a-option>java</a-option>
              <a-option>cpp</a-option>
              <a-option>go</a-option>
              <a-option>python</a-option>
            </a-select>
          </a-form-item>
        </a-form>
        <CodeEditor
          :modelValue="form.code || ''"
          :language="form.language"
          @update:modelValue="(value) => form.code = value"
        />
        <a-divider size="0" />
        <a-button type="primary" style="min-width: 200px" @click="doSubmit" :loading="submitting">
          提交代码
        </a-button>
        <!-- 判题结果展示 -->
        <a-card v-if="judgeResult" title="判题结果" style="margin-top: 16px;">
          <a-descriptions :column="{ xs: 1, md: 2 }">
            <a-descriptions-item label="执行状态">
              <a-tag :color="getStatusColor(judgeResult.status)">
                {{ getStatusText(judgeResult.status) }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="执行信息">
              {{ judgeResult.judgeInfo?.message || '无' }}
            </a-descriptions-item>
            <a-descriptions-item label="执行时间">
              {{ judgeResult.judgeInfo?.time || 0 }}ms
            </a-descriptions-item>
            <a-descriptions-item label="内存使用">
              {{ judgeResult.judgeInfo?.memory || 0 }}KB
            </a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import {
  QuestionControllerService,
  QuestionVO,
  QuestionSubmitControllerService,
  QuestionSubmitVO,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import CodeEditor from "@/components/CodeEditor.vue";
import MdViewer from "@/components/MdViewer.vue";

// eslint-disable-next-line no-undef
const props = defineProps<{
  id: string;
}>();

const question = ref<QuestionVO>();
const submitting = ref<boolean>(false);
const judgeResult = ref<QuestionSubmitVO>();

// 创建本地类型，确保language和code是必填的
interface LocalSubmitForm {
  language: string;
  code: string;
  questionId?: string;
}

const form = ref<LocalSubmitForm>({
  language: "java",
  code: "",
});

const loadData = async () => {
  if (!props.id) {
    message.error("题目ID不能为空");
    return;
  }
  try {
    // 强制转换为string类型，确保不会出现精度丢失
    const stringId = String(props.id);
    const res = await QuestionControllerService.getQuestionVoByIdUsingGet(
      stringId
    );
    if (res.code === 0 && res.data) {
      question.value = res.data as QuestionVO;
    } else {
      message.error("加载失败，" + res.message);
      question.value = undefined;
    }
  } catch (error) {
    message.error("请求失败，" + (error as Error).message);
    question.value = undefined;
  }
};

/**
 * 监听ID变化，重新请求数据
 */
watch(
  () => props.id,
  () => {
    loadData();
  },
  { immediate: true }
);

/**
 * 页面加载时，请求数据
 */
onMounted(() => {
  loadData();
});

/**
 * 查询判题结果
 */
const queryJudgeResult = async (submitId: number) => {
  try {
    // 使用类型断言将请求对象转换为any，因为后端API实际支持id参数但OpenAPI生成的类型定义中没有包含
    const res = await QuestionSubmitControllerService.listQuestionSubmitByPageUsingPost({
      pageSize: 1,
      id: submitId,
    } as any);
    if (res.code === 0 && res.data?.records?.length > 0) {
      return res.data.records[0] as QuestionSubmitVO;
    }
  } catch (error) {
    message.error("查询判题结果失败，" + (error as Error).message);
  }
  return null;
};

/**
 * 轮询查询判题结果
 */
const pollJudgeResult = async (submitId: number) => {
  let result = await queryJudgeResult(submitId);
  let retryCount = 0;
  const maxRetry = 30;
  const interval = 1000;
  
  while (!result?.status && retryCount < maxRetry) {
    await new Promise(resolve => setTimeout(resolve, interval));
    result = await queryJudgeResult(submitId);
    retryCount++;
  }
  
  return result;
};

/**
 * 根据状态码获取状态文本
 */
const getStatusText = (status?: number): string => {
  if (!status) return '未知';
  const statusMap: Record<number, string> = {
    0: '等待判题',
    1: '判题中',
    2: 'AC',
    3: 'WA',
    4: 'RE',
    5: 'TLE',
    6: 'MLE',
    7: 'CE'
  };
  return statusMap[status] || `状态${status}`;
};

/**
 * 根据状态码获取标签颜色
 */
const getStatusColor = (status?: number): string => {
  if (!status) return 'default';
  const colorMap: Record<number, string> = {
    0: 'default',
    1: 'processing',
    2: 'success',
    3: 'warning',
    4: 'danger',
    5: 'danger',
    6: 'danger',
    7: 'danger'
  };
  return colorMap[status] || 'default';
};

/**
 * 提交代码
 */
const doSubmit = async () => {
  if (!question.value?.id) {
    return;
  }
  
  submitting.value = true;
  judgeResult.value = undefined;
  
  try {
    // 确保id是string类型，使用断言消除类型错误
    const questionId = question.value.id as string;
    const res = await QuestionSubmitControllerService.doQuestionSubmitUsingPost({
      ...form.value,
      // 直接传递字符串ID，不进行类型转换，避免精度丢失
      questionId,
    });
    
    if (res.code === 0 && res.data) {
      message.success("提交成功，正在判题...");
      const submitId = res.data as number;
      
      // 轮询获取判题结果
      const result = await pollJudgeResult(submitId);
      if (result) {
        judgeResult.value = result;
        message.success(`判题完成: ${result.status}`);
      } else {
        message.warning("判题超时，请稍后查看结果");
      }
    } else {
      message.error("提交失败，" + res.message);
    }
  } catch (error) {
    message.error("提交失败，" + (error as Error).message);
  } finally {
    submitting.value = false;
  }
};


</script>

<style>
#viewQuestionView {
  max-width: 1400px;
  margin: 0 auto;
}

#viewQuestionView .arco-space-horizontal .arco-space-item {
  margin-bottom: 0 !important;
}

/* 移除浅色主题下标签文字颜色的所有强制设置，让组件使用默认的文字颜色 */
/* Arco Design 会根据标签颜色自动调整文字颜色，确保良好的对比度 */
:root:not([data-theme="dark"]) #viewQuestionView .arco-tag,
:root:not([data-theme="dark"]) #viewQuestionView .arco-tag-success,
:root:not([data-theme="dark"]) #viewQuestionView .arco-tag-green,
:root:not([data-theme="dark"]) #viewQuestionView .arco-tag-warning,
:root:not([data-theme="dark"]) #viewQuestionView .arco-tag-orange,
:root:not([data-theme="dark"]) #viewQuestionView .arco-tag-danger,
:root:not([data-theme="dark"]) #viewQuestionView .arco-tag-blue {
  color: inherit !important;
}

/* 确保深色主题下卡片正确显示 */
:root[data-theme="dark"] #viewQuestionView .arco-card {
  background-color: #1f1f1f !important;
  border-color: #333333 !important;
  color: #ffffff !important;
}

/* 确保深色主题下标签正确显示 */
:root[data-theme="dark"] #viewQuestionView .arco-tag {
  background-color: #2d2d2d !important;
  color: #ffffff !important;
  border-color: #333333 !important;
}

/* 确保深色主题下按钮正确显示 */
:root[data-theme="dark"] #viewQuestionView .arco-btn {
  background-color: #1f1f1f !important;
  color: #ffffff !important;
  border-color: #333333 !important;
}

:root[data-theme="dark"] #viewQuestionView .arco-btn-primary {
  background-color: #409eff !important;
  color: white !important;
}

/* 确保深色主题下输入框和选择框正确显示 */
:root[data-theme="dark"] #viewQuestionView .arco-input,
:root[data-theme="dark"] #viewQuestionView .arco-select-selector {
  background-color: #1f1f1f !important;
  color: #ffffff !important;
  border-color: #333333 !important;
}

/* 确保深色主题下表格正确显示 */
:root[data-theme="dark"] #viewQuestionView .arco-table {
  background-color: #1f1f1f !important;
  border-color: #333333 !important;
}

:root[data-theme="dark"] #viewQuestionView .arco-table-th,
:root[data-theme="dark"] #viewQuestionView .arco-table-td {
  background-color: #1f1f1f !important;
  border-color: #333333 !important;
  color: #ffffff !important;
}

/* 确保深色主题下标题和描述正确显示 */
:root[data-theme="dark"] #viewQuestionView h1,
:root[data-theme="dark"] #viewQuestionView h2,
:root[data-theme="dark"] #viewQuestionView h3,
:root[data-theme="dark"] #viewQuestionView h4,
:root[data-theme="dark"] #viewQuestionView p,
:root[data-theme="dark"] #viewQuestionView .arco-descriptions-item-label,
:root[data-theme="dark"] #viewQuestionView .arco-descriptions-item-content {
  color: #ffffff !important;
}

/* 确保深色主题下链接正确显示 */
:root[data-theme="dark"] #viewQuestionView a {
  color: #409eff !important;
}
</style>
