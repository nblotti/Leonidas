package ch.nblotti.leonidas.quote.fx;


import ch.nblotti.leonidas.quote.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FXQuoteController {


  @Autowired
  FXQuoteService FXQuoteService;


  @GetMapping("/quotes/fx/{currencyPair}")
  public Iterable<Quote> getFXQuote(@PathVariable String currencyPair) {

    return FXQuoteService.getFXQuotes(currencyPair);

  }


}
