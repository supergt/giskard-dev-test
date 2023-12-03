package com.grt.milleniumfalcon.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Config {
    int autonomy;
    String departure;
    String arrival;
    @JsonProperty("routes_db")
    String routesDb;

    public void copy(Config newConfig) {
        this.autonomy = newConfig.autonomy;
        this.departure = newConfig.departure;
        this.arrival = newConfig.arrival;
        this.routesDb = newConfig.routesDb;
    }
}
