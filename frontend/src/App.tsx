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
import type {ChartData} from "./models/ChartData.ts";
import AssetDetail from "./components/AssetDetail.tsx";


function App() {
    const [user, setUser] = useState<string | undefined | null>(undefined)
    const [chartData, setChartData] = useState<ChartData[]>([]);
    const [livePrices, setLivePrices] = useState<Record<string, number>>({});
    const [appUser, setAppUser] = useState<AppUser>({
        id: "", username: "", assets: []
    });

    const loadUser = () => {
        axios.get("/api/auth")
            .then((response) => setUser(response.data))
            .then(() => axios.get("/api/appuser").then(response => setAppUser(response.data)))
            .then(() => postChartData())
            .catch(() => setUser(null))
    }

    function postChartData() {
        axios.post("/api/historical")
            .catch((error) => {
                console.log(error)
            });
        getChartData();
    }

    function getChartData() {
        axios.get("/api/historical/chart")
            .then((response) =>
                setChartData(response.data.map((item: { date: string, value: number; invested: number; }) => ({
                    ...item,
                    date: new Date(item.date).toLocaleDateString().slice(0, -4),
                    value: Number(item.value.toFixed(2)),
                    invested: Number(item.invested.toFixed(2))
                })))
            )
            .catch((error) => {
                console.log(error)
            });
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
                    <Route path="/dashboard"
                           element={<Dashboard user={appUser} chartData={chartData} setChartData={setChartData}
                                               livePrices={livePrices} setLivePrices={setLivePrices}/>}/>
                    <Route path="/newTransaction"
                           element={<TransactionNew loadUser={() => loadUser()}/>}/>
                    <Route path="/transaction/:id" element={<TransactionDetail loadUser={() => loadUser()}/>}/>
                    <Route path="/transaction/update/:id"
                           element={<TransactionUpdate loadUser={() => loadUser()}/>}/>
                    <Route path="/asset/:ticker"
                           element={<AssetDetail assets={appUser.assets} livePrices={livePrices} getChartData={getChartData}/>}/>
                </Route>
            </Routes>

        </>
    )
}

export default App
