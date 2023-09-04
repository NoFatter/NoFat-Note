# 分布式锁

在多线程环境下，如果多个线程同时访问共享资源（数据库），往往会发生数据竞争。要想在某一线程访问资源时，令其他线程阻塞等待，就需要使用分布式锁，确保共享资源同时只有一个线程访问。

实现思路：
向Redis中插入同一key：
A插入key，如果成功则获取到锁，B再来插入式发现key已经存在了，则失败，无法获取到锁，知道A将key删除。

为确保锁可用，我们需要在确保锁的实现时，同时满足四个条件：
1、互斥性：任意时刻，只有一个客户端持有锁
2、不发生*死锁*：即使有一个客户端在持有锁的期间崩溃而没有解锁，后续客户端也能正常加锁
3、加锁和解锁必须是同一个客户端，客户端不能解开其他客户端的锁
4、容错性：只要大多数Redis节点能正常运行，客户端就能正常获取和释放锁

> 死锁的产生条件
> 	1、**互斥**：指进程对所分配到的资源进行排它性使用，即在一段时间内某资源只由一个进程占用。如果此时还有其它进程请求资源，则请求者只能等待，直至占有资源的进程用毕释放。
> 	2、**请求和保持**：指进程已经保持至少一个资源，但又提出了新的资源请求，而该资源已被其它进程占有，此时请求进程阻塞，但又对自己已获得的其它资源保持不放。
> 	3、**不剥夺**：指进程已获得的资源，在未使用完之前，不能被剥夺，只能在使用完时由自己释放。
> 	4、**环路等待**：指在发生死锁时，必然存在一个进程——资源的环形链，即进程集合{P0，P1，P2，···，Pn}中的P0正在等待一个P1占用的资源；P1正在等待P2占用的资源，……，Pn正在等待已被P0占用的资源。


# 使用Jedis简单实现分布式锁

这里使用Jedis简单完成了一个分布式锁：

```java
// 定时任务注解，在生产环境的多实例下会同时执行，出现竞争问题
@Scheduled(cron = "*/30 * * * * *") 
public void timeTask(){  
    Jedis jedis = new Jedis(redisServerAddress,Integer.parseInt(redisServerPort));  
    jedis.auth(redisServerAuth);  
    // 尝试获取锁,TIME_OUT为超时时间
    String result = jedis.set(LOCK_KEY, "locked","nx","ex",TIME_OUT);  
    if(result!=null) {  
        try {  
            logger.info("进入定时任务");  
            // 在这里执行定时任务
        } catch (Exception e) {  
            throw new Exception;
        } finally {  
            jedis.del(LOCK_KEY);  
            jedis.close();  
        }  
    }  
}
```

# Redisson实现分布式锁

## Redisson简介

在Jedis的基础上，我们可以引用Redisson：是架设在Redis基础上的一个Java驻内存数据网格（In-Memory Data Grid）。

Redisson基于Java实用工具包中常用接口，为使用者提供了一系列具有分布式特性的常用工具类。使得原本作为协调单机多线程并发程序的工具包获得了协调分布式多机多线程并发系统的能力，大大降低了设计和研发大规模分布式系统的难度。同时结合各富特色的分布式服务，更进一步简化了分布式环境中程序相互之间的协作。

## 配置Redisson

### maven引入
```xml
<dependency> 
<groupId>org.redisson</groupId> 
<artifactId>redisson</artifactId> 
<version>${redisson.version}</version> 
</dependency>
```

### Config配置

实现一个RedissonConfig配置类
配置声明在*application.properties*中
```java
/**  
 * RedissonConfig * 
 * @author liyutao  
 * @version 2023/08/31 15:53  
 **/
 @Configuration  
public class RedissonConfig {  
    @Value("${redis.host}")  
    private String redisServerAddress;  
  
    @Value("${redis.pwd}")  
    private String redisServerAuth;  
  
    @Value("${redis.port}")  
    private String redisServerPort;  
  
    @Value("${redis.database}")  
    private String redisServerDatabase;  
  
    @Bean  
    public RedissonClient getClient(){  
        Config config = new Config();  
        config.useSingleServer().setAddress("redis://"+redisServerAddress+":"+redisServerPort)  
                .setPassword(redisServerAuth)  
                .setDatabase(Integer.parseInt(redisServerDatabase));  
        return Redisson.create(config);  
    }  
  
}
```

### 实现RLocker类，完成常用的加锁/解锁方法

```java 
/**  
 * RLocker * 
 * @author liyutao  
 * @version 2023/08/31 15:40  
 **/@Component  
public class RLocker {  
    @Autowired  
    private RedissonClient redissonClient;  
  
    public static final Long R_LOCK_DEFAULT_WAIT_TIME = 5L;//默认等待时间  
  
    public static final Long R_LOCK_DEFAULT_LEASE_TIME = 15L;//默认超时时间  
  
  
    /**  
     * 获取锁，并且加锁  
     * 若未拿到锁，则线程block  
     * @return RLock lock  
     * @author NoFat  
     **/    
     public RLock lock(String lockKey){  
        RLock lock = redissonClient.getLock(lockKey);  
        lock.lock();  
        return lock;  
    }  
  
    /**  
     * 获取锁，并且加锁(超时时间为timeout秒)  
     * 若未拿到锁，则线程block  
     * @return RLock lock  
     * @author NoFat  
     **/    
     public RLock lock(String lockKey,long timeout){  
        RLock lock = redissonClient.getLock(lockKey);  
        lock.lock(timeout, TimeUnit.SECONDS);  
        return lock;  
    }  
  
    /**  
     * 获取锁，并且加锁(超时时间为timeout,单位为timeUnit)  
     * 若未拿到锁，则线程block  
     * @return RLock lock  
     * @author NoFat  
     **/    
     public RLock lock(String lockKey,long timeout,TimeUnit timeUnit){  
        RLock lock = redissonClient.getLock(lockKey);  
        lock.lock(timeout, timeUnit);  
        return lock;  
    }  
  
    /**  
     * 在waitTime时间内尝试获取锁，并加锁  
     * waitTime时间后，结束等待，返回false  
     * leaseTime时间后，锁过期  
     * @return boolean result  
     * @author NoFat  
     **/    
     public boolean tryLock(String lockKey,long waitTime,long leaseTime,TimeUnit timeUnit){  
        RLock lock = redissonClient.getLock(lockKey);  
        try {  
            return lock.tryLock(waitTime,leaseTime,timeUnit);  
        } catch (InterruptedException e) {  
            return false;  
        }  
    }  
  
    /**  
     * 在默认等待时间内尝试获取锁，并加锁  
     * 在默认超时时间后，结束等待，返回false  
     * leaseTime时间后，锁过期  
     * @return boolean result  
     * @author NoFat  
     **/    
     public boolean tryLock(String lockKey){  
        RLock lock = redissonClient.getLock(lockKey);  
        try {  
            return lock.tryLock(R_LOCK_DEFAULT_WAIT_TIME,R_LOCK_DEFAULT_LEASE_TIME,TimeUnit.SECONDS);  
        } catch (InterruptedException e) {  
            return false;  
        }  
    }  
  
    /**  
     * 解锁  
     * @author NoFat  
     **/    
     public void unlock(String lockKey) {  
        RLock lock = redissonClient.getLock(lockKey);  
        lock.unlock();  
    }  
  
    /**  
     * 解锁  
     * @author NoFat  
     **/    
     public void unlock(RLock lock) {  
        lock.unlock();  
    }  
}
```

以上就完成了Redisson的一个简单配置。
接下来，就可以使用RLocker类的方法，实现一个简单的分布式锁，来避免多线程下的资源冲突问题。

## Redisson分布式锁-代码示例

```java
@Autowired  
private RLocker rLocker;  
  
private static final String LOCK_KEY = "MY_KEY";  
private static final Long WAIT_TIME = 5L;//等待时间  
  
private static final Long LEASE_TIME = 15L;//超时时间  
  
// 定时任务注解，在生产环境的多实例下会同时执行，出现竞争问题
@Scheduled(cron = "*/30 * * * * *")  
public void timeTask(){  
    //获取锁  
    boolean result = rLocker.tryLock(LOCK_KEY,WAIT_TIME,LEASE_TIME,TimeUnit.SECONDS);  
    try {  
        if(result){  
            logger.info("进入定时任务");  
            // 这里执行定时任务
        }  
    } catch (Exception e){  
        throw new ZTBusinessException(BaseRspConstants.RSP_DESC_DB_ERROR);  
    } finally {  
        if(result){  
            rLocker.unlock(LOCK_KEY);  
        }  
    }  
}
```








