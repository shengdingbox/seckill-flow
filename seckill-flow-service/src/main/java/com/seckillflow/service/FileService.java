package com.seckillflow.service;


import com.seckillflow.domain.dto.FileDetailDTO;
import com.seckillflow.domain.model.BatchFileReg;
import com.seckillflow.controller.scheduler.request.BatchFileQueryRequest;
import com.seckillflow.controller.scheduler.request.FileDetailQueryRequest;

import java.util.List;

public interface FileService {


    public List<BatchFileReg> findByDateAndSource(BatchFileQueryRequest request);

    FileDetailDTO detail(FileDetailQueryRequest request);
}
