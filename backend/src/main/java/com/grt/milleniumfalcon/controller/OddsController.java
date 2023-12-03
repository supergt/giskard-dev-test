package com.grt.milleniumfalcon.controller;

import com.grt.milleniumfalcon.calculator.OddsCalculator;
import com.grt.milleniumfalcon.dto.OddsCalculationResult;
import com.grt.milleniumfalcon.dto.StolenPlans;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@CrossOrigin(origins = "*")
@RestController
public class OddsController {
    final
    OddsCalculator oddsCalculator;

    public OddsController(OddsCalculator oddsCalculator) {
        this.oddsCalculator = oddsCalculator;
    }

    @PostMapping("")
    public OddsCalculationResult calculateOdds(@RequestBody StolenPlans stolenPlans) throws IOException {
        return oddsCalculator.calculateWithDefaultConfig(stolenPlans);
    }
}
