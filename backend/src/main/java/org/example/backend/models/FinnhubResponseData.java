package org.example.backend.models;

import java.math.BigDecimal;

public record FinnhubResponseData(BigDecimal p, String s, long t, double v) {
}
