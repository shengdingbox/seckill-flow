package com.seckillflow.controller.monitor;

import cn.hutool.core.bean.BeanUtil;
import com.seckillflow.config.RedisCache;
import com.seckillflow.domain.model.LoginUser;
import com.seckillflow.domain.model.LoginUserToken;
import com.seckillflow.domain.model.SysUserOnline;
import com.seckillflow.service.ISysUserOnlineService;
import com.zhouzifei.common.config.Response;
import com.zhouzifei.common.constant.CacheConstants;
import com.zhouzifei.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * 在线用户监控
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/monitor/online")
public class SysUserOnlineController {
    @Autowired
    private ISysUserOnlineService userOnlineService;

    @Autowired
    private RedisCache redisCache;

    @PreAuthorize("@ss.hasPermi('monitor:online:list')")
    @GetMapping("/list")
    public Response list(String ipaddr, String userName) {
        Collection<String> keys = redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
        List<SysUserOnline> userOnlineList = new ArrayList<SysUserOnline>();
        for (String key : keys) {
            LoginUserToken user = redisCache.getCacheObject(key);
            LoginUser loginUser = new LoginUser();
            BeanUtil.copyProperties(user, loginUser);
            if (StringUtils.isNotEmpty(ipaddr) && StringUtils.isNotEmpty(userName)) {
                userOnlineList.add(userOnlineService.selectOnlineByInfo(ipaddr, userName, loginUser));
            } else if (StringUtils.isNotEmpty(ipaddr)) {
                userOnlineList.add(userOnlineService.selectOnlineByIpaddr(ipaddr, loginUser));
            } else if (StringUtils.isNotEmpty(userName) && StringUtils.isNotNull(loginUser.getUser())) {
                userOnlineList.add(userOnlineService.selectOnlineByUserName(userName, loginUser));
            } else {
                userOnlineList.add(userOnlineService.loginUserToUserOnline(loginUser));
            }
        }
        Collections.reverse(userOnlineList);
        userOnlineList.removeAll(Collections.singleton(null));
        return Response.ok(userOnlineList);
    }

    /**
     * 强退用户
     */
    @PreAuthorize("@ss.hasPermi('monitor:online:forceLogout')")
//    @Log(title = "在线用户", businessType = BusinessType.FORCE)
    @DeleteMapping("/{tokenId}")
    public Response forceLogout(@PathVariable String tokenId) {
        redisCache.deleteObject(CacheConstants.LOGIN_TOKEN_KEY + tokenId);
        return Response.ok();
    }
}
