package ua.edu.viti.military.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Cache Configuration.
 * Enables caching for frequently accessed data to improve performance.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @SuppressWarnings("null")
	@Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Default configuration (1 hour TTL)
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()
                        )
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer(objectMapper)
                        )
                );

        // Specific cache configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Unit Types - cache for 24 hours (rarely change)
        cacheConfigurations.put("unitTypes", defaultConfig.entryTtl(Duration.ofHours(24)));

        // Military Units - cache for 12 hours
        cacheConfigurations.put("militaryUnits", defaultConfig.entryTtl(Duration.ofHours(12)));

        // Personnel - cache for 30 minutes (frequently updated)
        cacheConfigurations.put("personnel", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Statistics - cache for 5 minutes
        cacheConfigurations.put("statistics", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
