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
public class QuoteService extends AbstractQuoteService {


  public static final String QUOTES = "quotes";



  @Autowired
  private DateTimeFormatter dateTimeFormatter;


  @Override
  protected String getCashName() {
    return QUOTES;
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
        if (currentQuote.getDate().equals(localDate.format(getQuoteDateTimeFormatter()))) {
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
