package com.grt.milleniumfalcon.calculator;

import com.grt.milleniumfalcon.dto.Config;
import com.grt.milleniumfalcon.dto.OddsCalculationResult;
import com.grt.milleniumfalcon.dto.PlanetEnum;
import com.grt.milleniumfalcon.dto.StolenPlans;
import com.grt.milleniumfalcon.dto.TargetPlanetTravelTime;
import com.grt.milleniumfalcon.helper.ClassPathFileLoader;
import com.grt.milleniumfalcon.model.DynamicSqliteDataSource;
import com.grt.milleniumfalcon.model.Route;
import com.grt.milleniumfalcon.repository.RouteRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
        validateStolenPlans(stolenPlans);
        validateConfig(overrideConfig);

        List<Route> routes = loadRoutesWithCustomConfig(configFolderPath, overrideConfig);

        if (overrideConfig.getArrival().equals(overrideConfig.getDeparture())) {
            return OddsCalculationResult.builder()
                    .oddsPercentage(100)
                    .build();
        }

        List<OddsCalculationResult.EscapePlan> optimalEscapePlans = buildOptimalEscapePlans(stolenPlans, routes, overrideConfig);

        BigDecimal captureChance = getCaptureChance(optimalEscapePlans, stolenPlans);

        return OddsCalculationResult.builder()
                .escapePlanSteps(optimalEscapePlans)
                .oddsPercentage(getOddsOfEscape(captureChance))
                .build();
    }

    // region validators
    private void validateConfig(Config overrideConfig) {
        if (null == overrideConfig
        || !StringUtils.hasText(overrideConfig.getArrival())
        || !StringUtils.hasText(overrideConfig.getDeparture())
        || overrideConfig.getAutonomy() < 0
        || !StringUtils.hasText(overrideConfig.getRoutesDb())) {
            throw new IllegalArgumentException("Invalid config");
        }
    }

    private void validateStolenPlans(StolenPlans stolenPlans) {
        if (null == stolenPlans
                || stolenPlans.getCountdown() < 0
                || CollectionUtils.isEmpty(stolenPlans.getBountyHunters())
                || stolenPlans.getBountyHunters().stream().anyMatch(bountyHunter -> bountyHunter.getDay() < 0 || !StringUtils.hasText(bountyHunter.getPlanet()))) {
            throw new IllegalArgumentException("Invalid stolen plans");
        }
    }
    // endregion

    protected @NotNull List<OddsCalculationResult.EscapePlan> buildOptimalEscapePlans(@NotNull StolenPlans stolenPlans, @NotNull List<Route> routes, @NotNull Config config) {
        Map<PlanetEnum, Set<TargetPlanetTravelTime>> planetToAllPossibleDestinations = getPlanetToAllPossibleDestinations(routes);

        PlanetEnum startPlanet = PlanetEnum.valueOf(config.getDeparture());
        PlanetEnum targetPlanet = PlanetEnum.valueOf(config.getArrival());
        int totalDays = stolenPlans.getCountdown();

        List<List<OddsCalculationResult.EscapePlan>> allEscapePlans = buildEscapePlans(startPlanet, 0, targetPlanet, totalDays, config.getAutonomy(), config.getAutonomy(), planetToAllPossibleDestinations);

        if (CollectionUtils.isEmpty(allEscapePlans)) {
            return List.of();
        }

        Comparator<List<OddsCalculationResult.EscapePlan>> escapePlanComparator1 = Comparator.comparing(escapePlans -> getCaptureChance(escapePlans, stolenPlans));
        Comparator<List<OddsCalculationResult.EscapePlan>> escapePlanComparator2 = Comparator.comparing(List::size);

        return allEscapePlans.stream()
                .min(escapePlanComparator1.thenComparing(escapePlanComparator2))
                .orElse(List.of());
    }

    protected Map<PlanetEnum, Set<TargetPlanetTravelTime>> getPlanetToAllPossibleDestinations(@NotNull List<Route> routes) {
        Map<PlanetEnum, Set<TargetPlanetTravelTime>> planetToAllPossibleDestinations = new EnumMap<>(PlanetEnum.class);

        for (Route route : routes) {
            PlanetEnum origin = PlanetEnum.valueOf(route.getId().getOrigin());
            planetToAllPossibleDestinations.putIfAbsent(origin, new HashSet<>());
            planetToAllPossibleDestinations.get(origin).add(TargetPlanetTravelTime.builder()
                    .targetPlanet(PlanetEnum.valueOf(route.getId().getDestination()))
                    .travelTime(route.getTravelTime())
                    .build());
        }

        return planetToAllPossibleDestinations;
    }

    protected List<List<OddsCalculationResult.EscapePlan>> buildEscapePlans(
            PlanetEnum currentPlanet,
            int currentDay,
            PlanetEnum targetPlanet,
            int daysLeft,
            int autonomyLeft,
            int maxAutonomy,
            @NotNull Map<PlanetEnum, Set<TargetPlanetTravelTime>> planetToAllPossibleDestinations) {

        List<List<OddsCalculationResult.EscapePlan>> currentEscapePlansList = new ArrayList<>();

        if (currentPlanet.equals(targetPlanet)) {
            return currentEscapePlansList;
        }

        if (daysLeft <= 0) {
            return null;
        }

        // Refuel
        List<List<OddsCalculationResult.EscapePlan>> allRefuelPlans = buildEscapePlans(currentPlanet, currentDay + 1, targetPlanet, daysLeft - 1, maxAutonomy, maxAutonomy, planetToAllPossibleDestinations);
        if (null != allRefuelPlans) {
            if (allRefuelPlans.isEmpty()) {
                allRefuelPlans.add(new ArrayList<>());
            }

            OddsCalculationResult.EscapePlan newEntry = OddsCalculationResult.EscapePlan.builder()
                    .startPlanet(currentPlanet)
                    .startDay(currentDay)
                    .endDay(currentDay + 1)
                    .refuel(true)
                    .build();

            allRefuelPlans.forEach(refuelPlan -> refuelPlan.add(0, newEntry));
            currentEscapePlansList.addAll(allRefuelPlans);
        }

        // Go to a destination
        Set<TargetPlanetTravelTime> possibleDestinations = planetToAllPossibleDestinations.get(currentPlanet);
        if (null != possibleDestinations) {
            for (TargetPlanetTravelTime possibleDestination : possibleDestinations) {
                if (possibleDestination.getTravelTime() <= autonomyLeft && possibleDestination.getTravelTime() <= daysLeft) {
                    int endDay = currentDay + possibleDestination.getTravelTime();
                    PlanetEnum endPlanet = possibleDestination.getTargetPlanet();
                    List<List<OddsCalculationResult.EscapePlan>> possibleDestinationPlans = buildEscapePlans(endPlanet,
                            endDay,
                            targetPlanet,
                            daysLeft - possibleDestination.getTravelTime(),
                            autonomyLeft - possibleDestination.getTravelTime(),
                            maxAutonomy,
                            planetToAllPossibleDestinations);

                    if (null != possibleDestinationPlans) {
                        if (possibleDestinationPlans.isEmpty()) {
                            possibleDestinationPlans.add(new ArrayList<>());
                        }

                        OddsCalculationResult.EscapePlan newEntry = OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(currentPlanet)
                                .endPlanet(endPlanet)
                                .startDay(currentDay)
                                .endDay(endDay)
                                .build();

                        possibleDestinationPlans.forEach(refuelPlan -> refuelPlan.add(0, newEntry));
                        currentEscapePlansList.addAll(possibleDestinationPlans);
                    }
                }
            }
        }

        return currentEscapePlansList.isEmpty() ? null : currentEscapePlansList;
    }

    protected int getOddsOfEscape(BigDecimal captureChance) {
        if (null == captureChance) {
            return 0;
        }

        int oddsOfEscape = BigDecimal.ONE.subtract(captureChance).multiply(new BigDecimal("100")).toBigInteger().intValue();

        return Math.max(oddsOfEscape, 0);

    }

    protected synchronized List<Route> loadRoutesWithCustomConfig(String configFolderPath, Config overrideConfig) {
        ((DynamicSqliteDataSource) this.dataSource).setUrl(configFolderPath, overrideConfig);
        return routeRepository.findAll();
    }

    protected @NotNull BigDecimal getCaptureChance(List<OddsCalculationResult.EscapePlan> escapePlans, @NotNull StolenPlans stolenPlans) {
        if (CollectionUtils.isEmpty(escapePlans)) {
            return null;
        }

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

    protected @NotNull BigDecimal calculateCaptureChance(int nbDaysSamePlanet) {
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
