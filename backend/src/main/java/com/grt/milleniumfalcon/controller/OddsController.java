package com.grt.milleniumfalcon.controller;

import com.grt.milleniumfalcon.dto.OddsCalculationResult;
import com.grt.milleniumfalcon.model.Config;
import com.grt.milleniumfalcon.model.Route;
import com.grt.milleniumfalcon.repository.RouteRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
public class OddsController {
    final
    RouteRepository routeRepository;

    final Config config;

    public OddsController(RouteRepository routeRepository, Config config) {
        this.routeRepository = routeRepository;
        this.config = config;
    }

    @GetMapping("")
    public OddsCalculationResult calculateOdds() {
        List<Route> routes = routeRepository.findAll();

        System.out.println("Autonomy = " + config.getAutonomy());

        return OddsCalculationResult.builder()
                .oddsPercentage(new BigDecimal("0.5"))
                .canEscape(false)
                .build();
    }
}
