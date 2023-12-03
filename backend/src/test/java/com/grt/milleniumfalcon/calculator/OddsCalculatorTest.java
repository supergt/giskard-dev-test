package com.grt.milleniumfalcon.calculator;

import com.grt.milleniumfalcon.dto.OddsCalculationResult;
import com.grt.milleniumfalcon.dto.StolenPlans;
import com.grt.milleniumfalcon.model.Config;
import com.grt.milleniumfalcon.model.Route;
import com.grt.milleniumfalcon.model.RouteCompositeId;
import com.grt.milleniumfalcon.repository.RouteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

import static com.grt.milleniumfalcon.PlanetEnum.Dagobah;
import static com.grt.milleniumfalcon.PlanetEnum.Endor;
import static com.grt.milleniumfalcon.PlanetEnum.Hoth;
import static com.grt.milleniumfalcon.PlanetEnum.Tatooine;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
public class OddsCalculatorTest {
    @Mock
    RouteRepository routeRepository;

    @Mock
    Config config;

    @InjectMocks
    OddsCalculator oddsCalculator;

    // region calculate
    private static StolenPlans getNominalStolenPlans() {
        return StolenPlans.builder()
                .countdown(4)
                .bountyHunters(List.of(
                        StolenPlans.BountyHunter.builder().planet("Hoth").day(5).build(),
                        StolenPlans.BountyHunter.builder().planet("Endor").day(4).build()
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
        when(config.getAutonomy()).thenReturn(config.getAutonomy());
        when(config.getDeparture()).thenReturn(config.getDeparture());
        when(config.getArrival()).thenReturn(config.getArrival());

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
                        OddsCalculationResult.EscapePlan.builder().startPlanet(Tatooine.name()).endPlanet(Hoth.name()).build(),
                        OddsCalculationResult.EscapePlan.builder().startPlanet(Hoth.name()).refuel(true).build(),
                        OddsCalculationResult.EscapePlan.builder().startPlanet(Hoth.name()).endPlanet(Endor.name()).build()
                ))
                .build();

        // Mocks
        when(routeRepository.findAll()).thenReturn(getRoutes());
        when(config.getAutonomy()).thenReturn(config.getAutonomy());
        when(config.getDeparture()).thenReturn(config.getDeparture());
        when(config.getArrival()).thenReturn(config.getArrival());

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
                        OddsCalculationResult.EscapePlan.builder().startPlanet(Tatooine.name()).endPlanet(Dagobah.name()).build(),
                        OddsCalculationResult.EscapePlan.builder().startPlanet(Dagobah.name()).refuel(true).build(),
                        OddsCalculationResult.EscapePlan.builder().startPlanet(Dagobah.name()).endPlanet(Hoth.name()).build(),
                        OddsCalculationResult.EscapePlan.builder().startPlanet(Hoth.name()).endPlanet(Endor.name()).build()
                ))
                .build();

        // Mocks
        when(routeRepository.findAll()).thenReturn(getRoutes());
        when(config.getAutonomy()).thenReturn(config.getAutonomy());
        when(config.getDeparture()).thenReturn(config.getDeparture());
        when(config.getArrival()).thenReturn(config.getArrival());

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
                        OddsCalculationResult.EscapePlan.builder().startPlanet(Tatooine.name()).endPlanet(Dagobah.name()).build(),
                        OddsCalculationResult.EscapePlan.builder().startPlanet(Dagobah.name()).refuel(true).build(),
                        OddsCalculationResult.EscapePlan.builder().startPlanet(Dagobah.name()).waitOneDay(true).build(),
                        OddsCalculationResult.EscapePlan.builder().startPlanet(Dagobah.name()).endPlanet(Hoth.name()).build(),
                        OddsCalculationResult.EscapePlan.builder().startPlanet(Hoth.name()).endPlanet(Endor.name()).build()
                ))
                .build();

        // Mocks
        when(routeRepository.findAll()).thenReturn(getRoutes());
        when(config.getAutonomy()).thenReturn(config.getAutonomy());
        when(config.getDeparture()).thenReturn(config.getDeparture());
        when(config.getArrival()).thenReturn(config.getArrival());

        // When
        OddsCalculationResult result = oddsCalculator.calculateWithDefaultConfig(stolenPlans);

        // Then
        assertEquals(expected, result);
    }


    // endregion
}
