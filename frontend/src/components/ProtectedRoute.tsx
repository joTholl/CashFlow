import {Navigate, Outlet} from "react-router-dom"

type ProtectedRouteProps = {
    user: string | undefined | null
}

export default function ProtectedRoute({user}: ProtectedRouteProps) {
    if (user === undefined) {
        return <h3>Loading...</h3>;
    }
    if (user === null) {
        return <Navigate to={"/"}/>
    }
    return <Outlet/>;
}