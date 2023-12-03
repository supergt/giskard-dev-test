package com.grt.milleniumfalcon.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Data
@Builder
public class DynamicSqliteDataSource extends DriverManagerDataSource {

    public DynamicSqliteDataSource() {
        super();
    }

    public DynamicSqliteDataSource(String configPath, Config config) {
        super();
        this.setDriverClassName("org.sqlite.JDBC");
        this.setUrl(configPath, config);
    }

    public void setUrl(String configPath, Config config) {
        this.setUrl("jdbc:sqlite::resource:config/" + configPath + config.getRoutesDb());
    }
}
