
package com.crio.warmup.stock.portfolio;

import com.crio.warmup.stock.PortfolioManagerApplication;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;
import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {


  private RestTemplate restTemplate;

  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  private Double getOpeningPriceOnStartDate(List<Candle> candle){
    return candle.get(0).getOpen();
  }

  private Double getClosingPriceOnEndDate(List<Candle> candle){
    return candle.get(candle.size()-1).getClose();
  }
  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  //CHECKSTYLE:OFF

  private AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
        double totalReturn = (sellPrice - buyPrice)/buyPrice;
        double total_num_years = ChronoUnit.DAYS.between(trade.getPurchaseDate(),endDate)/365.2422;
        double annualized_returns = Math.pow((1.0 + totalReturn),(1.0/total_num_years))-1;
      return new AnnualizedReturn(trade.getSymbol(), annualized_returns, totalReturn);
  }

  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate) throws JsonProcessingException{
        List<AnnualizedReturn> list=new ArrayList<>();
        for(PortfolioTrade t: portfolioTrades){
          List<Candle> candle = getStockQuote(t.getSymbol(), t.getPurchaseDate(), endDate);
          AnnualizedReturn annualizedReturn = calculateAnnualizedReturns(endDate, t, getOpeningPriceOnStartDate(candle), getClosingPriceOnEndDate(candle));
          list.add(annualizedReturn);
        }
        
        return list.stream().sorted(getComparator()).collect(Collectors.toList());
      }



  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {
        String url = buildUri(symbol, from, to);
        TiingoCandle[] tiingoCandles = restTemplate.getForObject(url, TiingoCandle[].class);
        if (tiingoCandles == null) {
          return new ArrayList<Candle>();
        } else {
          List<Candle> stock = Arrays.asList(tiingoCandles);
          return stock;
        }
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
       String uriTemplate = "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?startDate="
       + startDate +"&endDate=" + endDate + "&token=" + PortfolioManagerApplication.getToken();
       return uriTemplate;
  }
}
