package ch.nblotti.leonidas.quote.fx;


import ch.nblotti.leonidas.quote.QuoteDTO;
import ch.nblotti.leonidas.quote.asset.AbstractQuoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class FXQuoteService extends AbstractQuoteService {


  public static final String QUOTES = "quotes";
  private final static String FOREX = "FOREX";



  @Override
  protected String getCashName() {
    return QUOTES;
  }


  public List<QuoteDTO> getFXQuotes(String currencyPair) {

    return getQuotes(FOREX, currencyPair);

  }

  //TODO NBL : test me
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


    while (lastElement == null) {

      for (Iterator<QuoteDTO> collectionItr = getFXQuotes(currencyPair).iterator(); collectionItr.hasNext(); ) {

        QuoteDTO currentQuoteDTO = collectionItr.next();
        if (currentQuoteDTO.getDate().equals(localDate.format(getQuoteDateTimeFormatter()))) {
          lastElement = currentQuoteDTO;
          break;
        }
      }
      localDate = localDate.minusDays(1);
      if (localDate.equals(getDateTimeFormatter().parse("01.01.1900")))
        throw new IllegalStateException(String.format("No quotes found for symbol %s", currencyPair));
    }
    return lastElement;
  }


}
