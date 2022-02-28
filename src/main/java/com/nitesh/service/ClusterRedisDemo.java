package com.nitesh.service;

import io.lettuce.core.ReadFrom;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.resource.DefaultClientResources;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.time.Duration;

public class ClusterRedisDemo {

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
class Operation {
    private final StatefulRedisClusterConnection<byte[], byte[]> redisLabsConnect;

    Operation(StatefulRedisClusterConnection<byte[], byte[]> redisLabsConnect) {
        this.redisLabsConnect = redisLabsConnect;
    }

    public void putRandom() {
        RedisAdvancedClusterCommands<byte[], byte[]> redisLabsCommands = redisLabsConnect.sync();
        String key = RandomStringUtils.randomAlphabetic(8);
        String val = RandomStringUtils.randomAlphabetic(5);
        redisLabsCommands.setex(key.getBytes(),6000000, val.getBytes());
    }
}

@RestController
@RequestMapping("/cluster")
@Slf4j
class Controller
{
    @Autowired
    private Operation operation;

    @GetMapping
    public ResponseEntity<?> createCustomer() {
        try{
            operation.putRandom();
            return ResponseEntity.ok("Created");
        } catch (Exception ex) {
            log.error("Error Recorded!! ", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
        }
    }
}
