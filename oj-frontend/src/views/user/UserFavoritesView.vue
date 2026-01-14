<template>
  <div id="userFavoritesView">
    <h2>我的收藏</h2>
    
    <!-- 收藏题目列表 -->
    <a-table
      :columns="columns"
      :data="favorites"
      :pagination="{
        showTotal: true,
        pageSize: searchParams.pageSize,
        current: searchParams.current,
        total: total,
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
      <template #optional="{ record }">
        <a-space>
          <a-button type="primary" @click="toQuestionPage(record.id)">做题</a-button>
          <a-button danger @click="removeFavorite(record.id)">取消收藏</a-button>
        </a-space>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import message from "@arco-design/web-vue/es/message";
import moment from "moment";
import axios from "axios";

const router = useRouter();

// 定义题目类型
interface Question {
  id: string;
  title: string;
  tags: string[];
  createTime: string;
  acceptedNum?: number;
  submitNum?: number;
}

// 收藏题目列表
const favorites = ref<Question[]>([]);
const total = ref(0);

// 搜索参数
const searchParams = ref({
  pageSize: 10,
  current: 1
});

// 列配置
const columns = [
  {
    title: "题号",
    dataIndex: "id"
  },
  {
    title: "题目名称",
    dataIndex: "title",
    ellipsis: true
  },
  {
    title: "标签",
    slotName: "tags",
    ellipsis: true
  },
  {
    title: "通过率",
    dataIndex: "acceptedNum",
    customRender: ({ record }: { record: Question }) => {
      if (!record.acceptedNum || !record.submitNum || record.submitNum === 0) {
        return "0%";
      }
      return ((record.acceptedNum / record.submitNum) * 100).toFixed(2) + "%";
    }
  },
  {
    title: "提交数",
    dataIndex: "submitNum"
  },
  {
    title: "收藏时间",
    dataIndex: "createTime",
    customRender: ({ record }: { record: Question }) => {
      return moment(record.createTime).format("YYYY-MM-DD");
    }
  },
  {
    slotName: "optional"
  }
];

// 跳转到题目详情页
const toQuestionPage = (questionId?: string) => {
  if (questionId) {
    router.push(`/view/question/${questionId}`);
  }
};

// 移除收藏
const removeFavorite = async (questionId?: string) => {
  if (!questionId) return;
  try {
    // 调用取消收藏的API
    const res = await axios.post("/api/question_favour/", {
      questionId: Number(questionId)
    });
    if (res.data.code === 0) {
      message.success("取消收藏成功");
      // 重新加载收藏列表
      loadFavorites();
    } else {
      message.error("取消收藏失败：" + res.data.message);
    }
  } catch (error) {
    console.error("取消收藏异常：", error);
    message.error("取消收藏失败：" + (error as Error).message);
  }
};

// 分页变化
const onPageChange = (page: number) => {
  searchParams.value.current = page;
  loadFavorites();
};

// 加载收藏列表
const loadFavorites = async () => {
  try {
    // 调用获取收藏列表的API
    const res = await axios.post("/api/question_favour/my/list/page", {
      current: searchParams.value.current,
      pageSize: searchParams.value.pageSize
    });
    if (res.data.code === 0) {
      favorites.value = res.data.data.records;
      total.value = res.data.data.total;
    } else {
      message.error("加载收藏列表失败：" + res.data.message);
    }
  } catch (error) {
    console.error("加载收藏列表异常：", error);
    message.error("加载收藏列表失败：" + (error as Error).message);
  }
};

// 页面加载时获取数据
onMounted(() => {
  loadFavorites();
});
</script>

<style scoped>
#userFavoritesView {
  padding: 24px;
  max-width: 1280px;
  margin: 0 auto;
}

/* 移除浅色主题下标签文字颜色的所有强制设置，让组件使用默认的文字颜色 */
/* Arco Design 会根据标签颜色自动调整文字颜色，确保良好的对比度 */
:root:not([data-theme="dark"]) #userFavoritesView .arco-tag,
:root:not([data-theme="dark"]) #userFavoritesView .arco-tag-success,
:root:not([data-theme="dark"]) #userFavoritesView .arco-tag-green,
:root:not([data-theme="dark"]) #userFavoritesView .arco-tag-warning,
:root:not([data-theme="dark"]) #userFavoritesView .arco-tag-orange,
:root:not([data-theme="dark"]) #userFavoritesView .arco-tag-danger,
:root:not([data-theme="dark"]) #userFavoritesView .arco-tag-blue {
  color: inherit !important;
}

/* 确保深色主题下卡片正确显示 */
:root[data-theme="dark"] #userFavoritesView .arco-card {
  background-color: #1f1f1f !important;
  border-color: #333333 !important;
  color: #ffffff !important;
}

/* 确保深色主题下表格正确显示 */
:root[data-theme="dark"] #userFavoritesView .arco-table {
  background-color: #1f1f1f !important;
  border-color: #333333 !important;
}

:root[data-theme="dark"] #userFavoritesView .arco-table-th,
:root[data-theme="dark"] #userFavoritesView .arco-table-td {
  background-color: #1f1f1f !important;
  border-color: #333333 !important;
  color: #ffffff !important;
}

/* 确保深色主题下标题正确显示 */
:root[data-theme="dark"] #userFavoritesView h1,
:root[data-theme="dark"] #userFavoritesView h2,
:root[data-theme="dark"] #userFavoritesView h3,
:root[data-theme="dark"] #userFavoritesView h4,
:root[data-theme="dark"] #userFavoritesView p {
  color: #ffffff !important;
}

/* 确保深色主题下链接正确显示 */
:root[data-theme="dark"] #userFavoritesView a {
  color: #409eff !important;
}

/* 确保深色主题下输入框和选择框正确显示 */
:root[data-theme="dark"] #userFavoritesView .arco-input,
:root[data-theme="dark"] #userFavoritesView .arco-select-selector {
  background-color: #1f1f1f !important;
  color: #ffffff !important;
  border-color: #333333 !important;
}

/* 确保深色主题下标签正确显示 */
:root[data-theme="dark"] #userFavoritesView .arco-tag {
  background-color: #2d2d2d !important;
  color: #ffffff !important;
  border-color: #333333 !important;
}
</style>