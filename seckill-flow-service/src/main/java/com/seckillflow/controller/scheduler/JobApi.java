package com.seckillflow.controller.scheduler;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhouzifei.common.config.Response;
import com.seckillflow.controller.scheduler.request.JobCreateRequest;
import com.seckillflow.controller.scheduler.request.JobQueryRequest;
import com.seckillflow.controller.scheduler.request.JobUpdateRequest;
import com.seckillflow.core.executor.JobExecution;
import com.seckillflow.domain.dto.JobDTO;
import com.seckillflow.domain.dto.ResultDto;
import com.seckillflow.domain.entity.TaskJobEntity;
import com.seckillflow.service.TaskJobService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping({"/v1/jobs"})
public class JobApi {
    @Autowired
    private TaskJobService taskJobService;


    @ApiOperation(value = "查询", notes = "查询")
    @ApiImplicitParams({@ApiImplicitParam(name = "page", value = "第几页", required = false, dataType = "String", paramType = "query"), @ApiImplicitParam(name = "size", value = "每页大小", required = false, dataType = "String", paramType = "query"), @ApiImplicitParam(name = "name", value = "作业名称", required = false, dataType = "String", paramType = "query"), @ApiImplicitParam(name = "groupName", value = "组名称", required = false, dataType = "String", paramType = "query")})
    @GetMapping
    public Response findAll(Pageable pageable, JobQueryRequest request) {
        Page<JobDTO> page = this.taskJobService.findAll(pageable, request);
        return Response.ok(page);
    }

    @ApiOperation(value = "查询明细", notes = "查询")
    @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "String", paramType = "path")
    @GetMapping({"/{id}"})
    public Response findById(@PathVariable String id) {
        JobDTO jobDTO = this.taskJobService.findById(id);
        return Response.ok(jobDTO);
    }

    @ApiOperation(value = "创建作业", notes = "创建作业")
    @ApiImplicitParam(name = "request", value = "创建作业参数", required = true, dataType = "JobCreateRequest", paramType = "body")
    @PostMapping
    public Response create(@Valid @RequestBody JobCreateRequest request) {
        taskJobService.create(request);
        return Response.ok();
    }

    @ApiOperation(value = "更新作业", notes = "更新作业")
    @ApiImplicitParams({@ApiImplicitParam(name = "request", value = "更新作业参数", required = true, dataType = "JobUpdateRequest", paramType = "body"), @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "String", paramType = "path")})
    @PutMapping({"/{id}"})
    public Response update(@PathVariable String id, @Valid @RequestBody JobUpdateRequest request) {
        taskJobService.update(id, request);
        return Response.ok();
    }

    @ApiOperation(value = "删除", notes = "删除作业")
    @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "String", paramType = "path")
    @DeleteMapping({"/{id}"})
    public Response delete(@PathVariable String id) {
        taskJobService.delete(id);
        return Response.ok();
    }

    @ApiOperation(value = "执行", notes = "执行作业")
    @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "String", paramType = "path")
    @PostMapping({"/{id}:execute"})
    public Response execute(@PathVariable String id) {
        JobExecution jobExecution = taskJobService.execute(id);
        return Response.ok(jobExecution);
    }

    @ApiOperation(value = "执行", notes = "异步执行作业")
    @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "String", paramType = "path")
    @PostMapping({"/{id}:execute-async"})
    public Response executeAsync(@PathVariable String id) {
        this.taskJobService.executeAsync(id);
        return Response.ok();
    }
    /**
     * 需要接口权限
     */
    @PreAuthorize("@ss.resourceAuth()")
    @PostMapping(value = "/exec", name = "测试1的接口")
    public Response execHttp(@RequestBody TaskJobEntity taskJobEntity) {
        List<ResultDto> jobExecution =  taskJobService.execHttp(taskJobEntity);
        return Response.ok(jobExecution);
    }
    /**
     * 需要接口权限
     */
    @PreAuthorize("@ss.resourceAuth()")
    @PostMapping(value = "/importJob", name = "测试1的接口")
    public Response importJob(@RequestBody MultipartFile file, @RequestParam(value = "updateSupport", required = false) String updateSupport ) {
        taskJobService.importJob(file,updateSupport);
        return Response.ok();
    }
}
