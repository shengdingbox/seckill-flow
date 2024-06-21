package com.seckillflow.controller.system;

import com.seckillflow.domain.entity.SysConfigEntity;
import com.zhouzifei.common.annotation.ApiResource;
import com.zhouzifei.common.config.Response;
import com.zhouzifei.common.enums.ResBizTypeEnum;
import com.seckillflow.domain.PageResult;
import com.seckillflow.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 配置管理
 */
@RestController
@RequestMapping("/system/config")
@ApiResource(name = "参数配置管理", resBizType = ResBizTypeEnum.SYSTEM)
public class SysConfigController {
    @Autowired
    private SysConfigService configService;

    @GetMapping(value = "page", name = "参数配置管理-分页")
    @PreAuthorize("@ss.hasPermi('system:config:list')")
    public Response page(SysConfigEntity sysConfigEntity) {
        PageResult<SysConfigEntity> page = configService.page(sysConfigEntity);
        return Response.ok(page);
    }

    @GetMapping(value = "{id}", name = "参数配置管理-查询id信息")
    @PreAuthorize("@ss.hasPermi('system:config:query')")
    public Response getInfo(@PathVariable("id") Long id) {
        SysConfigEntity entity = configService.selectConfigById(id);
        return Response.ok(entity);
    }

    /**
     * 根据参数键名查询参数值
     */
    @GetMapping(value = "/configKey/{configKey:.+}")
    public Response getConfigKey(@PathVariable String configKey) {
        return Response.ok(configService.selectConfigByKey(configKey));
    }

    @PostMapping(name = "参数配置管理-新增")
    @PreAuthorize("@ss.hasPermi('system:config:add')")
    public Response add(@Validated @RequestBody SysConfigEntity config) {
        if (!configService.checkConfigKeyUnique(config)) {
            return Response.failed("新增参数'" + config.getConfigName() + "'失败，参数键名已存在");
        }
        return Response.ok(configService.insertConfig(config));

    }

    @PutMapping(name = "参数配置管理-修改")
    @PreAuthorize("@ss.hasPermi('system:config:edit')")
    public Response edit(@Validated @RequestBody SysConfigEntity config) {
        if (!configService.checkConfigKeyUnique(config)) {
            return Response.failed("修改参数'" + config.getConfigName() + "'失败，参数键名已存在");
        }
        return Response.ok(configService.updateConfig(config));

    }

    @DeleteMapping(value = "/{configIds}", name = "参数配置管理-删除")
    @PreAuthorize("@ss.hasPermi('system:config:remove')")
    public Response remove(@PathVariable Long[] configIds) {
        configService.deleteConfigByIds(configIds);

        return Response.ok();
    }

    /**
     * 刷新参数缓存
     */
    @PreAuthorize("@ss.hasPermi('system:config:remove')")
    @DeleteMapping(value = "/refreshCache", name = "参数配置管理-刷新缓存")
    public Response refreshCache() {
        configService.resetConfigCache();
        return Response.ok();
    }
}
