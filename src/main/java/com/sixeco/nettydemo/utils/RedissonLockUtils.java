package com.sixeco.nettydemo.utils;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @author ycy
 * @program: youganw-platform-new
 * @description: redisson分布式锁工具类
 * @date 2021-09-14 13:03:36
 */
@Data
@AllArgsConstructor
@Slf4j
public class RedissonLockUtils {

    //redisoon开关
    private static final String ENABLE="redisson.enable";

    private static final String HOST="spring.redis.host";

    private static final String PORT="spring.redis.port";

    private static final String PASSWORD="spring.redis.password";

    private static final String DATABASE="spring.redis.database";

    private static RedissonClient redissonClient;

    static {
        try {
            //从spring中检查获取是否存在对应实例
            redissonClient = SpringUtil.getBean(RedissonClient.class);
        }catch (BeansException exception){
            LoggerFactory.getLogger("root").warn("RedissonLockUtils not found RedissonClient");
            //检查是否不允许开启redisson
            String enable = SpringUtil.getProperty(RedissonLockUtils.ENABLE);
            if (!StrUtil.equals(enable,Boolean.FALSE.toString())){
                //获取redis相关配置信息
                String host = SpringUtil.getProperty(RedissonLockUtils.HOST);
                String port = SpringUtil.getProperty(RedissonLockUtils.PORT);
                String password = SpringUtil.getProperty(RedissonLockUtils.PASSWORD);
                String database = SpringUtil.getProperty(RedissonLockUtils.DATABASE);
                try {
                    //手动创建redisson实例注入到spring中
                    LoggerFactory.getLogger("root").info("RedissonLockUtils prepare init RedissonClient");
                    Config config = new Config();
                    SingleServerConfig singleServerConfig = config.useSingleServer()
                            .setAddress("redis://"+host.trim()+":"+port.trim())
                            .setDatabase(StrUtil.isNotBlank(database) ? Integer.valueOf(database):0);
                    if (StrUtil.isNotBlank(password)){
                        singleServerConfig.setPassword(password);
                    }
                    redissonClient = Redisson.create(config);
                    LoggerFactory.getLogger("root").info("RedissonLockUtils finish init RedissonClient: {}",redissonClient.getConfig().toJSON());
                    SpringUtil.registerBean("redissonClient",redissonClient);
                }catch (Exception ex){
                    LoggerFactory.getLogger("root").error("RedissonLockUtils init RedissonClient fail",
                            ExceptionUtil.stacktraceToString(ex));
                }
            }else {
                LoggerFactory.getLogger("root").warn("RedissonLockUtils not allow init RedissonClient");
            }
        }
    }

    // lock(), 拿不到lock就不罢休，不然线程就一直block
    public static RLock lock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        return lock;
    }

    // leaseTime为加锁时间，单位为秒
    public static RLock lock(String lockKey, long leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(leaseTime, TimeUnit.SECONDS);
        return lock;
    }

    // timeout为加锁时间，时间单位由unit确定
    public static RLock lock(String lockKey, TimeUnit unit, long timeout) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(timeout, unit);
        return lock;
    }

    /**
     * 设置了leaseTime之后，如果waitTime大于leaseTime，那么锁自动失效后，其他线程会等待waitTime之后，才会获取锁
     * 而不是立即获取锁。
     */
    /**
     * 尝试获取锁
     *
     * @param lockKey
     * @param unit      时间单位
     * @param waitTime  最多等待时间
     * @param leaseTime 上锁后自动释放锁时间
     * @return
     */
    public static boolean tryLock(String lockKey, TimeUnit unit, long waitTime, long leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            return false;
        }
    }


    public static boolean tryLock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(30,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    public static void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.unlock();
    }

    public static void unlock(RLock lock) {
        lock.unlock();
    }

    public static boolean tryLock(String lockKey,TimeUnit unit, long waitTime) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime,unit);
        } catch (InterruptedException e) {
            return false;
        }
    }

    @SneakyThrows
    public static<T> T runLockTask(String lockKey, Callable<T> task){
        RLock lock = null;
        try {
            lock = redissonClient.getLock(lockKey);
            if (lock.tryLock(10, TimeUnit.SECONDS)){
                return task.call();
            }
            log.error("获取分布式锁[{}]失败,等待时间已超时", lock);
            throw new RuntimeException("获取分布式锁失败");
        }catch (InterruptedException e) {
            log.error("获取分布式锁[{}]失败,发生中断: {}", lock,ExceptionUtil.stacktraceToString(e));
            throw e;
        }catch (Exception ex) {
            log.error("获取分布式锁[{}]执行业务失败,异常信息: {}", lock,ExceptionUtil.stacktraceToString(ex));
            throw ex;
        }finally {
            if (ObjectUtil.isNotNull(lock) && lock.isLocked()){
                lock.unlock();
            }
        }
    }

}
