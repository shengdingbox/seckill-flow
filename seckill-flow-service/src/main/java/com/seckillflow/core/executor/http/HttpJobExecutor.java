package com.seckillflow.core.executor.http;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.zhouzifei.common.utils.DateUtils;
import com.seckillflow.core.executor.JobExecution;
import com.seckillflow.core.executor.JobExecutor;
import com.seckillflow.domain.entity.TaskJobEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;

@Slf4j
@Component
public class HttpJobExecutor implements JobExecutor {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8; // 定义默认字符集


    @Override
    public JobExecution execute(TaskJobEntity taskJobEntity) throws IOException {
        log.info(">>>>>>>>>>请求路径：{}-{}", taskJobEntity.getHttpMethod(), taskJobEntity.getHttpUrl());
        // 验证HTTP方法是否有效
        HttpMethod method = validateAndConvertMethod(taskJobEntity.getHttpMethod());
        HttpRequest httpRequest = createHttpRequest(method, taskJobEntity.getHttpUrl());
        try {
            if (method != HttpMethod.GET) {
                setRequestBody(httpRequest, taskJobEntity);
            }
        } catch (Exception e) {
            // 异常处理逻辑，例如记录日志或抛出自定义异常
            log.error("Error processing HTTP request: ", e);
            throw new RuntimeException("Error processing HTTP request", e);
        }

        HttpResponse resp = httpRequest.execute();
        String body = resp.body();
        JobExecution jobExecution = new JobExecution();
        jobExecution.setStatus(resp.getStatus());
        jobExecution.setResult(body);
        return jobExecution;
    }

    private HttpMethod validateAndConvertMethod(String methodStr) {
        if (Objects.isNull(methodStr) || methodStr.trim().isEmpty()) {
            throw new IllegalArgumentException("HTTP method cannot be null or empty");
        }
        try {
            return HttpMethod.valueOf(methodStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported HTTP method: " + methodStr);
        }
    }

    private HttpRequest createHttpRequest(HttpMethod method, String httpUrl) {
        switch (method) {
            case GET:
                return HttpRequest.get(httpUrl);
            case POST:
                return HttpRequest.post(httpUrl);
            case DELETE:
                return HttpRequest.delete(httpUrl);
            case PUT:
                return HttpRequest.put(httpUrl);
            case PATCH:
                return HttpRequest.patch(httpUrl);
            default:
                // 由于上方已进行有效判断，实际上这里永远不会执行，但为了完整性保留
                throw new IllegalStateException("Unexpected HttpMethod encountered");
        }
    }

    private void setRequestBody(HttpRequest httpRequest, TaskJobEntity taskJobEntity) throws Exception {
        MediaType contentType = MediaType.valueOf(taskJobEntity.getHttpContentType());
        Charset charset = contentType.getCharset() != null ? contentType.getCharset() : DEFAULT_CHARSET;
        String body = bodyReplace(taskJobEntity.getHttpBody());
        log.info("post body: {}", body); // 使用占位符优化日志记录
        httpRequest.body(body).charset(charset).contentType(taskJobEntity.getHttpContentType());
    }

    private String bodyReplace(String body) {
        return body.replace("${sys_date.yyyyMM}", DateUtils.format(new Date(), "yyyyMM")).replace("${sys_date.yyyyMMdd}", DateUtils.formatToday()).replace("${sys_date.yyyy-MM-dd}", DateUtils.formatDate(new Date())).replace("${sys_date.yyyy-MM-dd HH:mm:ss}", DateUtils.formatDateTime(new Date())).replace("${sys_date.yyyyMMddHHmmss}", DateUtils.format(new Date(), "yyyyMMddHHmmss"));
    }
}
