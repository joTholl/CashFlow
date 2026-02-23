package org.example.backend.models;

import java.util.List;

public record FinnhubSearchResponse(int count, List<FinnhubSearchResponseResult> result) {
}
