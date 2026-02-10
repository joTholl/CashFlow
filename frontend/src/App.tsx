import {useState} from 'react'
import './App.css'
import Login from "./components/Login.tsx";
import {Route, Routes} from "react-router-dom";
import ProtectedRoute from "./components/ProtectedRoute.tsx";
import Dashboard from "./components/Dashboard.tsx";
import Navbar from "./components/Navbar.tsx";
import Home from "./components/Home.tsx";
import Logout from "./components/Logout.tsx";

function App() {
    const [user, setUser] = useState<string | undefined | null>(undefined)
    return (
        <>
            <Navbar/>
            <Login setUser={setUser}/>
            <Routes>
                <Route path="/" element={<Home/>}/>
                <Route path="/logout" element={<Logout/>}/>
                <Route element={<ProtectedRoute user={user}/>}>
                    <Route path="/dashboard" element={<Dashboard user={user}/>}/>
                </Route>
            </Routes>

        </>
    )
}

export default App
