package com.seckillflow.controller.scheduler.request;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class JobCreateRequest {

    private String name;
    private String groupId;
    private String type;
    private String httpUrl;
    private String httpBody;
    private String httpMethod;
    private String httpContentType;
    private String httpHeader;
    private String getField;
    private String datasource;
    private String scriptCommand;
}
