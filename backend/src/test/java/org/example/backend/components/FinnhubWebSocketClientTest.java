package org.example.backend.components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.http.WebSocket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.mockito.Mockito.*;

class FinnhubWebSocketClientTest {

    private final WebSocket webSocket = mock(WebSocket.class);
    private final LivePriceStore livePriceStore = mock(LivePriceStore.class);
    private final List<String> symbolsToSubscribe = new CopyOnWriteArrayList<>();
    private final FinnhubWebSocketClient finnhubWebSocketClient = new FinnhubWebSocketClient(livePriceStore);



    @BeforeEach
    void setUp() {
        symbolsToSubscribe.removeAll(symbolsToSubscribe);
    }

    @Test
    void onText_shouldReceiveAndParseJSON() {
        CharSequence data = """
                {
                  "data": [
                    {
                      "p": 7296.89,
                      "s": "BINANCE:BTCUSDT",
                      "t": 1575526691134,
                      "v": 0.011467
                    },
                    {
                      "p": 7296.90,
                      "s": "BINANCE:BTCUSDT",
                      "t": 1575526691154,
                      "v": 0.011467
                    }
                  ],
                  "type": "trade"
                }""";
        finnhubWebSocketClient.onText(webSocket, data, true);
        verify(livePriceStore, times(2)).updatePrice(anyString(), any(BigDecimal.class));
        verify(webSocket).request(1);
    }

    @Test
    void onText_shouldCatchException() {
        CharSequence data = """
                {
                  "data": [
                    {
                      "a": 7296.89,
                      "c": "BINANCE:BTCUSDT",
                      "u": 1575526691134,
                      "z": 0.011467
                    },
                    {
                      "p": 7296.90,
                      "s": "BINANCE:BTCUSDT",
                      "t": 1575526691154,
                      "v": 0.011467
                    }
                  ],
                  "type": "trade"
                }""";
        finnhubWebSocketClient.onText(webSocket, data, true);
        verify(livePriceStore, never()).updatePrice(anyString(), any(BigDecimal.class));
        verify(webSocket, times(1)).request(1);
    }

    @Test
    void addSymbol_shouldAddAndSubscribe() {
        finnhubWebSocketClient.onOpen(webSocket);
        finnhubWebSocketClient.addSymbol("BINANCE:BTCUSDT");
        verify(webSocket).sendText("""
                {"type":"subscribe","symbol":"BINANCE:BTCUSDT"}""", true);
    }

    @Test
    void addSymbol_shouldAddAndSubscribeOnce_whenCalledWithDuplicate() {
        finnhubWebSocketClient.onOpen(webSocket);
        finnhubWebSocketClient.addSymbol("BINANCE:BTCUSDT");
        finnhubWebSocketClient.addSymbol("BINANCE:BTCUSDT");
        verify(webSocket, times(1)).sendText("""
                {"type":"subscribe","symbol":"BINANCE:BTCUSDT"}""", true);
    }

    @Test
    void addSymbol_shouldNotAddAndSubscribe_whenWebSocketIsNull() {
        finnhubWebSocketClient.addSymbol("BINANCE:BTCUSDT");
        verify(webSocket, never()).sendText(anyString(), anyBoolean());
    }

    @Test
    void removeSymbol_shouldRemoveAndUnsubscribe() {
        finnhubWebSocketClient.onOpen(webSocket);
        finnhubWebSocketClient.addSymbol("BINANCE:BTCUSDT");
        finnhubWebSocketClient.removeSymbol("BINANCE:BTCUSDT");
        verify(webSocket, times(2)).sendText(anyString(), anyBoolean());
        verify(webSocket).request(1);
    }

    @Test
    void removeSymbol_shouldDoNothing_whenSymbolDoesNotExist() {
        finnhubWebSocketClient.onOpen(webSocket);
        finnhubWebSocketClient.removeSymbol("BINANCE:BTCUSDT");
        verify(webSocket, never()).sendText(anyString(), anyBoolean());
        verify(webSocket).request(1);
    }

    @Test
    void removeSymbol_shouldDoNothing_whenWebSocketIsNull() {
        finnhubWebSocketClient.removeSymbol("BINANCE:BTCUSDT");
        verify(webSocket, never()).sendText(anyString(), anyBoolean());
        verify(webSocket, never()).request(1);
    }

    @Test
    void onOpen_shouldSubscribeAllSymbolsAndRequest1() {
        WebSocket mockWebSocket = mock(WebSocket.class);
        finnhubWebSocketClient.addSymbol("AAPL");
        finnhubWebSocketClient.addSymbol("MSFT");
        finnhubWebSocketClient.onOpen(mockWebSocket);
        verify(mockWebSocket).sendText("{\"type\":\"subscribe\",\"symbol\":\"AAPL\"}", true);
        verify(mockWebSocket).sendText("{\"type\":\"subscribe\",\"symbol\":\"MSFT\"}", true);
        verify(mockWebSocket).request(1);
    }

}