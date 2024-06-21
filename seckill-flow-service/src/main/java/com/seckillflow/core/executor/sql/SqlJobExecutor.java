package com.seckillflow.core.executor.sql;

import com.alibaba.fastjson.JSONObject;
import com.seckillflow.core.executor.JobExecution;
import com.seckillflow.core.executor.JobExecutor;
import com.seckillflow.domain.entity.TaskDataSourceEntity;
import com.seckillflow.domain.entity.TaskJobEntity;
import com.seckillflow.mapper.JobMapper;
import com.seckillflow.mapper.TaskDataSourceMapper;
import com.seckillflow.utils.DataSourceUtils;
import com.seckillflow.utils.ExtractUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 实现JobExecutor接口，负责执行特定的SQL任务。
 */
@Slf4j
@Component
public class SqlJobExecutor implements JobExecutor {
    @Autowired
    private TaskDataSourceMapper taskDataSourceMapper;
    @Autowired
    private JobMapper jobMapper;

    /**
     * 执行SQL任务。
     *
     * @param taskJobEntity 任务实体，包含任务相关的配置信息。
     * @return JobExecution 任务执行结果的封装。
     */
    @Override
    public JobExecution execute(TaskJobEntity taskJobEntity) {
        JobExecution jobExecution = new JobExecution();
        // 获取任务配置的数据库源和SQL语句
        String datasource = taskJobEntity.getDatasource();
        String sql = taskJobEntity.getScriptCommand();
        // 检查数据源和SQL是否为空
        if (StringUtils.isBlank(datasource)) {
            log.warn("数据源ID为空！");
        } else if (StringUtils.isBlank(sql)) {
            log.warn("sql为空！");
        } else {
            // 根据数据源ID查询数据源配置
            TaskDataSourceEntity TaskDataSourceEntityEntity = taskDataSourceMapper.selectById(datasource);
            // 创建JdbcTemplate实例，用于执行SQL
            JdbcTemplate template = new JdbcTemplate(DataSourceUtils.createDataSource(TaskDataSourceEntityEntity));
            // 提取SQL中的参数，并替换为占位符
            // 把变量替换成占位符
            List<String> parameters = ExtractUtils.getMatchers(sql, "#(.*?)#", true);
            sql = sql.replaceAll("#(.*?)#", "?");
            // 准备SQL执行的参数
            int size = parameters.size();
            Object[] params = new Object[size];
            log.debug("执行sql：{}", sql);
            // 执行SQL查询，并将结果转换为JSON字符串
            List<Map<String, Object>> maps = template.queryForList(sql, params);
            jobExecution.setResult(JSONObject.toJSONString(maps));
        }
        return jobExecution;
    }

    /**
     * 将参数对象转换为适用于批量执行的参数数组。
     *
     * @param params 原始参数对象。
     * @param length 需要转换的参数数组的长度。
     * @return 转换后的参数数组列表。
     */
    private List<Object[]> convertParameters(Object[] params, int length) {
        List<Object[]> result = new ArrayList<>(length);
        int size = params.length;
        for (int i = 0; i < length; i++) {
            Object[] parameters = new Object[size];
            for (int j = 0; j < size; j++) {
                parameters[j] = getValue(params[j], i);
            }
            result.add(parameters);
        }
        return result;
    }

    /**
     * 根据索引从对象中获取相应的值。
     *
     * @param object 原始对象，可以是单个值、列表或数组。
     * @param index 索引，用于从列表或数组中获取值。
     * @return 根据索引获取的值。
     */
    private Object getValue(Object object, int index) {
        if (object == null) {
            return null;
        } else if (object instanceof List) {
            List<?> list = (List<?>) object;
            int size = list.size();
            if (size > 0) {
                return list.get(Math.min(list.size() - 1, index));
            }
        } else if (object.getClass().isArray()) {
            int size = Array.getLength(object);
            if (size > 0) {
                return Array.get(object, Math.min(size - 1, index));
            }
        } else {
            return object;
        }
        return null;
    }
}
