import type {AssetType} from "./AssetType.ts";

export type Asset = {
    ticker: string,
    shares: number,
    assetName: string,
    cost: number;
    assetType: AssetType;
}