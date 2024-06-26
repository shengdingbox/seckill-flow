package com.seckillflow.manager;

import com.seckillflow.domain.entity.SysLoginLogEntity;
import com.seckillflow.domain.entity.SysOperLogEntity;
import com.seckillflow.service.SysLoginLogService;
import com.seckillflow.service.SysOperLogService;
import com.zhouzifei.common.config.SpringUtils;
import com.zhouzifei.common.constant.Constants;
import com.zhouzifei.common.utils.LogUtils;
import com.zhouzifei.common.utils.StringUtils;
import com.zhouzifei.common.utils.ip.AddressUtils;
import com.zhouzifei.common.utils.ip.IpUtil;
import com.zhouzifei.common.utils.spring.ServletUtils;
import eu.bitwalker.useragentutils.UserAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Objects;
import java.util.TimerTask;

/**
 * 异步工厂（产生任务用）
 *
 * @author ruoyi
 */
public class AsyncFactory {
    private static final Logger sys_user_logger = LoggerFactory.getLogger("sys-user");

    /**
     * 记录登录信息
     *
     * @param userName 用户名
     * @param status   状态
     * @param message  消息
     * @param args     列表
     * @return 任务task
     */
    public static TimerTask recordLogininfor(final String userName, final Long userId, final String status,
                                             final String message, final Object... args) {
        final UserAgent userAgent = UserAgent.parseUserAgentString(Objects.requireNonNull(ServletUtils.getRequest()).getHeader("User-Agent"));
        final String ip = IpUtil.getIpAddr(ServletUtils.getRequest2());
        return new TimerTask() {
            @Override
            public void run() {
                String address = AddressUtils.getRealAddressByIP(ip);
                StringBuilder s = new StringBuilder();
                s.append(LogUtils.getBlock(ip));
                s.append(address);
                s.append(LogUtils.getBlock(userName));
                s.append(LogUtils.getBlock(userId));
                s.append(LogUtils.getBlock(status));
                s.append(LogUtils.getBlock(message));
                // 打印信息到日志
                sys_user_logger.info(s.toString(), args);
                // 获取客户端操作系统
                String os = userAgent.getOperatingSystem().getName();
                // 获取客户端浏览器
                String browser = userAgent.getBrowser().getName();
                // 封装对象
                SysLoginLogEntity logininfor = new SysLoginLogEntity();
                logininfor.setLoginTime(new Date());
                logininfor.setUserName(userName);
                logininfor.setUserId(userId);
                logininfor.setIpaddr(ip);
                logininfor.setLoginLocation(address);
                logininfor.setBrowser(browser);
                logininfor.setOs(os);
                logininfor.setMsg(message);
                // 日志状态
                if (StringUtils.equalsAny(status, Constants.LOGIN_SUCCESS, Constants.LOGOUT, Constants.REGISTER)) {
                    logininfor.setStatus(Constants.SUCCESS);
                } else if (Constants.LOGIN_FAIL.equals(status)) {
                    logininfor.setStatus(Constants.FAIL);
                }
                // 插入数据
                SpringUtils.getBean(SysLoginLogService.class).insertLogininfor(logininfor);
            }
        };
    }

    /**
     * 操作日志记录
     *
     * @param operLog 操作日志信息
     * @return 任务task
     */
    public static TimerTask recordOper(final SysOperLogEntity operLog) {
        return new TimerTask() {
            @Override
            public void run() {
                SpringUtils.getBean(SysOperLogService.class).insertOperlog(operLog);
            }
        };
    }
}
