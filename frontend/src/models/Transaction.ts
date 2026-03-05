import AssetType from "./AssetType.ts";

export type Transaction = {
    id: string,
    ticker: string,
    assetName: string,
    cost: number,
    shares: number,
    timestamp: string,
    fee: number,
    assetType: AssetType
}