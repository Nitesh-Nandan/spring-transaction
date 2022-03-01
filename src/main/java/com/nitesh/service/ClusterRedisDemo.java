package com.nitesh.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.resource.DefaultClientResources;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceExceptionConverter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public class ClusterRedisDemo {

}

@Data
@AllArgsConstructor
class Person {
    private String name;
    private String phone;
}

@Configuration
@Data
class ClusterRedisConfiguration {

    private Long commandTimeout = 20000L;

    private Boolean isMasterPreferred = false;

    private String redisLabsUri = "";
    private String redisLabsPassword = "";


    @Bean("redisLabsClient")
    public RedisClusterClient redisLabsClusterClient() {
        RedisURI redisURI = RedisURI.create(getRedisLabsUri());
        redisURI.setPassword(redisLabsPassword);
        RedisClusterClient clusterClient = RedisClusterClient.create(DefaultClientResources.builder().build(), redisURI);
        ClusterTopologyRefreshOptions topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                .enableAllAdaptiveRefreshTriggers()
                .enablePeriodicRefresh(true)
                .build();
        clusterClient.setOptions(ClusterClientOptions.builder()
                .topologyRefreshOptions(topologyRefreshOptions)
                .build());
        clusterClient.setDefaultTimeout(Duration.ofMillis(getCommandTimeout()));
        return clusterClient;
    }

    @Bean(name = "redisLabsConnect")
    public StatefulRedisClusterConnection<byte[], byte[]> redisLabsPool(@Qualifier("redisLabsClient") RedisClusterClient redisClusterClient) {
        StatefulRedisClusterConnection<byte[], byte[]> connect = redisClusterClient.connect(ByteArrayCodec.INSTANCE);
        if (getIsMasterPreferred()) {
            connect.setReadFrom(ReadFrom.MASTER_PREFERRED);
        } else {
            connect.setReadFrom(ReadFrom.ANY);
        }
        connect.setTimeout(Duration.ofMillis(getCommandTimeout()));
        return connect;
    }
}

@Service
@Slf4j
class ClusterRedisOperationManager {

    private final LettuceExceptionConverter lettuceExceptionConverter = new LettuceExceptionConverter();

    private final StatefulRedisClusterConnection<byte[], byte[]> redisLabsConnect;

    public ClusterRedisOperationManager(
            @Qualifier("redisLabsConnect") StatefulRedisClusterConnection<byte[], byte[]> redisLabsConnect) {
        this.redisLabsConnect = redisLabsConnect;
    }

    public void set(String key, String value, Duration ttl) {
        try {
            RedisAdvancedClusterCommands<byte[], byte[]> redisLabsCommands = redisLabsConnect.sync();
            redisLabsCommands.setex(key.getBytes(), ttl.getSeconds(), value.getBytes());
        } catch (Exception e) {
            log.error("Exception in cluster redis set-ex", e);
            throw Objects.requireNonNull(lettuceExceptionConverter.convert(e));
        }
    }

    public Object get(String key) {
        try {
            RedisAdvancedClusterCommands<byte[], byte[]> redisLabsCommands = redisLabsConnect.sync();
            byte[] bytes = redisLabsCommands.get(key.getBytes());
            return convertByteToString(bytes);
        } catch (Exception e) {
            log.error("Exception in cluster redis get", e);
            throw Objects.requireNonNull(lettuceExceptionConverter.convert(e));
        }
    }

    public void multiPut(Map<String, String> keyValues) {
        try {
            RedisAdvancedClusterCommands<byte[], byte[]> redisLabsCommands = redisLabsConnect.sync();
            for (Map.Entry<String, String> entry : keyValues.entrySet()) {
                redisLabsCommands.set(entry.getKey().getBytes(), entry.getValue().getBytes());
            }
        } catch (Exception e) {
            log.error("Exception in cluster redis multi put", e);
            throw Objects.requireNonNull(lettuceExceptionConverter.convert(e));
        }
    }

    public void delete(String key) {
        try {
            RedisAdvancedClusterCommands<byte[], byte[]> redisLabsSync = redisLabsConnect.sync();
            redisLabsSync.del(key.getBytes());
        } catch (Exception e) {
            log.error("Exception in cluster redis delete", e);
            throw Objects.requireNonNull(lettuceExceptionConverter.convert(e));
        }
    }

    public Long increment(String key) {
        try {
            RedisAdvancedClusterCommands<byte[], byte[]> redisLabsSync = redisLabsConnect.sync();
            return redisLabsSync.incr(key.getBytes());
        } catch (Exception e) {
            log.error("Exception in cluster redis Increment", e);
            throw Objects.requireNonNull(lettuceExceptionConverter.convert(e));
        }
    }

    public void set(String key, String value) {
        try {
            RedisAdvancedClusterCommands<byte[], byte[]> redisLabsCommands = redisLabsConnect.sync();
            redisLabsCommands.set(key.getBytes(), value.getBytes());
        } catch (Exception e) {
            log.error("Exception in cluster redis set", e);
            throw Objects.requireNonNull(lettuceExceptionConverter.convert(e));
        }
    }

    public boolean setIfNotExist(String key, byte[] value, int ttlInSec) {
        try {
            RedisAdvancedClusterCommands<byte[], byte[]> redisLabsCommands = redisLabsConnect.sync();
            boolean isSet = redisLabsCommands.setnx(key.getBytes(), value);
            if (isSet) {
                redisLabsCommands.expire(key.getBytes(), ttlInSec);
            }
            return isSet;
        } catch (Exception e) {
            log.error("Exception in cluster redis setIfNotExist", e);
            throw Objects.requireNonNull(lettuceExceptionConverter.convert(e));
        }
    }

    private String convertByteToString(byte[] bytes) {
        return new String(bytes);
    }

}


@Service
@Slf4j
class ClusterOperation {
    private final ClusterRedisOperationManager clusterRedisOperationManager;

    private final ObjectMapper objectMapper;

    ClusterOperation(ClusterRedisOperationManager clusterRedisOperationManager, ObjectMapper objectMapper) {
        this.clusterRedisOperationManager = clusterRedisOperationManager;
        this.objectMapper = objectMapper;
    }

    public void process() throws JsonProcessingException {

        String key = RandomStringUtils.randomAlphabetic(8);
        String val = RandomStringUtils.randomAlphabetic(5);

        clusterRedisOperationManager.set(key, val);

        key = RandomStringUtils.randomAlphabetic(8);
        val = RandomStringUtils.randomAlphabetic(5);

        clusterRedisOperationManager.set(key, val);

        key = RandomStringUtils.randomAlphabetic(8);
        val = RandomStringUtils.randomAlphabetic(5);

        clusterRedisOperationManager.set(key, val);

        key = RandomStringUtils.randomAlphabetic(8);
        val = RandomStringUtils.randomAlphabetic(5);

        clusterRedisOperationManager.set(key, val);


        int num = (int) (10 + Math.random() * 1000);

        if (num % 53 < 50) {
            clusterRedisOperationManager.get(key);
        }
    }
}


@RestController
@RequestMapping("/cluster")
@Slf4j
class Controller {
    @Autowired
    private ClusterOperation clusterOperation;

    @GetMapping
    public ResponseEntity<?> createCustomer() {
        try {
            clusterOperation.process();
            return ResponseEntity.ok("Created");
        } catch (Exception ex) {
            log.error("Error Recorded!! ", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
        }
    }
}
