package com.seckillflow.core.executor;

import com.seckillflow.domain.entity.TaskJobEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Collections;
import java.util.Properties;

@Slf4j
public class RetryableJobExecutor implements JobExecutor {

    private JobExecutor jobExecutor;
    // 假设这些配置可以从外部配置文件中加载
    private int retryCount; // 重试次数
    private long backOffPeriod; // 重试间隔

    public RetryableJobExecutor(JobExecutor jobExecutor, Properties config) {
        this.jobExecutor = jobExecutor;
        this.retryCount = Integer.parseInt(config.getProperty("retryCount", "3")); // 默认重试3次
        this.backOffPeriod = Long.parseLong(config.getProperty("backOffPeriod", "3000")); // 默认重试间隔3秒
        if (jobExecutor == null) {
            throw new IllegalArgumentException("JobExecutor cannot be null");
        }
    }

    @Override
    public JobExecution execute(TaskJobEntity taskJobEntity) throws Exception {
        RetryTemplate template = new RetryTemplate();
        SimpleRetryPolicy policy = new SimpleRetryPolicy(retryCount, Collections.singletonMap(Exception.class, Boolean.TRUE));
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(backOffPeriod);
        template.setBackOffPolicy(backOffPolicy);
        template.setRetryPolicy(policy);

        return (JobExecution) template.execute(context -> {
            log.info("Executing job: " + taskJobEntity.toString());
            try {
                return jobExecutor.execute(taskJobEntity);
            } catch (Exception e) {
                log.error("Job execution failed, will retry. Job: " + taskJobEntity.toString(), e);
                throw e;
            }
        });
    }
}
