package com.seckillflow.controller.monitor;

import com.seckillflow.domain.entity.SysOperLogEntity;
import com.zhouzifei.common.config.Response;
import com.seckillflow.domain.PageResult;
import com.zhouzifei.common.annotation.ApiResource;
import com.zhouzifei.common.annotation.Log;
import com.zhouzifei.common.enums.ResBizTypeEnum;
import com.seckillflow.service.SysOperLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志记录
 */
@RestController
@RequestMapping("/monitor/operlog")
@Log(openLog = false)
@ApiResource(name = "操作日志管理", resBizType = ResBizTypeEnum.SYSTEM)
public class SysOperlogController {

    @Autowired
    private SysOperLogService operLogService;

    @PreAuthorize("@ss.hasPermi('monitor:operlog:list')")
    @GetMapping(value = "/list", name = "操作日志-分页")
    public Response list(SysOperLogEntity operLog) {
        PageResult<SysOperLogEntity> page = operLogService.selectOperLogPage(operLog);
        return Response.ok(page);
    }


    @PreAuthorize("@ss.hasPermi('monitor:operlog:remove')")
    @DeleteMapping(value = "/{operIds}", name = "操作日志-删除")
    public Response remove(@PathVariable Long[] operIds) {
        return Response.ok(operLogService.deleteOperLogByIds(operIds));
    }

    @PreAuthorize("@ss.hasPermi('monitor:operlog:remove')")
    @DeleteMapping(value = "/clean", name = "操作日志-清空")
    public Response clean() {
        operLogService.cleanOperLog();
        return Response.ok();
    }
}
