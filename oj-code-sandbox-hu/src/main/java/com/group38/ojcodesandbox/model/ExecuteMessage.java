package com.group38.ojcodesandbox.model;

import lombok.Data;

//执行进程信息
@Data
public class ExecuteMessage {
    private Integer exitValue;

    private String message;

    private String errorMessage;

    private Long time;
}
