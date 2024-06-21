package com.seckillflow.controller.scheduler.request;
import lombok.Data;

import java.util.Date;

@Data
public class LogQueryRequest {


    private String jobId;
    private String jobName;
    private String scheduleId;
    private String status;
    private Date startTimeFrom;
    private Date startTimeTo;
}
