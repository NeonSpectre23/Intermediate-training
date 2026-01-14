<template>
  <div id="manageQuestionView">
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
      <template #optional="{ record }">
        <a-space>
          <a-button type="primary" @click="doUpdate(record)"> 修改</a-button>
          <a-button status="danger" @click="doDelete(record)">删除</a-button>
        </a-space>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watchEffect } from "vue";
import {
  Question,
  QuestionControllerService,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import { safeCellText } from "@/utils/safeRender";

const tableRef = ref();

const dataList = ref([]);
const total = ref(0);
const searchParams = ref({
  pageSize: 10,
  current: 1,
});

const loadData = async () => {
  const res = await QuestionControllerService.listQuestionVoByPageUsingPost(
    searchParams.value
  );
  if (res.code === 0) {
    // 使用深拷贝将可能带有特殊原型的对象转换为普通 JSON 对象，避免 String 转换报错
    try {
      dataList.value = JSON.parse(JSON.stringify(res.data.records || []));
    } catch {
      dataList.value = (res.data.records || []) as any;
    }
    // 确保分页 total 为普通数字
    total.value = Number((res.data.total as any) || 0);
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

const columns = [
  {
    title: "id",
    dataIndex: "id",
    customRender: ({ text }) => {
      return safeCellText(text);
    }
  },
  {
    title: "标题",
    dataIndex: "title",
    customRender: ({ text }) => {
      return safeCellText(text);
    }
  },
  {
    title: "内容",
    dataIndex: "content",
    customRender: ({ text }) => {
      return safeCellText(text);
    }
  },
  {
    title: "标签",
    dataIndex: "tags",
    customRender: ({ text }) => {
      if (!text) return "";
      if (Array.isArray(text)) {
        return text.map((tag) => safeCellText(tag)).join(", ");
      }
      if (typeof text === "string") {
        try {
          const parsed = JSON.parse(text);
          return Array.isArray(parsed)
            ? parsed.map((tag) => safeCellText(tag)).join(", ")
            : safeCellText(text);
        } catch {
          return safeCellText(text);
        }
      }
      return safeCellText(text);
    }
  },
  {
    title: "答案",
    dataIndex: "answer",
    customRender: ({ text }) => {
      return safeCellText(text);
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
    title: "通过数",
    dataIndex: "acceptedNum",
    customRender: ({ text }) => {
      return safeCellText(Number(text || 0));
    }
  },
  {        title: "判题配置",
    dataIndex: "judgeConfig",
    customRender: ({ text }) => {
      return safeCellText(text);
    }
  },
  {
    title: "判题用例",
    dataIndex: "judgeCase",
    customRender: ({ text }) => {
      return safeCellText(text);
    }
  },
  {
    title: "用户id",
    dataIndex: "userId",
    customRender: ({ text }) => {
      return safeCellText(text);
    }
  },
  {
    title: "创建时间",
    dataIndex: "createTime",
    customRender: ({ text }) => {
      return safeCellText(text);
    }
  },
  {
    title: "操作",
    slotName: "optional",
  },
];

const onPageChange = (page: number) => {
  searchParams.value = {
    ...searchParams.value,
    current: page,
  };
};

const doDelete = async (question: Question) => {
  try {
    // 确保question.id是有效的
    if (!question.id || question.id === "") {
      console.error("题目ID无效");
      message.error("题目不存在");
      return;
    }
    
    // 使用类型断言避免精度丢失，同时解决类型不匹配问题
    const deleteId = question.id;
    
    const res = await QuestionControllerService.deleteQuestionUsingPost({
      id: deleteId as unknown as number,
    });
    if (res.code === 0) {
      message.success("删除成功");
      loadData();
    } else {
      message.error("删除失败: " + res.message);
    }
  } catch (error) {
    console.error("删除题目失败: ", error);
    message.error("删除失败: " + (error as Error).message);
  }
};

const router = useRouter();

const doUpdate = (question: Question) => {
  router.push({
    path: "/update/question",
    query: {
      id: question.id,
    },
  });
};
</script>

<style scoped>
#manageQuestionView {
  padding: 24px;
  max-width: 1280px;
  margin: 0 auto;
}
</style>
