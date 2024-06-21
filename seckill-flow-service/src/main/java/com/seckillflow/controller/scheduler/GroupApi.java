package com.seckillflow.controller.scheduler;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhouzifei.common.config.Response;
import com.seckillflow.controller.scheduler.request.GroupSaveRequest;
import com.seckillflow.domain.entity.TaskGroupEntity;
import com.seckillflow.service.TaskGroupService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/v1/groups"})
public class GroupApi {
    private static final Logger log = LoggerFactory.getLogger(GroupApi.class);
    @Autowired
    private TaskGroupService taskGroupService;

    public GroupApi() {
    }

    @ApiOperation(value = "查询", notes = "查询")
    @ApiImplicitParams({@ApiImplicitParam(name = "page", value = "第几页", required = false, dataType = "String", paramType = "query"), @ApiImplicitParam(name = "size", value = "每页大小", required = false, dataType = "String", paramType = "query"), @ApiImplicitParam(name = "name", value = "组名称", required = false, dataType = "String", paramType = "query")})
    @GetMapping
    public Response findAll(Pageable pageable, String name) {
        Page<TaskGroupEntity> page = this.taskGroupService.findAll(pageable, name);
        return Response.ok(page);
    }

    @ApiOperation(value = "创建组", notes = "创建组")
    @ApiImplicitParam(name = "request", value = "创建组参数", required = true, dataType = "GroupSaveRequest", paramType = "body")
    @PostMapping
    public Response create(@RequestBody GroupSaveRequest request) {
        this.taskGroupService.save(request);
        return Response.ok();
    }

    @ApiOperation("更新组")
    @ApiImplicitParams({@ApiImplicitParam(name = "request", value = "更新组参数", required = true, dataType = "GroupSaveRequest", paramType = "body"), @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "String", paramType = "path")})
    @PutMapping({"/{id}"})
    public Response update(@PathVariable String id, @RequestBody GroupSaveRequest request) {
        this.taskGroupService.update(id, request);
        return Response.ok();
    }

    @ApiOperation(value = "删除", notes = "删除组")
    @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "String", paramType = "path")
    @DeleteMapping({"/{id}"})
    public Response delete(@PathVariable String id) {
        this.taskGroupService.delete(id);
        return Response.ok();
    }
}
