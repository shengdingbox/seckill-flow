package com.seckillflow.core.executor;

import com.seckillflow.domain.entity.TaskJobEntity;

public interface JobExecutor {

    JobExecution execute(TaskJobEntity taskJobEntity) throws Exception;

    default boolean isThread(){
        return true;
    }
}
