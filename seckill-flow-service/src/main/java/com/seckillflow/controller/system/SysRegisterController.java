package com.seckillflow.controller.system;

import com.seckillflow.domain.entity.SysUserEntity;
import com.zhouzifei.common.config.Response;
import com.zhouzifei.common.utils.StringUtils;
import com.seckillflow.domain.model.RegisterBody;
import com.seckillflow.service.SysConfigService;
import com.seckillflow.service.SysRegisterService;
import com.seckillflow.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 注册验证
 */
@RestController
public class SysRegisterController {
    @Autowired
    private SysRegisterService registerService;

    @Autowired
    private SysConfigService configService;

    @Autowired
    private SysUserService userService;

    @PostMapping("/register")
    public Response register(@RequestBody RegisterBody user) {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser")))) {
            return Response.failed("当前系统没有开启注册功能！");
        }
        String msg = registerService.register(user);
        return StringUtils.isEmpty(msg) ? Response.ok() : Response.failed(msg);
    }

    //@Anonymous
    @GetMapping("/userNameUnique")
    public Response userNameUnique(String userName) {
        SysUserEntity userEntity = new SysUserEntity();
        userEntity.setUserName(userName);
        return Response.ok(userService.checkUserNameUnique(userEntity));
    }

    //@Anonymous
    @GetMapping("/emailUnique")
    public Response emailUnique(String email) {
        SysUserEntity userEntity = new SysUserEntity();
        userEntity.setEmail(email);
        return Response.ok(userService.checkEmailUnique(userEntity));
    }

}
