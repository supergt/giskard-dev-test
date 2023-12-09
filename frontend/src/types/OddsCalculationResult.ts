export type OddsCalculationResult = {
    oddsPercentage: number
    escapePlanSteps: EscapePlanStep[]
}

export type EscapePlanStep = {
    startPlanet: string
    startDay: number
    endPlanet?: string
    endDay: number
    refuel: boolean
}