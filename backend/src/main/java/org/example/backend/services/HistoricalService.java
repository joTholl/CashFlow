package org.example.backend.services;

import java.util.*;

import org.example.backend.components.LivePriceStore;
import org.example.backend.models.*;

import org.example.backend.dtos.AppUserOutDto;
import org.example.backend.dtos.TransactionOutDto;
import org.example.backend.enums.AssetType;
import org.example.backend.repositories.HistoricalRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Collectors;


@Service
public class HistoricalService {

    private final HistoricalRepository historicalRepository;
    private final AppUserService appUserService;
    private final RestClient restClient;
    private final TransactionService transactionService;
    private final HelperService helperService;
    private final LivePriceStore livePriceStore;

    public HistoricalService(HistoricalRepository historicalRepository, AppUserService appUserService, TransactionService transactionService, HelperService helperService, RestClient.Builder restClientBuilder, LivePriceStore livePriceStore) {
        this.historicalRepository = historicalRepository;
        this.appUserService = appUserService;
        this.transactionService = transactionService;
        this.helperService = helperService;
        this.restClient = restClientBuilder.baseUrl("https://eodhd.com/api/eod/").build();
        this.livePriceStore = livePriceStore;
    }

    public List<HistoricalEntry> getAllHistoricalEntries() {
        return historicalRepository.findAll();
    }

    public HistoricalEntry getHistoricalEntryByTicker(String ticker) {
        return historicalRepository.findById(ticker).orElseThrow(() -> new NoSuchElementException("Ticker " + ticker + "  not found!"));
    }

    public void addHistoricalEntries(String userId) {
        List<HistoricalEntry> historicalEntries = historicalRepository.findAll();
        Map<String, HistoricalEntry> tickers = new HashMap<>();
        LocalDate to = helperService.getLocalDateNow().minusDays(1);
        LocalDate from = helperService.getLocalDateNow().minusYears(1);
        for (HistoricalEntry historicalEntry : historicalEntries) {
            tickers.put(historicalEntry.ticker(), historicalEntry);
        }
        AppUserOutDto user = appUserService.getAppUser(userId);
        for (Asset asset : user.assets()) {
            if (!tickers.containsKey(asset.ticker()) || !tickers.get(asset.ticker()).closePrice().containsKey(to)) {
                String ticker = asset.assetType().equals(AssetType.CRYPTO) ? asset.ticker() + "-USD.CC" : asset.ticker() + ".US";
                EODHDResponse[] eodhdResponses = restClient.get().uri(ticker + "?from=" + from + "&to=" + to + "&period=d&fmt=json&api_token=" + System.getenv("EODHD_API_TOKEN")).retrieve().toEntity(EODHDResponse[].class).getBody();
                Map<LocalDate, BigDecimal> closePrice = new HashMap<>();
                fillClosePriceMap(eodhdResponses, closePrice);
                historicalRepository.save(new HistoricalEntry(asset.ticker(), closePrice));

            }
        }
    }

    private void fillClosePriceMap(EODHDResponse[] eodhdResponses, Map<LocalDate, BigDecimal> closePrice) {
        for (EODHDResponse eodhdResponse : eodhdResponses) {
            closePrice.put(LocalDate.parse(eodhdResponse.date()), eodhdResponse.close());
        }
        for (LocalDate date = helperService.getLocalDateNow().minusYears(1).plusDays(1); date.isBefore(helperService.getLocalDateNow()); date = date.plusDays(1)) {
            if (!closePrice.containsKey(date)) {
                try {
                    closePrice.put(date, closePrice.get(date.minusDays(1)));
                } catch (Exception _) {
                    closePrice.put(date, eodhdResponses[0].close());
                }
            }
        }
    }

    public List<ChartData> getAllChartData() {
        List<TransactionOutDto> tods = new ArrayList<>(transactionService.getAllTransactions());
        tods.sort(Comparator.comparing(TransactionOutDto::timestamp));
        Map<String, BigDecimal> shares = new HashMap<>();
        List<ChartData> chartDataList = new ArrayList<>();
        LocalDate from = helperService.getLocalDateNow().minusYears(1).plusDays(1);
        ChartData firstChartData = new ChartData(from, BigDecimal.ZERO, BigDecimal.ZERO);
        for (TransactionOutDto tod : tods) {
            if (tod.timestamp().isBefore(from.atStartOfDay())) {
                firstChartData = fillSharesAndChartData(shares, firstChartData, tod);
            } else {
                break;
            }
        }
        addToChartDataList(chartDataList, firstChartData, tods, shares);
        return chartDataList;
    }

    public List<ChartData> getChartDataByTicker(String ticker) {
        List<TransactionOutDto> tods = new ArrayList<>(transactionService.getAllTransactions());
        tods.sort(Comparator.comparing(TransactionOutDto::timestamp));
        Map<String, BigDecimal> shares = new HashMap<>();
        List<ChartData> chartDataList = new ArrayList<>();
        LocalDate from = helperService.getLocalDateNow().minusYears(1).plusDays(1);
        ChartData firstChartData = new ChartData(from, BigDecimal.ZERO, BigDecimal.ZERO);
        List<TransactionOutDto> filteredTods = new ArrayList<>();
        for (TransactionOutDto tod : tods) {
            if (tod.ticker().equals(ticker)) {
                filteredTods.add(tod);
            }
        }
        for (TransactionOutDto tod : filteredTods) {
            if (tod.timestamp().isBefore(from.atStartOfDay())) {
                firstChartData = fillSharesAndChartData(shares, firstChartData, tod);
            } else {
                break;
            }
        }
        addToChartDataList(chartDataList, firstChartData, filteredTods, shares);
        return chartDataList;

    }

    private ChartData fillSharesAndChartData(Map<String, BigDecimal> shares, ChartData chartData, TransactionOutDto tod) {
        if (shares.containsKey(tod.ticker())) {
            shares.put(tod.ticker(), shares.get(tod.ticker()).add(tod.shares()));
        } else {
            shares.put(tod.ticker(), tod.shares());
        }
        chartData = chartData.withInvested(chartData.invested().add(tod.cost().add(tod.fee())));
        return chartData;
    }

    private void addToChartDataList(List<ChartData> chartDataList, ChartData firstChartData, List<TransactionOutDto> tods, Map<String, BigDecimal> shares) {
        LocalDate from = helperService.getLocalDateNow().minusYears(1).plusDays(1);
        ChartData chartData = new ChartData(from, BigDecimal.ZERO, BigDecimal.ZERO);
        Map<String, HistoricalEntry> historicalEntryMap = getAllHistoricalEntries().stream().collect(Collectors.toMap(HistoricalEntry::ticker, entry -> entry));
        livePriceStore.safeUpdatePrices(historicalEntryMap, helperService.getLocalDateNow().minusDays(1));
        for (LocalDate date = from; date.isBefore(helperService.getLocalDateNow()); date = date.plusDays(1)) {
            if (date.isEqual(from)) {
                chartData = firstChartData;
            } else {
                chartData = new ChartData(date, chartData.invested(), BigDecimal.ZERO);
            }
            for (TransactionOutDto tod : tods) {
                if (tod.timestamp().isAfter(date.plusDays(1).atStartOfDay())) {
                    break;
                } else if (tod.timestamp().isBefore(date.plusDays(1).atStartOfDay()) &&
                        tod.timestamp().isAfter(date.atStartOfDay())) {
                    chartData = fillSharesAndChartData(shares, chartData, tod);
                }
            }
            for (Map.Entry<String, BigDecimal> share : shares.entrySet()) {
                HistoricalEntry historicalEntry = historicalEntryMap.get(share.getKey());
                chartData = chartData.withValue(share.getValue().multiply(historicalEntry.closePrice().get(date)).add(chartData.value()));
            }
            chartDataList.add(chartData);
        }
    }
}
