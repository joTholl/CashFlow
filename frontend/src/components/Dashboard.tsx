import Chart from "./Chart.tsx";
import Assets from "./Assets.tsx";
import type {AppUser} from "../models/AppUser.ts";
import Transactions from "./transaction/Transactions.tsx";
import "../styles/Dashboard.css"
import {type Dispatch, type SetStateAction, useEffect} from "react";
import axios from "axios";
import type {ChartData} from "../models/ChartData.ts";
import type {AssetWithLivePrices} from "../models/AssetWithLivePrices.ts";

type DashboardProps = {
    user: AppUser
    chartData: ChartData[]
    setChartData: Dispatch<SetStateAction<ChartData[]>>
    livePrices: Record<string, number>
    setLivePrices: Dispatch<SetStateAction<Record<string, number>>>
}

function subscribeSymbols() {
    axios.post("/api/live", {});
}

export default function Dashboard({user, chartData, setChartData, livePrices, setLivePrices}: Readonly<DashboardProps>) {



    const fetchLoop = async () => {
        try {
            await axios.get("/api/live").then((response) => {
                setLivePrices(response.data);
            });
        } catch (error) {
            console.error(error);
        }

        setTimeout(fetchLoop, 5000);
    };
    useEffect(() => {
        fetchLoop();
        subscribeSymbols();
    }, []);

    const newAssets: AssetWithLivePrices[] = [];
    let priceSum: number = 0;
    for (const asset of user.assets) {
        const newAsset: AssetWithLivePrices = {
            asset: asset,
            pricePerShare: livePrices[asset.ticker],
            price: (livePrices[asset.ticker] * asset.shares),
            percent: (livePrices[asset.ticker] * asset.shares - asset.cost) / asset.cost * 100
        }
        newAssets.push(newAsset);
        priceSum += newAsset.price
    }

    useEffect(() => {
        const today:string = new Date().toLocaleDateString().slice(0,-4);
        setChartData(prev => {
            if (prev.length === 0) return prev;
            const last = prev[prev.length - 1];
            if (last.date === today) {
                return [
                    ...prev.slice(0, -1),
                    {...last, value: Number(priceSum.toFixed(2))}
                ];
            }
            return [
                ...prev,
                {
                    date: today,
                    value: Number(priceSum.toFixed(2)),
                    invested: Number(last.invested.toFixed(2))
                }
            ];
        });
    }, [priceSum]);

    return (

        <div className="dashboard">
            <h1>{user.username}'s Dashboard</h1>
            <Chart chartData={chartData}/>
            <div className="components">
                <Assets assets={newAssets}/>
                <Transactions/>
            </div>
        </div>

    )
}