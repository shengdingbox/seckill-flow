package com.seckillflow.core.executor;

import com.seckillflow.domain.entity.TaskJobEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ShellJobExecutor implements JobExecutor {

    @Override
    public JobExecution execute(TaskJobEntity taskJobEntity) throws Exception {
        //TODO JobExecution jobExecution = Shells.exec(job.getShellCommand());
        JobExecution jobExecution = new JobExecution();
        return jobExecution;
    }

}
