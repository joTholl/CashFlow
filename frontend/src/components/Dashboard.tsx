import Chart from "./Chart.tsx";
import Assets from "./Assets.tsx";
import type {AppUser} from "../models/AppUser.ts";

type DashboardProps = {
    user: AppUser
}

export default function Dashboard({user}: Readonly<DashboardProps>) {
    return (
        <>
            <h1>Dashboard von {user.username}</h1>
            <Chart/>
            <Assets assets={user.assets}/>
        </>
    )
}