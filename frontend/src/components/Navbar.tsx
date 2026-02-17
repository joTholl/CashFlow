import {useNavigate} from "react-router-dom";

type NavbarProps = {
    user: string | undefined | null;
};
export default function Navbar({user}: Readonly<NavbarProps>) {
    const nav = useNavigate();
    if (user) {
        return (
            <>
                <button onClick={() => nav("/")}>Home</button>
                <button onClick={() => nav("/dashboard")}>Dashboard</button>
            </>
        )
    } else {
        return (
            <>
                <button onClick={() => nav("/")}>Home</button>
            </>
        )
    }

}