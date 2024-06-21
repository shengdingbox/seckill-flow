package com.seckillflow.domain.dto;

import com.seckillflow.domain.entity.TaskJobEntity;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Data
public class JobDTO {


    private Integer id;
    private String name;
    private String groupId;
    private String groupName;
    private String type;
    private String httpUrl;
    private String httpBody;
    private String httpMethod;
    private String httpContentType;
    private String httpHeader;
    private String getField;
    private String datasource;
    private String scriptCommand;
    private Date createTime;
    private Date updateTime;

    public static JobDTO of(TaskJobEntity taskJobEntity) {
        JobDTO jobDTO = new JobDTO();
        BeanUtils.copyProperties(taskJobEntity, jobDTO);
        return jobDTO;
    }
}
