package com.seckillflow.service.impl;


import com.seckillflow.core.executor.JobExecutorFactory;
import com.seckillflow.core.executor.JobExecution;
import com.seckillflow.core.executor.JobExecutor;
import com.seckillflow.domain.entity.TaskJobEntity;
import com.seckillflow.domain.model.LogModel;
import com.seckillflow.mapper.LogMapper;
import com.seckillflow.service.JobExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Slf4j
public class JobExecutorServiceImpl implements JobExecutorService {
    @Autowired
    private LogMapper logMapper;
    @Autowired
    private JobExecutorFactory jobExecutorFactory;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public JobExecution exec(TaskJobEntity taskJobEntity) throws Exception {
        log.info("开始执行作业：{}", taskJobEntity.getName());
        LogModel logModel = createLogModel(taskJobEntity);

        try {
            JobExecution jobExecutions = executeJob(taskJobEntity, logModel);
            logModel.setStatus(String.valueOf(jobExecutions.getStatus()));
            logExecResult(jobExecutions.getResult());
            logModel.setEndTime(new Date());
            return jobExecutions;
        } catch (Exception e) {
            log.error("任务执行出错", e);
            logModel.setStatus("999999");
            throw e;
        } finally {
            // 将日志保存操作放在事务中，确保操作的原子性
            logMapper.insert(logModel);
        }
    }

    private JobExecution executeJob(TaskJobEntity taskJobEntity, LogModel logModel) throws Exception {
        JobExecutor executor = jobExecutorFactory.get(taskJobEntity);
        return executor.execute(taskJobEntity);
    }

    private void logExecResult(String result) {
        if (StringUtils.isNotBlank(result)) {
            log.info(StringUtils.substring(result, 0, 4000) + (StringUtils.length(result) > 4000 ? "..." : ""));
        }
    }

    private LogModel createLogModel(TaskJobEntity taskJobEntity) {
        LogModel logModel = new LogModel();
        logModel.setJobId(String.valueOf(taskJobEntity.getId()));
        logModel.setJobName(taskJobEntity.getName());
        logModel.setStartTime(new Date());
        return logModel;
    }
}
