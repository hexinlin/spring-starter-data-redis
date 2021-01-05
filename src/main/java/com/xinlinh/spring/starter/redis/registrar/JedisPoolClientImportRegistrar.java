package com.xinlinh.spring.starter.redis.registrar;

import com.xinlinh.spring.starter.redis.annotation.EnableJedisPoolClient;
import com.xinlinh.spring.starter.redis.util.JedisPoolProperty;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @ClassName: JedisPoolClientImportRegistrar
 * @Description: 基于Import注解向spring容器中注入JedisPool
 * @Author:xinlinh
 * @Date: 2020/12/30 11:08
 * @Version: 1.0
 **/
public class JedisPoolClientImportRegistrar implements ImportBeanDefinitionRegistrar,EnvironmentAware {

    private Environment environment;

    private String namespace;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> annotationAttributes =  importingClassMetadata.getAnnotationAttributes(EnableJedisPoolClient.class.getName());
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(annotationAttributes);
        namespace = attributes.getString("namespace");

        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(JedisPool.class);

        Map<String,String> basicMap = parseBaseJedisProperty();

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        bindPoolConfig(poolConfig);

        beanDefinitionBuilder.addConstructorArgValue(poolConfig);
        beanDefinitionBuilder.addConstructorArgValue(basicMap.get(getFullName(JedisPoolProperty.host.name())));
        beanDefinitionBuilder.addConstructorArgValue(Integer.parseInt(basicMap.get(getFullName(JedisPoolProperty.port.name()))));
        beanDefinitionBuilder.addConstructorArgValue(Integer.parseInt(basicMap.get(getFullName(JedisPoolProperty.timeout.name()))));
        beanDefinitionBuilder.addConstructorArgValue(basicMap.get(getFullName(JedisPoolProperty.user.name())));
        beanDefinitionBuilder.addConstructorArgValue(basicMap.get(getFullName(JedisPoolProperty.password.name())));
        beanDefinitionBuilder.addConstructorArgValue(Integer.parseInt(basicMap.get(getFullName(JedisPoolProperty.database.name()))));
        beanDefinitionBuilder.addConstructorArgValue(Boolean.parseBoolean(basicMap.get(getFullName(JedisPoolProperty.useSsl.name()))));

        BeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        registry.registerBeanDefinition(String.format("%s%s",namespace,JedisPool.class.getSimpleName()),beanDefinition);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * @Author xinlinh
     * @Description 解析jedisPool的配置属性
     * @Date 2021/1/4 9:47
     * @Param [config, prefix]
     * @return void
     **/
    private void bindPoolConfig(JedisPoolConfig config) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(JedisPoolConfig.class);
            PropertyDescriptor[] propertyDescriptors =  beanInfo.getPropertyDescriptors();
            Arrays.stream(propertyDescriptors).forEach(propertyDescriptor -> {
                String name = propertyDescriptor.getName();
                Optional.ofNullable(environment.getProperty(getFullName(name))).ifPresent(s -> {
                    try {
                        propertyDescriptor.getWriteMethod().invoke(config,transferType(s,propertyDescriptor.getPropertyType()));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                });
            });
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
    }
    /**
     * @Author xinlinh
     * @Description 解析创建jedisPool的基本属性
     * @Date 2020/12/31 17:10
     * @Param [prefix, environment]
     * @return java.util.Map<java.lang.String,java.lang.String>
     **/
    private Map<String,String> parseBaseJedisProperty() {
        Map<String,String> basicMap = new HashMap<>();
        Arrays.stream(JedisPoolProperty.values()).forEach(jedisPoolProperty -> basicMap.put(getFullName(jedisPoolProperty.name()),jedisPoolProperty.getVal()));
        basicMap.keySet().forEach(key -> {
            Optional.ofNullable(environment.getProperty(key)).ifPresent(s -> basicMap.put(key,s));
        });
        return basicMap;
    }

    private String getFullName (String name) {
        return String.format("%s.jedisPool.%s",namespace,name);
    }

    /**
     * @Author xinlinh
     * @Description 转换参数到指定类型
     * @Date 2020/12/31 17:55
     * @Param [source, targetClass]
     * @return java.lang.Object
     **/
    private Object transferType(String source,Class targetClass) {
        if(targetClass.toString().equals("boolean")) {
            return Boolean.parseBoolean(source);
        }else if(targetClass.toString().equals("long")) {
            return Long.parseLong(source);
        }else if(targetClass.toString().equals("int")) {
            return Integer.parseInt(source);
        }else {
            return source;
        }
    }
}
