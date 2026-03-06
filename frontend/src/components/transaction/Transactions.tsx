import type {Transaction} from "../../models/Transaction.ts";
import TransactionCard from "../../Cards/TransactionCard.tsx";
import axios from "axios";
import {useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";

type TransactionsProps = {
    ticker?: string;
}

export default function Transactions({ticker}: Readonly<TransactionsProps>) {
    const nav = useNavigate();
    let url: string = "/api/transactions";
    if (ticker) {
        url = url + "/ticker?ticker=" + ticker;
    }
    const getTransactions = () => {
        axios.get(url).then((response) => {
            setTransactions(response.data);
        })
            .catch((error) => {
                console.log(error);
            });
    }
    const [transactions, setTransactions] = useState<Transaction[]>([]);

    const sortedTransactions = [...transactions].sort((a, b) => b.timestamp > a.timestamp ? 1 : -1)
    useEffect(() => {
        getTransactions();
    }, []);
    return (
        <div className="component">
            <h2>Transactions:</h2>
            <button onClick={() => nav("/newTransaction")}>New Transaction</button>
            <div className="content">
                {sortedTransactions.map(transaction => <TransactionCard key={transaction.id}
                                                                        transaction={transaction}/>)}
            </div>
        </div>
    )
}