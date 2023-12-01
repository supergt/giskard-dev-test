package com.grt.milleniumfalcon.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grt.milleniumfalcon.model.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class AppConfiguration {
    @Bean
    public Config config() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("config/millennium_falcon.json");

        return new ObjectMapper().readValue(inputStream, Config.class);
    }

    @Bean
    public DataSource dataSource() throws IOException {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.sqlite.JDBC");
        dataSource.setUrl("jdbc:sqlite::resource:config/" + config().getRoutesDb());
        return dataSource;
    }
}
