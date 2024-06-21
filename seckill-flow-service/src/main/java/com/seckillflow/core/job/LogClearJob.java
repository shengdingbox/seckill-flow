package com.seckillflow.core.job;

import com.seckillflow.mapper.LogMapper;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogClearJob implements Job {

    @Autowired
    private LogMapper logMapper;

    @Value("${scheduler.log.keepDays}")
    private int keepDays;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("开始清理日志：" + keepDays + "天前的日志");
        int deletedRows = logMapper.deleteBeforeDay(keepDays);
        log.info("清理记录数：" + deletedRows);
    }
}
