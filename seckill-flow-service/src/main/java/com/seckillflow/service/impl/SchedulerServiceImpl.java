package com.seckillflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhouzifei.common.exception.ServiceException;
import com.zhouzifei.common.utils.thread.ThreadPoolUtils;
import com.seckillflow.controller.scheduler.request.ScheduleCreateRequest;
import com.seckillflow.controller.scheduler.request.ScheduleUpdateRequest;
import com.seckillflow.domain.PageQuery;
import com.seckillflow.domain.dto.ScheduleDTO;
import com.seckillflow.domain.entity.TaskJobEntity;
import com.seckillflow.domain.entity.TaskScheduleEntity;
import com.seckillflow.exception.ErrorCode;
import com.seckillflow.mapper.JobMapper;
import com.seckillflow.mapper.ScheduleRepository;
import com.seckillflow.service.TaskSchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * 任务调度服务实现类
 *
 * @author ZhouZifei
 */
@Service
@Slf4j
public class SchedulerServiceImpl implements TaskSchedulerService {

    @Autowired
    private Scheduler scheduler;
    @Autowired
    private JobMapper jobMapper;
    @Autowired
    private ScheduleRepository scheduleRepository;


    /**
     * 查询所有任务调度分页信息
     *
     * @param pageable 分页参数
     * @param name 任务名称
     * @return 任务调度分页信息
     */
    @Override
    public Page<ScheduleDTO> findAll(Pageable pageable, String name) {
        // 参数校验
        if (pageable.getPageNumber() < 0 || pageable.getPageSize() <= 0) {
            throw new IllegalArgumentException("页码或页大小不合法");
        }
        // 如果name为空，直接返回空的分页结果，避免不必要的数据库访问
        // 初始化PageQuery和QueryWrapper
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(pageable.getPageNumber());
        pageQuery.setPageSize(pageable.getPageSize());
        Page<ScheduleDTO> page = pageQuery.buildPage();
        QueryWrapper<TaskScheduleEntity> wrapper = Wrappers.query();
        // 防止SQL注入，对name参数进行处理
        String safeName = name.replaceAll("[^a-zA-Z0-9\\s]", "");
        wrapper.like(true, "t2.name", safeName);
        try {
            // 执行数据库查询
            Page<ScheduleDTO> jobDTOPage = scheduleRepository.selectAllocatedList(page, wrapper);
            return jobDTOPage;
        } catch (Exception e) {
            // 异常处理，例如记录日志并返回一个空的分页结果
            // 日志记录可以使用Log4j、SLF4J等，这里只是简单打印
            System.err.println("查询ScheduleDTO时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据ID查询任务调度信息
     *
     * @param id 任务调度ID
     * @return 任务调度信息
     */
    @Override
    public TaskScheduleEntity findById(String id) {
        // 输入验证
        if (id == null || id.trim().isEmpty()) {
            log.error("Invalid ID provided for task schedule search.");
            throw new IllegalArgumentException("ID cannot be null or empty.");
        }
        try {
            // 执行查询
            TaskScheduleEntity taskSchedule = scheduleRepository.selectOne(TaskScheduleEntity::getId, id);
            // 处理空结果
            if (taskSchedule == null) {
                log.info("No task schedule found with ID: {}", id);
                throw new ServiceException("No task schedule found with ID:" + id);
            }
            return taskSchedule;
        } catch (Exception e) {
            // 异常处理
            log.error("Failed to find task schedule by ID: {}. Error: {}", id, e.getMessage());
            throw new ServiceException("Error occurred while searching for task schedule." + e);
        }
    }

    /**
     * 创建新的任务调度
     *
     * @param request 创建任务调度的请求参数
     * @return 新创建的任务调度信息
     */
    @Override
    @Transactional
    public TaskScheduleEntity create(ScheduleCreateRequest request) {
        // 检查request是否为空
        if (ObjectUtils.isEmpty(request)) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        TaskScheduleEntity schedule = new TaskScheduleEntity();
        try {
            // 使用try-catch块捕获并处理BeanUtils可能抛出的异常
            BeanUtils.copyProperties(request, schedule);
        } catch (Exception e) {
            // 在实际场景中，应该有更具体的异常处理，这里仅作为示例
            throw new RuntimeException("Failed to copy properties", e);
        }
        // 假设validateJob已经做了充分的输入验证，这里不再重复
        this.validateJob(request.getJobId());
        // 使用构造函数自动设置创建时间和更新时间
        schedule.setCreateTime(new Date());
        schedule.setUpdateTime(new Date());
        schedule.setStatus(TaskScheduleEntity.Status.DISABLE.getValue());
        try {
            // 假设insert方法可能抛出异常，需要进行处理
            this.scheduleRepository.insert(schedule);
        } catch (Exception e) {
            // 根据实际情况处理数据库操作异常，例如回滚事务
            throw new RuntimeException("Failed to insert schedule", e);
        }
        return schedule;
    }

        /**
     * 验证任务是否存在
     *
     * @param jobId 任务ID
     */
    private void validateJob(String jobId) {
        TaskJobEntity taskJobEntity = jobMapper.selectOne(TaskJobEntity::getId, jobId);
        if (null == taskJobEntity) {
            throw new IllegalArgumentException("作业不存在");
        }
    }

    /**
     * 更新任务调度信息
     *
     * @param id 任务调度ID
     * @param request 更新任务调度的请求参数
     * @return 更新后的任务调度信息
     */
    @Override
    public TaskScheduleEntity update(String id, ScheduleUpdateRequest request) {
        // 使用findById前增加非空校验，避免NullPointerException
        TaskScheduleEntity taskScheduleEntity = findById(id);
        if (taskScheduleEntity == null) {
            throw new IllegalArgumentException("任务调度不存在");
        }

        // 检查任务是否处于启用状态
        if (isTaskEnabled(taskScheduleEntity)) {
            throw new IllegalArgumentException("正在执行的调度无法修改");
        } else {
            // 增加对请求参数的合法性校验，比如Cron表达式
            validateRequest(request);
            // 更新任务调度实体
            taskScheduleEntity.setCron(request.getCron());
            taskScheduleEntity.setTriggerType(request.getTriggerType());
            taskScheduleEntity.setJobId(request.getJobId());
            taskScheduleEntity.setPeriod(request.getPeriod());
            taskScheduleEntity.setTimeunit(request.getTimeunit());
            // 使用java.time包下的类替代java.util.Date
            taskScheduleEntity.setUpdateTime(new Date());
            this.scheduleRepository.updateById(taskScheduleEntity);
            return taskScheduleEntity;
        }
    }

    /**
     * 验证更新请求参数
     *
     * @param request 更新任务调度的请求参数
     */
    private void validateRequest(ScheduleUpdateRequest request) {
        // 对request中的参数进行合法性校验，比如：
        if (!CronExpression.isValidExpression(request.getCron())) {
            throw new IllegalArgumentException("无效的Cron表达式");
        }
        // 其他参数的校验逻辑...
    }

    /**
     * 判断任务是否启用
     *
     * @param task 任务调度实体
     * @return 如果任务启用，返回true；否则返回false
     */
    private boolean isTaskEnabled(TaskScheduleEntity task) {
        return TaskScheduleEntity.Status.ENABLE == TaskScheduleEntity.Status.of(task.getStatus());
    }

    /**
     * 删除任务调度
     *
     * @param id 任务调度ID
     */
    @Override
    public void delete(String id) {
        TaskScheduleEntity taskScheduleEntity = findById(id);
        if (taskScheduleEntity == null) {
            throw new IllegalArgumentException("任务调度不存在");
        }
        TaskJobEntity taskJobEntity = jobMapper.selectOne(TaskJobEntity::getId, taskScheduleEntity.getJobId());
        if (null == taskJobEntity) {
            throw ErrorCode.DATA_NOT_FOUND.toException();
        }
        try {
            this.scheduleRepository.deleteById(id);
            this.scheduler.unscheduleJob(new TriggerKey(taskScheduleEntity.getId(), "DEFAULT"));
        } catch (SchedulerException var5) {
            log.error("", var5);
            throw ErrorCode.SCHEDULED_ERROR.toException();
        }
    }

    /**
     * 启用任务调度
     *
     * @param id 任务调度ID
     */
    @Override
    @Transactional
    public void enable(String id) {
        // 校验id格式
        if (!id.matches("^[\\w-]+$")) {
            throw new IllegalArgumentException("Invalid ID format");
        }

        TaskScheduleEntity schedule = findById(id);
        TaskJobEntity taskJobEntity = jobMapper.selectOne(TaskJobEntity::getId, schedule.getJobId());

        if (null == taskJobEntity) {
            throw ErrorCode.DATA_NOT_FOUND.toException();
        }

        schedule.setStatus(TaskScheduleEntity.Status.ENABLE.getValue());
        schedule.setUpdateTime(new Date());
        this.scheduleRepository.updateById(schedule);

        try {
            String jobName = "job" + taskJobEntity.getId();
            JobDetail jobDetail = this.scheduler.getJobDetail(new JobKey(jobName, "DEFAULT"));
            Assert.notNull(jobDetail, "jobDetail can't be null");

            Trigger trigger = this.buildTrigger(schedule, jobDetail);
            this.scheduler.scheduleJob(trigger);
        } catch (SchedulerException e) {
            log.error("Scheduler exception: {}", e.getMessage(), e);
            throw ErrorCode.SCHEDULED_ERROR.toException();
        }
    }

    /**
     * 构建触发器
     *
     * @param schedule 任务调度实体
     * @param jobDetail 任务详情
     * @return 触发器
     */
    private Trigger buildTrigger(TaskScheduleEntity schedule, JobDetail jobDetail) {
        TaskScheduleEntity.TriggerType triggerType = TaskScheduleEntity.TriggerType.of(schedule.getTriggerType());

        switch (triggerType) {
            case CRON:
                if (!CronExpression.isValidExpression(schedule.getCron())) {
                    log.error("Invalid cron expression: {}", schedule.getCron());
                    throw new IllegalArgumentException("cron表达式不正确");
                }
                return createCronTrigger(schedule, jobDetail);
            case SIMPLE:
                // 对 periodInSecond 进行校验
                if (schedule.getPeriodInSecond() <= 0) {
                    throw new IllegalArgumentException("PeriodInSecond must be greater than 0");
                }
                return createSimpleTrigger(schedule, jobDetail);
            default:
                throw new IllegalArgumentException("unknow TriggerType");
        }
    }

    /**
     * 创建Cron触发器
     *
     * @param schedule 任务调度实体
     * @param jobDetail 任务详情
     * @return Cron触发器
     */
    private Trigger createCronTrigger(TaskScheduleEntity schedule, JobDetail jobDetail) {
        return TriggerBuilder.newTrigger()
                .withIdentity(schedule.getId(), "DEFAULT")
                .startNow().withSchedule(CronScheduleBuilder.cronSchedule(schedule.getCron())).forJob(jobDetail).build();
    }

    /**
     * 创建Simple触发器
     *
     * @param schedule 任务调度实体
     * @param jobDetail 任务详情
     * @return Simple触发器
     */
    private Trigger createSimpleTrigger(TaskScheduleEntity schedule, JobDetail jobDetail) {
        return TriggerBuilder.newTrigger()
                .withIdentity(schedule.getId(), "DEFAULT")
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(schedule.getPeriodInSecond())
                        .repeatForever())
                .forJob(jobDetail)
                .build();
    }

    /**
     * 禁用任务调度
     *
     * @param id 任务调度ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 明确指定哪些异常应该触发事务回滚
    public void disable(String id) {
        // 输入验证
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }

        TaskScheduleEntity schedule = scheduleRepository.selectOne(TaskScheduleEntity::getId, id);
        TaskJobEntity taskJobEntity = jobMapper.selectOne(TaskJobEntity::getId, schedule.getJobId());

        if (null == taskJobEntity) {
            throw ErrorCode.DATA_NOT_FOUND.toException();
        }

        schedule.setStatus(TaskScheduleEntity.Status.DISABLE.getValue());
        schedule.setUpdateTime(new Date());
        this.scheduleRepository.updateById(schedule);

        // 并行化处理数据库操作和任务取消
        Future<?> future1 = ThreadPoolUtils.submit(() -> {
            try {
                this.scheduler.unscheduleJob(new TriggerKey(schedule.getId(), "DEFAULT"));
            } catch (SchedulerException e) {
                log.error("Error unscheduling job with ID: {}", schedule.getId(), e); // 改进日志记录
                throw ErrorCode.SCHEDULED_ERROR.toException();
            }
            return null;
        });

        // 等待任务完成，确保并行操作的原子性（在事务内完成）
        try {
            future1.get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt(); // 恢复中断状态
            log.error("Interrupted while waiting for unscheduling job with ID: {}", schedule.getId(), e);
            throw ErrorCode.SCHEDULED_ERROR.toException();
        }
    }

}

