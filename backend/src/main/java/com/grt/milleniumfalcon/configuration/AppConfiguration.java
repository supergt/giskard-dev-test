package com.grt.milleniumfalcon.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grt.milleniumfalcon.dto.Config;
import com.grt.milleniumfalcon.model.DynamicSqliteDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class AppConfiguration {
    @Bean
    public DataSource dataSource() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("config/millennium-falcon.json");

        Config config = new ObjectMapper().readValue(inputStream, Config.class);

        return new DynamicSqliteDataSource("", config);
    }
}
