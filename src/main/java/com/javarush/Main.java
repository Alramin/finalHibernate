package com.javarush;

import com.javarush.config.HibernateUtil;
import com.javarush.dao.CityDAO;
import com.javarush.dao.CountryDAO;
import com.javarush.domain.City;
import com.javarush.domain.Country;
import com.javarush.domain.CountryLanguage;
import com.javarush.redis.CityMapper;
import com.javarush.redis.CityCountry;
import com.javarush.redis.RedisService;
import com.javarush.util.IdValidator;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

public class Main {

    private final SessionFactory sessionFactory;
    private final RedisService redisService;
    private final CityDAO cityDAO;
    private final CountryDAO countryDAO;

    public Main() {
        sessionFactory = HibernateUtil.getSessionFactory();
        cityDAO = new CityDAO(sessionFactory);
        countryDAO = new CountryDAO(sessionFactory);
        redisService = new RedisService("localhost", 6379);
    }

     static void main(String[] args) {
        Main main = new Main();

        List<City> allCities = main.fetchData();
        System.out.println("Загружено городов из БД: " + allCities.size());
        List<CityCountry> preparedData = CityMapper.toCityCountryList(allCities);
        main.redisService.pushData(preparedData);
        main.sessionFactory.getCurrentSession().close();

         List<Integer> rawIds = List.of(3, 2545, 123, 4, 189, 89, 3458, 1189, 10, 102,4080);

         List<Integer> validIds = IdValidator.filterValidIds(rawIds, allCities);
         System.out.println("Валидных ID для теста: " + validIds.size() + " из " + rawIds.size());

        long startRedis = System.currentTimeMillis();
        main.redisService.readDataByIds(validIds);
        long stopRedis = System.currentTimeMillis();
        long startMysql = System.currentTimeMillis();
        main.testMysqlData(validIds);
        long stopMysql = System.currentTimeMillis();

        System.out.printf("%s:\t%d ms\n", "Redis", (stopRedis - startRedis));
        System.out.printf("%s:\t%d ms\n", "MySQL", (stopMysql - startMysql));
        main.shutdown();
    }


    private List<City> fetchData() {
        try (Session session = sessionFactory.getCurrentSession()) {
            List<City> allCities = new ArrayList<>();
            session.beginTransaction();

            List<Country> countries = countryDAO.getAll();

            int totalCount = cityDAO.getTotalCount();
            int step = 500;
            for (int i = 0; i < totalCount; i += step) {
                allCities.addAll(cityDAO.getItems(i, step));
            }
            session.getTransaction().commit();
            return allCities;
        }
    }

    private void testMysqlData(List<Integer> ids) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            for (Integer id : ids) {
                if (id == null) continue;

                City city = cityDAO.getById(id);
                if (city != null) {
                    Set<CountryLanguage> languages = city.getCountry().getLanguages();
                } else {
                    System.out.println("MySQL: город с id=" + id + " не найден");
                }
            }
            session.getTransaction().commit();
        }
    }

    private void shutdown() {
        HibernateUtil.shutdown();
        if (nonNull(redisService)) {
            redisService.close();
        }
    }
}