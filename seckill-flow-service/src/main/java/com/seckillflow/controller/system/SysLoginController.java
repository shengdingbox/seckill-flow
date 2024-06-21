package com.seckillflow.controller.system;

import com.seckillflow.domain.entity.SysMenuEntity;
import com.seckillflow.domain.entity.SysUserEntity;
import com.zhouzifei.common.annotation.ApiResource;
import com.zhouzifei.common.config.Response;
import com.zhouzifei.common.constant.Constants;
import com.zhouzifei.common.enums.ResBizTypeEnum;
import com.seckillflow.domain.model.LoginBody;
import com.seckillflow.service.SysLoginService;
import com.seckillflow.service.SysMenuService;
import com.seckillflow.service.SysPermissionService;
import com.seckillflow.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping
@ApiResource(name = "登录路由", resBizType = ResBizTypeEnum.SYSTEM)
public class SysLoginController {

    @Autowired
    private SysMenuService menuService;
    @Autowired
    private SysLoginService loginService;

    @Autowired
    private SysPermissionService permissionService;

    /**
     * 登录方法
     *
     * @param loginBody 登录信息
     * @return 结果
     */
    @PostMapping(value = "/login", name = "登录方法")
    public Response login(@RequestBody LoginBody loginBody) {
        Map r = new HashMap();
        // 生成令牌
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
                loginBody.getUuid());
        r.put(Constants.TOKEN, token);
        return Response.ok(r);
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping(value = "getInfo", name = "获取用户信息")
    public Response getInfo() {
        SysUserEntity user = SecurityUtils.getLoginUser().getUser();
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(user);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(user);
        Map ajax = new HashMap();
        ajax.put("user", user);
        ajax.put("roles", roles);
        ajax.put("permissions", permissions);
        return Response.ok(ajax);
    }

    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    @GetMapping(value = "getRouters", name = "获取路由信息")
    public Response getRouters() {
        Long userId = SecurityUtils.getUserId();
        List<SysMenuEntity> menus = menuService.selectMenuTreeByUserId(userId);
        return Response.ok(menuService.buildMenus(menus));
    }

}
