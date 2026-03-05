import type {AssetType} from "./AssetType.ts";

export type TransactionIn = {
    ticker: string,
    assetName: string,
    cost: number,
    shares: number,
    timestamp: string,
    fee: number,
    assetType: AssetType
}