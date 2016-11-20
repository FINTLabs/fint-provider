package no.fint.provider.eventstate;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisConfiguration {

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory jedisConFactory = new JedisConnectionFactory();
        jedisConFactory.setHostName("localhost");
        jedisConFactory.setPort(6379);
        return jedisConFactory;
    }

    @Bean
    public RedisTemplate<String, EventState> redisTemplate() {
        RedisTemplate<String, EventState> template = new RedisTemplate<String, EventState>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }

}
