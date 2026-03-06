import AssetCard from "../Cards/AssetCard.tsx";
import type {AssetWithLivePrices} from "../models/AssetWithLivePrices.ts";

type AssetProps = {
    assets: AssetWithLivePrices[],
}

export default function Assets({assets}: Readonly<AssetProps>) {

    const sortedAssets = [...assets].sort((a, b) => b.price - a.price)


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