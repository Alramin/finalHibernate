package com.javarush.util;



import com.javarush.domain.City;
import com.javarush.util.IdValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdValidatorTest {

    private List<City> mockCities;

    @BeforeEach
    void setUp() {
        City city1 = new City();
        city1.setId(10);

        City city2 = new City();
        city2.setId(25);

        City city3 = new City();
        city3.setId(100);

        mockCities = List.of(city1, city2, city3);
    }

    @Test
    void shouldFilterOutNonExistentAndNullIds() {
        List<Integer> rawIds = Arrays.asList(10, 999, null, 25, -5, 100);

        List<Integer> validIds = IdValidator.filterValidIds(rawIds, mockCities);

        assertEquals(3, validIds.size());
        assertEquals(List.of(10, 25, 100), validIds);
    }

    @Test
    void shouldReturnEmptyListWhenNoIdsMatch() {
        List<Integer> rawIds = List.of(1, 2, 3);

        List<Integer> validIds = IdValidator.filterValidIds(rawIds, mockCities);

        assertTrue(validIds.isEmpty());
    }

    @Test
    void shouldHandleNullInputsSafely() {
        assertTrue(IdValidator.filterValidIds(null, mockCities).isEmpty());
        assertTrue(IdValidator.filterValidIds(List.of(10), null).isEmpty());
    }
}
