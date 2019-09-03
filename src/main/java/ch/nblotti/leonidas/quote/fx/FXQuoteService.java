package ch.nblotti.leonidas.quote.fx;


import ch.nblotti.leonidas.quote.Quote;
import ch.nblotti.leonidas.quote.asset.AbstractQuoteService;
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
public class FXQuoteService extends AbstractQuoteService {


  public static final String QUOTES = "quotes";
  private final static String FOREX = "FOREX";


  @Autowired
  private DateTimeFormatter dateTimeFormatter;



  @Override
  protected String getCashName() {
    return QUOTES;
  }


  public List<Quote> getFXQuotes(String currencyPair) {

    return getQuotes(FOREX, currencyPair);

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
      quote.setDate(date.format(getQuoteDateTimeFormatter()));
      return quote;

    } else {
      currencyPair = String.format("%s%s", firstCurrency, secondCurrency);
    }


    while (lastElement == null) {

      for (Iterator<Quote> collectionItr = getFXQuotes(currencyPair).iterator(); collectionItr.hasNext(); ) {

        Quote currentQuote = collectionItr.next();
        if (currentQuote.getDate().equals(localDate.format(getQuoteDateTimeFormatter()))) {
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
