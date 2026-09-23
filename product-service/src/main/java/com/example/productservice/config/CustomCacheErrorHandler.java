package com.example.productservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

public class CustomCacheErrorHandler implements CacheErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomCacheErrorHandler.class);


    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        logger.warn("⚠️ [CACHE GET ERROR] Cache: '{}', Key: '{}' — Fallback về Database. Lỗi: {}",
                cache.getName(), key, exception.getMessage());
    }


    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        logger.warn("⚠️ [CACHE PUT ERROR] Cache: '{}', Key: '{}' — Dữ liệu không được cache. Lỗi: {}",
                cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        logger.warn("⚠️ [CACHE EVICT ERROR] Cache: '{}', Key: '{}' — Cache có thể chứa dữ liệu cũ. Lỗi: {}",
                cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        logger.warn(" [CACHE CLEAR ERROR] Cache: '{}' — Không thể xóa toàn bộ cache. Lỗi: {}",
                cache.getName(), exception.getMessage());
    }
}
