package com.nitesh.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Service
public class JedisPoolConfigDemo {

}

@Configuration
@Slf4j
class JedisPoolDemo {

    private String host = "127.0.0.1";
    private Integer port = 6379;

    public JedisPoolConfig getJedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(100);
        jedisPoolConfig.setMaxIdle(100);
        jedisPoolConfig.setMinIdle(100);
        jedisPoolConfig.setTestWhileIdle(true);
        jedisPoolConfig.setMinEvictableIdleTimeMillis(60000L);
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(30000L);
        jedisPoolConfig.setNumTestsPerEvictionRun(-1);
        return jedisPoolConfig;
    }

    @Bean("jedistest")
    public JedisPool jedisPool() {
        JedisPool jedisPool = new JedisPool(getJedisPoolConfig(),
                host,
                port,
                100);

        return jedisPool;
    }
}

@RestController
@RequestMapping("/jedis")
class JedisPoolController {
    @Autowired
    @Qualifier("jedistest")
    private JedisPool jedisPool;

    @GetMapping
    public ResponseEntity<String> createCustomer() {

        String key = RandomStringUtils.randomAlphabetic(8);
        String val = RandomStringUtils.randomAlphabetic(5);
        try(Jedis jedis = jedisPool.getResource()) {
            jedis.set(key, val);
        }
        return ResponseEntity.ok("Created");
    }
}
