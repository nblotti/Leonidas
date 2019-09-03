package ch.nblotti.leonidas.quote.asset;

import ch.nblotti.leonidas.quote.QuoteDTO;
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


  protected List<QuoteDTO> getQuotes(String exchange, String symbol) {

    Map<String, List<QuoteDTO>> cachedQuotes;


    if (cacheManager.getCache(getCashName()).get(exchange) == null)
      cachedQuotes = new HashMap<>();
    else
      cachedQuotes = (Map<String, List<QuoteDTO>>) cacheManager.getCache(getCashName()).get(exchange).get();

    if (!cachedQuotes.containsKey(symbol)) {
      ResponseEntity<QuoteDTO[]> responseEntity = rt.getForEntity(String.format(quoteUrl, symbol + "." + exchange, eodApiToken), QuoteDTO[].class);

      Map<String, List<QuoteDTO>> quotesMap = new HashMap<>();
      quotesMap.put(symbol, Arrays.asList(responseEntity.getBody()));
      cacheManager.getCache(getCashName()).put(exchange, quotesMap);

    }

    return ((Map<String, List<QuoteDTO>>) cacheManager.getCache(getCashName()).get(exchange).get()).get(symbol);

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
