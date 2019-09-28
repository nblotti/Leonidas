package ch.nblotti.leonidas.quote;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
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

  @Autowired
  private DateTimeFormatter dateTimeFormatter;


  @Value("${spring.application.eod.api.key}")
  private String eodApiToken;


  Map<LocalDate, QuoteDTO> getQuotes(String exchange, String symbol) {

    Map<String, Map<LocalDate, QuoteDTO>> cachedQuotes;

    if (cacheManager.getCache(getCashName()).get(exchange) == null)
      cachedQuotes = new HashMap<>();
    else
      cachedQuotes = (Map<String, Map<LocalDate, QuoteDTO>>) cacheManager.getCache(getCashName()).get(exchange).get();

    if (!cachedQuotes.containsKey(symbol)) {
      ResponseEntity<QuoteDTO[]> responseEntity = rt.getForEntity(String.format(quoteUrl, symbol + "." + exchange, eodApiToken), QuoteDTO[].class);

      List<QuoteDTO> quotes = Arrays.asList(responseEntity.getBody());

      Map<LocalDate, QuoteDTO> quotesByDate = Maps.newHashMap();
      quotes.forEach(k -> quotesByDate.put(LocalDate.parse(k.getDate(), getQuoteDateTimeFormatter()), k));

      cachedQuotes.put(symbol, quotesByDate);
      cacheManager.getCache(getCashName()).put(exchange, cachedQuotes);

    }

    return cachedQuotes.get(symbol);

  }

  @Scheduled(fixedRate = 10800000)
  public void clearCache() {
    cacheManager.getCache(getCashName()).clear();
  }


  protected abstract String getCashName();

  protected DateTimeFormatter getQuoteDateTimeFormatter() {
    return quoteDateTimeFormatter;
  }

  protected DateTimeFormatter getDateTimeFormatter() {
    return dateTimeFormatter;
  }
}
