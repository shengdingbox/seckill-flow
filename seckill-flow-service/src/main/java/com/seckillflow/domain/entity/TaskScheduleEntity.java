package com.seckillflow.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.concurrent.TimeUnit;


@Data
@TableName("TASK_SCHEDULE")
public class TaskScheduleEntity {

    private String id;
    private String jobId;
    private String triggerType;
    private String cron;
    private Integer period;
    private String timeunit;
    private String status;
    private Date createTime;
    private Date updateTime;

    public int getPeriodInSecond() {
        if (!TriggerType.SIMPLE.value.equals(triggerType)) {
            return 0;
        }else if ("S".equals(timeunit)) {
            return period;
        }else if ("M".equals(timeunit)) {
            return Math.toIntExact(TimeUnit.MINUTES.toSeconds(period));
        }else if ("H".equals(timeunit)) {
            return Math.toIntExact(TimeUnit.HOURS.toSeconds(period));
        }else if ("D".equals(timeunit)) {
            return Math.toIntExact(TimeUnit.DAYS.toSeconds(period));
        } else {
            throw new IllegalArgumentException("未知的时间单位：" + timeunit);
        }
    }

    public static enum TriggerType {
        SIMPLE("1"),
        CRON("2");
        private String value;

        public static TriggerType of(String value) {
            if (SIMPLE.value.equals(value)) {
                return SIMPLE;
            }else if (CRON.value.equals(value)) {
                return CRON;
            } else {
                throw new IllegalArgumentException("illegal TriggerType:" + value);
            }
        }
        private TriggerType(String value) {
            this.value = value;
        }
    }

    public static enum Status {
        DISABLE("0"),
        ENABLE("1");
        private String value;

        Status(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Status of(String value) {
            if (DISABLE.value.equals(value)) {
                return DISABLE;
            } else if (ENABLE.value.equals(value)) {
                return ENABLE;
            } else {
                throw new IllegalArgumentException("illegal status:" + value);
            }
        }
    }
}
