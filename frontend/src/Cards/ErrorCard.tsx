
type ErrorCardProps = {
    errorMsg: string;
    onClose: () => void;
}

export default function ErrorCard({ errorMsg, onClose }: Readonly<ErrorCardProps>) {
    return (
        <div className="error-overlay">
            <div className="error-popup">
                <p>⚠ {errorMsg}</p>
                <button onClick={onClose}>OK</button>
            </div>
        </div>
    )
}