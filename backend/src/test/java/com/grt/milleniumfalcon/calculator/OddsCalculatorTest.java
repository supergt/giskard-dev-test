package com.grt.milleniumfalcon.calculator;

import com.grt.milleniumfalcon.dto.Config;
import com.grt.milleniumfalcon.dto.OddsCalculationResult;
import com.grt.milleniumfalcon.dto.PlanetEnum;
import com.grt.milleniumfalcon.dto.StolenPlans;
import com.grt.milleniumfalcon.dto.TargetPlanetTravelTime;
import com.grt.milleniumfalcon.helper.ClassPathFileLoader;
import com.grt.milleniumfalcon.model.DynamicSqliteDataSource;
import com.grt.milleniumfalcon.model.Route;
import com.grt.milleniumfalcon.model.RouteCompositeId;
import com.grt.milleniumfalcon.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.grt.milleniumfalcon.dto.PlanetEnum.Dagobah;
import static com.grt.milleniumfalcon.dto.PlanetEnum.Endor;
import static com.grt.milleniumfalcon.dto.PlanetEnum.Hoth;
import static com.grt.milleniumfalcon.dto.PlanetEnum.Tatooine;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "spring.shell.interactive.enabled=false")
class OddsCalculatorTest {
    @InjectMocks
    OddsCalculator oddsCalculator;

    // region Mocks
    @Mock
    RouteRepository routeRepository;

    @Mock
    DynamicSqliteDataSource dataSource;

    @Mock
    ClassPathFileLoader classPathFileLoader;

    @BeforeEach
    void setUp() throws IOException {
        Config mockConfig = Config.builder()
                .autonomy(6)
                .departure(Tatooine.name())
                .arrival(Endor.name())
                .routesDb("universe.db")
                .build();
        doReturn(mockConfig).when(classPathFileLoader).loadFile(any(), eq(Config.class));
    }
    // region calculate
    private static StolenPlans getNominalStolenPlans() {
        return StolenPlans.builder()
                .countdown(4)
                .bountyHunters(List.of(
                        StolenPlans.BountyHunter.builder().planet(Hoth.name()).day(5).build(),
                        StolenPlans.BountyHunter.builder().planet(Endor.name()).day(4).build()
                ))
                .build();
    }

    private static List<Route> getRoutes() {
        return List.of(
                Route.builder().id(RouteCompositeId.builder().origin(Tatooine.name()).destination(Dagobah.name()).build()).travelTime(6).build(),
                Route.builder().id(RouteCompositeId.builder().origin(Dagobah.name()).destination(Endor.name()).build()).travelTime(4).build(),
                Route.builder().id(RouteCompositeId.builder().origin(Dagobah.name()).destination(Hoth.name()).build()).travelTime(1).build(),
                Route.builder().id(RouteCompositeId.builder().origin(Hoth.name()).destination(Endor.name()).build()).travelTime(1).build(),
                Route.builder().id(RouteCompositeId.builder().origin(Tatooine.name()).destination(Hoth.name()).build()).travelTime(6).build()
        );
    }
    // region required params
    @Test
    void calculate_stolenPlansNull_exception() {
        // Given
        StolenPlans stolenPlans = null;

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> oddsCalculator.calculateWithDefaultConfig(stolenPlans));

        // Then
        assertNotNull(exception);
    }

    @Test
    void calculate_stolenPlansEmpty_exception() {
        // Given
        StolenPlans stolenPlans = new StolenPlans();

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> oddsCalculator.calculateWithDefaultConfig(stolenPlans));

        // Then
        assertNotNull(exception);
    }

    @Test
    void calculate_countdownNegative_exception() {
        // Given
        StolenPlans stolenPlans = getNominalStolenPlans();
        stolenPlans.setCountdown(-1);

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> oddsCalculator.calculateWithDefaultConfig(stolenPlans));

        // Then
        assertNotNull(exception);
    }

    @ParameterizedTest
    @CsvSource({
            "false",
            "true",
    })
    void calculate_nullOrEmptyBountyHunterList_exception(boolean isNull) {
        // Given
        StolenPlans stolenPlans = getNominalStolenPlans();
        stolenPlans.setBountyHunters(isNull ? null : List.of());

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> oddsCalculator.calculateWithDefaultConfig(stolenPlans));

        // Then
        assertNotNull(exception);
    }

    @ParameterizedTest
    @CsvSource({
            "false",
            "true",
    })
    void calculate_bountyHunterPlanetNullOrEmpty_exception(boolean isNull) {
        // Given
        StolenPlans stolenPlans = getNominalStolenPlans();
        stolenPlans.getBountyHunters().get(0).setPlanet(isNull ? null : "");

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> oddsCalculator.calculateWithDefaultConfig(stolenPlans));

        // Then
        assertNotNull(exception);
    }

    @Test
    void calculate_bountyHunterDayNegative_exception() {
        // Given
        StolenPlans stolenPlans = getNominalStolenPlans();
        stolenPlans.getBountyHunters().get(1).setDay(-10);

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> oddsCalculator.calculateWithDefaultConfig(stolenPlans));

        // Then
        assertNotNull(exception);
    }
    // endregion
    @Test
    void calculate_example_1() throws IOException {
        // Given
        StolenPlans stolenPlans = StolenPlans.builder()
                .countdown(7)
                .bountyHunters(List.of(
                        StolenPlans.BountyHunter.builder().planet(Hoth.name()).day(6).build(),
                        StolenPlans.BountyHunter.builder().planet(Hoth.name()).day(7).build(),
                        StolenPlans.BountyHunter.builder().planet(Hoth.name()).day(8).build()
                ))
                .build();

        Config config = Config.builder()
                .autonomy(6)
                .departure(Tatooine.name())
                .arrival(Endor.name())
                .build();

        OddsCalculationResult expected = OddsCalculationResult.builder()
                .oddsPercentage(0)
                .build();

        // Mocks
        when(routeRepository.findAll()).thenReturn(getRoutes());
        doReturn(config).when(classPathFileLoader).loadFile(any(), eq(Config.class));

        // When
        OddsCalculationResult result = oddsCalculator.calculateWithDefaultConfig(stolenPlans);

        // Then
        assertEquals(expected, result);
    }

    @Test
    void calculate_example_2() throws IOException {
        // Given
        StolenPlans stolenPlans = StolenPlans.builder()
                .countdown(8)
                .bountyHunters(List.of(
                        StolenPlans.BountyHunter.builder().planet(Hoth.name()).day(6).build(),
                        StolenPlans.BountyHunter.builder().planet(Hoth.name()).day(7).build(),
                        StolenPlans.BountyHunter.builder().planet(Hoth.name()).day(8).build()
                ))
                .build();

        Config config = Config.builder()
                .autonomy(6)
                .departure(Tatooine.name())
                .arrival(Endor.name())
                .build();

        OddsCalculationResult expected = OddsCalculationResult.builder()
                .oddsPercentage(81)
                .escapePlanSteps(List.of(
                        OddsCalculationResult.EscapePlan.builder().startPlanet(Tatooine).endPlanet(Hoth).startDay(0).endDay(6).build(),
                        OddsCalculationResult.EscapePlan.builder().startPlanet(Hoth).refuel(true).startDay(6).endDay(7).build(),
                        OddsCalculationResult.EscapePlan.builder().startPlanet(Hoth).endPlanet(Endor).startDay(7).endDay(8).build()
                ))
                .build();

        // Mocks
        when(routeRepository.findAll()).thenReturn(getRoutes());
        doReturn(config).when(classPathFileLoader).loadFile(any(), eq(Config.class));

        // When
        OddsCalculationResult result = oddsCalculator.calculateWithDefaultConfig(stolenPlans);

        // Then
        assertEquals(expected, result);
    }

    @Test
    void calculate_example_3() throws IOException {
        // Given
        StolenPlans stolenPlans = StolenPlans.builder()
                .countdown(9)
                .bountyHunters(List.of(
                        StolenPlans.BountyHunter.builder().planet(Hoth.name()).day(6).build(),
                        StolenPlans.BountyHunter.builder().planet(Hoth.name()).day(7).build(),
                        StolenPlans.BountyHunter.builder().planet(Hoth.name()).day(8).build()
                ))
                .build();

        Config config = Config.builder()
                .autonomy(6)
                .departure(Tatooine.name())
                .arrival(Endor.name())
                .build();

        OddsCalculationResult expected = OddsCalculationResult.builder()
                .oddsPercentage(90)
                .escapePlanSteps(List.of(
                        OddsCalculationResult.EscapePlan.builder().startPlanet(Tatooine).endPlanet(Dagobah).startDay(0).endDay(6).build(),
                        OddsCalculationResult.EscapePlan.builder().startPlanet(Dagobah).refuel(true).startDay(6).endDay(7).build(),
                        OddsCalculationResult.EscapePlan.builder().startPlanet(Dagobah).endPlanet(Hoth).startDay(7).endDay(8).build(),
                        OddsCalculationResult.EscapePlan.builder().startPlanet(Hoth).endPlanet(Endor).startDay(8).endDay(9).build()
                ))
                .build();

        // Mocks
        when(routeRepository.findAll()).thenReturn(getRoutes());
        doReturn(config).when(classPathFileLoader).loadFile(any(), eq(Config.class));

        // When
        OddsCalculationResult result = oddsCalculator.calculateWithDefaultConfig(stolenPlans);

        // Then
        assertEquals(expected, result);
    }

    @Test
    void calculate_example_4() throws IOException {
        // Given
        StolenPlans stolenPlans = StolenPlans.builder()
                .countdown(10)
                .bountyHunters(List.of(
                        StolenPlans.BountyHunter.builder().planet(Hoth.name()).day(6).build(),
                        StolenPlans.BountyHunter.builder().planet(Hoth.name()).day(7).build(),
                        StolenPlans.BountyHunter.builder().planet(Hoth.name()).day(8).build()
                ))
                .build();

        Config config = Config.builder()
                .autonomy(6)
                .departure(Tatooine.name())
                .arrival(Endor.name())
                .build();

        OddsCalculationResult expected = OddsCalculationResult.builder()
                .oddsPercentage(100)
                .escapePlanSteps(List.of(
                        OddsCalculationResult.EscapePlan.builder().startPlanet(Tatooine).endPlanet(Dagobah).startDay(0).endDay(6).build(),
                        OddsCalculationResult.EscapePlan.builder().startPlanet(Dagobah).refuel(true).startDay(6).endDay(7).build(),
                        OddsCalculationResult.EscapePlan.builder().startPlanet(Dagobah).refuel(true).startDay(7).endDay(8).build(),
                        OddsCalculationResult.EscapePlan.builder().startPlanet(Dagobah).endPlanet(Hoth).startDay(8).endDay(9).build(),
                        OddsCalculationResult.EscapePlan.builder().startPlanet(Hoth).endPlanet(Endor).startDay(9).endDay(10).build()
                ))
                .build();

        // Mocks
        when(routeRepository.findAll()).thenReturn(getRoutes());
        doReturn(config).when(classPathFileLoader).loadFile(any(), eq(Config.class));

        // When
        OddsCalculationResult result = oddsCalculator.calculateWithDefaultConfig(stolenPlans);

        // Then
        assertEquals(expected, result);
    }


    // endregion
    // region calculateCaptureChance
    @ParameterizedTest
    @CsvSource({
            "0,0",
            "1,0.10000",
            "2,0.19000",
            "3,0.27100",
            "4,0.34390",
            "5,0.40951",
            "6,0.46856",
            "7,0.52170",
            "8,0.56953",
            "9,0.61258",
            "10,0.65132",
    })
    void calculateCaptureChance_cases(int nbDaysSamePlanet, String expectedStr) {
        // Given
        BigDecimal expected = new BigDecimal(expectedStr);

        // When
        BigDecimal result = oddsCalculator.calculateCaptureChance(nbDaysSamePlanet);

        // Then
        assertEquals(expected, result);
    }
    // endregion
    // region getDayToPlanets
    @ParameterizedTest
    @CsvSource({
            "false",
            "true",
    })
    void getDayToPlanets_emptyList_emptyMap(boolean isListNull) {
        // Given
        StolenPlans stolenPlans = StolenPlans.builder().bountyHunters(isListNull ? null : List.of()).build();
        Map<Integer, Set<PlanetEnum>> expected = Map.of();

        // When
        Map<Integer, Set<PlanetEnum>> result = oddsCalculator.getDayToPlanets(stolenPlans);

        // Then
        assertEquals(expected, result);
    }

    @Test
    void getDayToPlanets_nominal_ok() {
        // Given
        StolenPlans stolenPlans = StolenPlans.builder()
                .bountyHunters(List.of(
                        StolenPlans.BountyHunter.builder().day(1).planet(Tatooine.name()).build(),
                        StolenPlans.BountyHunter.builder().day(1).planet(Endor.name()).build(),
                        StolenPlans.BountyHunter.builder().day(2).planet(Endor.name()).build(),
                        StolenPlans.BountyHunter.builder().day(5).planet(Hoth.name()).build()
                ))
                .build();
        Map<Integer, Set<PlanetEnum>> expected = Map.of(
                1, Set.of(Tatooine, Endor),
                2, Set.of(Endor),
                5, Set.of(Hoth)
        );

        // When
        Map<Integer, Set<PlanetEnum>> result = oddsCalculator.getDayToPlanets(stolenPlans);

        // Then
        assertEquals(expected, result);
    }
    // endregion
    // region getDayToPlanet
    @Test
    void getDayToPlanet_emptyList_emptyMap() {
        // Given
        List<OddsCalculationResult.EscapePlan> escapePlans = List.of();
        Map<Integer, PlanetEnum> expected = Map.of();

        // When
        Map<Integer, PlanetEnum> result = oddsCalculator.getDayToPlanet(escapePlans);

        // Then
        assertEquals(expected, result);
    }

    @Test
    void getDayToPlanet_nominal_ok() {
        // Given
        List<OddsCalculationResult.EscapePlan> escapePlans = List.of(
                OddsCalculationResult.EscapePlan.builder().startPlanet(Tatooine).endPlanet(Dagobah).startDay(0).endDay(6).build(),
                OddsCalculationResult.EscapePlan.builder().startPlanet(Dagobah).refuel(true).startDay(6).endDay(7).build(),
                OddsCalculationResult.EscapePlan.builder().startPlanet(Dagobah).refuel(true).startDay(7).endDay(8).build(),
                OddsCalculationResult.EscapePlan.builder().startPlanet(Dagobah).endPlanet(Hoth).startDay(8).endDay(9).build(),
                OddsCalculationResult.EscapePlan.builder().startPlanet(Hoth).endPlanet(Endor).startDay(9).endDay(10).build()
        );
        Map<Integer, PlanetEnum> expected = Map.of(
                6, Dagobah,
                7, Dagobah,
                8, Dagobah,
                9, Hoth,
                10, Endor
        );

        // When
        Map<Integer, PlanetEnum> result = oddsCalculator.getDayToPlanet(escapePlans);

        // Then
        assertEquals(expected, result);
    }
    // endregion
    // region getCaptureChance
    @ParameterizedTest
    @CsvSource({
            "false",
            "true",
    })
    void getCaptureChance_emptyList_null(boolean isListNull) {
        // 3 days on the same planet as bounty hunters
        List<OddsCalculationResult.EscapePlan> escapePlans = isListNull ? null : List.of();
        StolenPlans stolenPlans = StolenPlans.builder()
                .bountyHunters(List.of(
                        StolenPlans.BountyHunter.builder().day(6).planet(Tatooine.name()).build(),
                        StolenPlans.BountyHunter.builder().day(6).planet(Dagobah.name()).build(),
                        StolenPlans.BountyHunter.builder().day(8).planet(Dagobah.name()).build(),
                        StolenPlans.BountyHunter.builder().day(9).planet(Hoth.name()).build()
                ))
                .build();

        BigDecimal expected = null;

        BigDecimal result = oddsCalculator.getCaptureChance(escapePlans, stolenPlans);

        assertEquals(expected, result);
    }

    @Test
    void getCaptureChance_nominal_ok() {
        // 3 days on the same planet as bounty hunters
        List<OddsCalculationResult.EscapePlan> escapePlans = List.of(
                OddsCalculationResult.EscapePlan.builder().startPlanet(Tatooine).endPlanet(Dagobah).startDay(0).endDay(6).build(),
                OddsCalculationResult.EscapePlan.builder().startPlanet(Dagobah).refuel(true).startDay(6).endDay(7).build(),
                OddsCalculationResult.EscapePlan.builder().startPlanet(Dagobah).refuel(true).startDay(7).endDay(8).build(),
                OddsCalculationResult.EscapePlan.builder().startPlanet(Dagobah).endPlanet(Hoth).startDay(8).endDay(9).build(),
                OddsCalculationResult.EscapePlan.builder().startPlanet(Hoth).endPlanet(Endor).startDay(9).endDay(10).build()
        );
        StolenPlans stolenPlans = StolenPlans.builder()
                .bountyHunters(List.of(
                        StolenPlans.BountyHunter.builder().day(6).planet(Tatooine.name()).build(),
                        StolenPlans.BountyHunter.builder().day(6).planet(Dagobah.name()).build(),
                        StolenPlans.BountyHunter.builder().day(8).planet(Dagobah.name()).build(),
                        StolenPlans.BountyHunter.builder().day(9).planet(Hoth.name()).build()
                ))
                .build();

        BigDecimal expected = new BigDecimal("0.27100");

        BigDecimal result = oddsCalculator.getCaptureChance(escapePlans, stolenPlans);

        assertEquals(expected, result);
    }
    // endregion
    // region getOddsOfEscape
    @ParameterizedTest
    @CsvSource({
            ",0",
            "0.01,99",
            "0.1,90",
            "0.9,10",
            "1,0",
            "10,0",
    })
    void getOddsOfEscape_captureChanceNull_returnZero(String captureChanceStr, int expected) {
        // Given
        BigDecimal captureChance = null == captureChanceStr ? null : new BigDecimal(captureChanceStr);

        // When
        int result = oddsCalculator.getOddsOfEscape(captureChance);

        // Then
        assertEquals(expected, result);
    }
    // endregion
    // region buildEscapePlans
    private static Map<PlanetEnum, Set<TargetPlanetTravelTime>> getSimplePlanetToAllPossibleDestinations() {
        return Map.of(
                Tatooine,
                Set.of(TargetPlanetTravelTime.builder().targetPlanet(Dagobah).travelTime(6).build()),
                Dagobah,
                Set.of(
                        TargetPlanetTravelTime.builder().targetPlanet(Endor).travelTime(1).build(),
                        TargetPlanetTravelTime.builder().targetPlanet(Hoth).travelTime(1).build()
                ),
                Hoth,
                Set.of(
                        TargetPlanetTravelTime.builder().targetPlanet(Endor).travelTime(1).build()
                )

        );
    }

    private static Map<PlanetEnum, Set<TargetPlanetTravelTime>> getFullPlanetToAllPossibleDestinations() {
        return Map.of(
                Tatooine,
                Set.of(
                        TargetPlanetTravelTime.builder().targetPlanet(Dagobah).travelTime(6).build(),
                        TargetPlanetTravelTime.builder().targetPlanet(Endor).travelTime(6).build()
                ),
                Dagobah,
                Set.of(
                        TargetPlanetTravelTime.builder().targetPlanet(Endor).travelTime(4).build(),
                        TargetPlanetTravelTime.builder().targetPlanet(Hoth).travelTime(1).build()
                ),
                Hoth,
                Set.of(
                        TargetPlanetTravelTime.builder().targetPlanet(Endor).travelTime(1).build()
                )

        );
    }

    @Test
    void buildEscapePlans_atTarget_returnEmptyList() {
        // Given
        PlanetEnum currentPlanet = Tatooine;
        int currentDay = 1;
        PlanetEnum targetPlanet = Tatooine;
        int daysLeft = 0;
        int autonomyLeft = 0;
        int maxAutonomy = 6;
        Map<PlanetEnum, Set<TargetPlanetTravelTime>> planetToAllPossibleDestinations = getSimplePlanetToAllPossibleDestinations();
        List<List<OddsCalculationResult.EscapePlan>> expected = List.of();

        // When
        List<List<OddsCalculationResult.EscapePlan>> result = oddsCalculator.buildEscapePlans(currentPlanet, currentDay, targetPlanet, daysLeft, autonomyLeft, maxAutonomy, planetToAllPossibleDestinations);

        // Then
        assertEquals(expected, result);
    }

    @Test
    void buildEscapePlans_noMoreDaysLeft_returnNull() {
        // Given
        PlanetEnum currentPlanet = Tatooine;
        int currentDay = 1;
        PlanetEnum targetPlanet = Hoth;
        int daysLeft = 0;
        int autonomyLeft = 0;
        int maxAutonomy = 6;
        Map<PlanetEnum, Set<TargetPlanetTravelTime>> planetToAllPossibleDestinations = getSimplePlanetToAllPossibleDestinations();
        List<List<OddsCalculationResult.EscapePlan>> expected = null;

        // When
        List<List<OddsCalculationResult.EscapePlan>> result = oddsCalculator.buildEscapePlans(currentPlanet, currentDay, targetPlanet, daysLeft, autonomyLeft, maxAutonomy, planetToAllPossibleDestinations);

        // Then
        assertEquals(expected, result);
    }

    @Test
    void buildEscapePlans_oneStep_returnListWithOneItem() {
        // Given
        PlanetEnum currentPlanet = Dagobah;
        int currentDay = 0;
        PlanetEnum targetPlanet = Hoth;
        int daysLeft = 1;
        int autonomyLeft = 6;
        int maxAutonomy = 6;
        Map<PlanetEnum, Set<TargetPlanetTravelTime>> planetToAllPossibleDestinations = getSimplePlanetToAllPossibleDestinations();
        List<List<OddsCalculationResult.EscapePlan>> expected = List.of(
                List.of(
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Dagobah)
                                .startDay(0)
                                .endPlanet(Hoth)
                                .endDay(1)
                        .build()
                )
        );

        // When
        List<List<OddsCalculationResult.EscapePlan>> result = oddsCalculator.buildEscapePlans(currentPlanet, currentDay, targetPlanet, daysLeft, autonomyLeft, maxAutonomy, planetToAllPossibleDestinations);

        // Then
        assertEquals(expected, result);
    }

    @Test
    void buildEscapePlans_twoSteps_returnListWithOneItem() {
        // Given
        PlanetEnum currentPlanet = Tatooine;
        int currentDay = 0;
        PlanetEnum targetPlanet = Hoth;
        int daysLeft = 7;
        int autonomyLeft = 7;
        int maxAutonomy = 7;
        Map<PlanetEnum, Set<TargetPlanetTravelTime>> planetToAllPossibleDestinations = getSimplePlanetToAllPossibleDestinations();
        List<List<OddsCalculationResult.EscapePlan>> expected = List.of(
                List.of(
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Tatooine)
                                .startDay(0)
                                .endPlanet(Dagobah)
                                .endDay(6)
                                .build(),
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Dagobah)
                                .startDay(6)
                                .endPlanet(Hoth)
                                .endDay(7)
                                .build()
                )
        );

        // When
        List<List<OddsCalculationResult.EscapePlan>> result = oddsCalculator.buildEscapePlans(currentPlanet, currentDay, targetPlanet, daysLeft, autonomyLeft, maxAutonomy, planetToAllPossibleDestinations);

        // Then
        assertEquals(expected, result);
    }

    @Test
    void buildEscapePlans_twoPossibilities_returnListWithTwoItems() {
        // Given
        PlanetEnum currentPlanet = Dagobah;
        int currentDay = 0;
        PlanetEnum targetPlanet = Endor;
        int daysLeft = 2;
        int autonomyLeft = 7;
        int maxAutonomy = 7;
        Map<PlanetEnum, Set<TargetPlanetTravelTime>> planetToAllPossibleDestinations = getSimplePlanetToAllPossibleDestinations();
        Set<List<OddsCalculationResult.EscapePlan>> expected = Set.of(
                List.of(
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Dagobah)
                                .startDay(0)
                                .endPlanet(Hoth)
                                .endDay(1)
                                .build(),
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Hoth)
                                .startDay(1)
                                .endPlanet(Endor)
                                .endDay(2)
                                .build()
                ),
                List.of(
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Dagobah)
                                .startDay(0)
                                .endPlanet(Endor)
                                .endDay(1)
                                .build()
                ),
                List.of(
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Dagobah)
                                .startDay(0)
                                .endDay(1)
                                .refuel(true)
                                .build(),
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Dagobah)
                                .startDay(1)
                                .endPlanet(Endor)
                                .endDay(2)
                                .build()
                )
        );

        // When
        List<List<OddsCalculationResult.EscapePlan>> result = oddsCalculator.buildEscapePlans(currentPlanet, currentDay, targetPlanet, daysLeft, autonomyLeft, maxAutonomy, planetToAllPossibleDestinations);

        // Then
        assertEquals(expected, new HashSet<>(result));
    }

    @Test
    void buildEscapePlans_complex_case() {
        // Given
        PlanetEnum currentPlanet = Tatooine;
        int currentDay = 0;
        PlanetEnum targetPlanet = Endor;
        int daysLeft = 10;
        int autonomyLeft = 6;
        int maxAutonomy = 6;
        Map<PlanetEnum, Set<TargetPlanetTravelTime>> planetToAllPossibleDestinations = getFullPlanetToAllPossibleDestinations();
        Set<List<OddsCalculationResult.EscapePlan>> expected = Set.of(
                List.of(
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Tatooine)
                                .startDay(0)
                                .endPlanet(Endor)
                                .endDay(6)
                                .build()
                ),
                List.of(
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Tatooine)
                                .startDay(0)
                                .endDay(1)
                                .refuel(true)
                                .build(),
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Tatooine)
                                .startDay(1)
                                .endPlanet(Endor)
                                .endDay(7)
                                .build()
                ),
                List.of(
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Tatooine)
                                .startDay(0)
                                .endDay(1)
                                .refuel(true)
                                .build(),
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Tatooine)
                                .startDay(1)
                                .endDay(2)
                                .refuel(true)
                                .build(),
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Tatooine)
                                .startDay(2)
                                .endPlanet(Endor)
                                .endDay(8)
                                .build()
                ),
                List.of(
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Tatooine)
                                .startDay(0)
                                .endDay(1)
                                .refuel(true)
                                .build(),
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Tatooine)
                                .startDay(1)
                                .endDay(2)
                                .refuel(true)
                                .build(),
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Tatooine)
                                .startDay(2)
                                .endDay(3)
                                .refuel(true)
                                .build(),
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Tatooine)
                                .startDay(3)
                                .endPlanet(Endor)
                                .endDay(9)
                                .build()
                ),
                List.of(
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Tatooine)
                                .startDay(0)
                                .endDay(1)
                                .refuel(true)
                                .build(),
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Tatooine)
                                .startDay(1)
                                .endDay(2)
                                .refuel(true)
                                .build(),
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Tatooine)
                                .startDay(2)
                                .endDay(3)
                                .refuel(true)
                                .build(),
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Tatooine)
                                .startDay(3)
                                .endDay(4)
                                .refuel(true)
                                .build(),
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Tatooine)
                                .startDay(4)
                                .endPlanet(Endor)
                                .endDay(10)
                                .build()
                ),
                List.of(
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Tatooine)
                                .startDay(0)
                                .endPlanet(Dagobah)
                                .endDay(6)
                                .build(),
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Dagobah)
                                .startDay(6)
                                .endDay(7)
                                .refuel(true)
                                .build(),
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Dagobah)
                                .startDay(7)
                                .endPlanet(Hoth)
                                .endDay(8)
                                .build(),
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Hoth)
                                .startDay(8)
                                .endPlanet(Endor)
                                .endDay(9)
                                .build()
                ),
                List.of(
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Tatooine)
                                .startDay(0)
                                .endDay(1)
                                .refuel(true)
                                .build(),
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Tatooine)
                                .startDay(1)
                                .endPlanet(Dagobah)
                                .endDay(7)
                                .build(),
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Dagobah)
                                .startDay(7)
                                .endDay(8)
                                .refuel(true)
                                .build(),
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Dagobah)
                                .startDay(8)
                                .endPlanet(Hoth)
                                .endDay(9)
                                .build(),
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Hoth)
                                .startDay(9)
                                .endPlanet(Endor)
                                .endDay(10)
                                .build()
                ),
                List.of(
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Tatooine)
                                .startDay(0)
                                .endPlanet(Dagobah)
                                .endDay(6)
                                .build(),
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Dagobah)
                                .startDay(6)
                                .endDay(7)
                                .refuel(true)
                                .build(),
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Dagobah)
                                .startDay(7)
                                .endDay(8)
                                .refuel(true)
                                .build(),
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Dagobah)
                                .startDay(8)
                                .endPlanet(Hoth)
                                .endDay(9)
                                .build(),
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Hoth)
                                .startDay(9)
                                .endPlanet(Endor)
                                .endDay(10)
                                .build()
                ),
                List.of(
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Tatooine)
                                .startDay(0)
                                .endPlanet(Dagobah)
                                .endDay(6)
                                .build(),
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Dagobah)
                                .startDay(6)
                                .endDay(7)
                                .refuel(true)
                                .build(),
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Dagobah)
                                .startDay(7)
                                .endPlanet(Hoth)
                                .endDay(8)
                                .build(),
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Hoth)
                                .startDay(8)
                                .endDay(9)
                                .refuel(true)
                                .build(),
                        OddsCalculationResult.EscapePlan.builder()
                                .startPlanet(Hoth)
                                .startDay(9)
                                .endPlanet(Endor)
                                .endDay(10)
                                .build()
                )
        );

        // When
        List<List<OddsCalculationResult.EscapePlan>> result = oddsCalculator.buildEscapePlans(currentPlanet, currentDay, targetPlanet, daysLeft, autonomyLeft, maxAutonomy, planetToAllPossibleDestinations);

        // Then
        assertEquals(expected, new HashSet<>(result));
    }
    // endregion
}
