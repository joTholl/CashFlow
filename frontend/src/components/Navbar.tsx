import {useNavigate} from "react-router-dom";


export default function Navbar(){
    const nav = useNavigate();
    return(
        <>
            <button onClick={()=>nav("/")}>Home</button>
            <button onClick={()=>nav("/dashboard")}>Dashboard</button>


        </>
    )
}