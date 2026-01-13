package com.group38.oj.controller;

import cn.hutool.core.io.FileUtil;
import com.group38.oj.common.BaseResponse;
import com.group38.oj.common.ErrorCode;
import com.group38.oj.common.ResultUtils;
import com.group38.oj.exception.BusinessException;
import com.group38.oj.model.enums.FileUploadBizEnum;
import com.group38.oj.service.UserService;
import java.util.Arrays;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件接口
 *


 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private UserService userService;

    /**
     * 文件上传
     *
     * @param multipartFile
     * @param uploadFileRequest
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
            @RequestParam(value = "biz", required = false) String biz,
            HttpServletRequest request) {
        if (biz == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        validFile(multipartFile, fileUploadBizEnum);
        
        // 生成随机UUID
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        
        // 直接返回模拟URL，不依赖任何外部存储服务
        // 使用 picsum.photos 提供的随机图片服务作为模拟
        String mockUrl = "https://picsum.photos/400/400?random=" + uuid;
        log.info("File uploaded successfully, using mock URL: {}", mockUrl);
        
        return ResultUtils.success(mockUrl);
    }

    /**
     * 校验文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024L;
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }
}
