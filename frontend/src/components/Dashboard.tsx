import Chart from "./Chart.tsx";
import Assets from "./Assets.tsx";
import type {AppUser} from "../models/AppUser.ts";
import Transactions from "./transaction/Transactions.tsx";
import "../styles/Dashboard.css"
import {useEffect, useState} from "react";
import axios from "axios";

type DashboardProps = {
    user: AppUser
}
function subscribeSymbols() {
    axios.post("/api/live", {});
}

export default function Dashboard({user}: Readonly<DashboardProps>) {

    const [livePrices, setLivePrices] = useState<Record<string, number>>({});

    const fetchLoop = async () => {
        try {
            await axios.get("/api/live").then((response) => {setLivePrices(response.data);});
        } catch (error) {
            console.error(error);
        }

        setTimeout(fetchLoop, 5000);
    };
    useEffect(() => {
        fetchLoop();
        subscribeSymbols();
    }, []);
    return (

            <div className="dashboard">
                <h1>Dashboard von {user.username}</h1>
                <Chart/>
                <div className="components">
                    <Assets assets={user.assets} livePrices={livePrices}/>
                    <Transactions/>
                </div>
            </div>

    )
}