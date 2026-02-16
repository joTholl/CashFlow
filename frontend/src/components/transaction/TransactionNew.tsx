import {useNavigate} from "react-router-dom";
import { type SyntheticEvent, useState} from "react";
import axios from "axios";
import type {TransactionIn} from "../../models/TransactionIn.ts";
type TransactionNewProps = {
    loadUser: () => void;
}

export default function TransactionNew({loadUser}: TransactionNewProps) {
    const nav = useNavigate();
    const [assetName, setAssetName] = useState<string>("");
    const [ticker, setTicker] = useState<string>("");
    const [cost, setCost] = useState<number>(0);
    const [shares, setShares] = useState<number>(0);
    const [fee, setFee] = useState<number>(0);
    const [timestamp, setTimestamp] = useState<string>("");


    function saveTransaction(e:SyntheticEvent<HTMLFormElement>) {
        e.preventDefault();
        const saveTransaction: TransactionIn = {ticker, assetName, cost, shares, timestamp, fee};
        axios.post(`/api/transactions`, saveTransaction)
            .then(loadUser)
            .then(() => nav("/dashboard"))
            .catch((error) => {
                console.log(error)
            })
    }


    return (
        <>
            <form onSubmit={saveTransaction}>
                <h3>New Transaction</h3>
                <label>Asset Name:
                    <input
                        type="string"
                        value={assetName}
                        onChange={(e) => setAssetName(e.target.value)}/>
                </label>
                <label>Ticker:
                    <input
                        type="string"
                        value={ticker}
                        onChange={(e) => setTicker(e.target.value)}/>
                </label>
                <label>Cost:
                    <input
                        type="number"
                        value={cost}
                        onChange={(e) => setCost(Number(e.target.value))}/>
                    €
                </label>
                <label>Shares:
                    <input
                        type="number"
                        value={shares}
                        onChange={(e) => setShares(Number(e.target.value))}/>
                </label>
                <label>Fee:
                    <input
                        type="number"
                        value={fee}
                        onChange={(e) => setFee(Number(e.target.value))}/>
                    €
                </label>
                <label>Timestamp:
                    <input
                        //Change to datetime
                        type="string"
                        value={timestamp}
                        onChange={(e) => setTimestamp(e.target.value)}/>
                </label>


                <button type={"submit"}>Save Transaction</button>
                <button type={"reset"} onClick={() => nav("/dashboard")}>Go Back</button>
            </form>
        </>
    )
}