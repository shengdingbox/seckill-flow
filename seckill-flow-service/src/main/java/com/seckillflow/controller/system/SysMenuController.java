package com.seckillflow.controller.system;

import com.seckillflow.domain.entity.SysMenuEntity;
import com.zhouzifei.common.annotation.ApiResource;
import com.zhouzifei.common.config.Response;
import com.zhouzifei.common.constant.UserConstants;
import com.zhouzifei.common.enums.ResBizTypeEnum;
import com.zhouzifei.common.utils.StringUtils;
import com.seckillflow.service.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.seckillflow.utils.SecurityUtils.getUserId;

@RestController
@RequestMapping("/system/menu")
@ApiResource(name = "菜单管理", resBizType = ResBizTypeEnum.SYSTEM)
public class SysMenuController {

    @Autowired
    private SysMenuService menuService;

    /**
     * 获取菜单列表
     */
    @PreAuthorize("@ss.hasPermi('system:menu:list')")
    @GetMapping(value = "/list", name = "菜单管理-分页")
    public Response list(SysMenuEntity menu) {
        List<SysMenuEntity> menus = menuService.selectMenuList(menu, getUserId());
        return Response.ok(menus);
    }

    /**
     * 根据菜单编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:menu:query')")
    @GetMapping(value = "/{menuId}", name = "菜单管理-查询")
    public Response getInfo(@PathVariable Long menuId) {
        return Response.ok(menuService.selectMenuById(menuId));
    }

    /**
     * 获取菜单下拉树列表
     */
    @GetMapping(value = "/treeselect", name = "菜单管理-获取菜单下拉树列表")
    public Response treeSelect(SysMenuEntity menu) {
        List<SysMenuEntity> menus = menuService.selectMenuList(menu, getUserId());
        return Response.ok(menuService.buildMenuTreeSelect(menus));
    }

    /**
     * 加载对应角色菜单列表树
     */
    @GetMapping(value = "/roleMenuTreeselect/{roleId}", name = "菜单管理-加载对应角色菜单列表树")
    public Response roleMenuTreeselect(@PathVariable("roleId") Long roleId) {
        List<SysMenuEntity> menus = menuService.selectMenuList(getUserId());
        Map ajax = new HashMap();
        ajax.put("checkedKeys", menuService.selectMenuListByRoleId(roleId));
        ajax.put("menus", menuService.buildMenuTreeSelect(menus));
        return Response.ok(ajax);
    }

    /**
     * 新增菜单
     */
    @PreAuthorize("@ss.hasPermi('system:menu:add')")
    @PostMapping(name = "菜单管理-新增")
    public Response add(@Validated @RequestBody SysMenuEntity menu) {
        if (!menuService.checkMenuNameUnique(menu)) {
            return Response.failed("新增菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
        } else if (UserConstants.YES_FRAME.equals(menu.getIsFrame()) && !StringUtils.ishttp(menu.getPath())) {
            return Response.failed("新增菜单'" + menu.getMenuName() + "'失败，地址必须以http(s)://开头");
        }
        return Response.ok(menuService.insertMenu(menu));
    }

    /**
     * 修改菜单
     */
    @PreAuthorize("@ss.hasPermi('system:menu:edit')")
    @PutMapping(name = "菜单管理-修改")
    public Response edit(@Validated @RequestBody SysMenuEntity menu) {
        if (!menuService.checkMenuNameUnique(menu)) {
            return Response.failed("修改菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
        } else if (UserConstants.YES_FRAME.equals(menu.getIsFrame()) && !StringUtils.ishttp(menu.getPath())) {
            return Response.failed("修改菜单'" + menu.getMenuName() + "'失败，地址必须以http(s)://开头");
        } else if (menu.getMenuId().equals(menu.getParentId())) {
            return Response.failed("修改菜单'" + menu.getMenuName() + "'失败，上级菜单不能选择自己");
        }
        return Response.ok(menuService.updateMenu(menu));
    }

    /**
     * 删除菜单
     */
    @PreAuthorize("@ss.hasPermi('system:menu:remove')")
    @DeleteMapping(value = "/{menuId}", name = "菜单管理-删除")
    public Response remove(@PathVariable("menuId") Long menuId) {
        if (menuService.hasChildByMenuId(menuId)) {
            return Response.failed("存在子菜单,不允许删除");
        }
        if (menuService.checkMenuExistRole(menuId)) {
            return Response.failed("菜单已分配,不允许删除");
        }
        return Response.ok(menuService.deleteMenuById(menuId));
    }


}
