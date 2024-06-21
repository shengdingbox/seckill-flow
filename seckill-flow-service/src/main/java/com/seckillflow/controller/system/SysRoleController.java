package com.seckillflow.controller.system;

import com.seckillflow.domain.entity.SysRoleEntity;
import com.seckillflow.domain.entity.SysUserEntity;
import com.seckillflow.domain.entity.SysUserRoleEntity;
import com.seckillflow.domain.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhouzifei.common.annotation.ApiResource;
import com.seckillflow.domain.model.LoginUser;
import com.zhouzifei.common.enums.ResBizTypeEnum;
import com.seckillflow.utils.SecurityUtils;
import com.zhouzifei.common.utils.StringUtils;
import com.seckillflow.service.SysRoleService;
import com.seckillflow.service.SysUserService;
import com.seckillflow.service.SysPermissionService;
import com.seckillflow.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.zhouzifei.common.config.Response;

import java.util.Arrays;

@RestController
@RequestMapping("/system/role")
@ApiResource(name = "角色管理" , resBizType = ResBizTypeEnum.SYSTEM)
public class SysRoleController {

    @Autowired
    private SysRoleService roleService;
    @Autowired
    private SysUserService userService;
    @Autowired
    private SysPermissionService permissionService;
    @Autowired
    private TokenService tokenService;

    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @GetMapping("/list")
    public Response list(SysRoleEntity role) {
        PageResult<SysRoleEntity> list = roleService.page(role);
        return Response.ok(list);
    }

    /**
     * 根据角色编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:role:query')")
    @GetMapping(value = "/{roleId}")
    public Response getInfo(@PathVariable Long roleId) {
        return Response.ok(roleService.selectRoleById(roleId));
    }

    /**
     * 新增角色
     */
    @PreAuthorize("@ss.hasPermi('system:role:add')")
    @PostMapping
    public Response add(@Validated @RequestBody SysRoleEntity role) {
        if (!roleService.checkRoleNameUnique(role)) {
            return Response.failed("新增角色'" + role.getRoleName() + "'失败，角色名称已存在");
        } else if (!roleService.checkRoleKeyUnique(role)) {
            return Response.failed("新增角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        return Response.ok(roleService.insertRole(role));

    }

    /**
     * 修改保存角色
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @PutMapping
    public Response edit(@Validated @RequestBody SysRoleEntity role) {
        roleService.checkRoleAllowed(role);
        if (!roleService.checkRoleNameUnique(role)) {
            return Response.failed("修改角色'" + role.getRoleName() + "'失败，角色名称已存在");
        } else if (!roleService.checkRoleKeyUnique(role)) {
            return Response.failed("修改角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }

        if (roleService.updateRole(role) > 0) {
            // 更新缓存用户权限
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (StringUtils.isNotNull(loginUser.getUser()) && !loginUser.getUser().isAdmin()) {
                loginUser.setPermissions(permissionService.getMenuPermission(loginUser.getUser()));
                loginUser.setUser(userService.selectUserByUserName(loginUser.getUser().getUserName()));
                tokenService.setLoginUser(loginUser);
            }
            permissionService.resetLoginUserRoleCache(role.getRoleId());
            return Response.ok();
        }
        return Response.failed("修改角色'" + role.getRoleName() + "'失败，请联系管理员");
    }

    /**
     * 状态修改
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @PutMapping("/changeStatus")
    public Response changeStatus(@RequestBody SysRoleEntity role) {
        roleService.checkRoleAllowed(role);
        roleService.updateRoleStatus(role);
        //更新redis缓存权限数据
        permissionService.resetLoginUserRoleCache(role.getRoleId());
        return Response.ok();
    }

    /**
     * 删除角色
     */
    @PreAuthorize("@ss.hasPermi('system:role:remove')")
    @DeleteMapping("/{roleIds}")
    public Response remove(@PathVariable Long[] roleIds) {
        roleService.deleteRoleByIds(roleIds);
        //更新redis缓存权限数据
        Arrays.stream(roleIds).forEach(id -> permissionService.resetLoginUserRoleCache(id));

        return Response.ok();
    }


    /**
     * 查询已分配用户角色列表
     */
    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @GetMapping("/authUser/allocatedList")
    public Response allocatedList(SysUserEntity user) {
        Page<SysUserEntity> page = userService.selectAllocatedList(user);
        return Response.ok(page);
    }

    /**
     * 查询未分配用户角色列表
     */
    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @GetMapping("/authUser/unallocatedList")
    public Response unallocatedList(SysUserEntity user) {
        Page<SysUserEntity> page = userService.selectUnallocatedList(user);
        return Response.ok(page);
    }

    /**
     * 取消授权用户
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @PutMapping("/authUser/cancel")
    public Response cancelAuthUser(@RequestBody SysUserRoleEntity userRole) {
        int i = roleService.deleteAuthUser(userRole);
        //更新redis缓存权限数据
        permissionService.resetLoginUserRoleCache(userRole.getRoleId());
        return Response.ok(i);
    }

    /**
     * 批量取消授权用户
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @PutMapping("/authUser/cancelAll")
    public Response cancelAuthUserAll(Long roleId, Long[] userIds) {
        int i = roleService.deleteAuthUsers(roleId, userIds);
        //更新redis缓存权限数据
        permissionService.resetLoginUserRoleCache(roleId);

        return Response.ok(i);
    }

    /**
     * 批量选择用户授权
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @PutMapping("/authUser/selectAll")
    public Response selectAuthUserAll(Long roleId, Long[] userIds) {
        return Response.ok(roleService.insertAuthUsers(roleId, userIds));
    }

}
