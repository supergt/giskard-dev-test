import {useEffect, useState} from "react";
import axios from "axios";
import { OddsCalculationResult } from "../types/OddsCalculationResult";
import { StolenPlans } from "../types/StolenPlans";

export const CalculateOdds = () => {
    const [odds, setOdds] = useState<number>()
    const [stolenPlans, setStolenPlans] = useState<StolenPlans>()

    useEffect(() => {
        setStolenPlans({
            countdown: 6,
            bounty_hunters: [
                {
                    planet: "Tatooine",
                    day: 4,
                },
                {
                    planet: "Dagobah",
                    day: 5,
                },
            ]
        })
    }, [])

    useEffect(() => {
        if (stolenPlans) {
            axios.post<OddsCalculationResult>("http://localhost:8080", stolenPlans).then((res) => {
                setOdds(res.data.oddsPercentage)
            })
        }

        return () => {}
    }, [stolenPlans])

    return <>
        {!odds ? 
            <>Calculating odds...</>
        : <>
            <p>Odds calculated - Millennium Falcon has {odds}% chance of escaping.</p>
        </>}
    </>
}