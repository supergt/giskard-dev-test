package com.grt.milleniumfalcon.calculator;

import com.grt.milleniumfalcon.dto.Config;
import com.grt.milleniumfalcon.dto.OddsCalculationResult;
import com.grt.milleniumfalcon.dto.PlanetEnum;
import com.grt.milleniumfalcon.dto.StolenPlans;
import com.grt.milleniumfalcon.helper.ClassPathFileLoader;
import com.grt.milleniumfalcon.model.DynamicSqliteDataSource;
import com.grt.milleniumfalcon.model.Route;
import com.grt.milleniumfalcon.repository.RouteRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

@Component
public class OddsCalculator {
    final RouteRepository routeRepository;
    final ClassPathFileLoader classPathFileLoader;
    final DataSource dataSource;
    public OddsCalculator(RouteRepository routeRepository, ClassPathFileLoader classPathFileLoader, DataSource dataSource) {
        this.routeRepository = routeRepository;
        this.classPathFileLoader = classPathFileLoader;
        this.dataSource = dataSource;
    }

    public OddsCalculationResult calculateWithDefaultConfig(@NotNull StolenPlans stolenPlans) throws IOException {
        Config defaultConfig = classPathFileLoader.loadFile("millennium-falcon.json", Config.class);
        return calculate(stolenPlans, "", defaultConfig);
    }

    public OddsCalculationResult calculate(@NotNull StolenPlans stolenPlans, @NotNull String configFolderPath, @NotNull Config overrideConfig) {
        List<Route> routes = loadRoutesWithCustomConfig(configFolderPath, overrideConfig);

        return OddsCalculationResult.builder()
                .oddsPercentage(5)
                .build();
    }

    protected synchronized List<Route> loadRoutesWithCustomConfig(String configFolderPath, Config overrideConfig) {
        ((DynamicSqliteDataSource) this.dataSource).setUrl(configFolderPath, overrideConfig);
        return routeRepository.findAll();
    }

    protected BigDecimal getCaptureChance(List<OddsCalculationResult.EscapePlan> escapePlans, StolenPlans stolenPlans) {
        final Map<Integer, Set<PlanetEnum>> dayToPlanetsBountyHuntersAreIn = getDayToPlanets(stolenPlans);

        final Map<Integer, PlanetEnum> dayToPlanetForEscapePlan = getDayToPlanet(escapePlans);

        // Every day that the escape plan planet is included in the bounty hunter planets, it adds to the capture chance
        int nbDaysSamePlanet = dayToPlanetForEscapePlan.entrySet()
                .stream()
                .mapToInt(entry -> {
                    Integer day = entry.getKey();
                    PlanetEnum planet = entry.getValue();

                    Set<PlanetEnum> bountyHunterPlanets = dayToPlanetsBountyHuntersAreIn.get(day);
                    if (null != bountyHunterPlanets && bountyHunterPlanets.contains(planet)) {
                        return 1;
                    }

                    return 0;
                })
                .sum();

        return calculateCaptureChance(nbDaysSamePlanet);
    }

    protected BigDecimal calculateCaptureChance(int nbDaysSamePlanet) {
        return IntStream.range(0, nbDaysSamePlanet)
                .mapToObj(i -> new BigDecimal("9").pow(i).divide(BigDecimal.TEN.pow(i + 1), 5, RoundingMode.HALF_UP))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    protected Map<Integer, Set<PlanetEnum>> getDayToPlanets(@NotNull StolenPlans stolenPlans) {
        Map<Integer, Set<PlanetEnum>> dayToPlanets = new HashMap<>();

        if (!CollectionUtils.isEmpty(stolenPlans.getBountyHunters())) {
            for (StolenPlans.BountyHunter bountyHunter : stolenPlans.getBountyHunters()) {
                dayToPlanets.putIfAbsent(bountyHunter.getDay(), new HashSet<>());
                dayToPlanets.get(bountyHunter.getDay()).add(PlanetEnum.valueOf(bountyHunter.getPlanet()));
            }
        }
        return dayToPlanets;
    }

    protected Map<Integer, PlanetEnum> getDayToPlanet(@NotNull List<OddsCalculationResult.EscapePlan> escapePlans) {
        Map<Integer, PlanetEnum> dayToPlanet = new HashMap<>();

        for (OddsCalculationResult.EscapePlan escapePlan : escapePlans) {
            // The way the escape plans are organized, each step is a leg of the journey
            // Even if the plans include 2 consecutive refuel steps, they will not be combined into one
            // So we can just take the endDay as the "day" of the step
            if (null == escapePlan.getEndPlanet()) {
                // Refueling
                dayToPlanet.put(escapePlan.getEndDay(), escapePlan.getStartPlanet());
            } else {
                dayToPlanet.put(escapePlan.getEndDay(), escapePlan.getEndPlanet());
            }
        }

        return dayToPlanet;
    }
}
