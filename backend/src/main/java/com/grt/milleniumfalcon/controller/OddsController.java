package com.grt.milleniumfalcon.controller;

import com.grt.milleniumfalcon.dto.OddsCalculationResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@CrossOrigin(origins = "*")
@RestController
public class OddsController {
    @GetMapping("")
    public OddsCalculationResult calculateOdds() {
        return OddsCalculationResult.builder()
                .oddsPercentage(new BigDecimal("0.5"))
                .canEscape(false)
                .build();
    }
}
