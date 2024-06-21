package com.seckillflow.service;


import com.seckillflow.core.executor.JobExecution;
import com.seckillflow.domain.entity.TaskJobEntity;

public interface JobExecutorService {

    public abstract JobExecution exec(TaskJobEntity job)
        throws Exception;
}
