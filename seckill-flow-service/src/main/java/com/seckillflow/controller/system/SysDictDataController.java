package com.seckillflow.controller.system;

import com.seckillflow.domain.entity.SysDictDataEntity;
import com.zhouzifei.common.annotation.ApiResource;
import com.zhouzifei.common.config.Response;
import com.zhouzifei.common.enums.ResBizTypeEnum;
import com.seckillflow.domain.PageResult;
import com.seckillflow.service.SysDictDataService;
import com.seckillflow.service.SysDictTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/system/dict/data")
@ApiResource(name = "字典数据管理", resBizType = ResBizTypeEnum.SYSTEM)
public class SysDictDataController {
    @Autowired
    private SysDictDataService dictDataService;
    @Autowired
    private SysDictTypeService dictTypeService;


    @PreAuthorize("@ss.hasPermi('system:dict:list')")
    @GetMapping(value = "/list", name = "字典数据管理-分页")
    public Response page(SysDictDataEntity dictData) {
        PageResult<SysDictDataEntity> page = dictDataService.page(dictData);
        return Response.ok(page);
    }

    /**
     * 根据字典类型查询字典数据信息
     */
    @GetMapping(value = "/type/{dictType}", name = "字典数据管理-根据字典类型查询字典数据信息")
    //@Anonymous
    public Response dictType(@PathVariable String dictType) {

        List<SysDictDataEntity> data = dictTypeService.selectDictDataByType(dictType);
        if (StringUtils.isEmpty(data)) {
            data = new ArrayList<SysDictDataEntity>();
        }
        return Response.ok(data);
    }


    /**
     * 查询字典数据详细
     */
    @PreAuthorize("@ss.hasPermi('system:dict:query')")
    @GetMapping(value = "/{dictCode}", name = "字典数据管理-查询")
    public Response getInfo(@PathVariable Long dictCode) {
        return Response.ok(dictDataService.selectDictDataById(dictCode));
    }


    /**
     * 新增字典类型
     */
    @PreAuthorize("@ss.hasPermi('system:dict:add')")
    @PostMapping(name = "字典数据管理-新增")
    public Response add(@Validated @RequestBody SysDictDataEntity dict) {
        return Response.ok(dictDataService.insertDictData(dict));
    }

    /**
     * 修改保存字典类型
     */
    @PreAuthorize("@ss.hasPermi('system:dict:edit')")
    @PutMapping(name = "字典数据管理-修改")
    public Response edit(@Validated @RequestBody SysDictDataEntity dict) {
        return Response.ok(dictDataService.updateDictData(dict));
    }

    /**
     * 删除字典类型
     */
    @PreAuthorize("@ss.hasPermi('system:dict:remove')")
    @DeleteMapping(value = "/{dictCodes}", name = "字典数据管理-删除")
    public Response remove(@PathVariable Long[] dictCodes) {
        dictDataService.deleteDictDataByIds(dictCodes);
        return Response.ok();
    }
}
