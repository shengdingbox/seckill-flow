package com.seckillflow.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;

/**
 * 【请填写功能名称】对象 sp_datasource
 *
 * @author ruoyi
 * @date 2024-06-07
 */
@Data
@TableName("task_datasource")
public class TaskDataSourceEntity {
    /**
     * $column.columnComment
     */
    @Id
    private String id;

    /**
     * $column.columnComment
     */
    private String name;

    /**
     * $column.columnComment
     */
    private String driverClassName;

    /**
     * $column.columnComment
     */
    private String jdbcUrl;

    /**
     * $column.columnComment
     */
    private String username;

    /**
     * $column.columnComment
     */
    private String password;

    /**
     * $column.columnComment
     */
    private Date createDate;
}
