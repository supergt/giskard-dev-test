package com.grt.milleniumfalcon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OddsCalculationResult {
    BigDecimal oddsPercentage;
    boolean canEscape;
}
