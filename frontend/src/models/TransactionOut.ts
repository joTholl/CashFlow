import AssetType from "./AssetType.ts";

export type TransactionOut = {
    ticker: string,
    assetName: string,
    cost: number,
    shares: number,
    timestamp: string,
    fee: number,
    assetType: AssetType
}