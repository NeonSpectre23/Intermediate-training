package com.group38.oj.judge.sandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecCodeRequest {

    private String code; // 代码

    private String lang; // 编程语言

    private List<String> inputList; // 输入列表 <String>

}
