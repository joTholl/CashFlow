import type {Asset} from "../models/Asset.ts";
import AssetCard from "../Cards/AssetCard.tsx";
import type {AssetWithLivePrices} from "../models/AssetWithLivePrices.ts";
import type {ChartData} from "../models/ChartData.ts";
import {type Dispatch, type SetStateAction, useEffect} from "react";

type AssetProps = {
    assets: Asset[],
    livePrices: Record<string, number>
    setChartData: Dispatch<SetStateAction<ChartData[]>>
}

export default function Assets({assets, livePrices, setChartData}: Readonly<AssetProps>) {
    const newAssets: AssetWithLivePrices[] = [];
    let priceSum: number = 0;
    for (const asset of assets) {
        const newAsset: AssetWithLivePrices = {
            asset: asset,
            pricePerShare: livePrices[asset.ticker],
            price: (livePrices[asset.ticker] * asset.shares),
            percent: (livePrices[asset.ticker] * asset.shares - asset.cost) / asset.cost * 100
        }
        newAssets.push(newAsset);
        priceSum += newAsset.price
    }
    const sortedAssets = [...newAssets].sort((a, b) => b.price - a.price)

    useEffect(() => {
        const today:string = new Date().toLocaleDateString().slice(0,-4);
        setChartData(prev => {
            if (prev.length === 0) return prev;
            const last = prev[prev.length - 1];
            if (last.date === today) {
                return [
                    ...prev.slice(0, -1),
                    {...last, value: parseFloat(priceSum.toFixed(2))}
                ];
            }
            return [
                ...prev,
                {
                    date: today,
                    value: parseFloat(priceSum.toFixed(2)),
                    invested: parseFloat(last.invested.toFixed(2))
                }
            ];
        });
    }, [priceSum]);
    return (

        <div className="component">
            <h2>Assets:</h2>
            <div className="content">
                {sortedAssets.map(assetWithLivePrice => (
                    <AssetCard key={assetWithLivePrice.asset.ticker} asset={assetWithLivePrice}/>))}
            </div>
        </div>

    )

}