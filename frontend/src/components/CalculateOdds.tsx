import {useEffect, useState} from "react";
import axios from "axios";
import { OddsCalculationResult } from "../types/OddsCalculationResult";

export const CalculateOdds = () => {
    const [odds, setOdds] = useState<number>()
    const [canEscape, setCanEscape] = useState<boolean>()
    useEffect(() => {
        axios.get<OddsCalculationResult>("http://localhost:8080", {}).then((res) => {
            setOdds(res.data.oddsPercentage)
            setCanEscape(res.data.canEscape)
        })

        return () => {}
    }, [])

    return <>
        {!odds ? 
            <>Calculating odds...</>
        : <>
            <p>Odds calculated - {odds}%.</p>
            <p>The millenium falcon {canEscape ? "can" : "cannot"} escape.</p>
        </>}
    </>
}