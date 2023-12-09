package com.grt.milleniumfalcon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TargetPlanetTravelTime {
    PlanetEnum targetPlanet;
    int travelTime;
}
