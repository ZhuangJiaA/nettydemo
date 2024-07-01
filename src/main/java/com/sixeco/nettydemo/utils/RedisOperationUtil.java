package com.sixeco.nettydemo.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * redis操作工具类
 */
public class RedisOperationUtil {

    private static RedisTemplate redisTemplate;
    private static Logger log = LoggerFactory.getLogger(RedisOperationUtil.class);

    static {
        try {
            redisTemplate = SpringUtil.getBean("redisTemplate");
        } catch (NoSuchBeanDefinitionException ex) {
            try {
                redisTemplate = SpringUtil.getBean(RedisTemplate.class);
            } catch (Exception e) {
                log.warn("RedisOperationUtil无法从spring容器中自动装配RedisTemplate: {}", e.getMessage());
            }
        }
        if (ObjectUtil.isNull(redisTemplate)) {
            log.warn("RedisOperationUtil工具类自动创建新RedisTemplate进行装配");
            redisTemplate = new RedisTemplate<>();
            StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
            Jackson2JsonRedisSerializer<Object> jsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
            redisTemplate.setConnectionFactory(SpringUtil.getBean(RedisConnectionFactory.class));
            redisTemplate.setKeySerializer(stringRedisSerializer);
            redisTemplate.setStringSerializer(stringRedisSerializer);
            redisTemplate.setHashKeySerializer(stringRedisSerializer);
            redisTemplate.setValueSerializer(stringRedisSerializer);
            redisTemplate.setHashValueSerializer(jsonRedisSerializer);
            redisTemplate.afterPropertiesSet();
            log.warn("RedisOperationUtil工具类自动创建新RedisTemplate装配完成");
        }
    }

    public static RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }


    /**
     * @describe 添加redis缓存键值
     * @Param: key 键名
     * @Param: value 键值
     * @return
     */
    public static void addValue(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * @describe 添加redis缓存键值
     * @Param: key 键名
     * @Param: value 键值
     * @Param: timeout 超时时间 单位分钟
     * @return
     */
    public static void addValue(String key, String value, Long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.MINUTES);
    }

    /**
     * @describe 添加redis缓存键值
     * @Param: key 键名
     * @Param: value 键值
     * @Param: timeout 超时时间
     * @Param: timeUnit 超时时间单位
     * @return
     */
    public static void addValue(String key, String value, Long timeout,TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * @describe 添加redis缓存键值
     * @Param: key 键名
     * @Param: value 键值
     * @return
     */
    public static void addValueIfAbsent(String key, String value) {
        redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    /**
     * @describe 添加redis缓存键值
     * @Param: key 键名
     * @Param: value 键值
     * @Param: timeout 超时时间
     * @Param: timeUnit 超时时间单位
     * @return
     */
    public static void addValueIfAbsent(String key, String value, Long timeout,TimeUnit timeUnit) {
        redisTemplate.opsForValue().setIfAbsent(key, value, timeout, timeUnit);
    }

    /**
     * @describe 获取reids缓存值
     * @Param: key 键名
     * @return
     */
    public static String getValue(String key) {
        Object data = redisTemplate.opsForValue().get(key);
        return data == null ? null : data.toString();
    }

    /**
     * @describe 获取reids缓存值
     * @Param: keyList 键名集合
     * @return
     */
    public static Map<String,String> getValue(Collection<String> keyList) {
        List<String> result = redisTemplate.executePipelined((RedisCallback<String>) connection -> {
            for (String key : keyList) {
                connection.get(redisTemplate.getKeySerializer().serialize(key));
            }
            return null;
        });
        if (result == null || result.size() != keyList.size()) {
            return null;
        } else {
            Map<String,String> returnData = new HashMap<>(keyList.size());
            int i = 0;
            for (String key : keyList) {
                Optional.ofNullable(result.get(i++))
                        .ifPresent(item -> returnData.put(key,item));
            }
            return returnData;
        }
    }

    /**
     * @describe 保存redis缓存值
     * @Param: key 键名
     * @Param: value 键值
     * @return
     */
    public static void saveObj(String key, Object value) {
        redisTemplate.opsForValue().set(key,
                ObjectUtil.isBasicType(value) ? String.valueOf(value) : JSONObject.toJSONString(value));
    }

    /**
     * @describe 保存redis缓存值
     * @Param: key 键名
     * @Param: value 键值
     * @Param: timeout 超时时间 单位分钟
     * @return
     */
    public static void saveObj(String key, Object value, Long timeout) {
        redisTemplate.opsForValue().set(key,
                ObjectUtil.isBasicType(value) ? String.valueOf(value) : JSONObject.toJSONString(value),
                timeout, TimeUnit.MINUTES);
    }

    /**
     * @describe 保存redis缓存值
     * @Param: key 键名
     * @Param: value 键值
     * @Param: timeout 超时时间
     * @Param: unit 时间单位
     * @return
     */
    public static void saveObj(String key, Object value, Long timeout,TimeUnit unit) {
        redisTemplate.opsForValue().set(key,
                ObjectUtil.isBasicType(value) ? String.valueOf(value) : JSONObject.toJSONString(value),
                timeout, unit);
    }
    
    /**
     * @describe 获取reids缓存值
     * @Param: key 键名
     * @Param: classValue 序列化实例类型
     * @return
     */
    public static <T> T getObj(String key, Class<T> classValue) {
        return Optional.ofNullable(getValue(key))
                .map(e -> JSONObject.parseObject(e, classValue))
                .orElse(null);
    }

     /**
     * @describe 获取reids缓存值
     * @Param: keys 键名列表
     * @Param: classValue 序列化实例类型
     * @return
     */
    public static <T> Map<String,T> getObj(Collection<String> keys, Class<T> classValue) {
        List<Object> result = redisTemplate.executePipelined(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                for (String key : keys) {
                    connection.get(redisTemplate.getKeySerializer().serialize(key));
                }
                return null;
            }
        });
        if (result == null || result.size() != keys.size()) {
            return null;
        } else {
            Map<String,T> returnData = new HashMap<>(keys.size());
            int i = 0;
            for (String key : keys) {
                Optional.ofNullable(result.get(i++))
                        .map(v -> JSONObject.parseObject(v.toString(), classValue))
                        .ifPresent(item -> returnData.put(key,item));
            }
            return returnData;
        }
    }

    /**
     * @describe 删除键
     * @Param: key  键名
     * @return
     */
    public static Boolean removeValue(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * @describe 删除键
     * @Param: key 键名
     * @return
     */
    public static Long removeValue(Collection<String> keyList) {
        return redisTemplate.delete(keyList);
    }

    /**
     * @describe 添加set集合数据
     * @Param: key 键名
     * @Param: value 键值
     * @return
     */
    public static Long addSetValue(String key, String... value) {
        return redisTemplate.opsForSet().add(key, value);
    }

    /**
     * @describe 获取set集合数据
     * @Param: key 键名
     * @return
     */
    public static Set<String> getSetValue(String key){
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * @describe 获取set集合数据
     * @Param: key 键名
     * @return
     */
    public static <T> Set<T> getSetValue(String key,Class<T> classType){
        Set<String> dataSet = getSetValue(key);
        if (CollUtil.isNotEmpty(dataSet)){
            return dataSet.stream().map(da -> JSONObject.parseObject(da.toString(),classType))
                    .collect(Collectors.toSet());
        }
        return null;
    }

    /**
     * @describe 添加zset集合数据
     * @Param: key 键名
     * @Param: value 键值
     * @Param: score 分数
     * @return
     */
    public static Boolean addZSetValue(String key, String value, Object score) {
        return redisTemplate.opsForZSet().add(key, value, Double.valueOf(score.toString()));
    }

    /**
     * @describe 添加zset集合数据
     * @Param: key 键名
     * @Param: value 值
     * @Param: score 分数
     * @Param: timeout 超时时间 单位分钟
     * @return
     */
    public static Boolean addZSetValue(String key, String value, Object score, Long timeout) {
        Boolean result = addZSetValue(key, value, score);
        redisTemplate.expire(key, timeout, TimeUnit.MINUTES);
        return result;
    }

    /**
     * @describe 添加zset集合数据
     * @Param: key 键名
     * @Param: values 值数据
     * @return
     */
    public static Long addZSetValue(String key, Set<ZSetOperations.TypedTuple> values) {
        return redisTemplate.opsForZSet().add(key, values);
    }

    /**
     * @describe 添加zset集合数据
     * @Param: key 键名
     * @Param: values 值数据
     * @Param: timeout 超时时间 单位分钟
     * @return
     */
    public static Long addZSetValue(String key, Set<ZSetOperations.TypedTuple> values, Long timeout) {
        Long result = addZSetValue(key,values);
        redisTemplate.expire(key, timeout, TimeUnit.MINUTES);
        return result;
    }

    /**
     * @describe 获取zset集合数据
     * @Param: key 键名
     * @return
     */
    public static Set<String> getZsetValue(String key){
        return redisTemplate.opsForZSet().range(key,0,-1);
    }

    /**
     * @describe 获取zset集合数据
     * @Param: key 键名
     * @return
     */
    public static <T> Set<T> getZsetValue(String key,Class<T> classType){
        Set<String> dataSet = getZsetValue(key);
        if (CollUtil.isNotEmpty(dataSet)){
            return dataSet.stream().map(e -> JSONObject.parseObject(e,classType)).collect(Collectors.toSet());
        }
        return null;
    }


    /**
     * @return
     * @describe 根据score分值从小到大排序范围取值
     * @Param: key 键名
     * @Param: min 范围最小值(包含)
     * @Param: max 范围最大值(包含)
     * @Param: offset 开始取值的偏移量，从0开始
     * @Param: count 取值的个数
     */
    public static <T> Set<T> rangeZSetData(String key, Object min, Object max,
                                           Long offset, Long count) {
        return redisTemplate.opsForZSet().rangeByScore(key,
                Double.valueOf(min.toString()), Double.valueOf(max.toString()),
                offset, count);
    }

    /**
     * @return
     * @describe 根据score分值从小到大排序范围取值
     * @Param: key 键名
     * @Param: min 范围最小值(包含)
     * @Param: max 范围最大值(包含)
     * @Param: offset 开始取值的偏移量，从0开始
     * @Param: count 取值的个数
     */
    public static <T> Set<ZSetOperations.TypedTuple> rangeZSetDataWithScores(String key, Object min, Object max,
                                                                             Long offset, Long count) {
        return redisTemplate.opsForZSet().rangeByScoreWithScores(key,
                Double.valueOf(min.toString()), Double.valueOf(max.toString()),
                offset, count);
    }


    /**
     * @return
     * @describe 根据score分值从大到小排序范围取值
     * @Param: key 键名
     * @Param: min 范围最小值(包含)
     * @Param: max 范围最大值(包含)
     * @Param: offset 开始取值的偏移量，从0开始
     * @Param: count 取值的个数
     */
    public static <T> Set<T> revRangeZSetData(String key, Object min, Object max,
                                              Long offset, Long count) {
        return redisTemplate.opsForZSet().reverseRangeByScore(key,
                Double.valueOf(min.toString()), Double.valueOf(max.toString()),
                offset, count);
    }

    /**
     * @return
     * @describe 根据score分值从大到小排序范围取值
     * @Param: key 键名
     * @Param: min 范围最小值(包含)
     * @Param: max 范围最大值(包含)
     * @Param: offset 开始取值的偏移量，从0开始
     * @Param: count  取值的个数
     */
    public static <T> Set<ZSetOperations.TypedTuple> revRangeZSetDataWithScores(String key, Object min, Object max,
                                                                                Long offset, Long count) {
        return redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key,
                Double.valueOf(min.toString()), Double.valueOf(max.toString()),
                offset, count);
    }


    /**
     * @return
     * @describe 获取集合中value的分值
     * @Param: key 键名
     * @Param: value 值
     */
    public static Long getZsetValueScore(String key, String value) {
        return Optional.ofNullable(redisTemplate.opsForZSet().score(key, value))
                .map(e -> e.longValue()).orElse(null);
    }

    /**
     * @describe 获取zset中的元素个数
     * @Param: key 键名
     * @return
     */
    public static long getZsetCount(String key) {
        return redisTemplate.opsForZSet().count(key, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    /**
     * @describe 获取zset数据的分值
     * @Param: cacheKey 键名
     * @Param: values 值
     * @return
     */
    public static List<Long> getZsetValueListScore(String cacheKey, Object... values) {
        List<Double> scoreList = redisTemplate.opsForZSet().score(cacheKey, values);
        if (CollUtil.isEmpty(scoreList)) {
            return null;
        }
        return scoreList.stream().map(e -> e.longValue()).collect(Collectors.toList());
    }

    /**
     * @describe 删除zset数据中的值
     * @Param: cacheKey 键
     * @Param: value 值
     * @return
     */
    public static Long removeZSetValue(String cacheKey, String... value) {
        return redisTemplate.opsForZSet().remove(cacheKey, value);
    }

    /**
     * @describe 获取zset中的元素个数
     * @Param: key 键名
     * @Param: min 分值最小值（包含）
     * @Param: max 分值最最大值（包含）
     * @return
     */
    public static long getZsetCount(String key, double min, double max) {
        return redisTemplate.opsForZSet().count(key, min, max);
    }

    /**
     * @describe 保存hash对象
     * @Param: key 键名
     * @Param: value 值对象
     * @return
     */
    public static void saveObjHash(String key, Object value) {
        redisTemplate.opsForHash().putAll(key, BeanUtil.beanToMap(value, false, true));
    }

    public static void saveObjHash(String key, Object value, Long timeout) {
        saveObjHash(key,value);
        redisTemplate.expire(key, timeout, TimeUnit.MINUTES);
    }

    /**
     * @describe 更新hash对象
     * @Param: key 键名
     * @Param: hashKey hash键
     * @Param: value hash值
     * @return
     */
    public static void updateOneHash(String key, Object hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey.toString(), value);
    }

    /**
     * @describe hash值自增
     * @Param: key 键名
     * @Param: hashKey hash键
     * @Param: value 自增值
     * @return
     */
    public static void incHashKey(String key, Object hashKey, long value) {
        redisTemplate.opsForHash().increment(key, hashKey.toString(), value);
    }

    /**
     * @describe 获取hash对象
     * @Param: key 键名
     * @Param: beanClass 序列号实例对象
     * @return
     */
    public static <T> T getHashObj(String key, Class<T> beanClass) {
        Map mapData = redisTemplate.opsForHash().entries(key);
        if (CollUtil.isEmpty(mapData)) {
            return null;
        }
        return JSONObject.parseObject(JSONObject.toJSONString(mapData), beanClass);
    }

    /**
     * @describe 获取hash对象
     * @Param: keyList 键名
     * @Param: beanClass 序列号实例对象
     * @return
     */
    public static <T> List<T> getHashObjList(Collection<String> keyList, Class<T> beanClass) {
        List<Map<Object, Object>> result = redisTemplate.executePipelined(new RedisCallback() {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                for (String key : keyList) {
                    connection.hGetAll(redisTemplate.getKeySerializer().serialize(key));
                }
                return null;
            }
        });
        if (CollUtil.isEmpty(result)) {
            return null;
        }
        return result.stream().filter(e -> CollUtil.isNotEmpty(e))
                .map(v -> JSONObject.parseObject(JSONObject.toJSONString(v), beanClass)).collect(Collectors.toList());
    }

    /**
     * @return
     * @describe 批量保存hash对象
     * @Param: dataMap  key为键名，value为hash数据
     */
    public static void saveObjHashList(Map<String, Object> dataMap) {
        redisTemplate.executePipelined(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                for (Map.Entry<String, Object> data : dataMap.entrySet()) {
                    connection.hMSet(
                            redisTemplate.getKeySerializer().serialize(data.getKey()),
                            BeanUtil.beanToMap(data.getValue(), false, true)
                                    .entrySet().stream().collect(Collectors.toMap(
                                            key -> redisTemplate.getHashKeySerializer().serialize(key.getKey()),
                                            value -> redisTemplate.getHashValueSerializer().serialize(value.getValue())
                                    ))
                    );
                }
                return null;
            }
        });
    }

    /**
     * @return
     * @describe 批量保存hash对象
     * @Param: dataMap  key为键名，value为hash数据
     * @Param: expireTime  过期时间,单位分钟
     */
    public static void saveObjHashList(Map<String, Object> dataMap, Long expireTime) {
        redisTemplate.executePipelined(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                for (Map.Entry<String, Object> data : dataMap.entrySet()) {
                    connection.hMSet(
                            redisTemplate.getKeySerializer().serialize(data.getKey()),
                            BeanUtil.beanToMap(data.getValue(), false, true)
                                    .entrySet().stream().collect(Collectors.toMap(
                                            key -> redisTemplate.getHashKeySerializer().serialize(key.getKey()),
                                            value -> redisTemplate.getHashValueSerializer().serialize(value.getValue())
                                    ))
                    );
                    connection.expire(redisTemplate.getKeySerializer().serialize(data.getKey()),
                            TimeUnit.MINUTES.toSeconds(expireTime));
                }
                return null;
            }
        });
    }

    /**
     * @describe 往list集合右推数据
     * @Param: key 键名
     * @Param: value 键值
     * @return
     */
    public static Long rightPushListValue(String key, String... value) {
        return redisTemplate.opsForList().rightPushAll(key, value);
    }

    /**
     * @describe 获取list集合中数据
     * @Param: key 键名
     * @return
     */
    public static List<String> getList(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * @describe 获取list集合中数据
     * @Param: key 键名
     * @return
     */
    public static <T> List<T> getList(String key,Class<T> classType) {
        return CollUtil.map(getList(key),
                da -> JSONObject.parseObject(da.toString(),classType),
                true);
    }

    /**
     * @describe 获取list集合中某元素后的数据
     * @Param: key 键名
     * @Param: value 检索值
     * @Param: limit 获取元素个数
     * @return
     */
    public static <T> List<T> getListAfterData(String key, T value, Long limit) {
        Long index = redisTemplate.opsForList().indexOf(key, value instanceof String ? value : JSONObject.toJSONString(value));
        if (ObjectUtil.isNull(index)) {
            return null;
        }
        List dataList = redisTemplate.opsForList().range(key, index + 1, index + 1 + limit);
        if (value instanceof String){
            return dataList;
        }else {
            return (List<T>)CollUtil.map(dataList, da -> JSONObject.parseObject(da.toString(), value.getClass()), true);
        }
    }

    /**
     * @describe 获取list集合中某元素前的数据
     * @Param: key 键名
     * @Param: value 检索值
     * @Param: limit 获取元素个数
     * @return
     */
    public static <T> List<T> getListLeftData(String key, T value, Long limit) {
        Long index = redisTemplate.opsForList().indexOf(key, value instanceof String ? value : JSONObject.toJSONString(value));
        if (ObjectUtil.isNull(index) || index.equals(0L)) {
            return null;
        }
        List dataList = redisTemplate.opsForList().range(key,
                (index - 1 - limit) >= 0 ? (index - 1 - limit) : 0,
                (index - 1) >= 0 ? index - 1 : 0);
        if (value instanceof String){
            return dataList;
        }else {
            return (List<T>)CollUtil.map(dataList, da -> JSONObject.parseObject(da.toString(),value.getClass()), true);
        }
    }

    /**
     * @describe 检查键是否存在
     * @Param: key 键名
     * @return
     */
    public static boolean exist(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * @return
     * @describe 缓存键名的过期时间
     * @Param: cacheKey 键名
     */
    public static Long getExpire(String cacheKey, TimeUnit timeUnit) {
        return redisTemplate.getExpire(cacheKey, timeUnit);
    }

    /**
     * @return
     * @describe 缓存键名的过期时间
     * @Param: cacheKey 键名
     */
    public static Long getExpire(String cacheKey) {
        return redisTemplate.getExpire(cacheKey);
    }

    /**
     * @return
     * @describe 设置缓存键名的过期时间
     * @Param: cacheKey 键名
     * @Param: timeout 过期时间 单位分钟
     */
    public static boolean setExpire(String cacheKey, Long timeout) {
        return redisTemplate.expire(cacheKey, timeout, TimeUnit.MINUTES);
    }

    /**
     * @return
     * @describe 设置缓存键名的过期时间
     * @Param: cacheKey 键名
     * @Param: timeout 过期时间
     * @Param: unit 时间单位
     */
    public static boolean setExpire(String cacheKey, Long timeout,TimeUnit unit) {
        return redisTemplate.expire(cacheKey, timeout, unit);
    }

}
