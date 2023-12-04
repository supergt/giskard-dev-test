package com.grt.milleniumfalcon.model;

import com.grt.milleniumfalcon.dto.Config;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.StringUtils;

@Data
@Builder
public class DynamicSqliteDataSource extends DriverManagerDataSource {

    public DynamicSqliteDataSource() {
        super();
    }

    public DynamicSqliteDataSource(@NotNull String configPath, @NotNull Config config) {
        super();
        this.setDriverClassName("org.sqlite.JDBC");
        this.setUrl(configPath, config);
    }

    public void setUrl(@NotNull String configPath, @NotNull Config config) {
        if (!StringUtils.hasText(config.getRoutesDb())) {
            throw new IllegalArgumentException("Missing routesDb in config");
        }
        this.setUrl("jdbc:sqlite::resource:config/" + configPath + config.getRoutesDb());
    }
}
