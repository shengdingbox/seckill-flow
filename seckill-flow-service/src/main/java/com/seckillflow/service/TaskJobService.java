package com.seckillflow.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seckillflow.core.executor.JobExecution;
import com.seckillflow.domain.dto.JobDTO;
import com.seckillflow.domain.dto.ResultDto;
import com.seckillflow.domain.entity.TaskJobEntity;
import com.seckillflow.controller.scheduler.request.JobCreateRequest;
import com.seckillflow.controller.scheduler.request.JobQueryRequest;
import com.seckillflow.controller.scheduler.request.JobUpdateRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TaskJobService {

    public abstract Page<JobDTO> findAll(Pageable pageable, JobQueryRequest jobqueryrequest);

    public abstract JobDTO findById(String s);

    public abstract  void create(JobCreateRequest jobcreaterequest);

    public abstract void update(String s, JobUpdateRequest jobupdaterequest);

    public abstract void delete(String s);

    public abstract JobExecution execute(String s);

    public abstract void executeAsync(String s);

    public void importJob(MultipartFile file, String updateSupport);

    public List<ResultDto> execHttp(TaskJobEntity taskJobEntity);
}
