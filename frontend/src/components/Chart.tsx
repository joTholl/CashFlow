import type {ChartData} from "../models/ChartData.ts";
import {Legend, Line, LineChart, Tooltip, XAxis, YAxis} from "recharts";


type ChartProps = {
    chartData: ChartData[]
}

function RechartsDevtools() {
    return null;
}

type CustomTooltipProps = {
    payload: readonly ChartData[],
    label: string,
    active: boolean,
}

function CustomTooltip({payload, label, active}: Readonly<CustomTooltipProps>) {
    if (active && payload && payload.length) {
        return (
            <div
                className="custom-tooltip"
                style={{
                    border: '1px solid #d88488',
                    backgroundColor: '#000',
                    padding: '10px',
                    borderRadius: '5px',
                    boxShadow: '1px 1px 2px #d88488',
                }}
            >
                <p className="label" style={{margin: '0', fontWeight: '700'}}>{`${label}`}</p>
                <p className="label" style={{margin: '0', fontWeight: '700'}}>{`Invested: ${payload[0].value} $`}</p>
                <p className="label" style={{margin: '0', fontWeight: '700'}}>{`Value: ${payload[1].value} $`}</p>
            </div>
        );
    }

    return null;
}

export default function Chart({chartData}: Readonly<ChartProps>) {
    let color: string
    if (chartData.at(-1)?.invested > chartData.at(-1)?.value) {
        color = "red"
    } else {
        color = "green"
    }
    return (
        <LineChart className="chart"
                   responsive
                   data={chartData}>

            <Line type="monotone" dataKey="value" stroke={color} strokeWidth={3} name="Value" dot={false}/>
            <Line type="monotone" dataKey="invested" stroke="white" strokeWidth={2} name="Invested"
                  dot={false}/>
            <XAxis dataKey="date"/>
            <YAxis width="auto" label={{value: '$', position: 'insideLeft',}}/>
            <Legend align="center"/>
            <Tooltip content={CustomTooltip} defaultIndex={2} active/>
            <RechartsDevtools/>
        </LineChart>
    )
}