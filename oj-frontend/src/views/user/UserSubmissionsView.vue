<template>
  <div id="userSubmissionsView">
    <h2>我的提交记录</h2>
    
    <!-- 统计卡片 -->
    <a-row :gutter="[16, 16]" style="margin-bottom: 24px;">
      <a-col :md="6" :xs="12">
        <a-card hoverable>
          <a-statistic title="总提交数" :value="stats.totalSubmissions" />
        </a-card>
      </a-col>
      <a-col :md="6" :xs="12">
        <a-card hoverable>
          <a-statistic title="AC数" :value="stats.acCount" />
        </a-card>
      </a-col>
      <a-col :md="6" :xs="12">
        <a-card hoverable>
          <a-statistic title="AC率" :value="stats.acRate" suffix="%" />
        </a-card>
      </a-col>
      <a-col :md="6" :xs="12">
        <a-card hoverable>
          <a-statistic title="今日提交" :value="stats.todaySubmissions" />
        </a-card>
      </a-col>
    </a-row>
    
    <!-- 搜索和筛选 -->
    <a-card style="margin-bottom: 16px;">
      <a-form :model="searchParams" :layout="'inline'">
        <a-form-item field="questionId" label="题目ID">
          <a-input-number v-model="searchParams.questionId" :style="{ width: '120px' }" placeholder="题目ID" />
        </a-form-item>
        <a-form-item field="status" label="状态">
          <a-select v-model="searchParams.status" placeholder="全部状态">
            <a-option value="">全部</a-option>
            <a-option value="AC">AC</a-option>
            <a-option value="WA">WA</a-option>
            <a-option value="RE">RE</a-option>
            <a-option value="TLE">TLE</a-option>
            <a-option value="MLE">MLE</a-option>
            <a-option value="CE">CE</a-option>
          </a-select>
        </a-form-item>
        <a-form-item field="language" label="语言">
          <a-select v-model="searchParams.language" placeholder="全部语言">
            <a-option value="">全部</a-option>
            <a-option value="java">Java</a-option>
            <a-option value="cpp">C++</a-option>
            <a-option value="python">Python</a-option>
            <a-option value="go">Go</a-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="doSearch">搜索</a-button>
          <a-button style="margin-left: 8px;" @click="resetSearch">重置</a-button>
        </a-form-item>
      </a-form>
    </a-card>
    
    <!-- 提交记录表格 -->
    <a-table
      :columns="columns"
      :data="submissions"
      :pagination="{
        showTotal: true,
        pageSize: searchParams.pageSize,
        current: searchParams.current,
        total: total,
      }"
      @page-change="onPageChange"
    >
      <template #status="{ record }: { record: any }">
        <a-tag :color="getStatusColor(record.status)">
          {{ record.status }}
        </a-tag>
      </template>
      <template #question="{ record }: { record: any }">
        <a @click="toQuestionPage(record.questionId)">{{ record.questionTitle }}</a>
      </template>
      <template #judgeInfo="{ record }: { record: any }">
        {{ record.judgeInfo?.message || '无' }}
      </template>
      <template #time="{ record }: { record: any }">
        {{ record.judgeInfo?.time || 0 }}ms
      </template>
      <template #memory="{ record }: { record: any }">
        {{ record.judgeInfo?.memory || 0 }}KB
      </template>
      <template #createTime="{ record }: { record: any }">
        {{ moment(record.createTime).format("YYYY-MM-DD HH:mm:ss") }}
      </template>
      <template #optional="{ record }: { record: any }">
        <a-button size="small" @click="viewCode(record.id)">
          查看代码
        </a-button>
      </template>
    </a-table>
    
    <!-- 代码查看弹窗 -->
    <a-modal
      v-model:visible="codeModalVisible"
      title="代码详情"
      width="800px"
      @cancel="codeModalVisible = false"
    >
      <pre v-if="currentCode" style="background: #f5f5f5; padding: 16px; border-radius: 4px; overflow-x: auto;">{{ currentCode }}</pre>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { QuestionSubmitControllerService } from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import moment from "moment";

const router = useRouter();

// 统计数据
const stats = ref({
  totalSubmissions: 0,
  acCount: 0,
  acRate: 0,
  todaySubmissions: 0
});

// 提交记录
const submissions = ref([]);
const total = ref(0);

// 搜索参数
const searchParams = ref({
  questionId: undefined,
  status: "",
  language: "",
  pageSize: 10,
  current: 1,
  sortField: "createTime",
  sortOrder: "desc"
});

// 代码查看弹窗
const codeModalVisible = ref(false);
const currentCode = ref("");

// 列配置
const columns = [
  {
    title: "题目",
    slotName: "question"
  },
  {
    title: "状态",
    slotName: "status"
  },
  {
    title: "语言",
    dataIndex: "language"
  },
  {
    title: "执行信息",
    slotName: "judgeInfo",
    ellipsis: true
  },
  {
    title: "执行时间",
    slotName: "time"
  },
  {
    title: "内存使用",
    slotName: "memory"
  },
  {
    title: "提交时间",
    slotName: "createTime"
  },
  {
    slotName: "optional"
  }
];

// 获取状态颜色
const getStatusColor = (status?: string): "default" | "success" | "warning" | "danger" => {
  const colorMap: Record<string, "default" | "success" | "warning" | "danger"> = {
    "AC": "success",
    "WA": "warning",
    "RE": "danger",
    "TLE": "danger",
    "MLE": "danger",
    "CE": "danger"
  };
  return colorMap[status || ""] || "default";
};

// 跳转到题目页面
const toQuestionPage = (questionId?: string) => {
  if (questionId) {
    router.push(`/view/question/${questionId}`);
  }
};

// 查看代码
const viewCode = async (submitId?: string) => {
  if (!submitId) return;
  try {
    // 这里需要调用获取代码的API，目前使用模拟数据
    currentCode.value = "// 代码示例\npublic class Solution {\n    public int add(int a, int b) {\n        return a + b;\n    }\n}";
    codeModalVisible.value = true;
  } catch (error) {
    message.error("获取代码失败：" + (error as Error).message);
  }
};

// 搜索提交记录
const doSearch = () => {
  searchParams.value.current = 1;
  loadSubmissions();
};

// 重置搜索
const resetSearch = () => {
  searchParams.value = {
    questionId: undefined,
    status: "",
    language: "",
    pageSize: 10,
    current: 1,
    sortField: "createTime",
    sortOrder: "desc"
  };
  loadSubmissions();
};

// 分页变化
const onPageChange = (page: number) => {
  searchParams.value.current = page;
  loadSubmissions();
};

// 加载提交记录
const loadSubmissions = async () => {
  try {
    const res = await QuestionSubmitControllerService.listQuestionSubmitByPageUsingPost({
      ...searchParams.value,
      sortField: searchParams.value.sortField,
      sortOrder: searchParams.value.sortOrder
    } as any);
    if (res.code === 0) {
      submissions.value = res.data.records;
      total.value = res.data.total;
      // 计算统计数据
      calculateStats(res.data.records);
    } else {
      message.error("加载提交记录失败：" + res.message);
    }
  } catch (error) {
    console.error("加载提交记录异常：", error);
    message.error("加载提交记录失败：" + (error as Error).message);
  }
};

// 计算统计数据
const calculateStats = (records: any[]) => {
  // 从真实记录中计算统计数据
  const total = records.length;
  const acCount = records.filter(record => record.status === "AC").length;
  const acRate = total > 0 ? Math.round((acCount / total) * 100) : 0;
  
  // 计算今日提交数
  const today = new Date().toDateString();
  const todaySubmissions = records.filter(record => {
    if (!record.createTime) return false;
    const submitDate = new Date(record.createTime).toDateString();
    return submitDate === today;
  }).length;
  
  stats.value = {
    totalSubmissions: total,
    acCount,
    acRate,
    todaySubmissions
  };
};

// 页面加载时获取数据
onMounted(() => {
  loadSubmissions();
});
</script>

<style scoped>
#userSubmissionsView {
  padding: 24px;
  max-width: 1280px;
  margin: 0 auto;
}
</style>