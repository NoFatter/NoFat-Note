package cn.ccccltd.ctctl.sys.utils;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * RLocker
 *
 * @author liyutao
 * @version 2023/09/04 14:29
 **/
@Component
public class RLocker {
    @Autowired
    private RedissonClient redissonClient;

    public static final Long R_LOCK_DEFAULT_WAIT_TIME = 5L;//默认等待时间

    public static final Long R_LOCK_DEFAULT_LEASE_TIME = 15L;//默认超时时间


    /**
     * 获取锁，并且加锁
     * 若未拿到锁，则线程block
     *
     * @return RLock lock
     * @author NoFat
     */
    public RLock lock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        return lock;
    }

    /**
     * 获取锁，并且加锁(超时时间为timeout秒)
     * 若未拿到锁，则线程block
     *
     * @return RLock lock
     * @author NoFat
     */
    public RLock lock(String lockKey, long timeout) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(timeout, TimeUnit.SECONDS);
        return lock;
    }

    /**
     * 获取锁，并且加锁(超时时间为timeout,单位为timeUnit)
     * 若未拿到锁，则线程block
     *
     * @return RLock lock
     * @author NoFat
     */
    public RLock lock(String lockKey, long timeout, TimeUnit timeUnit) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(timeout, timeUnit);
        return lock;
    }

    /**
     * 在waitTime时间内尝试获取锁，并加锁
     * waitTime时间后，结束等待，返回false
     * leaseTime时间后，锁过期
     *
     * @return boolean result
     * @author NoFat
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, timeUnit);
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * 在默认等待时间内尝试获取锁，并加锁
     * 在默认超时时间后，结束等待，返回false
     * leaseTime时间后，锁过期
     *
     * @return boolean result
     * @author NoFat
     */
    public boolean tryLock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(R_LOCK_DEFAULT_WAIT_TIME, R_LOCK_DEFAULT_LEASE_TIME, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * 解锁
     *
     * @author NoFat
     */
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.unlock();
    }

    /**
     * 解锁
     *
     * @author NoFat
     */
    public void unlock(RLock lock) {
        lock.unlock();
    }
}



