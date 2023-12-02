package com.grt.milleniumfalcon.controller;

import com.grt.milleniumfalcon.calculator.OddsCalculator;
import com.grt.milleniumfalcon.dto.OddsCalculationResult;
import com.grt.milleniumfalcon.dto.StolenPlans;
import com.grt.milleniumfalcon.model.Config;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
public class OddsController {
    final
    OddsCalculator oddsCalculator;

    final Config config;

    public OddsController(OddsCalculator oddsCalculator, Config config) {
        this.oddsCalculator = oddsCalculator;
        this.config = config;
    }

    @PostMapping("")
    public OddsCalculationResult calculateOdds(@RequestBody StolenPlans stolenPlans) {
        return oddsCalculator.calculate(stolenPlans);
    }
}
