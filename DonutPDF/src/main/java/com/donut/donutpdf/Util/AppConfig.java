package com.donut.donutpdf.Util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig { // 또는 다른 설정 클래스 이름

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}