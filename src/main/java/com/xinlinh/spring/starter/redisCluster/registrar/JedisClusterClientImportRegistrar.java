package com.xinlinh.spring.starter.redisCluster.registrar;

import com.xinlinh.spring.starter.redisCluster.annotation.EnableJedisClusterClient;
import com.xinlinh.spring.starter.redisCluster.util.JedisClusterProperty;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @ClassName: JedisClusterClientImportRegistrar
 * @Description: 基于Import向spring容器中注入JedisCluster
 * @Author:xinlinh
 * @Date: 2021/1/4 10:38
 * @Version: 1.0
 **/
public class JedisClusterClientImportRegistrar implements ImportBeanDefinitionRegistrar ,EnvironmentAware {

    private Environment environment;
    private String namespace;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> annotationAttributes =  importingClassMetadata.getAnnotationAttributes(EnableJedisClusterClient.class.getName());
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(annotationAttributes);
        namespace = attributes.getString("namespace");

        Map<String,String> basicMap = parseBaseJedisClusterProperty();
        Set<HostAndPort> hostAndPorts = getNodes(basicMap.get(getFullName("nodes")));

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        bindPoolConfig(poolConfig);

        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(JedisCluster.class);
        beanDefinitionBuilder.addConstructorArgValue(hostAndPorts);
        beanDefinitionBuilder.addConstructorArgValue(Integer.parseInt(basicMap.get(getFullName(JedisClusterProperty.timeout.name()))));
        beanDefinitionBuilder.addConstructorArgValue(Integer.parseInt(basicMap.get(getFullName(JedisClusterProperty.maxAttempts.name()))));
        beanDefinitionBuilder.addConstructorArgValue(poolConfig);

        BeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        registry.registerBeanDefinition(String.format("%s%s",namespace,JedisCluster.class.getSimpleName()),beanDefinition);
    }

    private Map<String,String> parseBaseJedisClusterProperty() {
        Map<String,String> basicMap = new HashMap<>();
        Arrays.stream(JedisClusterProperty.values()).forEach(jedisClusterProperty -> basicMap.put(getFullName(jedisClusterProperty.name()),jedisClusterProperty.getVal()));
        basicMap.keySet().forEach(key -> {
            Optional.ofNullable(environment.getProperty(key)).ifPresent(s -> basicMap.put(key,s));
        });
        return basicMap;

    }

    private Set<HostAndPort> getNodes(String nodesStr) {
        String[] nodes = nodesStr.split(",");
        Set<HostAndPort> nodesSet = new HashSet<>();
        Arrays.stream(nodes).forEach(s -> nodesSet.add(HostAndPort.from(s)));
        return nodesSet;
    }

    private void bindPoolConfig(JedisPoolConfig poolConfig){
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(JedisPoolConfig.class);
            PropertyDescriptor[] propertyDescriptors =  beanInfo.getPropertyDescriptors();
            Arrays.stream(propertyDescriptors).forEach(propertyDescriptor -> {
                String name = propertyDescriptor.getName();
                Optional.ofNullable(environment.getProperty(getFullName(name))).ifPresent(s -> {
                    try {
                        propertyDescriptor.getWriteMethod().invoke(poolConfig,transferType(s,propertyDescriptor.getPropertyType()));
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

    private String getFullName (String name) {
        return String.format("%s.jedisCluster.%s",namespace,name);
    }
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
