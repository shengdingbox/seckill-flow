package com.seckillflow.mapper;

import com.seckillflow.core.BaseMapperX;
import com.seckillflow.core.LambdaQueryWrapperX;
import com.seckillflow.domain.PageResult;
import com.seckillflow.domain.model.LogModel;
import com.zhouzifei.common.utils.DateUtils;
import com.seckillflow.controller.scheduler.request.LogQueryRequest;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;


@Mapper
public interface LogMapper extends BaseMapperX<LogModel> {

    public default PageResult<LogModel> selectPage(LogQueryRequest request) {
        return selectPage(new LambdaQueryWrapperX<LogModel>()
                .eqIfPresent(LogModel::getId,request.getJobId())
                .likeIfPresent(LogModel::getJobName, request.getJobName())
                .eqIfPresent(LogModel::getScheduleId, request.getScheduleId())
                .eqIfPresent(LogModel::getStatus, request.getStatus())
                .betweenIfPresent(LogModel::getStartTime, request.getStartTimeFrom(),request.getStartTimeTo())

        );
    }
    public default int deleteBeforeDay(int day) {
        Date date = DateUtils.addDays(DateUtils.now(), -day);
        return delete(new LambdaQueryWrapperX<LogModel>().eqIfPresent(LogModel::getEndTime,date));
    }
}
