package com.seckillflow.service;


import com.seckillflow.domain.PageResult;
import com.seckillflow.domain.entity.TaskDataSourceEntity;

/**
 * 数据源Service接口
 *
 * @author ruoyi
 * @date 2024-06-07
 */
public interface TaskDataSourceService {
    /**
     * 查询数据源
     *
     * @param id 数据源主键
     * @return 数据源
     */
    public TaskDataSourceEntity selectSpDatasourceById(String id);

    /**
     * 查询数据源列表
     *
     * @param spDatasource 数据源
     * @return 数据源集合
     */
    public PageResult<TaskDataSourceEntity> selectSpDatasourceList(TaskDataSourceEntity spDatasource);

    /**
     * 新增数据源
     *
     * @param spDatasource 数据源
     * @return 结果
     */
    public int insertSpDatasource(TaskDataSourceEntity spDatasource);

    /**
     * 修改数据源
     *
     * @param spDatasource 数据源
     * @return 结果
     */
    public int updateSpDatasource(TaskDataSourceEntity spDatasource);
    /**
     * 删除数据源信息
     *
     * @param id 数据源主键
     * @return 结果
     */
    public int deleteSpDatasourceById(String id);
}
