package ch.nblotti.leonidas.entry.cash;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class CashEntryController {


  @Autowired
  private CashEntryService cashEntryService;


  /**
   * Look up all employees, and transform them into a REST collection resource.
   */
  @GetMapping("/cashentry")
  public Iterable<CashEntry> findAll() {

    return this.cashEntryService.findAll();

  }


  @PostMapping(value = "/cashentry")
  public CashEntry save(@Valid @RequestBody CashEntry cashEntry) { //NOSONAR

    //verifier que le compte existe
    //verifier que la valeur existe

    return this.cashEntryService.save(cashEntry);

  }


  public Iterable<CashEntry> findAllByCurrencyOrderByValueDateAsc(int account, String currency) {

    return this.cashEntryService.findAllByAccountAndCurrencyOrderByValueDateAsc(account, currency);
  }
}
