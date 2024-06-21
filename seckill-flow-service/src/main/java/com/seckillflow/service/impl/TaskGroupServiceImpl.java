package com.seckillflow.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seckillflow.controller.scheduler.request.GroupSaveRequest;
import com.seckillflow.domain.PageQuery;
import com.seckillflow.domain.entity.TaskGroupEntity;
import com.seckillflow.domain.entity.TaskJobEntity;
import com.seckillflow.mapper.GroupMapper;
import com.seckillflow.mapper.JobMapper;
import com.seckillflow.service.TaskGroupService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service
public class TaskGroupServiceImpl implements TaskGroupService {
    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private JobMapper jobMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Page<TaskGroupEntity> findAll(Pageable pageable, String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("名称不能为空");
        }
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(pageable.getPageNumber());
        pageQuery.setPageSize(pageable.getPageSize());
        Page<TaskGroupEntity> page = pageQuery.buildPage();
        QueryWrapper<TaskGroupEntity> wrapper = Wrappers.query();
        wrapper.like(ObjectUtil.isNotNull(name), "name", name);
        return this.groupMapper.selectAllocatedList(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(GroupSaveRequest request) {
        if (request.getName() == null) {
            throw new IllegalArgumentException("组名不能为空");
        }
        TaskGroupEntity taskGroupEntity = new TaskGroupEntity();
        BeanUtils.copyProperties(request, taskGroupEntity);
        taskGroupEntity.setCreateTime(new Date());
        taskGroupEntity.setUpdateTime(new Date());
        if (checkGroupNameExists(request.getName(), null)) {
            throw new IllegalArgumentException("组名已存在");
        }
        this.groupMapper.insert(taskGroupEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(String id, GroupSaveRequest request) {
        if (!StringUtils.hasText(id) || !StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("ID或组名不能为空");
        }
        TaskGroupEntity taskGroupEntity = groupMapper.selectOne(TaskGroupEntity::getId, id);
        if (taskGroupEntity == null) {
            throw new IllegalArgumentException("任务组不存在");
        }
        if (checkGroupNameExists(request.getName(), taskGroupEntity.getId())) {
            throw new IllegalArgumentException("组名已存在");
        }
        taskGroupEntity.setName(request.getName());
        taskGroupEntity.setDescription(request.getDescription());
        taskGroupEntity.setUpdateTime(new Date());
        this.groupMapper.updateById(taskGroupEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        if (!StringUtils.hasText(id)) {
            throw new IllegalArgumentException("ID不能为空");
        }
        Long groupId = Long.parseLong(id);
        Long count = jobMapper.selectCount(new QueryWrapper<TaskJobEntity>().eq("group_id", groupId));
        if (count != null && count > 0L) {
            throw new IllegalArgumentException("该组存在作业，无法删除");
        }
        boolean deleted = groupMapper.deleteById(id) > 0;
        if (!deleted) {
            throw new IllegalStateException("删除任务组失败，可能是因为任务组不存在");
        }
    }

    private boolean checkGroupNameExists(String groupName, String excludeId) {
        QueryWrapper<TaskGroupEntity> wrapper = Wrappers.query();
        wrapper.eq("name", groupName);
        if (excludeId != null) {
            wrapper.ne("id", excludeId);
        }
        return groupMapper.exists(wrapper);
    }
}