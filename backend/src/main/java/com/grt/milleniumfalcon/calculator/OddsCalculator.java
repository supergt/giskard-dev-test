package com.grt.milleniumfalcon.calculator;

import com.grt.milleniumfalcon.dto.OddsCalculationResult;
import com.grt.milleniumfalcon.dto.StolenPlans;
import com.grt.milleniumfalcon.helper.ClassPathFileLoader;
import com.grt.milleniumfalcon.model.Config;
import com.grt.milleniumfalcon.model.DynamicSqliteDataSource;
import com.grt.milleniumfalcon.repository.RouteRepository;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;

@Component
public class OddsCalculator {
    final RouteRepository routeRepository;
    final Config config;
    final ClassPathFileLoader classPathFileLoader;
    final DataSource dataSource;
    public OddsCalculator(RouteRepository routeRepository, Config config, ClassPathFileLoader classPathFileLoader, DataSource dataSource) {
        this.routeRepository = routeRepository;
        this.config = config;
        this.classPathFileLoader = classPathFileLoader;
        this.dataSource = dataSource;
    }

    public OddsCalculationResult calculateWithDefaultConfig(StolenPlans stolenPlans) throws IOException {
        Config defaultConfig = classPathFileLoader.loadFile("millennium-falcon.json", Config.class);
        return calculate(stolenPlans, "", defaultConfig);
    }

    public synchronized OddsCalculationResult calculate(StolenPlans stolenPlans, String configFolderPath, Config overrideConfig) {
        this.config.copy(overrideConfig);
        ((DynamicSqliteDataSource) this.dataSource).setUrl(configFolderPath, this.config);

        return OddsCalculationResult.builder()
                .oddsPercentage(5)
                .build();
    }
}
