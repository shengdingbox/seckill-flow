package com.seckillflow.controller.system;


import com.seckillflow.domain.entity.SysResourceEntity;
import com.zhouzifei.common.annotation.ApiResource;
import com.zhouzifei.common.config.Response;
import com.zhouzifei.common.enums.ResBizTypeEnum;
import com.seckillflow.service.SysPermissionService;
import com.seckillflow.service.SysResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.seckillflow.utils.SecurityUtils.getUserId;

@RestController
@RequestMapping("/system/resource")
@ApiResource(name = "资源管理", resBizType = ResBizTypeEnum.SYSTEM)
public class SysApiResourceController {
    @Autowired
    private SysResourceService resourceService;
    @Autowired
    private SysPermissionService permissionService;

    /**
     * 加载对应角色资源列表树
     */
    @GetMapping(value = "/roleApiTreeselect/{roleId}", name = "资源管理-加载对应角色资源列表树")
    public Response roleMenuTreeSelect(@PathVariable("roleId") Long roleId) {
        List<SysResourceEntity> resources = resourceService.selectApiResourceList(getUserId());
        Map ajax = new HashMap();
        ajax.put("checkedKeys", resourceService.selectResourceListByRoleId(roleId));
        ajax.put("resources", resourceService.buildResourceTreeSelect(resources));
        return Response.ok(ajax);
    }

    /**
     * 修改对应角色api资源
     */
    @PutMapping(value = "/roleApi", name = "修改对应角色api资源")
    public Response editRoleResource(Long roleId, Long[] resourceIds) {
        resourceService.editRoleResource(roleId, resourceIds);
        //更新redis缓存权限数据
        permissionService.resetLoginUserRoleCache(roleId);
        return Response.ok();
    }


}
