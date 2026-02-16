import Chart from "./Chart.tsx";
import Assets from "./Assets.tsx";
import type {AppUser} from "../models/AppUser.ts";
import Transactions from "./transaction/Transactions.tsx";
import "../styles/Dashboard.css"
import {useEffect} from "react";

type DashboardProps = {
    user: AppUser
}

export default function Dashboard({user}: Readonly<DashboardProps>) {

    useEffect(() => {

    })
    return (
        <>
            <div className="dashboard">
                <h1>Dashboard von {user.username}</h1>
                <Chart/>
                <div className="components">
                    <Assets assets={user.assets}/>
                    <Transactions/>
                </div>
            </div>
        </>
    )
}