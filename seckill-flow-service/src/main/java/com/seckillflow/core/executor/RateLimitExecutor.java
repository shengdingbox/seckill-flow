package com.seckillflow.core.executor;

import com.seckillflow.domain.entity.TaskJobEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Data
@Slf4j
public class RateLimitExecutor implements JobExecutor {

    private Semaphore permits;
    private JobExecutor jobExecutor;

    public RateLimitExecutor(Semaphore permits, JobExecutor jobExecutor) {
        this.permits = permits;
        this.jobExecutor = jobExecutor;
    }

    @Override
    public JobExecution execute(TaskJobEntity taskJobEntity) throws Exception {
        JobExecution jobExecution = null;
        boolean acquired = false;
        try {
            log.info("开始获取令牌");
            // 尝试带有超时的获取许可证，以避免长时间阻塞
            acquired = permits.tryAcquire(5, TimeUnit.SECONDS);
            if (!acquired) {
                throw new InterruptedException("超时未获取到许可证");
            }
            log.info("成功获取到令牌,当前可用线程数：" + permits.availablePermits());
            jobExecution = jobExecutor.execute(taskJobEntity);
        } catch (InterruptedException e) {
            // 重新设置中断状态，以保留线程的中断状态
            Thread.currentThread().interrupt();
            // 可选：根据业务需求决定是否需要抛出自定义异常或进行其他错误处理
            throw new Exception("执行任务过程中被中断", e);
        } finally {
            if (acquired) {
                permits.release();
                log.info("释放令牌，当前可用线程数：" + permits.availablePermits());
            }
        }
        return jobExecution;
    }
}
