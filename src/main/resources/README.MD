# Spring容器中一键开启redis
此插件基于jedis实现了redis的单机客户端redis client和redis cluster的客户端redis cluster client。
需要引入的依赖：
```xml
<dependencies>
        <dependency>
            <groupId>redis.clients</groupId>
            <artifactId>jedis</artifactId>
            <version>3.4.0</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>5.3.2</version>
            <scope>provided</scope>
        </dependency>
</dependencies>
```

## redis client：
* 自定义注解EnableJedisPoolClient：
```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({JedisPoolClientImportRegistrar.class})
public @interface EnableJedisPoolClient {

    String namespace() default "default";//namespace用于区分多个redis client，从而可以在通过spring容器中注入多个redis client。
}
```
* 基于Import注解，Spring容器启动过程中会对@Import注解中的属性值进行解析，我们通过实现ImportBeanDefinitionRegistrar接口向Spring容器中注入JedisPool工具。

* JedisPoolClientImportRegistrar：实现了ImportBeanDefinitionRegistrar和EnvironmentAware接口，前者我们用来通过registry注册beanDefinition，后者我们用于获取相关的配置属性值。
```java
//核心代码
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
    }//方法执行结束后，最终会向spring容器中注入一个名字为namespaceJedisPool，类型为JedisPool的bean。
```
* 测试：
```java
@Configuration
@EnableJedisPoolClient(namespace = "demo")
@PropertySource("classpath:redis-config.properties")
public class AppConfig {
}
//Spring配置入口类，使用@EnableJedisPoolClient注解，开启redis client功能
```
```properties
#jedis Pool配置信息
demo.jedisPool.host=127.0.0.1
demo.jedisPool.port=6379
demo.jedisPool.timeout=3000
demo.jedisPool.user=
demo.jedisPool.password=owl1234owl
demo.jedisPool.database=0
demo.jedisPool.clientName=
demo.jedisPool.useSsl=false

demo.jedisPool.maxIdle=30
demo.jedisPool.maxTotal=100
demo.jedisPool.maxWaitMillis=10000
demo.jedisPool.minEvictableIdleTimeMillis=300000
demo.jedisPool.numTestsPerEvictionRun=1024
demo.jedisPool.timeBetweenEvictionRunsMillis=30000
demo.jedisPool.testOnBorrow=true
demo.jedisPool.testWhileIdle=true
```
```java
//Main方法测试
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
```

## redis cluster client：
* 自定义注解EnableJedisClusterClient：
```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(JedisClusterClientImportRegistrar.class)
public @interface EnableJedisClusterClient {
    String namespace() default "default";
}
```
* 基于Import注解，Spring容器启动过程中会对@Import注解中的属性值进行解析，我们通过实现ImportBeanDefinitionRegistrar接口向Spring容器中注入JedisCluster工具。

* JedisClusterClientImportRegistrar：实现了ImportBeanDefinitionRegistrar和EnvironmentAware接口，前者我们用来通过registry注册beanDefinition，后者我们用于获取相关的配置属性值。
```java
//核心代码
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
    }//方法执行结束后，最终会向spring容器中注入一个名字为namespaceJedisCluster，类型为JedisCluster的bean。
```
* 测试：
```java
@Configuration
@EnableJedisClusterClient(namespace = "demo")
@PropertySource("classpath:redis-cluster-config.properties")
public class AppConfig {
}
```
```properties
#jedis cluseter配置信息
demo.jedisCluster.nodes=192.168.149.144:29001,192.168.149.144:29002,192.168.149.144:29003
demo.jedisCluster.maxAttempts=3
demo.jedisCluster.timeout=2000

demo.jedisCluster.maxIdle=30
demo.jedisCluster.maxTotal=100
demo.jedisCluster.maxWaitMillis=10000
demo.jedisCluster.minEvictableIdleTimeMillis=300000
demo.jedisCluster.numTestsPerEvictionRun=1024
demo.jedisCluster.timeBetweenEvictionRunsMillis=30000
demo.jedisCluster.testOnBorrow=true
demo.jedisCluster.testWhileIdle=true
```
```java
//Main方法测试
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
```

## 直接可用的JAR包：
* maven方式引用(resources目录下已打好包)：
```xml
        <dependency>
            <groupId>com.xinlinh</groupId>
             <artifactId>spring-starter-data-redis</artifactId>
             <version>1.0.0</version>
        </dependency>
```