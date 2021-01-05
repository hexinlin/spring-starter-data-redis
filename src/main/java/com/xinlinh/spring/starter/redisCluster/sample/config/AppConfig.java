package com.xinlinh.spring.starter.redisCluster.sample.config;

import com.xinlinh.spring.starter.redisCluster.annotation.EnableJedisClusterClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @ClassName: AppConfig
 * @Description: Spring配置入口类
 * @Author:xinlinh
 * @Date: 2021/1/4 11:24
 * @Version: 1.0
 **/
@Configuration
@EnableJedisClusterClient(namespace = "demo")
@PropertySource("classpath:redis-cluster-config.properties")
public class AppConfig {
}
