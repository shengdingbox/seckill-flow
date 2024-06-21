package com.seckillflow.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Data
@TableName("TASK_LOG")
public class LogModel {

    @Id
    private Integer id;
    private String jobId;
    private String jobName;
    private String scheduleId;
    private String requestBody;
    private String responseBody;
    private String status;
    private Date startTime;
    private Date endTime;
}
