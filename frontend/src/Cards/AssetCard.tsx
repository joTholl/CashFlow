import type {AssetWithLivePrices} from "../models/AssetWithLivePrices.ts";

type AssetCardProps = {
    asset: AssetWithLivePrices
}

export default function AssetCard({asset}: Readonly<AssetCardProps>) {
    return (

            <div className="card">
                <h4>{asset.asset.assetName}</h4>
                <h4>{asset.asset.ticker}</h4>
                <p>{asset.asset.shares} shares</p>
                <p>{asset.asset.cost} $ invested</p>
                <p>{asset.pricePerShare.toFixed(2)} $/Share</p>
                <p>Value: {asset.price.toFixed(2)} $</p>
                <p>Value gained: {asset.percent.toFixed(2)} %</p>
            </div>

    )
}