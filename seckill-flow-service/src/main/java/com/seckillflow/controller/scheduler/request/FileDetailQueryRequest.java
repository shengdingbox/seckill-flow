package com.seckillflow.controller.scheduler.request;

import lombok.Data;

/**
 * @author 周子斐
 * @date 2023/4/3
 * @Description
 */
@Data
public class FileDetailQueryRequest {

    private String pathFileName;
    private String fileName;
    private String status;
}
