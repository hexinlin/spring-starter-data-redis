package com.xinlinh.spring.starter.redis.annotation;

import com.xinlinh.spring.starter.redis.registrar.JedisPoolClientImportRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({JedisPoolClientImportRegistrar.class})
public @interface EnableJedisPoolClient {

    String namespace() default "default";
}
