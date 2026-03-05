import {useParams} from "react-router-dom";
import {useEffect, useState} from "react";
import Chart from "./Chart.tsx";
import Transactions from "./transaction/Transactions.tsx";
import type {Asset} from "../models/Asset.ts";
import type {ChartData} from "../models/ChartData.ts";

type AssetDetailProps = {
    assets: Asset[];
    livePrices: Record<string, number>
    getChartData: ()=> void;
}

export default function AssetDetail({assets, livePrices, getChartData}: Readonly<AssetDetailProps>) {
    const {ticker} = useParams<{ ticker: string }>();
    const [chartData, setChartData] = useState<ChartData[]>([]);
    const asset: Asset  = assets.find((asset) => asset?.ticker === ticker)!

    useEffect(() => {
        getChartData();
    }, []);

    useEffect(() => {
        const today:string = new Date().toLocaleDateString().slice(0,-4);
        // eslint-disable-next-line react-hooks/set-state-in-effect
        setChartData(prev => {
            if (prev.length === 0) return prev;
            const last = prev[prev.length - 1];
            if (last.date === today) {
                return [
                    ...prev.slice(0, -1),
                    {...last, value: Number((livePrices[asset.ticker] * asset.shares).toFixed(2))}
                ];
            }
            return [
                ...prev,
                {
                    date: today,
                    value: Number((livePrices[asset.ticker] * asset.shares).toFixed(2)),
                    invested: Number(last.invested.toFixed(2))
                }
            ];
        });
    }, [livePrices]);

    return (

        <div className="dashboard">
            <h1>{asset.assetName} ({asset.ticker})</h1>
            <Chart chartData={chartData}/>
            <div className="components">
                <div className="component">
                    <h2>Asset Details:</h2>
                    <p>Name: {asset.assetName}</p>
                    <p>Ticker: {asset.ticker}</p>
                    <p>Invested: {asset.cost} $</p>
                    <p>Shares: {asset.shares}</p>
                    <p>PricePerShare: {livePrices[asset.ticker].toFixed(2)} $</p>
                    <p>Value: {(livePrices[asset.ticker] * asset.shares).toFixed(2)} $</p>
                    <p>Value gained: {((livePrices[asset.ticker] * asset.shares - asset.cost) / asset.cost * 100).toFixed(2)} $</p>

                </div>
                <Transactions ticker={ticker}/>
            </div>
        </div>


    )

}