package com.javarush.service;

import com.javarush.redis.*;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands; // <-- Исправленный импорт
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisServiceTest {

    @Mock
    private RedisClient redisClient;

    @Mock
    private StatefulRedisConnection<String, String> connection;

    @Mock
    private RedisCommands<String, String> syncCommands; // <-- Исправленный тип

    private RedisService redisService;

    @BeforeEach
    void setUp() {
        lenient().when(redisClient.connect()).thenReturn(connection);
        lenient().when(connection.sync()).thenReturn(syncCommands);

        redisService = new RedisService(redisClient);
    }

    @Test
    void shouldSerializeAndPushDataToRedis() {
        CityCountry city = new CityCountry();
        city.setId(42);
        city.setName("Rotterdam");

        redisService.pushData(List.of(city));

        verify(syncCommands, times(1)).set(eq("42"), contains("\"name\":\"Rotterdam\""));
        verify(connection, times(1)).close();
    }

    @Test
    void shouldReadAndDeserializeExistingDataFromRedis() {
        String mockJson = "{\"id\":42,\"name\":\"Rotterdam\"}";
        when(syncCommands.get("42")).thenReturn(mockJson);
        when(syncCommands.get("999")).thenReturn(null);

        redisService.readDataByIds(List.of(42, 999));

        verify(syncCommands, times(1)).get("42");
        verify(syncCommands, times(1)).get("999");
        verify(connection, times(1)).close();
    }

    @Test
    void shouldShutdownRedisClientOnClose() {
        redisService.close();

        verify(redisClient, times(1)).shutdown();
    }
}