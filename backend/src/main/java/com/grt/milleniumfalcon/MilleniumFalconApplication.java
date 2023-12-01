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
}
