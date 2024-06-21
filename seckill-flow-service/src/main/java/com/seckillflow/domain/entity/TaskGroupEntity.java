package com.seckillflow.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;


@Data
@TableName("TASK_GROUP")
public class TaskGroupEntity {

    @Id
    private String id;
    private String name;
    private String description;
    private Date createTime;
    private Date updateTime;
}
