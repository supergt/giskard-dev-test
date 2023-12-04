package com.grt.milleniumfalcon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

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
        PlanetEnum startPlanet;
        PlanetEnum endPlanet;
        int startDay;
        int endDay;
        boolean refuel;
    }

    protected int getLastEndDay() {
        int lastEndDay = 0;
        if (!CollectionUtils.isEmpty(this.escapePlanSteps)) {
            lastEndDay = this.escapePlanSteps.get(this.escapePlanSteps.size() - 1).endDay;
        }

        return lastEndDay;
    }

    public void addTravelStep(PlanetEnum startPlanet, PlanetEnum endPlanet, int travelTime) {
        int startDay = getLastEndDay();

        int endDay = startDay + travelTime;
        this.escapePlanSteps.add(EscapePlan.builder()
                .startDay(startDay)
                .endDay(endDay)
                .startPlanet(startPlanet)
                .endPlanet(endPlanet)
                .build());
    }

    public void addRefuelStep(PlanetEnum planet) {
        int startDay = getLastEndDay();

        int endDay = startDay + 1;
        this.escapePlanSteps.add(EscapePlan.builder()
                .startDay(startDay)
                .endDay(endDay)
                .startPlanet(planet)
                .refuel(true)
                .build());
    }
}
