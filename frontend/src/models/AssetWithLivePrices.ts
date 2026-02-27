import type {Asset} from "./Asset.ts";

export type AssetWithLivePrices = {
    asset: Asset;
    pricePerShare: number;
    price: number;
    percent : number;
}