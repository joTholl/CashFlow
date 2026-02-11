import type {Asset} from "./Asset.ts";

export type AppUserOut = {
    userName: string,
    assets: Asset[];
}