package com.example.mi.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisCacheUtil {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 设置缓存
    public void setCache(String key, Object value, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    // 获取缓存
    public Object getCache(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 删除缓存
    public void deleteCache(String key) {
        redisTemplate.delete(key);
    }
}