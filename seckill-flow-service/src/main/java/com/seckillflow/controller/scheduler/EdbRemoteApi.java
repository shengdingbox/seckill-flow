package com.seckillflow.controller.scheduler;

import com.zhouzifei.common.config.Response;
import com.seckillflow.controller.scheduler.request.BatchFileQueryRequest;
import com.seckillflow.controller.scheduler.request.FileDetailQueryRequest;
import com.seckillflow.domain.dto.FileDetailDTO;
import com.seckillflow.domain.model.BatchFileReg;
import com.seckillflow.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * @Author: 周子斐 (17600004572@163.com)
 * @Date:2020-11-28 10:53
 * @Description edb远端获取指定文件
 * @Copyright 盛鼎科技 Copyright(c)
 */
@RequestMapping("/v1")
@RestController
public class EdbRemoteApi {

    @Autowired
    private FileService fileService;

    @GetMapping("batch-files")
    public Response findByDate(BatchFileQueryRequest request) {
        List<BatchFileReg> batchFileRegs = fileService.findByDateAndSource(request);
        return Response.ok(batchFileRegs);
    }
    @GetMapping("batch-files/detail")
    public Response detail(FileDetailQueryRequest request) {
        FileDetailDTO batchFileRegs = fileService.detail(request);
        return Response.ok(batchFileRegs);
    }
}
