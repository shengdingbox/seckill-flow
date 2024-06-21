package com.seckillflow.domain.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ScheduleDTO {
    private String id;
    private String jobName;
    private String triggerType;
    private String cron;
    private Integer period;
    private String timeunit;
    private String status;
    private Date createTime;
    private Date updateTime;
}
