package com.seckillflow.service.impl;


import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhouzifei.common.exception.ServiceException;
import com.zhouzifei.common.utils.DateUtils;
import com.zhouzifei.common.utils.StreamUtil;
import com.zhouzifei.common.utils.thread.ThreadPoolUtils;
import com.seckillflow.controller.scheduler.request.JobCreateRequest;
import com.seckillflow.controller.scheduler.request.JobQueryRequest;
import com.seckillflow.controller.scheduler.request.JobUpdateRequest;
import com.seckillflow.core.executor.JobExecution;
import com.seckillflow.core.job.BaseJob;
import com.seckillflow.domain.PageQuery;
import com.seckillflow.domain.dto.JobDTO;
import com.seckillflow.domain.dto.ResultDto;
import com.seckillflow.domain.entity.TaskGroupEntity;
import com.seckillflow.domain.entity.TaskJobEntity;
import com.seckillflow.domain.entity.TaskScheduleEntity;
import com.seckillflow.exception.ErrorCode;
import com.seckillflow.mapper.GroupMapper;
import com.seckillflow.mapper.JobMapper;
import com.seckillflow.mapper.ScheduleRepository;
import com.seckillflow.service.JobExecutorService;
import com.seckillflow.service.TaskJobService;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TaskJobServiceImpl implements TaskJobService {
    @Autowired
    private JobMapper jobMapper;
    @Autowired
    private Scheduler scheduler;
    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private JobExecutorService jobExecutorService;


    @Override
    @Transactional(readOnly = true)
    public Page<JobDTO> findAll(Pageable pageable, JobQueryRequest request) {
        // 验证分页参数的合法性
        if (pageable.getPageNumber() < 0 || pageable.getPageSize() <= 0) {
            throw new IllegalArgumentException("Invalid page parameters");
        }
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(pageable.getPageNumber());
        pageQuery.setPageSize(pageable.getPageSize());
        Page<TaskJobEntity> page = pageQuery.buildPage();
        QueryWrapper<TaskJobEntity> wrapper = Wrappers.query();
        // 使用安全的方法来防止SQL注入
        if (request.getName() != null) {
            wrapper.like("t1.name", request.getName());
        }
        if (request.getGroupName() != null) {
            wrapper.like("t2.name", request.getGroupName());
        }
        wrapper.orderByDesc("t1.update_time");
        Page<JobDTO> sysUserPage = null;
        try {
            sysUserPage = jobMapper.selectAllocatedList(page, wrapper);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sysUserPage;
    }


    @Override
    public JobDTO findById(String id) {
        // 检查id是否为空
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        TaskJobEntity taskJobEntity = this.jobMapper.selectOne(TaskJobEntity::getId, id);
        // 使用Optional来优雅地处理null值
        Optional<JobDTO> dtoOptional = Optional.ofNullable(taskJobEntity)
                .map(JobDTO::of)
                .filter(dto -> dto.getGroupId() != null) // 确保groupId存在
                .map(dto -> {
                    // 进行group信息的查询和设置，以Optional方式处理可能的null值
                    TaskGroupEntity taskGroupEntity = this.groupMapper.selectOne(TaskGroupEntity::getId, dto.getGroupId());
                    if (taskGroupEntity != null) {
                        dto.setGroupName(taskGroupEntity.getName());
                    }
                    return dto;
                });
        // 确保最终返回值不为null
        return dtoOptional.orElseThrow(() -> new ServiceException("Job with ID: " + id + " not found"));
    }


    @Override
    @Transactional
    public void create(JobCreateRequest request) {
        this.validateGroupId(request.getGroupId());
        this.validateJobName(request.getName());
        TaskJobEntity taskJobEntity = new TaskJobEntity();
        BeanUtils.copyProperties(request, taskJobEntity);
        taskJobEntity.setCreateTime(new Date());
        taskJobEntity.setUpdateTime(new Date());
        this.jobMapper.insertJob(taskJobEntity);
        String jobName = "job" + taskJobEntity.getId();
        JobDetail jobDetail = JobBuilder.newJob(BaseJob.class).withIdentity(jobName, "DEFAULT").storeDurably(true).build();
        try {
            this.scheduler.addJob(jobDetail, true);
        } catch (SchedulerException e) {
            log.error("Failed to create job with request: {}", request, e);
            throw ErrorCode.SCHEDULED_ERROR.toException();
        }
    }

    private void validateJobName(String name) {
        TaskJobEntity taskJobEntity = jobMapper.selectOne(TaskJobEntity::getName, name);
        if (null != taskJobEntity) {
            throw new IllegalArgumentException("作业名称已存在");
        }
    }

    private void validateGroupId(String groupId) {
        TaskGroupEntity taskGroupEntity = this.groupMapper.selectOne(TaskGroupEntity::getId, groupId);
        if (null == taskGroupEntity) {
            throw new IllegalArgumentException("组id:" + groupId + " 不存在");
        }
    }

    @Override
    @Transactional
    public void update(String id, JobUpdateRequest request) {
        this.validateGroupId(request.getGroupId());
        TaskJobEntity taskJobEntity = this.jobMapper.selectOne(TaskJobEntity::getId, id);
        if (taskJobEntity == null) {
            throw new IllegalArgumentException("任务不存在");
        }
        TaskJobEntity jobOfName = this.jobMapper.selectOne(TaskJobEntity::getName, request.getName());
        if (null != jobOfName && !jobOfName.getId().equals(taskJobEntity.getId())) {
            throw new IllegalArgumentException("作业名称已存在");
        }
        // 使用一个方法来更新taskJobEntity的属性，减少代码重复
        updateTaskJobEntityProperties(taskJobEntity, request);
        this.jobMapper.updateById(taskJobEntity);
    }

    private void updateTaskJobEntityProperties(TaskJobEntity taskJobEntity, JobUpdateRequest request) {
        taskJobEntity.setGroupId(request.getGroupId());
        taskJobEntity.setName(request.getName());
        taskJobEntity.setType(request.getType());
        taskJobEntity.setUpdateTime(new Date());
        taskJobEntity.setHttpBody(request.getHttpBody());
        taskJobEntity.setHttpMethod(request.getHttpMethod());
        taskJobEntity.setHttpContentType(request.getHttpContentType());
        taskJobEntity.setHttpHeader(request.getHttpHeader());
        taskJobEntity.setHttpUrl(request.getHttpUrl());
        taskJobEntity.setScriptCommand(request.getScriptCommand());
        taskJobEntity.setDatasource(request.getDatasource());
    }


    @Validated
    @Transactional(rollbackFor = {IllegalArgumentException.class, SchedulerException.class})
    public void delete(String id) {
        // 校验id格式和有效性
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("作业ID不能为空");
        }
        // 通过乐观锁或类似机制确保数据一致性，这里假设已有相关实现
        Long scheduleCount = this.scheduleRepository.selectCount(TaskScheduleEntity::getId, id);
        if (scheduleCount > 0L) {
            throw new IllegalArgumentException("存在调度的任务，无法删除作业");
        } else {
            TaskJobEntity taskJobEntity = this.jobMapper.selectOne(TaskJobEntity::getId, id);
            if (taskJobEntity == null) {
                throw new IllegalArgumentException("找不到对应的作业");
            }
            try {
                this.scheduler.deleteJob(new JobKey(taskJobEntity.getName() + taskJobEntity.getId(), "DEFAULT"));
            } catch (SchedulerException schedulerException) {
                // 在日志系统中记录详细的异常信息（假设存在log对象）
                log.error("删除调度任务失败，任务ID: " + id, schedulerException);
                // 包装异常时，保留原始异常信息并提供更具体的上下文
                throw new IllegalArgumentException("删除调度任务失败，任务ID: " + id, schedulerException);
            }

            this.jobMapper.delete(new QueryWrapper<TaskJobEntity>().eq("id", id));
        }
    }


    @Override
    public JobExecution execute(String id) {
        TaskJobEntity taskJobEntity = this.jobMapper.selectOne(TaskJobEntity::getId, id);
        if (taskJobEntity == null) {
            throw new IllegalArgumentException("找不到对应的作业");
        }
        try {
            return jobExecutorService.exec(taskJobEntity);
        } catch (Exception e) {
            // 优化异常处理，保留原始异常信息
            throw new RuntimeException("执行任务失败", e);
        }
    }

    @Override
    public void executeAsync(String id) {
        log.info(">>>>>>>>>>异步执行作业,{}", id);
        TaskJobEntity taskJobEntity = this.jobMapper.selectOne(TaskJobEntity::getId, id);
        if (taskJobEntity == null) {
            throw new IllegalArgumentException("找不到对应的作业");
        }
        Observable.just(taskJobEntity)
                .subscribeOn(Schedulers.io())
                .subscribe(jobEntity-> executeTaskAsync(jobEntity),
                        error -> handleAsyncError(id, error) // 错误处理
        );
    }

    private void executeTaskAsync(TaskJobEntity taskJobEntity) {
        // 使用线程安全的方式执行任务
        ThreadPoolUtils.submit(() -> {
            try {
                jobExecutorService.exec(taskJobEntity);
                log.info("任务{}执行成功", taskJobEntity.getId());
            } catch (Exception e) {
                log.error("任务{}执行失败", taskJobEntity.getId(), e);
            }
            return null;
        });
    }

    private void handleAsyncError(String id, Throwable error) {
        // 对异步执行中的错误进行处理，如记录日志等
        log.error("异步执行作业失败, {}, 原因: {}", id, error.getMessage());
        // 可以根据需求进一步优化错误处理逻辑，比如重试执行等
    }

    @Override
    public void importJob(MultipartFile file, String updateSupport) {
        String string = "";
        try (InputStream inputStream = file.getInputStream()) {
            string = StreamUtil.toString(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (StringUtils.isNotBlank(string)) {
            JSONArray jsonArray = JSONArray.parseArray(string);
            TaskJobEntity taskJobEntity = new TaskJobEntity();
            taskJobEntity.setType("1");
            taskJobEntity.setCreateTime(new Date());
            taskJobEntity.setUpdateTime(new Date());
            taskJobEntity.setName(UUID.randomUUID().toString());
            taskJobEntity.setType("1");
            List<TaskGroupEntity> groups = groupMapper.selectList();
            if (CollectionUtils.isNotEmpty(groups)) {
                taskJobEntity.setGroupId(groups.get(0).getId());
            } else {
                taskJobEntity.setGroupId("1");
            }
            for (Object object : jsonArray) {
                JSONObject jsonObject = JSONObject.from(object);
                String method = jsonObject.getString("method");
                taskJobEntity.setHttpMethod(method);
                taskJobEntity.setHttpUrl(jsonObject.getString("scheme") + "://" + jsonObject.getString("host") + jsonObject.getString("path"));
                JSONObject request = jsonObject.getJSONObject("request");
                JSONObject header = request.getJSONObject("header");
                JSONArray headers = header.getJSONArray("headers");
                List<String> collect = headers.stream().filter(s -> !JSONObject.from(s).getString("name").startsWith(":")).map(s -> JSONObject.from(s).getString("name") + ":" + JSONObject.from(s).getString("value")).collect(Collectors.toList());
                taskJobEntity.setHttpHeader(String.join("\n", collect));
                String mimeType = request.getString("mimeType");
                taskJobEntity.setHttpContentType(mimeType);
                if (!"GET".equals(method)) {
                    JSONObject string1 = request.getJSONObject("body");
                    if (null != string1) {
                        taskJobEntity.setHttpBody(string1.getString("text"));
                    }
                }
            }
            jobMapper.insertJob(taskJobEntity);
            Integer id = taskJobEntity.getId();
            if (0 >= id) {
                throw new RuntimeException("保存作业失败");
            }
            JobDetail jobDetail = JobBuilder.newJob(BaseJob.class).withIdentity("job" + taskJobEntity.getId(), "DEFAULT").storeDurably(true).build();
            try {
                this.scheduler.addJob(jobDetail, true);
            } catch (SchedulerException var5) {
                log.error("", var5);
                throw ErrorCode.SCHEDULED_ERROR.toException();
            }
        }
    }
    @Override
    public List<ResultDto> execHttp(TaskJobEntity taskJobEntity) {
        log.info(">>>>>>>>>>请求路径：{}-{}", taskJobEntity.getHttpMethod(), taskJobEntity.getHttpUrl());
        String body = null;
        try {
            JobExecution jobExecution = jobExecutorService.exec(taskJobEntity);
            if (jobExecution.getStatus() == 1) {
                return null;
            }
            body = jobExecution.getResult();
        } catch (Exception var5) {
            throw new RuntimeException(var5);
        }
        switch (taskJobEntity.getType()) {
            case "1":
                return httpParse(body);
            case "2":
                return shellParse(body);
            case "3":
                return sqlParse(body);
            default:
                return null;
        }

    }

    private List<ResultDto> sqlParse(String body) {
        return null;
    }

    private List<ResultDto> shellParse(String body) {
        return null;
    }

    private List<ResultDto> httpParse(String body) {
        JSONObject jsonObject = JSONObject.parseObject(body);
        String sb = "";
        List<String> stringList = new ArrayList<>();
        Set<String> strings = jsonObject.keySet();
        for (String s : strings) {
            Object o = jsonObject.get(s);
            bodyToJson(o, s, sb, stringList);
        }
        List<String> collect = stringList.stream()
                .map(s -> s.replace("^^^", "^").replace("^^", "^")).collect(Collectors.toList());
        List<ResultDto> treeList = new ArrayList<>();
        for (String string : collect) {
            String[] split = string.split("\\^");
            List<ResultDto> resultDto = treeList;
            for (int i = 0; i < split.length; i++) {
                String s = split[i];
                ResultDto resultDto1 = new ResultDto();
                resultDto1.setLabel(s);
                for (int j = 0; j < i + 1; j++) {
                    String path = resultDto1.getPath();
                    if (StringUtils.isNotBlank(path)) {
                        path = path + "^";
                    }
                    resultDto1.setPath(path + split[j]);
                }
                if (resultDto.contains(resultDto1)) {
                    resultDto = resultDto.get(resultDto.indexOf(resultDto1)).getChildren();
                } else {
                    resultDto.add(resultDto1);
                    resultDto = resultDto.get(resultDto.indexOf(resultDto1)).getChildren();
                }
            }
        }
        System.out.println(collect);
        System.out.println(treeList);
        return treeList;
    }


    public String bodyToJson(Object o, String s, String sb, List<String> stringList) {
        if (StringUtils.isNotBlank(sb)) {
            sb = sb + "^";
        }
        if (o instanceof String) {
            if (!StringUtils.isEmpty(s)) {
                s = s + ":" + (String) o;
                sb = sb + s;
            }
            if (sb.endsWith(".")) {
                sb = sb.substring(0, sb.length() - 1);
            }
            stringList.add(sb);
            return sb;
        } else if (o instanceof JSONObject) {
            sb = sb + s;
            JSONObject from = JSONObject.from(o);
            Set<String> strings = from.keySet();
            for (String s1 : strings) {
                Object o1 = from.get(s1);
                bodyToJson(o1, s1, sb, stringList);
            }
        } else if (o instanceof JSONArray) {
            JSONArray jsonObject1 = (JSONArray) o;
            for (int i = 0; i < jsonObject1.size(); i++) {
                String s2 = sb + s + "[" + i + "]";
                Object jsonObject2 = jsonObject1.get(i);
                bodyToJson(jsonObject2, "", s2, stringList);
            }
        }
        return sb;
    }

    private String bodyReplace(String body) {
        return body.replace("${sys_date.yyyyMM}", DateUtils.format(new Date(), "yyyyMM")).replace("${sys_date.yyyyMMdd}", DateUtils.formatToday()).replace("${sys_date.yyyy-MM-dd}", DateUtils.formatDate(new Date())).replace("${sys_date.yyyy-MM-dd HH:mm:ss}", DateUtils.formatDateTime(new Date())).replace("${sys_date.yyyyMMddHHmmss}", DateUtils.format(new Date(), "yyyyMMddHHmmss"));
    }
}
