package ch.nblotti.leonidas.quote;


import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component(value = "fxQuoteService")
public class FXQuoteService extends AbstractQuoteService {


  static final String QUOTES = "quotes";
  static final String FOREX = "FOREX";


  @Override
  protected String getCashName() {
    return QUOTES;
  }


  public Map<LocalDate, QuoteDTO> getFXQuotes(String currencyPair) {

    return getQuotes(FOREX, currencyPair);

  }

  /*Gestion des jours fériés et week-end : on prend le dernier disponible*/
  public QuoteDTO getFXQuoteForDate(String firstCurrency, String secondCurrency, LocalDate date) {

    String currencyPair;
    QuoteDTO lastElement = null;
    LocalDate localDate = date;

    if (firstCurrency.equals(secondCurrency)) {
      QuoteDTO quoteDTO = new QuoteDTO();
      quoteDTO.setAdjustedClose("1");
      quoteDTO.setClose("1");
      quoteDTO.setHigh("1");
      quoteDTO.setLow("1");
      quoteDTO.setOpen("1");
      quoteDTO.setVolume("0");
      quoteDTO.setDate(date.format(getQuoteDateTimeFormatter()));
      return quoteDTO;

    } else {
      currencyPair = String.format("%s%s", firstCurrency, secondCurrency);
    }


    Map<LocalDate, QuoteDTO> quotes = getFXQuotes(currencyPair);

    while (!quotes.containsKey(localDate)) {
      localDate = localDate.minusDays(1);
      if (localDate.equals(LocalDate.parse("01.01.1900", getDateTimeFormatter())))
        throw new IllegalStateException(String.format("No quotes found for symbol %s", currencyPair));
    }
    return quotes.get(localDate);
  }


}
