package com.seckillflow.domain.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author 周子斐
 * @date 2023/4/3
 * @Description
 */
@Data
public class FileDetailDTO {
    private Long id;
    private String jobId;
    private String jobName;
    private String scheduleId;
    private String requestBody;
    private String responseBody;
    private String status;
    private Date startTime;
    private Date endTime;
}
