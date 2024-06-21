package com.seckillflow.controller.scheduler.request;

import lombok.Data;

@Data
public class JobQueryRequest {

    private String name;
    private String groupName;
}
