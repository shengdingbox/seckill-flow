package com.seckillflow.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;


@TableName("TASK_JOB")
@Data
public class TaskJobEntity {

    @Id
    private Integer id;
    private String name;
    private String groupId;
    private String type;
    private String httpUrl;
    private String httpBody;
    private String httpMethod;
    private String httpContentType;
    private String httpHeader;
    private String datasource;
    private String scriptCommand;
    private Date createTime;
    private Date updateTime;

    enum Type {
        HTTP("1"),
        SHELL("2");
        private String value;

        Type(String value) {
            this.value = value;
        }

        public static Type of(String value) {
            if (HTTP.value.equals(value)) {
                return HTTP;
            }else if(SHELL.value.equals(value)){
                return SHELL;
            }else {
                throw new IllegalArgumentException("illegal TriggerType:" + value);
            }
        }
    }
}
