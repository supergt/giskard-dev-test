import {useEffect, useState} from "react";
import axios from "axios";
import { EscapePlanStep, OddsCalculationResult } from "../types/OddsCalculationResult";
import { StolenPlans } from "../types/StolenPlans";

export const CalculateOdds = () => {
    const [odds, setOdds] = useState<number>()
    const [stolenPlans, setStolenPlans] = useState<StolenPlans>()
    const [escapePlans, setEscapePlans] = useState<EscapePlanStep[]>()

    useEffect(() => {
        setStolenPlans({
            countdown: 10,
            bounty_hunters: [
                {
                    planet: "Hoth",
                    day: 6,
                },
                {
                    planet: "Hoth",
                    day: 7,
                },
                {
                    planet: "Hoth",
                    day: 8,
                },
            ]
        })
    }, [])

    useEffect(() => {
        if (stolenPlans) {
            axios.post<OddsCalculationResult>("http://localhost:8080", stolenPlans).then((res) => {
                setOdds(res.data.oddsPercentage)
                setEscapePlans(res.data.escapePlanSteps)
            })
        }

        return () => {}
    }, [stolenPlans])

    return <>
        {odds === undefined
        ? 
            <>Calculating odds...</>
        : 
            <>
                <p>Odds calculated - Millennium Falcon has {odds}% chance of escaping.</p>
                {!!escapePlans && (
                    <ol>
                        {escapePlans.map(ep => (
                            <li key={ep.startDay}>
                                On day {ep.startDay},
                                {!!ep.endPlanet
                                    ? (<> travel from {ep.startPlanet} to {ep.endPlanet}</>)
                                    : (<> refuel on {ep.startPlanet}</>)
                                }
                            </li>
                        ))}
                    </ol>
                )}
            </>}
        </>
}