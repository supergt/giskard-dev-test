package com.grt.milleniumfalcon.calculator;

import com.grt.milleniumfalcon.dto.OddsCalculationResult;
import com.grt.milleniumfalcon.dto.StolenPlans;
import com.grt.milleniumfalcon.model.Config;
import com.grt.milleniumfalcon.repository.RouteRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OddsCalculator {
    final RouteRepository routeRepository;
    final Config config;
    public OddsCalculator(RouteRepository routeRepository, Config config) {
        this.routeRepository = routeRepository;
        this.config = config;
    }

    public OddsCalculationResult calculate(StolenPlans stolenPlans) {
        return OddsCalculationResult.builder()
                .oddsPercentage(new BigDecimal("0.5"))
                .canEscape(false)
                .build();
    }
}
