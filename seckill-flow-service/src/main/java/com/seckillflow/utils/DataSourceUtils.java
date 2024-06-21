package com.seckillflow.utils;

import com.seckillflow.domain.entity.TaskDataSourceEntity;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库连接工具类
 * @author jmxd
 *
 */
@Component
public class DataSourceUtils {
	
	private static final Map<String,DataSource> datasources = new HashMap<>();

	public static DataSource createDataSource(TaskDataSourceEntity taskDataSourceEntity){
		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setJdbcUrl(taskDataSourceEntity.getJdbcUrl());
		hikariConfig.setUsername(taskDataSourceEntity.getUsername());
		hikariConfig.setPassword(taskDataSourceEntity.getPassword());
		hikariConfig.setDriverClassName(taskDataSourceEntity.getDriverClassName());
		DataSource datasource = new HikariDataSource(hikariConfig);
		if(datasource != null){
			datasources.put(taskDataSourceEntity.getId(), datasource);
		}
		return datasource;
	}
	
	public static void remove(String dataSourceId){
		DataSource dataSource = datasources.get(dataSourceId);
		if(dataSource != null){
			DataSource ds = (HikariDataSource) dataSource;
			datasources.remove(dataSourceId);
		}
	}

}
