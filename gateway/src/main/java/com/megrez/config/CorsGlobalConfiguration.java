package com.megrez.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsGlobalConfiguration {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:5174"); // 前端地址
        config.addAllowedOrigin("http://localhost:5173"); // 前端地址
        config.addAllowedMethod("*"); // 允许所有方法 GET, POST...
        config.addAllowedHeader("*"); // 允许所有头
        config.setAllowCredentials(true); // 是否允许携带 cookie

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 对所有路径生效

        return new CorsWebFilter(source);
    }
}
