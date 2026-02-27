import type {Transaction} from "../models/Transaction.ts";
import {Link} from "react-router-dom";

type TransactionCardProps = {
    transaction: Transaction
}

export default function TransactionCard({transaction}: Readonly<TransactionCardProps>) {
    const timestamp = new Date(transaction.timestamp).toLocaleString("de-DE",{
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    });
    return (
        <Link to={`/transaction/${transaction.id}`} className="link">
            <div className="card">
                <h4>{transaction.assetName}</h4>
                <h4>{transaction.ticker}</h4>
                <p>{transaction.shares} shares</p>
                <p>Cost: {transaction.cost} $</p>
                <p>Fee: {transaction.fee} $</p>
                <p>{timestamp}</p>
            </div>
        </Link>
    )
}