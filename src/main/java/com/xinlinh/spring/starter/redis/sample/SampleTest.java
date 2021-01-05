package com.xinlinh.spring.starter.redis.sample;

import com.xinlinh.spring.starter.redis.sample.config.AppConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * @ClassName: SampleTest
 * @Description: 测试类
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

    private void test() throws InterruptedException {
        for (String s : context.getBeanDefinitionNames()) {
            System.out.println(s);
        }
        JedisPool jedisPool = context.getBean(JedisPool.class);
        Jedis jedis = jedisPool.getResource();
        jedis.set("intValue".getBytes(), "0".getBytes());
        int num = 100;
        CountDownLatch countDownLatch = new CountDownLatch(num);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        IntStream.rangeClosed(1, num).forEach(value -> {
            executorService.submit(() -> {
                try (Jedis jedis0 = jedisPool.getResource()) {
                    jedis0.incr("intValue".getBytes());
                    countDownLatch.countDown();
                }
            });
        });
        countDownLatch.await();
        System.out.println(new String(jedis.get("intValue".getBytes())));
        jedis.close();
        executorService.shutdown();
    }


    @Override
    public void close() throws Exception {
        context.registerShutdownHook();
    }
}
