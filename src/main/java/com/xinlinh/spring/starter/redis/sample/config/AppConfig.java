package com.xinlinh.spring.starter.redis.sample.config;

import com.xinlinh.spring.starter.redis.annotation.EnableJedisPoolClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @ClassName: AppConfig
 * @Description: Spring配置入口类
 * @Author:xinlinh
 * @Date: 2020/12/31 18:09
 * @Version: 1.0
 **/
@Configuration
@EnableJedisPoolClient(namespace = "demo")
@PropertySource("classpath:redis-config.properties")
public class AppConfig {
}
