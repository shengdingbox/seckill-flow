package com.seckillflow.controller.monitor;

import com.seckillflow.domain.entity.SysLoginLogEntity;
import com.zhouzifei.common.config.Response;
import com.seckillflow.domain.PageResult;
import com.zhouzifei.common.annotation.ApiResource;
import com.zhouzifei.common.annotation.Log;
import com.zhouzifei.common.enums.ResBizTypeEnum;
import com.seckillflow.service.SysLoginLogService;
import com.seckillflow.service.SysPasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 * 系统访问记录
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/monitor/logininfor")
@Log(openLog = false)
@ApiResource(name = "登录日志管理", resBizType = ResBizTypeEnum.SYSTEM)
public class SysLogininforController {
    @Autowired
    private SysLoginLogService logininforService;

    @Autowired
    private SysPasswordService passwordService;

    @PreAuthorize("@ss.hasPermi('monitor:logininfor:list')")
    @GetMapping(value = "/list",name = "登录日志-分类列表")
    public Response list(SysLoginLogEntity logininfor) {
        PageResult<SysLoginLogEntity> page = logininforService.selectLogininforPage(logininfor);
        return Response.ok(page);
    }

    @PreAuthorize("@ss.hasPermi('monitor:logininfor:remove')")
    @DeleteMapping(value = "/{infoIds}",name = "登录日志-删除")
    public Response remove(@PathVariable Long[] infoIds) {
        return Response.ok(logininforService.deleteLogininforByIds(infoIds));
    }

    @PreAuthorize("@ss.hasPermi('monitor:logininfor:remove')")
    @DeleteMapping(value = "/clean",name = "登录日志-清空")
    public Response clean() {
        logininforService.cleanLogininfor();
        return Response.ok();
    }

    @PreAuthorize("@ss.hasPermi('monitor:logininfor:unlock')")
    @GetMapping(value = "/unlock/{userName}",name = "登录日志-解锁")
    public Response unlock(@PathVariable("userName") String userName) {
        passwordService.clearLoginRecordCache(userName);
        return Response.ok();
    }
}
