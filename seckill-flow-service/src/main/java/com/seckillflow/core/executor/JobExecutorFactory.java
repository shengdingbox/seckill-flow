package com.seckillflow.core.executor;


import com.seckillflow.core.executor.http.HttpJobExecutor;
import com.seckillflow.core.executor.http.LBHttpJobExecutor;
import com.zhouzifei.common.config.SpringUtils;
import com.seckillflow.domain.entity.TaskJobEntity;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Semaphore;


@Data
@Component
public class JobExecutorFactory implements InitializingBean {

    @Value("${scheduler.executor.threads}")
    private int threadNumbers;
    private Semaphore semaphore;
    @Autowired
    private RestTemplate restTemplate;

    public JobExecutor get(TaskJobEntity taskJobEntity) {
        JobExecutor executor = null;
        if (null == taskJobEntity) {
            return executor;
        }
        if ("1".equals(taskJobEntity.getType())) {
            if (taskJobEntity.getHttpUrl().startsWith("lb://")) {
                executor = new LBHttpJobExecutor(restTemplate);
            } else {
                executor = new HttpJobExecutor();
           }
        }
        if ("2".equals(taskJobEntity.getType())) {
            executor = new ShellJobExecutor();
        }
        if ("3".equals(taskJobEntity.getType())) {
            executor = SpringUtils.getBean("sqlJobExecutor");
        }
        if ("4".equals(taskJobEntity.getType())) {
            executor = SpringUtils.getBean("javaJobExecutor");
        }
        if (executor == null) {
            throw new IllegalArgumentException("unsupported type:" + taskJobEntity.getType());
        } else {
            return new RateLimitExecutor(semaphore,executor);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        semaphore = new Semaphore(threadNumbers);
    }
}
