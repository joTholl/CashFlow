import {useNavigate} from "react-router-dom";
import {type SyntheticEvent, useState} from "react";
import axios from "axios";
import type {TransactionIn} from "../../models/TransactionIn.ts";
import TransactionForm from "./TransactionForm.tsx";
import type {AssetType} from "../../models/AssetType.ts";
import ErrorCard from "../../Cards/ErrorCard.tsx";

type TransactionNewProps = {
    loadUser: () => void;
}

export default function TransactionNew({loadUser}: Readonly<TransactionNewProps>) {
    const nav = useNavigate();
    const [assetName, setAssetName] = useState<string>("");
    const [ticker, setTicker] = useState<string>("");
    const [cost, setCost] = useState<number>(0);
    const [shares, setShares] = useState<number>(0);
    const [fee, setFee] = useState<number>(0);
    const [timestamp, setTimestamp] = useState<string>("");
    const [assetType, setAssetType] = useState<AssetType>("STOCK");
    const [error, setError] = useState<string | null>(null);


    function saveTransaction(e: SyntheticEvent<HTMLFormElement>) {
        e.preventDefault();
        setError(null);
        const saveTransaction: TransactionIn = {ticker, assetName, cost, shares, timestamp, fee, assetType};
        axios.post(`/api/transactions`, saveTransaction)
            .then(loadUser)
            .then(() => nav("/dashboard"))
            .catch((error) => {
                console.log(error);
                if (axios.isAxiosError(error)) {
                    const msg =
                        error.response?.data?.message ||
                        error.response?.data ||
                        error.message;

                    setError(msg);
                } else {
                    setError("Unknown error occurred");
                }
            })
    }


    return (
        <>
            <form onSubmit={saveTransaction} className="form">
                <h3>New Transaction</h3>
                <TransactionForm assetName={assetName} setAssetName={setAssetName} ticker={ticker} setTicker={setTicker}
                                 cost={cost} setCost={setCost} shares={shares} setShares={setShares} fee={fee}
                                 setFee={setFee} timestamp={timestamp} setTimestamp={setTimestamp} assetType={assetType}
                                 setAssetType={setAssetType}/>
            </form>

            {error && (<ErrorCard errorMsg={error}/>)}

        </>
    );
}