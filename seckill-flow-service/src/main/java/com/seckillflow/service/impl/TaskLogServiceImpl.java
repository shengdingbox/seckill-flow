package com.seckillflow.service.impl;

import com.seckillflow.controller.scheduler.request.LogQueryRequest;
import com.seckillflow.domain.PageResult;
import com.seckillflow.domain.model.LogModel;
import com.seckillflow.mapper.LogMapper;
import com.seckillflow.service.TaskLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 任务日志服务实现类
 * 提供任务日志的查询功能
 */
@Service
public class TaskLogServiceImpl implements TaskLogService {
    @Autowired
    private LogMapper logMapper;

    /**
     * 查询所有任务日志
     *
     * @param pageable 分页信息
     * @param request  查询条件
     * @return 分页查询结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public PageResult<LogModel> findAll(Pageable pageable, LogQueryRequest request) {
        // 校验分页参数的合法性
        // 确保分页参数合理
        if (pageable.getPageNumber() < 0 || pageable.getPageSize() <= 0) {
            throw new IllegalArgumentException("Invalid page parameters.");
        }
        // 调用LogMapper查询满足条件的日志信息
        // 这里假设LogMapper.selectPage已经具备了防注入等安全措施
        return this.logMapper.selectPage(request);
    }

    /**
     * 根据ID查询任务日志
     *
     * @param id 日志ID
     * @return 日志详情
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public LogModel findById(Long id) {
        // 校验ID的合法性
        // 确保id不为空或非法值
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid ID.");
        }
        // 根据ID查询日志信息
        LogModel logModel = logMapper.selectOne(LogModel::getId, id);
        // 如果查询结果为空，则抛出异常，表示未找到对应的日志
        if (logModel == null) {
            // 根据业务需求，这里可以抛出自定义异常或者返回null
            throw new RuntimeException("Log not found with ID: " + id);
        }
        return logModel;
    }
}
