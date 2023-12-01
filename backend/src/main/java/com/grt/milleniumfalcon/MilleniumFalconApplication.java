package com.grt.milleniumfalcon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grt.milleniumfalcon.model.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class MilleniumFalconApplication {

	public static void main(String[] args) {
		SpringApplication.run(MilleniumFalconApplication.class, args);
	}

	@Bean
	public DataSource dataSource() throws IOException {
		// The class loader that loaded the class
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream("config/millennium_falcon.json");

		Config config = new ObjectMapper().readValue(inputStream, Config.class);

		final DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.sqlite.JDBC");
		dataSource.setUrl("jdbc:sqlite::resource:config/" + config.getRoutesDb());
		return dataSource;
	}
}
