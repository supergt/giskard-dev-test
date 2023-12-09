import {ChangeEvent, useCallback, useEffect, useState} from "react";
import axios from "axios";
import {EscapePlanStep, OddsCalculationResult} from "../types/OddsCalculationResult";
import {StolenPlans} from "../types/StolenPlans";

const parseStolenPlansFromFile = async (file: File) =>
    new Promise<StolenPlans>((resolve, reject) => {
        if (file.type !== "application/json") reject("Error - not a json file");

        const reader = new FileReader();
        reader.onload = (event: ProgressEvent<FileReader>) => {
            const text = event.target?.result;

            try {
                const json = JSON.parse(text as string);
                resolve(json)
            } catch (error) {
                reject("Error: Cannot parse the file as JSON.");
            }
        };
        reader.onerror = (error) => reject(error);
        reader.readAsText(file);
    })

export const CalculateOdds = () => {
    const [odds, setOdds] = useState<number>()
    const [stolenPlans, setStolenPlans] = useState<StolenPlans>()
    const [escapePlans, setEscapePlans] = useState<EscapePlanStep[]>()
    const [error, setError] = useState<string>()
    const [loading, setLoading] = useState<boolean>(false)

    useEffect(() => {
        if (stolenPlans) {
            setError(undefined)
            setOdds(undefined)
            setEscapePlans(undefined)
            setLoading(true)
            axios.post<OddsCalculationResult>("http://localhost:8080", stolenPlans).then((res) => {
                setOdds(res.data.oddsPercentage)
                setEscapePlans(res.data.escapePlanSteps)
                setLoading(false)
            },
                (error) => {
                    setLoading(false)
                    if (typeof error === "string") {
                        setError(error as string)
                    } else {
                        setError("Unknown error while calculating odds")
                    }
                })
        }

        return () => {}
    }, [stolenPlans, setLoading, setOdds, setEscapePlans])

    const handleFileChange = useCallback(async (event: ChangeEvent<HTMLInputElement>) => {
        setStolenPlans(undefined)
        setError(undefined)
        setOdds(undefined)
        setLoading(false)
        setEscapePlans(undefined)

        const file = event.target.files && event.target.files[0];

        if (!file) return;

        try {
            const stolenPlans = await parseStolenPlansFromFile(file)

            if (!stolenPlans?.countdown
                || stolenPlans.countdown < 0
                || !stolenPlans.bounty_hunters?.length
                || stolenPlans.bounty_hunters.some(bh => !bh?.day || bh.day < 0 || !bh.planet)) {
                setError("Invalid format for stolen plans")
            } else {
                setStolenPlans(stolenPlans)
            }
        } catch (error: unknown) {
            if (typeof error === "string") {
                setError(error as string)
            } else {
                setError("Unknown error with uploaded file")
            }
        }
    }, [setStolenPlans, setError, setOdds, setLoading, setEscapePlans])

    return <>
        <h2>Upload stolen plans</h2>
        <input type="file" name="stolen-plans" accept="application/json" onChange={handleFileChange} />
        {!!error && (<p>{error}</p>)}
        {!!loading && (<p>Calculating odds...</p>)}
        {odds !== undefined && (<>
            <h2>Calculated Odds</h2>
                <p>Millennium Falcon has {odds}% chance of escaping.</p>
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
            </>)}
        </>
}