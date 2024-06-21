package com.seckillflow.controller.scheduler.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class BatchJobInstance implements Serializable {
    private String jobInstanceId;
    private String version;
    private String jobName;
    private String jobKey;

}
