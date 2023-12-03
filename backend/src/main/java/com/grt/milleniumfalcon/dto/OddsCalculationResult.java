package com.grt.milleniumfalcon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OddsCalculationResult {
    int oddsPercentage;
    List<EscapePlan> escapePlanSteps;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class EscapePlan {
        String startPlanet;
        String endPlanet;
        int startDay;
        int endDay;
        boolean refuel;
    }
}
