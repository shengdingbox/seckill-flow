package com.seckillflow.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seckillflow.domain.entity.TaskScheduleEntity;
import com.seckillflow.controller.scheduler.request.ScheduleCreateRequest;
import com.seckillflow.controller.scheduler.request.ScheduleUpdateRequest;
import com.seckillflow.domain.dto.ScheduleDTO;
import org.springframework.data.domain.Pageable;

public interface TaskSchedulerService {

    public abstract Page<ScheduleDTO> findAll(Pageable pageable, String s);

    public abstract TaskScheduleEntity findById(String s);

    public abstract TaskScheduleEntity create(ScheduleCreateRequest schedulecreaterequest);

    public abstract TaskScheduleEntity update(String s, ScheduleUpdateRequest scheduleupdaterequest);

    public abstract void delete(String s);

    public abstract void enable(String s);

    public abstract void disable(String s);
}
