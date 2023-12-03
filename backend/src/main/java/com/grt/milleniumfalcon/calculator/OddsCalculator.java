package com.grt.milleniumfalcon.calculator;

import com.grt.milleniumfalcon.dto.Config;
import com.grt.milleniumfalcon.dto.OddsCalculationResult;
import com.grt.milleniumfalcon.dto.StolenPlans;
import com.grt.milleniumfalcon.helper.ClassPathFileLoader;
import com.grt.milleniumfalcon.model.DynamicSqliteDataSource;
import com.grt.milleniumfalcon.model.Route;
import com.grt.milleniumfalcon.repository.RouteRepository;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;

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

    public OddsCalculationResult calculateWithDefaultConfig(StolenPlans stolenPlans) throws IOException {
        Config defaultConfig = classPathFileLoader.loadFile("millennium-falcon.json", Config.class);
        return calculate(stolenPlans, "", defaultConfig);
    }

    public OddsCalculationResult calculate(StolenPlans stolenPlans, String configFolderPath, Config overrideConfig) {
        List<Route> routes = loadRoutesWithCustomConfig(configFolderPath, overrideConfig);

        return OddsCalculationResult.builder()
                .oddsPercentage(5)
                .build();
    }

    protected synchronized List<Route> loadRoutesWithCustomConfig(String configFolderPath, Config overrideConfig) {
        ((DynamicSqliteDataSource) this.dataSource).setUrl(configFolderPath, overrideConfig);
        return routeRepository.findAll();
    }
}
