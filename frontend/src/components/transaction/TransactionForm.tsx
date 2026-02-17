import {useNavigate} from "react-router-dom";
import * as React from "react";

type TransactionFormProps = {
    assetName: string, setAssetName: React.Dispatch<React.SetStateAction<string>>,
    ticker: string, setTicker: React.Dispatch<React.SetStateAction<string>>,
    cost: number, setCost: React.Dispatch<React.SetStateAction<number>>,
    shares: number, setShares: React.Dispatch<React.SetStateAction<number>>,
    fee: number, setFee: React.Dispatch<React.SetStateAction<number>>,
    timestamp: string, setTimestamp: React.Dispatch<React.SetStateAction<string>>;
}

export default function TransactionForm(props: Readonly<TransactionFormProps>) {
    const nav = useNavigate();


    return (
        <>
            <label>Asset Name:{' '}
                <input type="string"
                       value={props.assetName}
                       onChange={(e) => props.setAssetName(e.target.value)}/>
            </label>
            <label>Ticker:{' '}
                <input type="string"
                       value={props.ticker}
                       onChange={(e) => props.setTicker(e.target.value)}/>
            </label>
            <label>Cost:{' '}
                <input type="number"
                       value={props.cost}
                       onChange={(e) => props.setCost(Number(e.target.value))}/>
                {' '}€
            </label>
            <label>Shares:{' '}
                <input type="number"
                       value={props.shares}
                       onChange={(e) => props.setShares(Number(e.target.value))}/>
            </label>
            <label>Fee:{' '}
                <input type="number"
                       value={props.fee}
                       onChange={(e) => props.setFee(Number(e.target.value))}/>
                {' '}€
            </label>
            <label>Timestamp:{' '}
                <input //Change to datetime
                    type="datetime-local"
                    value={props.timestamp? new Date(props.timestamp).toISOString().slice(0,16): ""}
                    onChange={(e) => props.setTimestamp(new Date(e.target.value).toISOString())}/>
            </label>
            <button type={"submit"}>Save</button>
            <button type={"reset"} onClick={() => nav("/dashboard")}>Go Back</button>

        </>
    )
}