package com.seckillflow.controller.scheduler;


import com.seckillflow.controller.scheduler.request.LogQueryRequest;
import com.seckillflow.domain.PageResult;
import com.seckillflow.domain.model.LogModel;
import com.seckillflow.service.TaskLogService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.zhouzifei.common.config.Response;

@RestController
@RequestMapping({"/v1/logs"})
public class LogApi {
    @Autowired
    private TaskLogService taskLogService;
    @Autowired
    private RestTemplate restTemplate;

    public LogApi() {
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addCustomFormatter(new DateFormatter("yyyy-MM-dd HH:mm:ss"));
    }

    @ApiOperation(value = "日志查询", notes = "查询")
    @ApiImplicitParams({@ApiImplicitParam(name = "page", value = "第几页", required = false, dataType = "String", paramType = "query"), @ApiImplicitParam(name = "size", value = "每页大小", required = false, dataType = "String", paramType = "query"), @ApiImplicitParam(name = "jobName", value = "作业名称", required = false, dataType = "String", paramType = "query"), @ApiImplicitParam(name = "scheduleId", value = "调度id", required = false, dataType = "String", paramType = "query"), @ApiImplicitParam(name = "jobId", value = "作业id", required = false, dataType = "String", paramType = "query"), @ApiImplicitParam(name = "startTimeFrom", value = "开始时间从", required = false, dataType = "Date", paramType = "query"), @ApiImplicitParam(name = "startTimeTo", value = "开始时间到", required = false, dataType = "Date", paramType = "query")})
    @GetMapping
    public Response findAll(Pageable pageable, LogQueryRequest queryRequest) {
        PageResult<LogModel> logs = this.taskLogService.findAll(pageable, queryRequest);
        return Response.ok(logs);
    }

    @ApiOperation(value = "日志详情", notes = "查询")
    @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "Long", paramType = "path")
    @GetMapping({"/{id}"})
    public Response findById(@PathVariable Long id) {
        LogModel log = this.taskLogService.findById(id);
        return Response.ok(log);
    }
}
