package com.grt.milleniumfalcon.controller;

import com.grt.milleniumfalcon.dto.OddsCalculationResult;
import com.grt.milleniumfalcon.model.Route;
import com.grt.milleniumfalcon.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
public class OddsController {
    @Autowired
    RouteRepository routeRepository;

    @GetMapping("")
    public OddsCalculationResult calculateOdds() {
        List<Route> routes = routeRepository.findAll();

        return OddsCalculationResult.builder()
                .oddsPercentage(new BigDecimal("0.5"))
                .canEscape(false)
                .build();
    }
}
