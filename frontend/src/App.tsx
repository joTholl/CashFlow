import {useState} from 'react'
import './styles/App.css'
import Login from "./components/Login.tsx";
import {Route, Routes} from "react-router-dom";
import ProtectedRoute from "./components/ProtectedRoute.tsx";
import Dashboard from "./components/Dashboard.tsx";
import Navbar from "./components/Navbar.tsx";
import Home from "./components/Home.tsx";
import Logout from "./components/Logout.tsx";
import type {AppUser} from "./models/AppUser.ts";
import TransactionDetail from "./components/transaction/TransactionDetail.tsx";
import TransactionUpdate from "./components/transaction/TransactionUpdate.tsx";
import axios from "axios";
import TransactionNew from "./components/transaction/TransactionNew.tsx";

function App() {
    const [user, setUser] = useState<string | undefined | null>(undefined)
    const [appUser, setAppUser] = useState<AppUser>({
        id: "", username: "", assets: []
    });

    const loadUser = () => {
        axios.get("/api/auth").then((response) => {
            setUser(response.data);
            axios.get("/api/appuser").then(response => setAppUser(response.data))
        })
            .catch(() => setUser(null))
    }
    return (
        <>
            <div className="navbar">
                <Navbar user={user}/>
                <Login user={user} loadUser={loadUser}/>
            </div>
            <Routes>
                <Route path="/" element={<Home/>}/>
                <Route path="/logout" element={<Logout/>}/>
                <Route element={<ProtectedRoute user={user}/>}>
                    <Route path="/dashboard" element={<Dashboard user={appUser}/>}/>
                    <Route path="/newTransaction" element={<TransactionNew loadUser={() => loadUser()}/>}/>
                    <Route path="/transaction/:id" element={<TransactionDetail loadUser={() => loadUser()}/>}/>
                    <Route path="/transaction/update/:id" element={<TransactionUpdate loadUser={() => loadUser()}/>}/>
                </Route>
            </Routes>

        </>
    )
}

export default App
