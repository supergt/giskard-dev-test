package com.grt.milleniumfalcon.shell;

import com.grt.milleniumfalcon.calculator.OddsCalculator;
import com.grt.milleniumfalcon.dto.StolenPlans;
import com.grt.milleniumfalcon.helper.ClassPathFileLoader;
import com.grt.milleniumfalcon.dto.Config;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

@ShellComponent
public class OddsShellCommands {
    final DataSource dataSource;
    final OddsCalculator oddsCalculator;
    final ClassPathFileLoader classPathFileLoader;
    public OddsShellCommands(DataSource dataSource, OddsCalculator oddsCalculator, ClassPathFileLoader classPathFileLoader) {
        this.dataSource = dataSource;
        this.oddsCalculator = oddsCalculator;
        this.classPathFileLoader = classPathFileLoader;
    }

    @ShellMethod(key = "give-me-the-odds")
    public int giveMeTheOdds(@ShellOption String configPath, @ShellOption String stolenPlansPath) throws IOException {
        StolenPlans stolenPlans = classPathFileLoader.loadFile(stolenPlansPath, StolenPlans.class);
        Config newConfig = classPathFileLoader.loadFile(configPath, Config.class);

        String[] configPathParts = configPath.split("/");
        // Copy everything except last part (file name)
        String configPathFolder = Arrays.stream(configPathParts, 0, configPathParts.length - 1).collect(Collectors.joining("/")) + "/";

        return oddsCalculator.calculate(stolenPlans, configPathFolder, newConfig).getOddsPercentage();
    }
}
