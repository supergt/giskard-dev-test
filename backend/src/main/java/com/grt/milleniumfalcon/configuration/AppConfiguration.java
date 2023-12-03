package com.grt.milleniumfalcon.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grt.milleniumfalcon.model.Config;
import com.grt.milleniumfalcon.model.DynamicSqliteDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class AppConfiguration {
    @Bean
    public Config config() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("config/millennium-falcon.json");

        return new ObjectMapper().readValue(inputStream, Config.class);
    }

    @Bean
    public DataSource dataSource() throws IOException {
        return new DynamicSqliteDataSource("", config());
    }
}
