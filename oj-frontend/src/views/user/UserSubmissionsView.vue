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
        <a-statistic title="AC率" :value="stats.acRate">
          <template #suffix>
            <span class="stat-suffix">%</span>
          </template>
        </a-statistic>
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
          {{ getDisplayStatus(record.status) }}
        </a-tag>
      </template>
      <template #question="{ record }: { record: any }">
        <a @click="toQuestionPage(record.questionId)">{{ record.questionVO?.id || (typeof record.questionId === 'number' ? record.questionId.toString() : String(record.questionId)) }}</a>
      </template>
      <template #judgeInfo="{ record }: { record: any }">
        {{ record.judgeInfo?.message || '无' }}
      </template>
      <template #time="{ record }: { record: any }">
        <!-- 处理judgeInfo可能是字符串的情况 -->
        {{ typeof record.judgeInfo === 'string' ? 0 : (record.judgeInfo?.time || 0) }}ms
      </template>
      <template #memory="{ record }: { record: any }">
        <!-- 处理judgeInfo可能是字符串的情况 -->
        {{ typeof record.judgeInfo === 'string' ? 0 : (record.judgeInfo?.memory || 0) }}KB
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
import { safeCellText } from "@/utils/safeRender";

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
  status: undefined,
  language: "",
  pageSize: 10,
  current: 1,
  sortField: "createTime",
  sortOrder: "desc"
});

// 状态映射：将前端显示的状态字符串映射为后端期望的状态码
const statusMap: Record<string, number> = {
  "AC": 2,
  "WA": 3,
  "RE": 4,
  "TLE": 5,
  "MLE": 6,
  "CE": 7
};

// 反向状态映射：将后端返回的状态码映射为前端显示的状态字符串
const reverseStatusMap: Record<number, string> = {
  0: "待判题",
  1: "判题中",
  2: "AC",
  3: "WA",
  4: "RE",
  5: "TLE",
  6: "MLE",
  7: "CE"
};

// 代码查看弹窗
const codeModalVisible = ref(false);
const currentCode = ref("");

// 列配置
const columns = [
  {
    title: "题目",
    slotName: "question",
    customRender: () => {
      // 实际渲染由slot处理，这里添加空的customRender防止Arco Design直接渲染数据
      return '';
    }
  },
  {
    title: "状态",
    slotName: "status",
    customRender: () => {
      // 实际渲染由slot处理，这里添加空的customRender防止Arco Design直接渲染数据
      return '';
    }
  },
  { 
    title: "语言", 
    dataIndex: "language",
    customRender: ({ text }) => {
      return safeCellText(text || "未知");
    }
  },
  {
    title: "执行信息",
    slotName: "judgeInfo",
    ellipsis: true,
    customRender: () => {
      // 实际渲染由slot处理，这里添加空的customRender防止Arco Design直接渲染数据
      return '';
    }
  },
  {
    title: "执行时间",
    slotName: "time",
    customRender: () => {
      // 实际渲染由slot处理，这里添加空的customRender防止Arco Design直接渲染数据
      return '';
    }
  },
  {
    title: "内存使用",
    slotName: "memory",
    customRender: () => {
      // 实际渲染由slot处理，这里添加空的customRender防止Arco Design直接渲染数据
      return '';
    }
  },
  {
    title: "提交时间",
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

// 获取状态颜色
const getStatusColor = (status?: number | string): "default" | "success" | "warning" | "danger" => {
  // 确保状态是字符串
  const statusStr = typeof status === "number" ? reverseStatusMap[status] || String(status) : status;
  
  const colorMap: Record<string, "default" | "success" | "warning" | "danger"> = {
    "AC": "success",
    "WA": "warning",
    "RE": "danger",
    "TLE": "danger",
    "MLE": "danger",
    "CE": "danger",
    "待判题": "default",
    "判题中": "default"
  };
  return colorMap[statusStr || ""] || "default";
};

// 获取显示状态
const getDisplayStatus = (status?: number): string => {
  return reverseStatusMap[status || 0] || "未知状态";
};

// 跳转到题目页面
const toQuestionPage = (questionId?: any) => {
  if (questionId) {
    // 确保questionId作为字符串传递，避免精度丢失
    router.push(`/view/question/${String(questionId)}`);
  }
};

// 查看代码
const viewCode = async (submitId?: any) => {
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

// 获取AC提交数量
const getAcCount = async () => {
  try {
    const acParams = {
      ...searchParams.value,
      pageSize: 1, // 只需要获取总数，不需要具体记录
      current: 1
    };
    
    // 确保获取的是AC状态的提交
    acParams.status = statusMap["AC"];
    
    const res = await QuestionSubmitControllerService.listQuestionSubmitByPageUsingPost(acParams as any);
    if (res.code === 0) {
      return res.data.total;
    }
    return 0;
  } catch (error) {
    console.error("获取AC数量失败：", error);
    return 0;
  }
};

// 获取今日提交数量
const getTodaySubmissionsCount = async () => {
  try {
    // 计算今日的日期字符串
    const today = new Date().toDateString();
    let todayCount = 0;
    let currentPage = 1;
    const pageSize = 50;
    let hasMore = true;
    
    // 分页获取所有提交记录，直到没有更多记录
    while (hasMore) {
      const todayParams = {
        ...searchParams.value,
        pageSize: pageSize,
        current: currentPage
      };
      
      const res = await QuestionSubmitControllerService.listQuestionSubmitByPageUsingPost(todayParams as any);
      if (res.code === 0) {
        // 过滤出今日的提交记录
        const todayRecords = res.data.records.filter((record: any) => {
          if (!record.createTime) return false;
          const submitDate = new Date(record.createTime).toDateString();
          return submitDate === today;
        });
        
        todayCount += todayRecords.length;
        
        // 检查是否还有更多记录
        if (res.data.records.length < pageSize) {
          hasMore = false;
        } else {
          currentPage++;
        }
      } else {
        hasMore = false;
      }
    }
    
    return todayCount;
  } catch (error) {
    console.error("获取今日提交数量失败：", error);
    return 0;
  }
};

// 加载提交记录
const loadSubmissions = async () => {
  try {
    // 构建API请求参数，确保参数类型正确
    const requestParams = {
      ...searchParams.value,
      sortField: searchParams.value.sortField,
      sortOrder: searchParams.value.sortOrder
    };
    
    // 如果status有值，将其转换为对应的数值状态码
    if (requestParams.status) {
      requestParams.status = statusMap[requestParams.status];
    } else {
      // 如果status为空，不传递该参数
      delete requestParams.status;
    }
    
    // 确保questionId是数值类型
    if (requestParams.questionId) {
      requestParams.questionId = Number(requestParams.questionId);
    } else {
      // 如果questionId为空，不传递该参数
      delete requestParams.questionId;
    }
    
    console.log("发送的请求参数：", requestParams);
    
    // 获取当前过滤条件下的所有提交记录，用于统计
    const allSubmissionsParams = {
      ...requestParams,
      pageSize: 1000, // 设置一个足够大的pageSize，确保获取所有记录
      current: 1
    };
    
    const allSubmissionsRes = await QuestionSubmitControllerService.listQuestionSubmitByPageUsingPost(allSubmissionsParams as any);
    
    if (allSubmissionsRes.code === 0) {
      const allRecords = allSubmissionsRes.data.records;
      const totalSubmissions = allSubmissionsRes.data.total;
      
      // 计算AC数：统计所有记录中状态为AC的数量
      const acCount = allRecords.filter((record: any) => {
        return record.status === "AC" || record.status === 2;
      }).length;
      
      // 计算AC率
      const acRate = totalSubmissions > 0 ? Math.round((acCount / totalSubmissions) * 100) : 0;
      
      // 计算今日提交数：统计所有记录中今天提交的数量
      const today = new Date().toDateString();
      const todaySubmissions = allRecords.filter((record: any) => {
        if (!record.createTime) return false;
        const submitDate = new Date(record.createTime).toDateString();
        return submitDate === today;
      }).length;
      
      // 现在获取当前页的提交记录（用于表格显示）
      const currentPageRes = await QuestionSubmitControllerService.listQuestionSubmitByPageUsingPost(requestParams as any);
      if (currentPageRes.code === 0) {
        submissions.value = currentPageRes.data.records;
        total.value = currentPageRes.data.total;
        
        // 更新统计数据
        stats.value = {
          totalSubmissions,
          acCount,
          acRate,
          todaySubmissions
        };
        
        console.log("统计数据：", stats.value);
        console.log("所有记录：", allRecords);
      } else {
        message.error("加载提交记录失败：" + currentPageRes.message);
      }
    } else {
      message.error("加载提交记录失败：" + allSubmissionsRes.message);
    }
  } catch (error) {
    console.error("加载提交记录异常：", error);
    message.error("加载提交记录失败：" + (error as Error).message);
  }
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
.stat-suffix {
  font-size: inherit;
  font-weight: inherit;
  line-height: inherit;
  margin-left: 2px;
}
/* 修改状态标签的字体颜色为黑色 */
#userSubmissionsView :deep(.arco-tag) {
  color: black !important;
}
</style>