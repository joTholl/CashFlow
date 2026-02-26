import type {Transaction} from "../models/Transaction.ts";
import {Link} from "react-router-dom";

type TransactionCardProps = {
    transaction: Transaction
}

export default function TransactionCard({transaction}: Readonly<TransactionCardProps>) {
    return (

            <div className="card">
                <Link to={`/transaction/${transaction.id}`} className="link">
                    <h4>{transaction.assetName}</h4>
                    <h4>{transaction.ticker}</h4>
                    <p>{transaction.shares} shares</p>
                    <p>Cost: {transaction.cost} $</p>
                    <p>Fee: {transaction.fee} $</p>
                    <p>{transaction.timestamp}</p>
                </Link>
            </div>

    )
}