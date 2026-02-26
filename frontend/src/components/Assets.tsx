import type {Asset} from "../models/Asset.ts";
import AssetCard from "../Cards/AssetCard.tsx";
import type {AssetWithLivePrices} from "../models/AssetWithLivePrices.ts";

type AssetProps = {
    assets: Asset[],
    livePrices: Record<string, number>
}

export default function Assets({assets, livePrices}: Readonly<AssetProps>) {
    const newAssets: AssetWithLivePrices[] = [];
    for (const asset of assets) {
        const newAsset: AssetWithLivePrices = {
            asset: asset,
            pricePerShare: livePrices[asset.ticker],
            price: (livePrices[asset.ticker] * asset.shares),
            percent: (livePrices[asset.ticker] * asset.shares - asset.cost) / asset.cost * 100
        }
        newAssets.push(newAsset);
    }
    const sortedAssets = [...newAssets].sort((a, b) => b.price - a.price)
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