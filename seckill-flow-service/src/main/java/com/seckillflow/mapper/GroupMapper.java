package com.seckillflow.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seckillflow.core.BaseMapperX;
import com.seckillflow.domain.entity.TaskGroupEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GroupMapper extends BaseMapperX<TaskGroupEntity> {


    /**
     * 根据条件分页查询任务列表
     *
     * @return 任务集合分页信息
     */
    Page<TaskGroupEntity> selectAllocatedList(@Param("page") Page<TaskGroupEntity> page, @Param(Constants.WRAPPER) Wrapper<TaskGroupEntity> queryWrapper);
}
