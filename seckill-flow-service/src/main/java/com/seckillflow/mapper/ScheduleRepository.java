package com.seckillflow.mapper;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seckillflow.core.BaseMapperX;
import com.seckillflow.domain.dto.ScheduleDTO;
import com.seckillflow.domain.entity.TaskScheduleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface ScheduleRepository extends BaseMapperX<TaskScheduleEntity> {


    /**
     * 根据条件分页查询调度列表
     *
     * @return 调度集合分页信息
     */
    Page<ScheduleDTO> selectAllocatedList(@Param("page") Page<ScheduleDTO> page, @Param(Constants.WRAPPER) Wrapper<TaskScheduleEntity> queryWrapper);
}
