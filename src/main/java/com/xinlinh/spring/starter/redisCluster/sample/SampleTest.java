package com.xinlinh.spring.starter.redisCluster.sample;

import com.xinlinh.spring.starter.redisCluster.sample.config.AppConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.util.JedisClusterCRC16;


/**
 * @ClassName: SampleTest
 * @Description: TODO
 * @Author:xinlinh
 * @Date: 2020/12/31 18:12
 * @Version: 1.0
 **/
public class SampleTest implements AutoCloseable {
    private AnnotationConfigApplicationContext context = null;

    public static void main(String[] args) throws Exception {
        try (SampleTest sampleTest = new SampleTest()) {
            sampleTest.test();
        }
    }


    public SampleTest() {
        init();
    }

    private void init() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
    }

    private void test() {
        for (String s : context.getBeanDefinitionNames()) {
            System.out.println(s);
        }
        String key = "name-test";
        JedisCluster jedisCluster = context.getBean(JedisCluster.class);
        jedisCluster.setex(key, 30, "xinlinh");
        System.out.println(jedisCluster.get(key));
        int slot = JedisClusterCRC16.getSlot(key);
        Jedis jedis = jedisCluster.getConnectionFromSlot(slot);
        System.out.println(String.format("slot:%s,jedis:%s", slot, jedis));
    }


    @Override
    public void close() throws Exception {
        context.registerShutdownHook();
    }
}
