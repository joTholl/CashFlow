type DashboardProps = {
    user: string | undefined | null
}

export default function Dashboard({user}: Readonly<DashboardProps>) {
    return (

        <h1>Dashboard von {user}</h1>

    )
}