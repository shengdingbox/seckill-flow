package com.seckillflow.controller.tool;


import com.zhouzifei.common.config.Response;
import com.seckillflow.domain.PageResult;
import com.seckillflow.domain.entity.TaskDataSourceEntity;
import com.seckillflow.service.TaskDataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 数据源Controller
 *
 * @author ruoyi
 * @date 2024-06-07
 */
@RestController
@RequestMapping("/tool/datasource")
public class SpDatasourceController {
    @Autowired
    private TaskDataSourceService taskDataSourceService;

    /**
     * 查询数据源列表
     */
    @PreAuthorize("@ss.hasPermi('system:datasource:list')")
    @GetMapping("/list")
    public Response list(TaskDataSourceEntity TaskDataSourceEntity) {
        PageResult<TaskDataSourceEntity> list = taskDataSourceService.selectSpDatasourceList(TaskDataSourceEntity);
        return Response.ok(list);
    }
    
    /**
     * 获取数据源详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:datasource:query')")
    @GetMapping(value = "/{id}")
    public Response getInfo(@PathVariable("id") String id) {
        TaskDataSourceEntity data = taskDataSourceService.selectSpDatasourceById(id);
        return Response.ok(data);
    }

    /**
     * 新增数据源
     */
    @PreAuthorize("@ss.hasPermi('system:datasource:add')")
    @PostMapping
    public Response add(@RequestBody TaskDataSourceEntity TaskDataSourceEntity) {
        return Response.ok(taskDataSourceService.insertSpDatasource(TaskDataSourceEntity));
    }

    /**
     * 修改数据源
     */
    @PreAuthorize("@ss.hasPermi('system:datasource:edit')")
    @PutMapping
    public Response edit(@RequestBody TaskDataSourceEntity TaskDataSourceEntity) {
        return Response.ok(taskDataSourceService.updateSpDatasource(TaskDataSourceEntity));
    }

    /**
     * 删除数据源
     */
    @PreAuthorize("@ss.hasPermi('system:datasource:remove')")
    @DeleteMapping("/{id}")
    public Response remove(@PathVariable String id) {
        return Response.ok(taskDataSourceService.deleteSpDatasourceById(id));
    }
}
