import {useNavigate, useParams} from "react-router-dom";
import { type SyntheticEvent, useEffect, useState} from "react";
import axios from "axios";
import type {Transaction} from "../../models/Transaction.ts";
import type {TransactionIn} from "../../models/TransactionIn.ts";
import TransactionForm from "./TransactionForm.tsx";
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

    function getTransaction() {
        axios.get(`/api/transactions/${id}`).then((response) => {
            setTransaction(response.data)
        })
            .catch(error => console.log(error));
    }

    function updateTransaction(e:SyntheticEvent<HTMLFormElement>) {
        e.preventDefault();
        const updateTransaction: TransactionIn = {ticker, assetName, cost, shares, timestamp, fee};
        axios.put(`/api/transactions/${id}`, updateTransaction)
            .then(loadUser)
            .then(() => nav("/dashboard"))
            .catch((error) => {
                console.log(error)
            })
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
        }
    }, [transaction]);


    return (

            <form onSubmit={updateTransaction}>
                <h3>Update Transaction</h3>
                <h4>Transaction Id: {transaction?.id}</h4>
                <TransactionForm assetName={assetName} setAssetName={setAssetName} ticker={ticker} setTicker={setTicker}
                                 cost={cost} setCost={setCost} shares={shares} setShares={setShares} fee={fee}
                                 setFee={setFee} timestamp={timestamp} setTimestamp={setTimestamp}/>
            </form>

    )
}