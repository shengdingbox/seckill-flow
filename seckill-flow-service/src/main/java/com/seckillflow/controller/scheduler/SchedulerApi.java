package com.seckillflow.controller.scheduler;


import com.zhouzifei.common.config.Response;
import com.seckillflow.controller.scheduler.request.ScheduleCreateRequest;
import com.seckillflow.controller.scheduler.request.ScheduleUpdateRequest;
import com.seckillflow.service.TaskSchedulerService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping({"/v1/schedulers"})
public class SchedulerApi {
    @Autowired
    private TaskSchedulerService taskSchedulerService;

    public SchedulerApi() {
    }

    @GetMapping
    @ApiOperation(value = "查询", notes = "查询")
    @ApiImplicitParams({@ApiImplicitParam(name = "page", value = "第几页", required = false, dataType = "String", paramType = "query"), @ApiImplicitParam(name = "size", value = "每页大小", required = false, dataType = "String", paramType = "query"), @ApiImplicitParam(name = "jobName", value = "作业名称", required = false, dataType = "String", paramType = "query")})
    public Response findAll(Pageable pageable, String jobName) {
        return Response.ok(this.taskSchedulerService.findAll(pageable, jobName));
    }

    @ApiOperation(value = "查询明细", notes = "查询明细")
    @GetMapping({"/{id}"})
    @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "String", paramType = "path")
    public Response findById(@PathVariable String id) {
        return Response.ok(this.taskSchedulerService.findById(id));
    }

    @ApiOperation(value = "创建调度", notes = "创建调度")
    @ApiImplicitParam(name = "request", value = "创建调度参数", required = true, dataType = "ScheduleCreateRequest", paramType = "body")
    @PostMapping
    public Response create(@Valid @RequestBody ScheduleCreateRequest request) {
        this.taskSchedulerService.create(request);
        return Response.ok();
    }

    @ApiOperation(value = "更新调度", notes = "更新调度")
    @ApiImplicitParams({@ApiImplicitParam(name = "request", value = "更新调度参数", required = true, dataType = "ScheduleUpdateRequest", paramType = "body"), @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "String", paramType = "path")})
    @PutMapping({"/{id}"})
    public Response update(@PathVariable String id, @Valid @RequestBody ScheduleUpdateRequest request) {
        this.taskSchedulerService.update(id, request);
        return Response.ok();
    }

    @ApiOperation(value = "开启调度", notes = "开启调度")
    @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "String", paramType = "path")
    @PostMapping({"/{id}:enable"})
    public Response enable(@PathVariable String id) {
        this.taskSchedulerService.enable(id);
        return Response.ok();
    }

    @ApiOperation(value = "停止调度", notes = "停止调度")
    @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "String", paramType = "path")
    @PostMapping({"/{id}:disable"})
    public Response disable(@PathVariable String id) {
        this.taskSchedulerService.disable(id);
        return Response.ok();
    }

    @ApiOperation(value = "删除调度", notes = "删除调度")
    @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "String", paramType = "path")
    @DeleteMapping({"/{id}"})
    public Response delete(@PathVariable String id) {
        this.taskSchedulerService.delete(id);
        return Response.ok();
    }
}
