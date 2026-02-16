import {useNavigate, useParams} from "react-router-dom";
import { type SyntheticEvent, useEffect, useState} from "react";
import axios from "axios";
import type {Transaction} from "../../models/Transaction.ts";
import type {TransactionIn} from "../../models/TransactionIn.ts";
type TransactionUpdateProps = {
    loadUser: () => void;
}


export default function TransactionUpdate({loadUser}: TransactionUpdateProps) {
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
        <>
            <form onSubmit={updateTransaction}>
                <h3>Update Transaction</h3>
                <h4>Transaction Id: {transaction?.id}</h4>
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


                <button type={"submit"}>Change Transaction</button>
                <button type={"reset"} onClick={() => nav("/dashboard")}>Go Back</button>
            </form>
        </>
    )
}