<template>
  <div id="questionsView">
    <a-form :model="searchParams" :layout="'inline'">
      <a-form-item field="title" label="名称" style="min-width: 240px">
        <a-input v-model="searchParams.title" placeholder="请输入名称" />
      </a-form-item>
      <a-form-item field="tags" label="标签" style="min-width: 240px">
        <a-input-tag v-model="searchParams.tags" placeholder="请输入标签" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="doSubmit">搜索</a-button>
        <a-button style="margin-left: 8px;" @click="resetSearch">重置</a-button>
      </a-form-item>
    </a-form>
    <a-divider size="0" />
    <a-table
      :ref="tableRef"
      :columns="columns"
      :data="dataList"
      :pagination="{
        showTotal: true,
        pageSize: searchParams.pageSize,
        current: searchParams.current,
        total,
      }"
      @page-change="onPageChange"
    >
      <template #tags="{ record }">
        <a-space wrap>
          <a-tag v-for="(tag, index) of record.tags" :key="index" color="green">
            {{ tag }}
          </a-tag>
        </a-space>
      </template>
      <template #acceptedRate="{ record }">
        {{
          `${
            record.submitNum ? record.acceptedNum / record.submitNum : "0"
          }% (${record.acceptedNum}/${record.submitNum})`
        }}
      </template>
      <template #createTime="{ record }">
        {{ moment(record.createTime).format("YYYY-MM-DD") }}
      </template>
      <template #optional="{ record }">
        <a-space>
          <a-button type="primary" @click="toQuestionPage(record)">
            做题
          </a-button>
          <a-button
            :type="isFavorite(record.id) ? 'primary' : 'secondary'"
            @click="toggleFavorite(record)"
          >
            {{ isFavorite(record.id) ? '已收藏' : '收藏' }}
          </a-button>
        </a-space>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watchEffect } from "vue";
import {
  QuestionControllerService,
  QuestionQueryRequest,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
import moment from "moment";
import axios from "axios";

const tableRef = ref();

const dataList = ref([]);
const total = ref(0);
const searchParams = ref<QuestionQueryRequest>({
  title: "",
  tags: [],
  pageSize: 10,
  current: 1,
});

const loadData = async () => {
  const res = await QuestionControllerService.listQuestionVoByPageUsingPost(
    searchParams.value
  );
  if (res.code === 0) {
    dataList.value = res.data.records;
    total.value = res.data.total;
  } else {
    message.error("加载失败，" + res.message);
  }
};

/**
 * 监听 searchParams 变量，改变时触发页面的重新加载
 */
watchEffect(() => {
  loadData();
});

/**
 * 页面加载时，请求数据
 */
onMounted(() => {
  loadData();
});

// {id: "1", title: "A+ D", content: "新的题目内容", tags: "["二叉树"]", answer: "新的答案", submitNum: 0,…}

// 定义题目类型
interface Question {
  id: string;
  title: string;
  tags: string[];
  submitNum: number;
  acceptedNum: number;
  createTime: string;
}

const columns = [
  {
    title: "题号",
    dataIndex: "id",
  },
  {
    title: "题目名称",
    dataIndex: "title",
  },
  {
    title: "标签",
    slotName: "tags",
  },
  {
    title: "通过率",
    slotName: "acceptedRate",
    width: 120
  },
  {
    title: "创建时间",
    slotName: "createTime",
    width: 120
  },
  {
    slotName: "optional",
    width: 160
  },
];

const onPageChange = (page: number) => {
  searchParams.value = {
    ...searchParams.value,
    current: page,
  };
};

const router = useRouter();
const store = useStore();

// 收藏状态管理
const favoriteIds = ref<Set<string>>(new Set());

/**
 * 跳转到做题页面
 * @param question
 */
const toQuestionPage = (question: Question) => {
  router.push({
    path: `/view/question/${question.id}`,
  });
};

/**
 * 检查题目是否已收藏
 */
const isFavorite = (questionId: string): boolean => {
  return favoriteIds.value.has(questionId);
};

/**
 * 切换收藏状态
 */
const toggleFavorite = async (question: Question) => {
  try {
    // 检查store对象是否存在
    if (!store) {
      console.error("Store对象未定义");
      message.warning("请先登录");
      // 跳转到登录页面
      router.push({
        path: "/user/login",
      });
      return;
    }
    
    // 检查是否登录
    const userModule = store.state?.user;
    const loginUser = userModule?.loginUser;
    if (!loginUser || !loginUser.userName || loginUser.userName === "未登录") {
      message.warning("请先登录");
      // 跳转到登录页面
      router.push({
        path: "/user/login",
      });
      return;
    }
    
    // 调用后端API切换收藏状态
    const res = await axios.post("/api/question_favour/", {
      questionId: Number(question.id)
    });
    
    if (res.data.code === 0) {
      if (isFavorite(question.id)) {
        // 取消收藏成功
        favoriteIds.value.delete(question.id);
        message.success("取消收藏成功");
      } else {
        // 收藏成功
        favoriteIds.value.add(question.id);
        message.success("收藏成功");
      }
    } else {
      message.error(res.data.message || "操作失败");
    }
  } catch (error) {
    console.error("切换收藏状态失败：", error);
    message.error("操作失败：" + (error as Error).message);
  }
};

/**
 * 加载收藏状态
 */
const loadFavorites = async () => {
  try {
    // 检查是否登录
    if (!store) return;
    const userModule = store.state?.user;
    const loginUser = userModule?.loginUser;
    if (!loginUser || !loginUser.userName || loginUser.userName === "未登录") {
      return;
    }
    
    // 从后端获取收藏列表
    const res = await axios.post("/api/question_favour/my/list/page", {
      current: 1,
      pageSize: 100 // 获取所有收藏的题目
    });
    
    if (res.data.code === 0) {
      // 提取收藏的题目ID
      const favoriteQuestionIds = res.data.data.records.map((item: any) => item.id);
      favoriteIds.value = new Set(favoriteQuestionIds);
    }
  } catch (error) {
    console.error("加载收藏状态失败：", error);
    // 不显示错误信息，避免影响用户体验
  }
};

// 页面加载时获取收藏状态
onMounted(() => {
  loadFavorites();
});

/**
 * 确认搜索，重新加载数据
 */
const doSubmit = () => {
  // 这里需要重置搜索页号
  searchParams.value = {
    ...searchParams.value,
    current: 1,
  };
};

/**
 * 重置搜索条件
 */
const resetSearch = () => {
  searchParams.value = {
    title: "",
    tags: [],
    pageSize: 10,
    current: 1,
  };
};
</script>

<style scoped>
#questionsView {
  max-width: 1280px;
  margin: 0 auto;
}

/* 移除浅色主题下标签文字颜色的所有强制设置，让组件使用默认的文字颜色 */
/* Arco Design 会根据标签颜色自动调整文字颜色，确保良好的对比度 */
:root:not([data-theme="dark"]) #questionsView .arco-tag,
:root:not([data-theme="dark"]) #questionsView .arco-tag-success,
:root:not([data-theme="dark"]) #questionsView .arco-tag-green,
:root:not([data-theme="dark"]) #questionsView .arco-tag-warning,
:root:not([data-theme="dark"]) #questionsView .arco-tag-orange,
:root:not([data-theme="dark"]) #questionsView .arco-tag-danger,
:root:not([data-theme="dark"]) #questionsView .arco-tag-blue {
  color: inherit !important;
}

/* 确保深色主题下卡片正确显示 */
:root[data-theme="dark"] #questionsView .arco-card {
  background-color: #1f1f1f !important;
  border-color: #333333 !important;
  color: #ffffff !important;
}

/* 确保深色主题下表格正确显示 */
:root[data-theme="dark"] #questionsView .arco-table {
  background-color: #1f1f1f !important;
  border-color: #333333 !important;
}

:root[data-theme="dark"] #questionsView .arco-table-th,
:root[data-theme="dark"] #questionsView .arco-table-td {
  background-color: #1f1f1f !important;
  border-color: #333333 !important;
  color: #ffffff !important;
}

/* 确保深色主题下标题正确显示 */
:root[data-theme="dark"] #questionsView h1,
:root[data-theme="dark"] #questionsView h2,
:root[data-theme="dark"] #questionsView h3,
:root[data-theme="dark"] #questionsView h4,
:root[data-theme="dark"] #questionsView p {
  color: #ffffff !important;
}

/* 确保深色主题下链接正确显示 */
:root[data-theme="dark"] #questionsView a {
  color: #409eff !important;
}

/* 确保深色主题下输入框和选择框正确显示 */
:root[data-theme="dark"] #questionsView .arco-input,
:root[data-theme="dark"] #questionsView .arco-select-selector {
  background-color: #1f1f1f !important;
  color: #ffffff !important;
  border-color: #333333 !important;
}

/* 确保深色主题下标签正确显示 */
:root[data-theme="dark"] #questionsView .arco-tag {
  background-color: #2d2d2d !important;
  color: #ffffff !important;
  border-color: #333333 !important;
}
</style>
