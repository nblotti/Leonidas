package ch.nblotti.leonidas.security.quote.fx;


import ch.nblotti.leonidas.security.quote.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class FXQuoteService {


  private final static String FOREX = "FOREX";
  public static final String QUOTES = "quotes";
  @Value("${spring.application.eod.api.key}")
  private String eodApiToken;
  @Value("${spring.application.eod.quote.url}")
  private String quoteUrl;

  @Autowired
  private RestTemplate rt;

  @Autowired
  private CacheManager cacheManager;


  DateTimeFormatter quoteDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  @Autowired
  private DateTimeFormatter dateTimeFormatter;


  //TODO NBL : test me
  public List<Quote> getFXQuotes(String currencyPair) {

    Map<String, List<Quote>> cachedQuotes;


    if (cacheManager.getCache(QUOTES).get(FOREX) == null)
      cachedQuotes = new HashMap<>();
    else
      cachedQuotes = (Map<String, List<Quote>>) cacheManager.getCache(QUOTES).get(FOREX).get();

    if (!cachedQuotes.containsKey(currencyPair)) {
      ResponseEntity<Quote[]> responseEntity = rt.getForEntity(String.format(quoteUrl, currencyPair + "." + FOREX, eodApiToken), Quote[].class);

      Map<String, List<Quote>> quotesMap = new HashMap<>();
      quotesMap.put(currencyPair, Arrays.asList(responseEntity.getBody()));
      cacheManager.getCache(QUOTES).put(FOREX, quotesMap);

    }

    return ((Map<String, List<Quote>>) cacheManager.getCache(QUOTES).get(FOREX).get()).get(currencyPair);


  }


  @Scheduled(fixedRate = 10800000)
  public void clearCache() {
    cacheManager.getCache(QUOTES).clear();

  }


  //TODO NBL : test me
  /*Gestion des jours fériés et week-end : on prend le dernier disponible*/
  public Quote getFXQuoteForDate(String firstCurrency, String secondCurrency, LocalDate date) {

    String currencyPair;
    Quote lastElement = null;
    LocalDate localDate = date;

    if (firstCurrency.equals(secondCurrency)) {
      Quote quote = new Quote();
      quote.setAdjustedClose("1");
      quote.setClose("1");
      quote.setHigh("1");
      quote.setLow("1");
      quote.setOpen("1");
      quote.setVolume("0");
      quote.setDate(date.format(quoteDateTimeFormatter));
      return quote;

    } else {
      currencyPair = String.format("%s%s", firstCurrency, secondCurrency);
    }


    while (lastElement == null) {

      for (Iterator<Quote> collectionItr = getFXQuotes(currencyPair).iterator(); collectionItr.hasNext(); ) {

        Quote currentQuote = collectionItr.next();
        if (currentQuote.getDate().equals(localDate.format(quoteDateTimeFormatter))) {
          lastElement = currentQuote;
          break;
        }
      }
      localDate = localDate.minusDays(1);
      if (localDate.equals(dateTimeFormatter.parse("01.01.1900")))
        throw new IllegalStateException(String.format("No quotes found for symbol %s", currencyPair));
    }
    return lastElement;
  }


}
