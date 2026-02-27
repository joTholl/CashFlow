import type {AssetWithLivePrices} from "../models/AssetWithLivePrices.ts";
import {useEffect, useRef, useState} from "react";

type AssetCardProps = {
    asset: AssetWithLivePrices
}

export default function AssetCard({asset}: Readonly<AssetCardProps>) {
    const prevValue = useRef<number | null>(null);
    const [colorClass, setColorClass] = useState("");

    useEffect(() => {
        if (prevValue.current !== null) {
            if (asset.pricePerShare > prevValue.current) {
                setColorClass("green");
            } else if (asset.pricePerShare < prevValue.current) {
                setColorClass("red");
            }
        }

        prevValue.current = asset.pricePerShare;
    }, [asset]);

    return (
        <div className="card">
            <h4>{asset.asset.assetName} ({asset.asset.ticker})</h4>
            <p>{asset.asset.shares} shares</p>
            <p>{asset.asset.cost} $ invested</p>
            <p className={colorClass}>{asset.pricePerShare?.toFixed(2)} $/Share</p>
            <p className={asset.percent >= 0 ? "green" : "red"}>Value: {asset.price?.toFixed(2)} $</p>
            <p className={asset.percent >= 0 ? "green" : "red"}>Value gained: {asset.percent?.toFixed(2)} %</p>
        </div>

    )
}