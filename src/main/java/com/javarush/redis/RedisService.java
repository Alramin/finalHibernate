package com.javarush.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.redis.CityCountry;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;

import java.util.List;

import static java.util.Objects.nonNull;

public class RedisService implements AutoCloseable {

    private final RedisClient redisClient;
    private final ObjectMapper mapper;

    public RedisService(String host, int port) {
        this.redisClient = prepareRedisClient(host, port);
        this.mapper = new ObjectMapper();
    }

    public RedisService(RedisClient redisClient) {
        this.redisClient = redisClient;
        this.mapper = new ObjectMapper();
    }

    private RedisClient prepareRedisClient(String host, int port) {
        RedisClient client = RedisClient.create(RedisURI.create(host, port));
        try (StatefulRedisConnection<String, String> connection = client.connect()) {
            System.out.println("\nConnected to Redis\n");
        }
        return client;
    }

    public void pushData(List<CityCountry> data) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = connection.sync();
            for (CityCountry cityCountry : data) {
                try {
                    String json = mapper.writeValueAsString(cityCountry);
                    sync.set(String.valueOf(cityCountry.getId()), json);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void readDataByIds(List<Integer> ids) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = connection.sync();
            for (Integer id : ids) {
                if (id == null) continue;

                String value = sync.get(String.valueOf(id));
                if (value != null) {
                    try {
                        mapper.readValue(value, CityCountry.class);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Redis: город с id=" + id + " не найден");
                }
            }
        }
    }

    @Override
    public void close() {
        if (nonNull(redisClient)) {
            redisClient.shutdown();
        }
    }
}
