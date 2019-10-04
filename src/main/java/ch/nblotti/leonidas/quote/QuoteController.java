package ch.nblotti.leonidas.quote;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
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
    return quoteService.getQuotes(exchange, symbol).values().stream().map(i -> new QuoteDTO(dateTimeFormatter.format(quoteDateTimeFormatter.parse(i.getDate())), i.getOpen(), i.getHigh(), i.getLow(), i.getClose(), i.getAdjustedClose(), i.getVolume())).
      collect(Collectors.toList());


  }

  @GetMapping("/quotes/market/bond/{symbol}")
  public Iterable<BondDTO> getQBonduote(@PathVariable String symbol) {

    Map<LocalDate,BondDTO> test = quoteService.getBondQuotes(symbol);
    //on reformate la date
    return quoteService.getBondQuotes(symbol).values().stream().map(i -> new BondDTO(symbol, dateTimeFormatter.format(quoteDateTimeFormatter.parse(i.getDate())), i.getPrice(), i.getYield(), i.getVolume())).
      collect(Collectors.toList());


  }




}
