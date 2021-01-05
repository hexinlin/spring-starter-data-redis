package com.xinlinh.spring.starter.redisCluster.util;

/**
 * @ClassName: JedisClusterProperty
 * @Description: JedisCluster的基本属性，提供默认属性
 * @Author:xinlinh
 * @Date: 2021/1/4 10:45
 * @Version: 1.0
 **/
public enum JedisClusterProperty {
    nodes(""),timeout("2000"),maxAttempts("3");

    private String val;

    private JedisClusterProperty(String val) {
        this.val = val;
    }

    public String getVal() {
        return val;
    }
}
