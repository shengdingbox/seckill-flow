package com.seckillflow.core.job;

import com.seckillflow.core.executor.JobExecution;
import com.seckillflow.core.executor.JobExecutor;
import com.seckillflow.core.executor.JobExecutorFactory;
import com.seckillflow.domain.entity.TaskJobEntity;
import com.seckillflow.domain.entity.TaskScheduleEntity;
import com.seckillflow.domain.model.LogModel;
import com.seckillflow.mapper.JobMapper;
import com.seckillflow.mapper.LogMapper;
import com.seckillflow.mapper.ScheduleRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 基础任务类，实现Quartz的Job接口，用于定义具体任务的执行逻辑。
 * 任务执行过程中，会根据任务ID查询任务详细信息，并根据触发器名称查询调度信息。
 * 执行任务并记录执行日志。
 */
@Slf4j
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
@Component
public class BaseJob implements org.quartz.Job {

    /**
     * 任务Mapper，用于查询任务相关信息。
     */
    @Autowired
    private JobMapper jobMapper;
    /**
     * 日志Mapper，用于插入任务执行日志。
     */
    @Autowired
    private LogMapper logMapper;
    /**
     * 调度仓库，用于查询调度信息。
     */
    @Autowired
    private ScheduleRepository scheduleRepository;
    /**
     * 任务执行器工厂，根据任务类型创建相应的任务执行器。
     */
    @Autowired
    private JobExecutorFactory jobExecutorFactory;

    /**
     * 执行任务。
     * @param context 任务执行上下文，包含任务详细信息和触发器信息。
     * @throws JobExecutionException 如果任务执行过程中发生异常。
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String id = context.getJobDetail().getKey().getName();
        TaskJobEntity taskJobEntity = queryJobById(id);
        TaskScheduleEntity taskScheduleEntity = queryScheduleByTriggerName(context.getTrigger().getKey().getName());

        // 如果调度记录不存在，记录日志并返回
        if (taskScheduleEntity == null) {
            log.warn("调度记录不存在：" + context.getTrigger().getKey().getName());
            return;
        }

        // 初始化任务日志列表
        List<LogModel> logModels = new ArrayList<>();
        LogModel logModel = new LogModel();
        logModel.setJobId(id);
        logModel.setScheduleId(taskScheduleEntity.getId());
        logModel.setStartTime(new Date());

        // 如果任务信息存在，则执行任务并记录日志
        if (taskJobEntity != null) {
            logModel.setJobName(taskJobEntity.getName());
            JobExecutor jobExecutor = jobExecutorFactory.get(taskJobEntity);
            JobExecution jobExecution = null;
            try {
                jobExecution = jobExecutor.execute(taskJobEntity);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            logModel.setStatus(String.valueOf(jobExecution.getStatus()));
            logModel.setResponseBody(StringUtils.substring(jobExecution.getResult(), 0, 4000)); // 保留优化建议：日志截断
            log.info("任务执行结果: {}", jobExecution.getResult());
            logModels.add(logModel);
        } else {
            // 如果任务信息不存在，记录错误日志
            logModel.setStatus("999998");
            logModel.setResponseBody("作业不存在");
            log.error("任务信息不存在，JobId：" + id);
            logModels.add(logModel);
        }

        // 更新任务日志的结束时间，并插入日志数据库
        logModel.setEndTime(new Date());
        logMapper.insertBatch(logModels);
    }

    /**
     * 根据任务ID查询任务详细信息。
     * @param jobId 任务ID
     * @return 任务详细信息实体
     */
    private TaskJobEntity queryJobById(String jobId) {
        if (StringUtils.isBlank(jobId)) {
            log.error("查询任务信息失败，JobId为空");
            return null;
        }
        // 优化字符串操作逻辑
        jobId = jobId.replaceFirst("job", "");
        try {
            return jobMapper.selectOne(TaskJobEntity::getId, jobId);
        } catch (Exception e) {
            log.error("查询任务信息失败，JobId：" + jobId, e);
            return null;
        }
    }

    /**
     * 根据触发器名称查询调度信息。
     * @param triggerName 触发器名称
     * @return 调度信息实体
     */
    private TaskScheduleEntity queryScheduleByTriggerName(String triggerName) {
        try {
            return scheduleRepository.selectOne(TaskScheduleEntity::getId, triggerName);
        } catch (Exception e) {
            log.error("查询调度信息失败，TriggerName：" + triggerName, e);
            return null;
        }
    }
}
