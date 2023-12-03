package com.grt.milleniumfalcon.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class ClassPathFileLoader {
    public <T> T loadFile(String path, Class<T> fileType) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("config/" + path);

        return new ObjectMapper().readValue(inputStream, fileType);
    }
}
