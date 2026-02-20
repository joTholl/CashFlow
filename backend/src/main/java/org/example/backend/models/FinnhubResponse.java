package org.example.backend.models;

import java.util.List;

public record FinnhubResponse(List<FinnhubResponseData> data, String type) {
}
