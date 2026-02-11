import type {Asset} from "./Asset.ts";

export type AppUser = {
    id: string,
    username: string,
    assets: Asset[];
}