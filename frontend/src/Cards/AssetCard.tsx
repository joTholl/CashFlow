import type {Asset} from "../models/Asset.ts";

type AssetCardProps = {
    asset: Asset
}

export default function AssetCard({asset}: Readonly<AssetCardProps>) {
    return (
        <>
            <div className="card">
                <h4>{asset.assetName}</h4>
                <p>{asset.shares}</p>
                <p>{asset.cost} €</p>
            </div>
        </>
    )
}