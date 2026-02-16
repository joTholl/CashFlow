import {useEffect} from "react";

type LoginProps = {
    user: string | undefined | null;
    loadUser: () => void;
};

export default function Login({user, loadUser}: Readonly<LoginProps>) {
    const host: string = globalThis.location.host === "localhost:5173" ? "http://localhost:8080" : globalThis.location.origin;

    function loginUser() {
        globalThis.open(host + "/oauth2/authorization/github", "_self")
    }

    function logoutUser() {
        globalThis.open(host + "/logout", "_self")
    }

    useEffect(() => {
        loadUser();
    }, []);
    if (user) {
        return <button className="navbar-btn" onClick={logoutUser}>Logout</button>
    } else
        return <button className="navbar-btn" onClick={loginUser}>Login</button>

}