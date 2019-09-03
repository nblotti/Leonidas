package ch.nblotti.leonidas.quote.asset;

import ch.nblotti.leonidas.quote.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractQuoteService {

  @Value("${spring.application.eod.quote.url}")
  private String quoteUrl;

  @Autowired
  private RestTemplate rt;

  @Autowired
  private CacheManager cacheManager;

  @Autowired
  private DateTimeFormatter quoteDateTimeFormatter;


  @Value("${spring.application.eod.api.key}")
  private String eodApiToken;


  protected List<Quote> getQuotes(String exchange, String symbol) {

    Map<String, List<Quote>> cachedQuotes;


    if (cacheManager.getCache(getCashName()).get(exchange) == null)
      cachedQuotes = new HashMap<>();
    else
      cachedQuotes = (Map<String, List<Quote>>) cacheManager.getCache(getCashName()).get(exchange).get();

    if (!cachedQuotes.containsKey(symbol)) {
      ResponseEntity<Quote[]> responseEntity = rt.getForEntity(String.format(quoteUrl, symbol + "." + exchange, eodApiToken), Quote[].class);

      Map<String, List<Quote>> quotesMap = new HashMap<>();
      quotesMap.put(symbol, Arrays.asList(responseEntity.getBody()));
      cacheManager.getCache(getCashName()).put(exchange, quotesMap);

    }

    return ((Map<String, List<Quote>>) cacheManager.getCache(getCashName()).get(exchange).get()).get(symbol);

  }

  @Scheduled(fixedRate = 10800000)
  public void clearCache() {
    cacheManager.getCache(getCashName()).clear();
  }


  protected DateTimeFormatter getQuoteDateTimeFormatter() {
    return quoteDateTimeFormatter;
  }

  protected abstract String getCashName();
}
