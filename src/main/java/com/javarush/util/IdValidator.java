package com.javarush.util;



import com.javarush.domain.City;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class IdValidator {

    public static List<Integer> filterValidIds(List<Integer> rawIds, List<City> allCities) {
        if (rawIds == null || allCities == null) {
            return List.of();
        }

        Set<Integer> existingIds = allCities.stream()
                .map(City::getId)
                .collect(Collectors.toSet());

        List<Integer> validIds = new ArrayList<>();
        for (Integer id : rawIds) {
            if (id != null && existingIds.contains(id)) {
                validIds.add(id);
            }
        }
        return validIds;
    }
}
