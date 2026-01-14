<template>
  <div id="homeView">
    <!-- 顶部横幅 -->
    <div class="banner">
      <div class="banner-content">
        <h1>欢迎来到在线判题系统</h1>
        <p>提升编程技能，挑战各类算法题目</p>
        <a-button type="primary" size="large" @click="toQuestions">
          开始做题
        </a-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <a-row :gutter="[16, 16]" style="margin: 24px 0;">
      <a-col :md="6" :xs="12">
        <a-card hoverable>
          <a-statistic title="总题目数" :value="stats.totalQuestions" />
        </a-card>
      </a-col>
      <a-col :md="6" :xs="12">
        <a-card hoverable>
          <a-statistic title="总用户数" :value="stats.totalUsers" />
        </a-card>
      </a-col>
      <a-col :md="6" :xs="12">
        <a-card hoverable>
          <a-statistic title="总提交数" :value="stats.totalSubmissions" />
        </a-card>
      </a-col>
      <a-col :md="6" :xs="12">
        <a-card hoverable>
          <a-statistic title="今日提交" :value="stats.todaySubmissions" />
        </a-card>
      </a-col>
    </a-row>

    <!-- 热门题目 -->
    <a-card title="热门题目" style="margin: 24px 0;">
      <a-table :columns="hotQuestionsColumns" :data="hotQuestions" :pagination="false">
        <template #tags="{ record }">
        <a-space wrap>
          <a-tag v-for="(tag, index) of getTags(record.tags)" :key="index" color="green">
            {{ tag }}
          </a-tag>
        </a-space>
      </template>
        <template #optional="{ record }">
          <a-button type="primary" size="small" @click="toQuestionPage(record)">
            做题
          </a-button>
        </template>
      </a-table>
    </a-card>

    <!-- 最近更新 -->
    <a-card title="最近更新" style="margin: 24px 0;">
      <a-table :columns="recentQuestionsColumns" :data="recentQuestions" :pagination="false">
        <template #tags="{ record }">
        <a-space wrap>
          <a-tag v-for="(tag, index) of getTags(record.tags)" :key="index" color="blue">
            {{ tag }}
          </a-tag>
        </a-space>
      </template>
        <template #createTime="{ record }">
          {{ moment(record.createTime).format("YYYY-MM-DD") }}
        </template>
        <template #optional="{ record }">
          <a-button type="primary" size="small" @click="toQuestionPage(record)">
            做题
          </a-button>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { QuestionControllerService, UserControllerService, QuestionSubmitControllerService } from "../../generated";
import message from "@arco-design/web-vue/es/message";
import moment from "moment";
import { safeCellText } from "@/utils/safeRender";

const router = useRouter();

// 统计数据
const stats = ref({
  totalQuestions: 0,
  totalUsers: 0,
  totalSubmissions: 0,
  todaySubmissions: 0
});

// 热门题目
const hotQuestions = ref<any[]>([]);

// 最近更新题目
const recentQuestions = ref<any[]>([]);

// 定义题目类型
interface Question {
  id: string;
  title: string;
  difficulty?: number;
  tags: any;
  submitNum: number;
  acceptedNum: number;
  createTime: string;
}

// 处理标签数据，确保返回字符串数组
const getTags = (tags: any): string[] => {
  if (!tags) return [];
  if (Array.isArray(tags)) {
    return tags.map(tag => String(tag));
  }
  if (typeof tags === 'string') {
    try {
      // 尝试解析JSON字符串
      const parsed = JSON.parse(tags);
      return Array.isArray(parsed) ? parsed.map(tag => String(tag)) : [tags];
    } catch {
      // 如果不是有效的JSON，直接返回字符串作为单个标签
      return [tags];
    }
  }
  // 其他类型转换为字符串
  return [String(tags)];
};

// 热门题目列配置
const hotQuestionsColumns = [
  {
    title: "题号",
    dataIndex: "id",
    customRender: ({ text }) => {
      return safeCellText(text);
    }
  },
  {
    title: "题目名称",
    dataIndex: "title",
    ellipsis: true,
    customRender: ({ text }) => {
      return safeCellText(text);
    }
  },
  {
    title: "标签",
    slotName: "tags",
    ellipsis: true,
    customRender: () => {
      // 实际渲染由slot处理，这里添加空的customRender防止Arco Design直接渲染数据
      return '';
    }
  },
  {
    title: "提交数",
    dataIndex: "submitNum",
    customRender: ({ text }) => {
      return safeCellText(Number(text || 0));
    }
  },
  {
    title: "通过率",
    dataIndex: "acceptedRate",
    customRender: ({ record }: { record: Question }) => {
      const acceptedNum = Number(record.acceptedNum || 0);
      const submitNum = Number(record.submitNum || 0);
      return `${submitNum > 0 ? Math.round((acceptedNum / submitNum) * 100) : 0}%`;
    }
  },
  {
    slotName: "optional",
    customRender: () => {
      // 实际渲染由slot处理，这里添加空的customRender防止Arco Design直接渲染数据
      return '';
    }
  }
];

// 最近更新题目列配置
const recentQuestionsColumns = [
  {
    title: "题号",
    dataIndex: "id",
    customRender: ({ text }) => {
      return safeCellText(text);
    }
  },
  {
    title: "题目名称",
    dataIndex: "title",
    ellipsis: true,
    customRender: ({ text }) => {
      return safeCellText(text);
    }
  },
  {
    title: "标签",
    slotName: "tags",
    ellipsis: true,
    customRender: () => {
      // 实际渲染由slot处理，这里添加空的customRender防止Arco Design直接渲染数据
      return '';
    }
  },
  {
    title: "更新时间",
    slotName: "createTime",
    customRender: () => {
      // 实际渲染由slot处理，这里添加空的customRender防止Arco Design直接渲染数据
      return '';
    }
  },
  {
    slotName: "optional",
    customRender: () => {
      // 实际渲染由slot处理，这里添加空的customRender防止Arco Design直接渲染数据
      return '';
    }
  }
];



// 跳转到题目列表
const toQuestions = () => {
  router.push("/questions");
};

// 跳转到题目详情页
const toQuestionPage = (question: any) => {
  router.push(`/view/question/${question.id}`);
};

// 加载首页数据
const loadHomeData = async () => {
  try {
    // 1. 获取总题目数
    const questionRes = await QuestionControllerService.listQuestionVoByPageUsingPost({
      pageSize: 1
    });
    const totalQuestions = questionRes.code === 0 ? questionRes.data.total : 0;

    // 2. 获取总用户数
    const userRes = await UserControllerService.listUserVoByPageUsingPost({
      pageSize: 1
    });
    const totalUsers = userRes.code === 0 ? userRes.data.total : 0;

    // 3. 获取总提交数
    const submissionRes = await QuestionSubmitControllerService.listQuestionSubmitByPageUsingPost({
      pageSize: 1
    });
    const totalSubmissions = submissionRes.code === 0 ? submissionRes.data.total : 0;

    // 4. 获取所有提交，然后过滤出今日提交
    const allSubmissionsRes = await QuestionSubmitControllerService.listQuestionSubmitByPageUsingPost({
      pageSize: 100, // 足够大的数量来获取今日所有提交
      sortField: "createTime",
      sortOrder: "desc"
    });
    
    // 计算今日提交数
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    let todaySubmissions = 0;
    if (allSubmissionsRes.code === 0) {
      todaySubmissions = allSubmissionsRes.data.records.filter((submission: any) => {
        if (!submission.createTime) return false;
        const submitDate = new Date(submission.createTime);
        return submitDate >= today;
      }).length;
    }

    // 5. 获取热门题目
    const hotRes = await QuestionControllerService.listQuestionVoByPageUsingPost({
      sortField: "submitNum",
      sortOrder: "desc",
      pageSize: 5
    });
    if (hotRes.code === 0) {
      hotQuestions.value = hotRes.data.records;
    }

    // 6. 获取最近更新题目
    const recentRes = await QuestionControllerService.listQuestionVoByPageUsingPost({
      sortField: "createTime",
      sortOrder: "desc",
      pageSize: 5
    });
    if (recentRes.code === 0) {
      recentQuestions.value = recentRes.data.records;
    }

    // 更新统计数据
    stats.value = {
      totalQuestions,
      totalUsers,
      totalSubmissions,
      todaySubmissions
    };
  } catch (error) {
    message.error("加载首页数据失败：" + (error as Error).message);
  }
};

// 页面加载时获取数据
onMounted(() => {
  loadHomeData();
});
</script>

<style scoped>
#homeView {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
}

.banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 80px 0;
  margin: -24px -24px 24px -24px;
}

.banner-content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
  text-align: center;
}

.banner-content h1 {
  font-size: 48px;
  margin-bottom: 16px;
}

.banner-content p {
  font-size: 20px;
  margin-bottom: 32px;
  opacity: 0.9;
}
</style>

<style>
/* 统计卡片样式优化 */
.arco-card {
  transition: all 0.3s ease;
}

.arco-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
}

/* 统计组件基本样式 */
.arco-statistic {
  text-align: center;
}

.arco-statistic-title {
  font-size: 14px !important;
  margin-bottom: 8px !important;
}

.arco-statistic-content {
  font-size: 32px !important;
  font-weight: bold !important;
}

/* 全局深色主题样式重置 */
[data-theme="dark"] {
  color: #ffffff !important;
}

[data-theme="dark"] * {
  color: inherit !important;
}

/* 确保深色主题下统计数字清晰可见 - 使用最高优先级 */
:root[data-theme="dark"] .arco-statistic,
:root[data-theme="dark"] .arco-statistic * {
  color: #ffffff !important;
}

:root[data-theme="dark"] .arco-statistic-content {
  color: #ffffff !important;
  font-weight: bold !important;
}

:root[data-theme="dark"] .arco-statistic-content-value {
  color: #ffffff !important;
  font-weight: bold !important;
}

:root[data-theme="dark"] .arco-statistic-title {
  color: #cccccc !important;
}

/* 确保卡片在深色主题下正确显示 */
:root[data-theme="dark"] #homeView .arco-card {
  background-color: #1f1f1f !important;
  border-color: #333333 !important;
  color: #ffffff !important;
}

/* 确保卡片内所有文本在深色主题下正确显示 */
:root[data-theme="dark"] #homeView .arco-card * {
  color: #ffffff !important;
}

/* 移除浅色主题下标签文字颜色的所有强制设置，让组件使用默认的文字颜色 */
/* Arco Design 会根据标签颜色自动调整文字颜色，确保良好的对比度 */
:root:not([data-theme="dark"]) #homeView .arco-tag,
:root:not([data-theme="dark"]) #homeView .arco-tag-success,
:root:not([data-theme="dark"]) #homeView .arco-tag-green,
:root:not([data-theme="dark"]) #homeView .arco-tag-warning,
:root:not([data-theme="dark"]) #homeView .arco-tag-orange,
:root:not([data-theme="dark"]) #homeView .arco-tag-danger,
:root:not([data-theme="dark"]) #homeView .arco-tag-blue {
  color: inherit !important;
}

/* 确保表格在深色主题下正确显示 */
:root[data-theme="dark"] #homeView .arco-table {
  background-color: #1f1f1f !important;
  border-color: #333333 !important;
}

:root[data-theme="dark"] #homeView .arco-table-th,
:root[data-theme="dark"] #homeView .arco-table-td {
  background-color: #1f1f1f !important;
  border-color: #333333 !important;
  color: #ffffff !important;
}

/* 确保标题在深色主题下正确显示 */
:root[data-theme="dark"] #homeView h1,
:root[data-theme="dark"] #homeView h2,
:root[data-theme="dark"] #homeView h3,
:root[data-theme="dark"] #homeView h4,
:root[data-theme="dark"] #homeView p {
  color: #ffffff !important;
}

/* 确保链接在深色主题下正确显示 */
:root[data-theme="dark"] #homeView a {
  color: #409eff !important;
}
</style>