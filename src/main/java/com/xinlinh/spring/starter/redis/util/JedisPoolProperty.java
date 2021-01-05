package com.xinlinh.spring.starter.redis.util;

/**
 * @Author xinlinh
 * @Description JedisPool的基本属性，提供默认属性值
 * @Date 2021/1/4 11:59
 * @Param
 * @return
 **/

public enum JedisPoolProperty {

    host("localhost"),port("6379"),timeout("2000"),user(""),password(""),database("0"),clientName(""),useSsl("false");

    private String val;

    private JedisPoolProperty(String val) {
        this.val = val;
    }

    public String getVal() {
        return val;
    }
}
