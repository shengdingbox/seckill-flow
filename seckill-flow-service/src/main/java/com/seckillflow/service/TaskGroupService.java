package com.seckillflow.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seckillflow.domain.entity.TaskGroupEntity;
import com.seckillflow.controller.scheduler.request.GroupSaveRequest;
import org.springframework.data.domain.Pageable;

public interface TaskGroupService {

    public abstract Page<TaskGroupEntity> findAll(Pageable pageable, String name);

    public abstract void save(GroupSaveRequest groupsaverequest);

    public abstract void update(String name, GroupSaveRequest groupsaverequest);

    public abstract void delete(String name);
}
