import type {Asset} from "../models/Asset.ts";

type AssetCardProps = {
    asset: Asset
}

export default function AssetCard({asset}: Readonly<AssetCardProps>) {
return(
    <>
        <h4>{asset.assetName}</h4>
        <p>{asset.cost} €</p>
    </>
)
}