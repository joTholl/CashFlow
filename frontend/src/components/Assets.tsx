import type {Asset} from "../models/Asset.ts";
import AssetCard from "../Cards/AssetCard.tsx";

type AssetProps = {
    assets: Asset[]
}

export default function Assets({assets}: Readonly<AssetProps>) {
    return (
        <>
            {assets.map(value => <AssetCard asset={value}/>)}
        </>
    )

}