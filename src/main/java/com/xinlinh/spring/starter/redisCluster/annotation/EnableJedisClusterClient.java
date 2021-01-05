package com.xinlinh.spring.starter.redisCluster.annotation;

import com.xinlinh.spring.starter.redisCluster.registrar.JedisClusterClientImportRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(JedisClusterClientImportRegistrar.class)
public @interface EnableJedisClusterClient {
    String namespace() default "default";
}
