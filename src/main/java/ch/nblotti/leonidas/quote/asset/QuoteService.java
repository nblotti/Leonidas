package ch.nblotti.leonidas.quote.asset;


import ch.nblotti.leonidas.quote.QuoteDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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


  public QuoteDTO getLastQuote(String exchange, String symbol) {

    QuoteDTO lastElement = null;

    for (Iterator<QuoteDTO> collectionItr = getQuotes(exchange, symbol).iterator(); collectionItr.hasNext(); ) {
      lastElement = collectionItr.next();
    }
    return lastElement;
  }

  //TODO NBL : test me
  /*Gestion des jours fériés et week-end : on prend le dernier disponible*/
  public QuoteDTO getQuoteForDate(String exchange, String symbol, LocalDate date) {

    QuoteDTO lastElement = null;
    LocalDate localDate = date;

    while (lastElement == null) {

      for (Iterator<QuoteDTO> collectionItr = getQuotes(exchange, symbol).iterator(); collectionItr.hasNext(); ) {

        QuoteDTO currentQuoteDTO = collectionItr.next();
        if (currentQuoteDTO.getDate().equals(localDate.format(getQuoteDateTimeFormatter()))) {
          lastElement = currentQuoteDTO;
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
