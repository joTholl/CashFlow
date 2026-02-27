import {useNavigate, useParams} from "react-router-dom";
import {type SyntheticEvent, useEffect, useState} from "react";
import axios from "axios";
import type {Transaction} from "../../models/Transaction.ts";
import type {TransactionIn} from "../../models/TransactionIn.ts";
import TransactionForm from "./TransactionForm.tsx";
import type {AssetType} from "../../models/AssetType.ts";
import ErrorCard from "../../Cards/ErrorCard.tsx";

type TransactionUpdateProps = {
    loadUser: () => void;
}


export default function TransactionUpdate({loadUser}: Readonly<TransactionUpdateProps>) {
    const {id} = useParams<{ id: string }>();
    const nav = useNavigate();
    const [transaction, setTransaction] = useState<Transaction>();
    const [assetName, setAssetName] = useState<string>("");
    const [ticker, setTicker] = useState<string>("");
    const [cost, setCost] = useState<number>(0);
    const [shares, setShares] = useState<number>(0);
    const [fee, setFee] = useState<number>(0);
    const [timestamp, setTimestamp] = useState<string>("");
    const [assetType, setAssetType] = useState<AssetType>("STOCK");
    const [errorMsg, setErrorMsg] = useState<string | null>(null);

    function getTransaction() {
        axios.get(`/api/transactions/${id}`).then((response) => {
            setTransaction(response.data)
        })
            .catch(error => console.log(error));
    }

    function updateTransaction(e: SyntheticEvent<HTMLFormElement>) {
        e.preventDefault();
        const updateTransaction: TransactionIn = {ticker, assetName, cost, shares, timestamp, fee, assetType};
        axios.put(`/api/transactions/${id}`, updateTransaction)
            .then(()=>loadUser())
            .then(() => nav("/dashboard"))
            .catch((error) => {
                if (axios.isAxiosError(error)) {
                    setErrorMsg(error.response?.data || error.message);
                } else {
                    setErrorMsg(String(error));
                }
                console.log(errorMsg);
            })
    }
    function handleClose() {
        setErrorMsg(null);
    }

    useEffect(() => {
        getTransaction();
    }, [id]);

    useEffect(() => {
        if (transaction) {
            setAssetName(transaction.assetName);
            setTicker(transaction.ticker);
            setCost(transaction.cost);
            setShares(transaction.shares);
            setFee(transaction.fee);
            setTimestamp(transaction.timestamp);
            setAssetType(transaction.assetType)
        }
    }, [transaction]);


    return (
        <>

            <form onSubmit={updateTransaction} className="form">
                <h3>Update Transaction</h3>
                <h4>Transaction Id: {transaction?.id}</h4>
                <TransactionForm assetName={assetName} setAssetName={setAssetName} ticker={ticker} setTicker={setTicker}
                                 cost={cost} setCost={setCost} shares={shares} setShares={setShares} fee={fee}
                                 setFee={setFee} timestamp={timestamp} setTimestamp={setTimestamp} assetType={assetType}
                                 setAssetType={setAssetType}/>
            </form>
            {errorMsg && <ErrorCard errorMsg={errorMsg} onClose={handleClose}/>}
        </>
    )
}