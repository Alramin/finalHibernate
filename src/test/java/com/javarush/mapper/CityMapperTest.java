package com.javarush.mapper;



import com.javarush.domain.City;
import com.javarush.domain.Continent;
import com.javarush.domain.Country;
import com.javarush.domain.CountryLanguage;
import com.javarush.redis.CityCountry;
import com.javarush.redis.CityMapper;
import com.javarush.redis.Language;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CityMapperTest {

    @Test
    void shouldCorrectlyMapCityToCityCountry() {
        // 1. Подготовка тестовых данных (Given)
        CountryLanguage lang = new CountryLanguage();
        lang.setLanguage("Dutch");
        lang.setOfficial(true);
        lang.setPercentage(BigDecimal.valueOf(95.6));

        Country country = new Country();
        country.setCode("NLD");
        country.setAlternativeCode("NL");
        country.setName("Netherlands");
        country.setContinent(Continent.EUROPE);
        country.setRegion("Western Europe");
        country.setSurfaceArea(BigDecimal.valueOf(41526.0));
        country.setPopulation(15864000);
        country.setLanguages(Set.of(lang));

        City city = new City();
        city.setId(5);
        city.setName("Amsterdam");
        city.setDistrict("Noord-Holland");
        city.setPopulation(731200);
        city.setCountry(country);

        // 2. Выполнение тестируемого метода (When)
        List<CityCountry> result = CityMapper.toCityCountryList(List.of(city));

        // 3. Проверка результатов (Then)
        assertEquals(1, result.size());
        CityCountry dto = result.get(0);

        assertEquals(5, dto.getId());
        assertEquals("Amsterdam", dto.getName());
        assertEquals("Noord-Holland", dto.getDistrict());
        assertEquals(731200, dto.getPopulation());
        assertEquals("NLD", dto.getCountryCode());
        assertEquals("Netherlands", dto.getCountryName());
        assertEquals(Continent.EUROPE, dto.getContinent());

        assertNotNull(dto.getLanguages());
        assertEquals(1, dto.getLanguages().size());
        Language mappedLang = dto.getLanguages().iterator().next();
        assertEquals("Dutch", mappedLang.getLanguage());
        assertTrue(mappedLang.getOfficial());
        assertEquals(BigDecimal.valueOf(95.6), mappedLang.getPercentage());
    }
}