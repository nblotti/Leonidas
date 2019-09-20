package ch.nblotti.leonidas.quote;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FXQuoteController {


  @Autowired
  FXQuoteService fXQuoteService;


  @GetMapping("/quotes/fx/{currencyPair}")
  public Iterable<QuoteDTO> getFXQuote(@PathVariable String currencyPair) {

    return fXQuoteService.getFXQuotes(currencyPair).values();

  }


}
