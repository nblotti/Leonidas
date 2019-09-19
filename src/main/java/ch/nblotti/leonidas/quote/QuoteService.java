package ch.nblotti.leonidas.quote;


import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Iterator;

@Component
public class QuoteService extends AbstractQuoteService {


  static final String QUOTES = "quotes";


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

      if (localDate.equals(LocalDate.parse("01.01.1900", getDateTimeFormatter())))
        throw new IllegalStateException(String.format("No quotes found for symbol %s", symbol));

    }
    return lastElement;
  }

}
