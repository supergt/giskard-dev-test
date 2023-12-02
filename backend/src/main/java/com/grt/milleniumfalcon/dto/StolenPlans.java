package com.grt.milleniumfalcon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StolenPlans {
    int countdown;
    @JsonProperty("bounty_hunters")
    List<BountyHunter> bountyHunters;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class BountyHunter {
        String planet;
        int day;
    }
}
