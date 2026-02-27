import {useNavigate, useParams} from "react-router-dom";
import {useEffect, useState} from "react";
import axios from "axios";
import type {Transaction} from "../../models/Transaction.ts";

type TransactionDetailProps = {
    loadUser: () => void;
}
export default function TransactionDetail({loadUser}: Readonly<TransactionDetailProps>) {
    const {id} = useParams<{ id: string }>();
    const nav = useNavigate();
    const [transaction, setTransaction] = useState<Transaction>();

    function getTransaction() {
        axios.get(`/api/transactions/${id}`).then((response) => {
            setTransaction(response.data)
        })
            .catch(error => console.log(error));
    }

    function deleteTransaction() {
        axios.delete(`/api/transactions/${id}`)
            .then(loadUser)
            .then(() => nav("/dashboard"))
            .catch((error) => {
                console.log(error)
            });
    }


    useEffect(() => {
        getTransaction();
    }, [id]);
    const timestamp = transaction?.timestamp ? new Date(transaction?.timestamp).toLocaleString("de-DE", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    }) : "";
    return (
        <div className="form">
            <h3>Transaction Details</h3>
            <h4>Transaction Id: {transaction?.id}</h4>
            <p>Asset Name: {transaction?.assetName}</p>
            <p>Ticker: {transaction?.ticker}</p>
            <p>Cost: {transaction?.cost} $</p>
            <p>Shares: {transaction?.shares}</p>
            <p>Fee: {transaction?.fee} $</p>
            <p>Time: {timestamp}</p>
            <p>Asset Type: {transaction?.assetType}</p>
            <div className="buttons">
                <button onClick={() => nav(`/transaction/update/${id}`)}>Edit Transaction</button>
                <button onClick={() => deleteTransaction()}>Delete Transaction</button>
            </div>
        </div>
    )
}