package com.seckillflow.service;


import com.seckillflow.domain.PageResult;
import com.seckillflow.domain.model.LogModel;
import com.seckillflow.controller.scheduler.request.LogQueryRequest;
import org.springframework.data.domain.Pageable;

public interface TaskLogService {

    public abstract PageResult<LogModel> findAll(Pageable pageable, LogQueryRequest logqueryrequest);

    public abstract LogModel findById(Long long1);
}
