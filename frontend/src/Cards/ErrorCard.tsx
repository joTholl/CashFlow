import {createPortal} from "react-dom";

type ErrorCardProps = {
    errorMsg: string;
}

export default function ErrorCard({ errorMsg}: Readonly<ErrorCardProps>) {
    return createPortal(
        <div className="error-overlay">
            <div className="error-popup" onClick={(e) => e.stopPropagation()}>
                <p>Error: {errorMsg}</p>
            </div>
        </div>,
        document.getElementById("portal-root")!
    )
}