package com.seckillflow.controller.monitor;


import com.zhouzifei.common.config.Response;
import com.seckillflow.domain.Server;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务器监控
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/monitor/index")
public class ServerController {
    @PreAuthorize("@ss.hasPermi('monitor:index:list')")
    @GetMapping()
    public Response getInfo() throws Exception {
        Server server = new Server();
        server.copyTo();
        return Response.ok(server);
    }
}
