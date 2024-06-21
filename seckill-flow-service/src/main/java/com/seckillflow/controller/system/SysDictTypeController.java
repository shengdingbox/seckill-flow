package com.seckillflow.controller.system;

import com.seckillflow.domain.entity.SysDictTypeEntity;
import com.zhouzifei.common.annotation.ApiResource;
import com.zhouzifei.common.config.Response;
import com.zhouzifei.common.enums.ResBizTypeEnum;
import com.seckillflow.domain.PageResult;
import com.seckillflow.service.SysDictTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/system/dict/type")
@ApiResource(name = "字典类型管理", resBizType = ResBizTypeEnum.SYSTEM)
public class SysDictTypeController {

    @Autowired
    private SysDictTypeService dictTypeService;

    @PreAuthorize("@ss.hasPermi('system:dict:list')")
    @GetMapping(value = "/list", name = "字典类型管理-分页")
    public Response list(SysDictTypeEntity sysDictTypeEntity) {
        PageResult<SysDictTypeEntity> page = dictTypeService.page(sysDictTypeEntity);
        return Response.ok(page);
    }

    /**
     * 查询字典类型详细
     */
    @PreAuthorize("@ss.hasPermi('system:dict:query')")
    @GetMapping(value = "/{dictId}", name = "字典类型管理-查询")
    public Response getInfo(@PathVariable Long dictId) {
        return Response.ok(dictTypeService.selectDictTypeById(dictId));
    }

    /**
     * 新增字典类型
     */
    @PreAuthorize("@ss.hasPermi('system:dict:add')")
    @PostMapping(name = "字典类型管理-新增")
    public Response add(@Validated @RequestBody SysDictTypeEntity dict) {
        if (!dictTypeService.checkDictTypeUnique(dict)) {
            return Response.failed("新增字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }

        return Response.ok(dictTypeService.insertDictType(dict));
    }

    /**
     * 修改字典类型
     */
    @PreAuthorize("@ss.hasPermi('system:dict:edit')")
    @PutMapping(name = "字典类型管理-修改")
    public Response edit(@Validated @RequestBody SysDictTypeEntity dict) {
        if (!dictTypeService.checkDictTypeUnique(dict)) {
            return Response.failed("修改字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }
        return Response.ok(dictTypeService.updateDictType(dict));
    }

    /**
     * 删除字典类型
     */
    @PreAuthorize("@ss.hasPermi('system:dict:remove')")
    @DeleteMapping(value = "/{dictIds}", name = "字典类型管理-删除")
    public Response remove(@PathVariable Long[] dictIds) {
        dictTypeService.deleteDictTypeByIds(dictIds);
        return Response.ok();
    }

    /**
     * 刷新字典缓存
     */
    @PreAuthorize("@ss.hasPermi('system:dict:remove')")
    @DeleteMapping(value = "/refreshCache", name = "字典类型管理-刷新")
    public Response refreshCache() {
        dictTypeService.resetDictCache();
        return Response.ok();
    }

    /**
     * 获取字典选择框列表
     */
    @GetMapping(value = "/optionselect", name = "字典类型管理-获取字典选择框列表")
    public Response optionselect() {
        List<SysDictTypeEntity> dictTypes = dictTypeService.selectDictTypeAll();
        return Response.ok(dictTypes);
    }

}
