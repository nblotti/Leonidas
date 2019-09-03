package ch.nblotti.leonidas.quote.asset;


import ch.nblotti.leonidas.quote.QuoteDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class QuoteController {


  @Autowired
  QuoteService quoteService;

  @Autowired
  private DateTimeFormatter dateTimeFormatter;

  DateTimeFormatter quoteDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


  @GetMapping("/quotes/market/{exchange}/{symbol}")
  public Iterable<QuoteDTO> getQuote(@PathVariable String exchange, @PathVariable String symbol) {


    //on reformate la date
    List<QuoteDTO> transformedQuoteDTOS = quoteService.getQuotes(exchange, symbol).stream().map(i -> new QuoteDTO(dateTimeFormatter.format(quoteDateTimeFormatter.parse(i.getDate())), i.getOpen(), i.getHigh(), i.getLow(), i.getClose(), i.getAdjustedClose(), i.getVolume())).
      collect(Collectors.toList());
    return transformedQuoteDTOS;

    //on reformate la date

  }



}