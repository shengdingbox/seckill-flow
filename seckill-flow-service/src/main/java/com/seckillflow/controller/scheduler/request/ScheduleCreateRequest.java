package com.seckillflow.controller.scheduler.request;

import lombok.Data;

@Data
public class ScheduleCreateRequest {

    private String jobId;
    private String triggerType;
    private String cron;
    private Integer period;
    private String timeunit;
}
