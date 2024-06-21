package com.seckillflow.core.executor.http;

import com.zhouzifei.common.utils.DateUtils;
import com.seckillflow.core.executor.JobExecution;
import com.seckillflow.core.executor.JobExecutor;
import com.seckillflow.domain.entity.TaskJobEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;


@Data
@Slf4j
@Component
public class LBHttpJobExecutor implements JobExecutor {

    private RestTemplate restTemplate;

    public LBHttpJobExecutor(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public JobExecution execute(TaskJobEntity taskJobEntity) throws IOException {
        log.info("请求路径：{}-{}", taskJobEntity.getHttpMethod(), sanitizeUrl(taskJobEntity.getHttpUrl()));
        try {
            ResponseEntity<String> resp = request(taskJobEntity);
            JobExecution jobExecution = new JobExecution();
            jobExecution.setStatus(resp.getStatusCodeValue());
            jobExecution.setResult(resp.getBody());
            return jobExecution;
        } catch (HttpClientErrorException | HttpServerErrorException | ResourceAccessException e) {
            // 网络或服务器错误，记录日志并抛出IOException
            log.error("请求失败: {}", e.getMessage());
            throw new IOException("请求失败", e);
        }
    }

    private ResponseEntity<String> request(TaskJobEntity taskJobEntity) {
        String url = sanitizeUrl(taskJobEntity.getHttpUrl().replace("lb", "http"));
        log.info("request url: {}", url); // 注意日志中敏感信息的脱敏
        if ("GET".equals(taskJobEntity.getHttpMethod())) {
            return getForEntity(url, taskJobEntity);
        }
        if ("POST".equals(taskJobEntity.getHttpMethod())) {
            return postForEntity(url, taskJobEntity);
        }
        if ("DELETE".equals(taskJobEntity.getHttpMethod())) {
            restTemplate.delete(url);
            // 对于DELETE请求，目前没有返回值，可以根据实际需求调整
        }
        throw new IllegalArgumentException("unsupported http method:" + taskJobEntity.getHttpMethod());
    }

    private String sanitizeUrl(String url) {
        // 对URL进行简单的验证和清理，防止注入攻击
        // 注意：这里需要根据实际情况实现安全的URL验证逻辑
        return url;
    }

    private ResponseEntity<String> getForEntity(String url, TaskJobEntity taskJobEntity) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(taskJobEntity.getHttpContentType()));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        log.info("get body: {}", entity.getBody()); // 注意日志中敏感信息的脱敏
        return restTemplate.getForEntity(url, String.class, new HashMap<>());
    }

    private ResponseEntity<String> postForEntity(String url, TaskJobEntity taskJobEntity) {
        String body = bodyReplace(taskJobEntity.getHttpBody());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(taskJobEntity.getHttpContentType()));
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        log.info("post body: {}", body); // 注意日志中敏感信息的脱敏
        return restTemplate.postForEntity(url, entity, String.class);
    }

    private String bodyReplace(String body) {
        // 优化日期格式化的逻辑，减少不必要的字符串操作
        if (body == null) {
            return null;
        }
        String formattedBody = body;
        for (String pattern : new String[]{"${sys_date.yyyyMM}", "${sys_date.yyyyMMdd}", "${sys_date.yyyy-MM-dd}", "${sys_date.yyyy-MM-dd HH:mm:ss}", "${sys_date.yyyyMMddHHmmss}"}) {
            if (formattedBody.contains(pattern)) {
                String replacement = "";
                switch (pattern) {
                    case "${sys_date.yyyyMM}":
                        replacement = DateUtils.format(new Date(), "yyyyMM");
                        break;
                    case "${sys_date.yyyyMMdd}":
                        replacement = DateUtils.formatToday();
                        break;
                    case "${sys_date.yyyy-MM-dd}":
                        replacement = DateUtils.formatDate(new Date());
                        break;
                    case "${sys_date.yyyy-MM-dd HH:mm:ss}":
                        replacement = DateUtils.formatDateTime(new Date());
                        break;
                    case "${sys_date.yyyyMMddHHmmss}":
                        replacement = DateUtils.format(new Date(), "yyyyMMddHHmmss");
                        break;
                }
                formattedBody = formattedBody.replace(pattern, replacement);
            }

        }
        return formattedBody;
    }

    // 注意：针对日志中的敏感信息脱敏，这里提供一个简化的脱敏方法示例，具体实现应根据实际情况调整
    private String sanitizeLogMessage(String message) {
        // 这里可以根据需要添加更多的脱敏规则
        return message.replaceAll("(?<=password=)[^&]*(?=(&|$))", "[REDACTED]") // 替换形如password=xxxx的部分
                .replaceAll("(?<=token=)[^&]*(?=(&|$))", "[REDACTED]"); // 替换形如token=yyyy的部分
    }
}