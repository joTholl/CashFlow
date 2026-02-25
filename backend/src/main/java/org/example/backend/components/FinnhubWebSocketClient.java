package org.example.backend.components;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.backend.models.FinnhubResponse;
import org.example.backend.models.FinnhubResponseData;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
@RequiredArgsConstructor
public class FinnhubWebSocketClient implements WebSocket.Listener {

    private WebSocket webSocket;
    private final LivePriceStore livePriceStore;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<String> symbolsToSubscribe = new CopyOnWriteArrayList<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(FinnhubWebSocketClient.class.getName());


    @PostConstruct
    public void connect() {
        String uri = "";
        if (System.getenv("FINNHUB_API_TOKEN") != null && !System.getenv("FINNHUB_API_TOKEN").isBlank()) {
            uri = "wss://ws.finnhub.io?token=" + System.getenv("FINNHUB_API_TOKEN");
        }
        try {
            httpClient.newWebSocketBuilder()
                    .buildAsync(
                            URI.create(uri), this
                    )
                    .thenAccept(ws ->
                            this.webSocket = ws
                    );
        } catch (Exception e) {
            LOGGER.error("Could not connect to Finnhub API endpoint", e);
        }
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket,
                                     CharSequence data,
                                     boolean last) {
        try {
            FinnhubResponse finnhubResponse = mapper.readValue(data.toString(), FinnhubResponse.class);
            if (finnhubResponse.data() != null) {
                for (FinnhubResponseData finnhubResponseData : finnhubResponse.data()) {
                    livePriceStore.updatePrice(finnhubResponseData.s(), finnhubResponseData.p());
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Could not parse Finnhub response", e);
        }
        webSocket.request(1);
        return null;
    }

    public void addSymbol(String symbol) {
        if (!symbolsToSubscribe.contains(symbol)) {
            symbolsToSubscribe.add(symbol);

            if (webSocket != null) {
                sendSubscribe(symbol);
            }
        }
    }

    private void sendSubscribe(String symbol) {
        String message = String.format("{'type':'subscribe','symbol':'%s'}", symbol);
        webSocket.sendText(message, true);
    }

    public void removeSymbol(String symbol) {
        if (symbolsToSubscribe.contains(symbol)) {
            symbolsToSubscribe.remove(symbol);

            if (webSocket != null) {
                sendUnsubscribe(symbol);
            }
        }
    }

    private void sendUnsubscribe(String symbol) {
        String message = String.format("{'type':'unsubscribe','symbol':'%s'}", symbol);
        webSocket.sendText(message, true);
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        LOGGER.info("Connected!");
        this.webSocket = webSocket;

        for (String symbol : symbolsToSubscribe) {
            sendSubscribe(symbol);
        }

        webSocket.request(1);
    }


    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        LOGGER.info("Connection closed: {} ({})", reason, statusCode);
        reconnect();
        return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        LOGGER.error("WebSocket error!", error);
        reconnect();
    }

    private void reconnect() {
        CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS).execute(this::connect);
    }
}
