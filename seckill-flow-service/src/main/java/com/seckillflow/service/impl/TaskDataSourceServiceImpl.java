package com.seckillflow.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhouzifei.common.exception.ServiceException;
import com.seckillflow.domain.PageResult;
import com.seckillflow.domain.entity.TaskDataSourceEntity;
import com.seckillflow.mapper.TaskDataSourceMapper;
import com.seckillflow.service.TaskDataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 数据源Service业务层处理
 *
 * @author ruoyi
 * @date 2024-06-07
 */
@Service
public class TaskDataSourceServiceImpl implements TaskDataSourceService {
    @Autowired
    private TaskDataSourceMapper taskDataSourceMapper;

    /**
     * 查询数据源
     *
     * @param id 数据源主键
     * @return 数据源
     */
    @Override
    public TaskDataSourceEntity selectSpDatasourceById(String id) {
        return taskDataSourceMapper.selectById(id);
    }

    /**
     * 查询数据源列表
     *
     * @param taskDataSourceEntity 数据源实体
     * @return 数据源列表
     */
    @Override
    public PageResult<TaskDataSourceEntity> selectSpDatasourceList(TaskDataSourceEntity taskDataSourceEntity) {
        QueryWrapper<TaskDataSourceEntity> queryWrapper = new QueryWrapper<>();
        // 防止SQL注入，使用占位符方式
        if (ObjectUtil.isNotNull(taskDataSourceEntity.getJdbcUrl())) {
            queryWrapper.like("jdbc_url", taskDataSourceEntity.getJdbcUrl());
        }
        if (ObjectUtil.isNotNull(taskDataSourceEntity.getUsername())) {
            queryWrapper.like("name", taskDataSourceEntity.getUsername());
        }
        return taskDataSourceMapper.selectPage(queryWrapper);
    }

    /**
     * 新增数据源
     *
     * @param taskDataSourceEntity 数据源实体
     * @return 结果
     */
    @Override
    public int insertSpDatasource(TaskDataSourceEntity taskDataSourceEntity) {
        taskDataSourceEntity.setCreateDate(new Date());
        return taskDataSourceMapper.insert(taskDataSourceEntity);
    }

    /**
     * 修改数据源
     *
     * @param taskDataSourceEntity 数据源实体
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateSpDatasource(TaskDataSourceEntity taskDataSourceEntity) {
        String id = taskDataSourceEntity.getId();
        if (ObjectUtil.isNull(taskDataSourceMapper.selectById(id))) {
            throw new ServiceException("数据源不存在");
        }
        return taskDataSourceMapper.updateById(taskDataSourceEntity);
    }

    /**
     * 删除数据源信息
     *
     * @param id 数据源主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteSpDatasourceById(String id) {
        if (ObjectUtil.isNull(taskDataSourceMapper.selectById(id))) {
            throw new ServiceException("数据源不存在");
        }
        return taskDataSourceMapper.deleteById(id);
    }
}
