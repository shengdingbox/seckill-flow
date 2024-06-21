package com.seckillflow.controller.system;

import com.seckillflow.domain.entity.SysRoleEntity;
import com.seckillflow.domain.entity.SysUserEntity;
import com.zhouzifei.common.annotation.ApiResource;
import com.zhouzifei.common.config.Response;
import com.zhouzifei.common.enums.ResBizTypeEnum;
import com.zhouzifei.common.utils.StringUtils;
import com.seckillflow.domain.PageResult;
import com.seckillflow.service.SysPermissionService;
import com.seckillflow.service.SysRoleService;
import com.seckillflow.service.SysUserService;
import com.seckillflow.utils.SecurityUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户管理
 *
 * @author oddfar
 */
@RestController
@RequestMapping("/system/user")
@ApiResource(name = "用户管理", resBizType = ResBizTypeEnum.SYSTEM)
public class SysUserController {
    @Autowired
    private SysUserService userService;
    @Autowired
    private SysRoleService roleService;
    @Autowired
    private SysPermissionService permissionService;

    /**
     * 分页
     */
    @GetMapping("list")
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    public Response page(SysUserEntity sysUserEntity) {
        PageResult<SysUserEntity> page = userService.page(sysUserEntity);

        return Response.ok(page);
    }

    /**
     * 信息
     */
    @GetMapping({"{userId}", "/"})
    @PreAuthorize("@ss.hasPermi('system:user:query')")
    public Response getInfo(@PathVariable(value = "userId", required = false) Long userId) {
        Map res = new HashMap();
        List<SysRoleEntity> roles = roleService.selectRoleAll();
        res.put("roles", SysUserEntity.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        if (StringUtils.isNotNull(userId)) {
            SysUserEntity sysUser = userService.selectUserById(userId);
            res.put("data", sysUser);
            res.put("roleIds", sysUser.getRoles().stream().map(SysRoleEntity::getRoleId).collect(Collectors.toList()));
        }

        return Response.ok(res);
    }

    /**
     * 新增用户
     */
    @PostMapping
    @PreAuthorize("@ss.hasPermi('system:user:add')")
    public Response add(@Validated @RequestBody SysUserEntity sysUserEntity) {
        userService.insertUser(sysUserEntity);

        return Response.ok();
    }

    /**
     * 修改
     */
    @PutMapping
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    public Response update(@Validated @RequestBody SysUserEntity user) {
        userService.checkUserAllowed(user);
        if (!(userService.checkUserNameUnique(user))) {
            return Response.failed("修改用户'" + user.getUserName() + "'失败，登录账号已存在");
        } else if (StringUtils.isNotEmpty(user.getPhonenumber())
                && !(userService.checkPhoneUnique(user))) {
            return Response.failed("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.getEmail())
                && !(userService.checkEmailUnique(user))) {
            return Response.failed("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setPassword(null);

        return Response.ok(userService.updateUser(user));
    }

    /**
     * 删除
     */
    @DeleteMapping("/{userIds}")
    @PreAuthorize("@ss.hasPermi('system:user:remove')")
    public Response remove(@PathVariable Long[] userIds) {
        if (ArrayUtils.contains(userIds, SecurityUtils.getUserId())) {
            return Response.failed("当前用户不能删除");
        }
        return Response.ok(userService.deleteUserByIds(userIds));
    }

    /**
     * 根据用户编号获取授权角色
     */
    @PreAuthorize("@ss.hasPermi('system:user:query')")
    @GetMapping("/authRole/{userId}")
    public Response authRole(@PathVariable("userId") Long userId) {
        Map res = new HashMap();
        SysUserEntity user = userService.selectUserById(userId);
        List<SysRoleEntity> roles = roleService.selectRolesByUserId(userId);
        res.put("user", user);
        res.put("roles", SysUserEntity.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        return Response.ok(res);
    }

    /**
     * 用户授权角色
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @PutMapping("/authRole")
    public Response insertAuthRole(Long userId, Long[] roleIds) {
        if (!SysUserEntity.isAdmin(userId)) {
            userService.insertUserAuth(userId, roleIds);
            return Response.ok();
        } else {
            return Response.failed("不可操作超级管理员");
        }


    }

    /**
     * 重置密码
     */
    @PreAuthorize("@ss.hasPermi('system:user:resetPwd')")
    @PutMapping("/resetPwd")
    public Response resetPwd(@RequestBody SysUserEntity user) {

        userService.checkUserAllowed(user);
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        return Response.ok(userService.resetPwd(user));
    }

    /**
     * 状态修改
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @PutMapping("/changeStatus")
    public Response changeStatus(@RequestBody SysUserEntity user) {

        userService.checkUserAllowed(user);
        userService.updateUserStatus(user);
        permissionService.resetUserRoleAuthCache(user.getUserId());
        return Response.ok();
    }


}
