package ch.nblotti.leonidas.quote;


import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class BondQuoteService {

  static final String BONDS_QUOTES = "bondsquote";

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




  protected String getCashName() {
    return BONDS_QUOTES;
  }


  /*Gestion des jours fériés et week-end : on prend le dernier disponible*/
  public BondQuoteDTO getQuoteForDate(String symbol, LocalDate date) {

    LocalDate localDate = date;


    Map<LocalDate, BondQuoteDTO> quotes = getBondQuotes(symbol);

    while (!quotes.containsKey(localDate)) {
      localDate = localDate.minusDays(1);
      if (localDate.equals(LocalDate.parse("01.01.1900", getDateTimeFormatter())))
        throw new IllegalStateException(String.format("No quotes found for symbol %s", symbol));
    }
    return quotes.get(localDate);

  }


  Map<LocalDate, BondQuoteDTO> getBondQuotes(String symbol) {


    if (cacheManager.getCache(getCashName()).get(symbol) != null)
      return ((Map<LocalDate, BondQuoteDTO>) cacheManager.getCache(getCashName()).get(symbol).get());


    ResponseEntity<BondQuoteDTO[]> responseEntity = rt.getForEntity(String.format(quoteUrl, symbol + ".BOND", eodApiToken), BondQuoteDTO[].class);

    List<BondQuoteDTO> quotes = Arrays.asList(responseEntity.getBody());

    Map<LocalDate, BondQuoteDTO> quotesByDate = Maps.newHashMap();
    quotes.forEach(k -> quotesByDate.put(LocalDate.parse(k.getDate(), getQuoteDateTimeFormatter()), k));

    cacheManager.getCache(getCashName()).put(symbol, quotesByDate);


    return quotesByDate;

  }


  @Scheduled(fixedRate = 10800000)
  public void clearCache() {
    cacheManager.getCache(getCashName()).clear();
  }

  protected DateTimeFormatter getQuoteDateTimeFormatter() {
    return quoteDateTimeFormatter;
  }

  protected DateTimeFormatter getDateTimeFormatter() {
    return dateTimeFormatter;
  }

}
