package org.example.backend.services;

import java.util.*;

import org.example.backend.dtos.AppUserOutDto;
import org.example.backend.dtos.TransactionOutDto;
import org.example.backend.enums.AssetType;
import org.example.backend.models.*;
import org.example.backend.repositories.HistoricalRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;


@Service
public class HistoricalService {

    private final HistoricalRepository historicalRepository;
    private final AppUserService appUserService;
    private final RestClient restClient;
    private final TransactionService transactionService;

    public HistoricalService(HistoricalRepository historicalRepository, AppUserService appUserService, TransactionService transactionService, RestClient.Builder restClientBuilder) {
        this.historicalRepository = historicalRepository;
        this.appUserService = appUserService;
        this.transactionService = transactionService;
        this.restClient = restClientBuilder.baseUrl("https://eodhd.com/api/eod/").build();
    }

    public List<HistoricalEntry> getAllHistoricalEntries() {
        return historicalRepository.findAll();
    }

    public void addHistoricalEntries(String id) {
        List<HistoricalEntry> historicalEntries = historicalRepository.findAll();
        Map<String, HistoricalEntry> tickers = new HashMap<>();
        for (HistoricalEntry historicalEntry : historicalEntries) {
            tickers.put(historicalEntry.ticker(), historicalEntry);
        }
        AppUserOutDto user = appUserService.getAppUser(id);
        for (Asset asset : user.assets()) {
            if (!tickers.containsKey(asset.ticker()) || !tickers.get(asset.ticker()).closePrice().containsKey(LocalDate.now().minusDays(1))) {
                String ticker = asset.assetType().equals(AssetType.CRYPTO) ? asset.ticker() + "-USD.CC" : asset.ticker() + ".US";
                LocalDate to = LocalDate.now().minusDays(1);
                LocalDate from = LocalDate.now().minusYears(1);
                EODHDResponse[] eodhdResponses = restClient.get().uri(ticker + "?from=" + from + "&to=" + to + "&period=d&fmt=json&api_token=" + System.getenv("EODHD_API_TOKEN")).retrieve().toEntity(EODHDResponse[].class).getBody();
                Map<LocalDate, BigDecimal> closePrice = new HashMap<>();
                for (EODHDResponse eodhdResponse : eodhdResponses) {
                    closePrice.put(LocalDate.parse(eodhdResponse.date()), eodhdResponse.close());
                }
                for (LocalDate date = LocalDate.now().minusYears(1); date.isBefore(LocalDate.now()); date = date.plusDays(1)) {
                    if (!closePrice.containsKey(date)) {
                        try {
                            closePrice.put(date, closePrice.get(date.minusDays(1)));
                        } catch (Exception _) {
                            closePrice.put(date, eodhdResponses[0].close());
                        }
                    }
                }
                historicalRepository.save(new HistoricalEntry(asset.ticker(), closePrice));
            }
        }
    }

    public HistoricalEntry getHistoricalEntryByTicker(String ticker) {
        return historicalRepository.findById(ticker).orElseThrow(() -> new NoSuchElementException("Ticker not found!"));
    }

    public List<ChartData> getAllChartData() {
        List<TransactionOutDto> tods = transactionService.getAllTransactions();
        tods.sort(Comparator.comparing(TransactionOutDto::timestamp));
        Map<String, BigDecimal> shares = new HashMap<>();
        List<ChartData> chartDataList = new ArrayList<>();
        LocalDate from = LocalDate.now().minusYears(1);
        ChartData firstChartData = new ChartData(from, BigDecimal.ZERO, BigDecimal.ZERO);
        for (TransactionOutDto tod : tods) {
            if (tod.timestamp().isBefore(from.atStartOfDay())) {
                firstChartData = getChartData(shares, firstChartData, tod);
            } else {
                break;
            }
        }
        addToChartDataList(chartDataList, firstChartData, tods, shares);
        return chartDataList;
    }

    private ChartData getChartData(Map<String, BigDecimal> shares, ChartData chartData, TransactionOutDto tod) {
        if (shares.containsKey(tod.ticker())) {
            shares.put(tod.ticker(), shares.get(tod.ticker()).add(tod.shares()));
        } else {
            shares.put(tod.ticker(), tod.shares());
        }
        chartData = chartData.withInvested(chartData.invested().add(tod.cost().add(tod.fee())));
        return chartData;
    }

    public List<ChartData> getChartDataByTicker(String ticker) {
        List<TransactionOutDto> tods = transactionService.getAllTransactions();
        tods.sort(Comparator.comparing(TransactionOutDto::timestamp));
        Map<String, BigDecimal> shares = new HashMap<>();
        List<ChartData> chartDataList = new ArrayList<>();
        LocalDate from = LocalDate.now().minusYears(1);
        ChartData firstChartData = new ChartData(from, BigDecimal.ZERO, BigDecimal.ZERO);
        List<TransactionOutDto> filteredTods = new ArrayList<>();
        for (TransactionOutDto tod : tods) {
            if (tod.ticker().equals(ticker)) {
                filteredTods.add(tod);
            }
        }
        for (TransactionOutDto tod : filteredTods) {
            if (tod.timestamp().isBefore(from.atStartOfDay())) {
                firstChartData = getChartData(shares, firstChartData, tod);
            } else {
                break;
            }
        }
        addToChartDataList(chartDataList, firstChartData, filteredTods, shares);
        return chartDataList;

    }

    private void addToChartDataList(List<ChartData> chartDataList, ChartData firstChartData, List<TransactionOutDto> tods, Map<String, BigDecimal> shares) {
        LocalDate from = LocalDate.now().minusYears(1);
        ChartData chartData = new ChartData(from, BigDecimal.ZERO, BigDecimal.ZERO);
        for (LocalDate date = from; date.isBefore(LocalDate.now()); date = date.plusDays(1)) {
            if (date.isEqual(from)) {
                chartData = firstChartData;
            } else {
                chartData = chartData.withDate(date);
            }
            for (TransactionOutDto tod : tods) {
                if (tod.timestamp().isAfter(date.plusDays(1).atStartOfDay())) {
                    break;
                } else if (tod.timestamp().isBefore(date.plusDays(1).atStartOfDay()) &&
                        tod.timestamp().isAfter(date.atStartOfDay())) {
                    chartData = getChartData(shares, chartData, tod);
                }
                for (Map.Entry<String, BigDecimal> share : shares.entrySet()) {
                    HistoricalEntry historicalEntry = getHistoricalEntryByTicker(share.getKey());
                    chartData = chartData.withValue(share.getValue().multiply(historicalEntry.closePrice().get(date)).add(chartData.value()));
                }
                chartDataList.add(chartData);
            }
        }
    }
}
