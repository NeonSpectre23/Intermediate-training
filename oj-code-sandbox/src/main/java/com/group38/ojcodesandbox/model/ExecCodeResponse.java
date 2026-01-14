package com.group38.ojcodesandbox.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecCodeResponse {

    private String message; // 接口信息

    private Integer status; // 状态码

    private JudgeInfo judgeInfo; // 判题信息

    private List<String> outputList; // 输出列表 <String>


}
