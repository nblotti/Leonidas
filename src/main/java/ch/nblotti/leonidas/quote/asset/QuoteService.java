package ch.nblotti.leonidas.quote.asset;


import ch.nblotti.leonidas.quote.Quote;
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
public class QuoteService {


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


  public List<Quote> getQuotes(String exchange, String symbol) {

    Map<String, List<Quote>> cachedQuotes;


    if (cacheManager.getCache(QUOTES).get(exchange) == null)
      cachedQuotes = new HashMap<>();
    else
      cachedQuotes = (Map<String, List<Quote>>) cacheManager.getCache(QUOTES).get(exchange).get();

    if (!cachedQuotes.containsKey(symbol)) {
      ResponseEntity<Quote[]> responseEntity = rt.getForEntity(String.format(quoteUrl, symbol + "." + exchange, eodApiToken), Quote[].class);

      Map<String, List<Quote>> quotesMap = new HashMap<>();
      quotesMap.put(symbol, Arrays.asList(responseEntity.getBody()));
      cacheManager.getCache(QUOTES).put(exchange, quotesMap);

    }

    return ((Map<String, List<Quote>>) cacheManager.getCache(QUOTES).get(exchange).get()).get(symbol);


  }


  @Scheduled(fixedRate = 10800000)
  public void clearCache() {
    cacheManager.getCache(QUOTES).clear();

  }


  public Quote getLastQuote(String exchange, String symbol) {

    Quote lastElement = null;

    for (Iterator<Quote> collectionItr = getQuotes(exchange, symbol).iterator(); collectionItr.hasNext(); ) {
      lastElement = collectionItr.next();
    }
    return lastElement;
  }

  //TODO NBL : test me
  /*Gestion des jours fériés et week-end : on prend le dernier disponible*/
  public Quote getQuoteForDate(String exchange, String symbol, LocalDate date) {

    Quote lastElement = null;
    LocalDate localDate = date;

    while (lastElement == null) {

      for (Iterator<Quote> collectionItr = getQuotes(exchange, symbol).iterator(); collectionItr.hasNext(); ) {

        Quote currentQuote = collectionItr.next();
        if (currentQuote.getDate().equals(localDate.format(quoteDateTimeFormatter))) {
          lastElement = currentQuote;
          break;
        }
      }
      localDate = localDate.minusDays(1);
      if (localDate.equals(dateTimeFormatter.parse("01.01.1900")))
        throw new IllegalStateException(String.format("No quotes found for symbol %s", symbol));
    }
    return lastElement;
  }



}
