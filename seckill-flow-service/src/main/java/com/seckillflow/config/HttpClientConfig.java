package com.seckillflow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpClientConfig {

    @Value("${scheduler.rest.timeoutInMin}")
    private int timeoutInMin;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory httpRequestFactory = new SimpleClientHttpRequestFactory();
        httpRequestFactory.setConnectTimeout(10 * timeoutInMin);
        httpRequestFactory.setReadTimeout(10 * timeoutInMin);
        return new RestTemplate(httpRequestFactory);
    }
}
