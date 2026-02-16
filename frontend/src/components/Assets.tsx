import type {Asset} from "../models/Asset.ts";
import AssetCard from "../Cards/AssetCard.tsx";

type AssetProps = {
    assets: Asset[]
}

export default function Assets({assets}: Readonly<AssetProps>) {
    const sortedAssets = [...assets].sort((a, b) => b.cost - a.cost)
    return (
        <>
            <div className="component">
                <h2>Assets:</h2>
                <div className="content">
                    {sortedAssets.map(asset => <AssetCard key={asset.ticker} asset={asset}/>)}
                </div>
            </div>
        </>
    )

}