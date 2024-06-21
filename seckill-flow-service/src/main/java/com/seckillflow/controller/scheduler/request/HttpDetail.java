package com.seckillflow.controller.scheduler.request;

import lombok.Data;


@Data
public class HttpDetail{


    private String type;
    private String httpUrl;
    private String httpBody;
    private String httpHeader;
    private String httpMethod;
    private String httpContentType;
    private String datasource;
    private String dataSql;
    private String classStr;
}
