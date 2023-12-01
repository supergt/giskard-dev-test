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
}
