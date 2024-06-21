package com.seckillflow.controller.system;

import com.seckillflow.domain.entity.SysUserEntity;
import com.zhouzifei.common.annotation.ApiResource;
import com.seckillflow.domain.model.LoginUser;
import com.zhouzifei.common.enums.ResBizTypeEnum;
import com.seckillflow.utils.SecurityUtils;
import com.zhouzifei.common.utils.StringUtils;
import com.zhouzifei.common.config.Response;
import com.seckillflow.service.SysUserService;
import com.seckillflow.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

import static com.seckillflow.utils.SecurityUtils.getLoginUser;

/**
 * 个人信息 业务处理
 */
@RestController
@RequestMapping("/system/user/profile")
@ApiResource(name = "个人信息管理", resBizType = ResBizTypeEnum.SYSTEM)
public class SysProfileController {
    @Autowired
    private SysUserService userService;

    @Autowired
    private TokenService tokenService;

    /**
     * 个人信息
     */
    @GetMapping(name = "个人信息管理-查询")
    public Response profile() {
        LoginUser loginUser = getLoginUser();
        SysUserEntity user = loginUser.getUser();
        Map ajax = new HashMap();
        ajax.put("roleGroup", userService.selectUserRoleGroup(loginUser.getUsername()));
        return Response.ok(ajax);
    }

    /**
     * 修改用户
     */
    @PutMapping(name = "个人信息管理-修改")
    public Response updateProfile(@RequestBody SysUserEntity user) {
        LoginUser loginUser = getLoginUser();
        SysUserEntity sysUser = loginUser.getUser();
        user.setUserName(sysUser.getUserName());
        if (StringUtils.isNotEmpty(user.getPhonenumber())
                && !(userService.checkPhoneUnique(user))) {
            return Response.failed("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        }
        if (StringUtils.isNotEmpty(user.getEmail())
                && !(userService.checkEmailUnique(user))) {
            return Response.failed("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setUserId(sysUser.getUserId());
        user.setPassword(null);
        user.setAvatar(null);
        if (userService.updateUserProfile(user) > 0) {
            // 更新缓存用户信息
            sysUser.setNickName(user.getNickName());
            sysUser.setPhonenumber(user.getPhonenumber());
            sysUser.setEmail(user.getEmail());
            sysUser.setSex(user.getSex());
            tokenService.setLoginUser(loginUser);
            return Response.ok();
        }
        return Response.failed("修改个人信息异常，请联系管理员");
    }

    /**
     * 重置密码
     */
    @PutMapping(value = "/updatePwd", name = "个人信息管理-重置密码")
    public Response updatePwd(String oldPassword, String newPassword) {
        LoginUser loginUser = getLoginUser();
        String userName = loginUser.getUsername();
        String password = loginUser.getPassword();
        if (!SecurityUtils.matchesPassword(oldPassword, password)) {
            return Response.failed("修改密码失败，旧密码错误");
        }
        if (SecurityUtils.matchesPassword(newPassword, password)) {
            return Response.failed("新密码不能与旧密码相同");
        }
        if (userService.resetUserPwd(userName, SecurityUtils.encryptPassword(newPassword)) > 0) {
            // 更新缓存用户密码
            loginUser.getUser().setPassword(SecurityUtils.encryptPassword(newPassword));
            tokenService.setLoginUser(loginUser);
            return Response.ok();
        }
        return Response.failed("修改密码异常，请联系管理员");
    }

    /**
     * 头像上传
     */
    @PostMapping(value = "/avatar", name = "个人信息管理-头像上次")
    public Response avatar(@RequestParam("avatarfile") MultipartFile file) throws Exception {
        if (!file.isEmpty()) {
            LoginUser loginUser = getLoginUser();
            String avatar = "";//FileUploadUtils.upload(ConfigExpander.getAvatarPath(), file, MimeTypeUtils.IMAGE_EXTENSION);
            if (userService.updateUserAvatar(loginUser.getUsername(), avatar)) {
                Map ajax = new HashMap();
                ajax.put("imgUrl", avatar);
                // 更新缓存用户头像
                loginUser.getUser().setAvatar(avatar);
                tokenService.setLoginUser(loginUser);
                return Response.ok(ajax);
            }
        }
        return Response.failed("上传图片异常，请联系管理员");
    }
}
