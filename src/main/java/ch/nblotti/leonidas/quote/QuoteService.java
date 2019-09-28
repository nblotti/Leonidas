package ch.nblotti.leonidas.quote;


import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component
public class QuoteService extends AbstractQuoteService {


  static final String QUOTES = "quotes";


  @Override
  protected String getCashName() {
    return QUOTES;
  }


  /*Gestion des jours fériés et week-end : on prend le dernier disponible*/
  public QuoteDTO getQuoteForDate(String exchange, String symbol, LocalDate date) {

    LocalDate localDate = date;


    Map<LocalDate, QuoteDTO> quotes = getQuotes(exchange, symbol);

    while (!quotes.containsKey(localDate)) {
      localDate = localDate.minusDays(1);
      if (localDate.equals(LocalDate.parse("01.01.1900", getDateTimeFormatter())))
        throw new IllegalStateException(String.format("No quotes found for symbol %s", symbol));
    }
    return quotes.get(localDate);

  }

}
