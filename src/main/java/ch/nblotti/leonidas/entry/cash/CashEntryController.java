package ch.nblotti.leonidas.entry.cash;


import ch.nblotti.leonidas.entry.EntryPO;
import ch.nblotti.leonidas.order.ORDER_TYPE;
import ch.nblotti.leonidas.process.order.ORDER_EVENTS;
import ch.nblotti.leonidas.process.order.ORDER_STATES;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
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
  public Iterable<CashEntryPO> findAll() {

    return this.cashEntryService.findAll();

  }


  @PostMapping(value = "/cashentry")
  public EntryPO save(@Valid @RequestBody CashEntryPO cashEntryTO) { //NOSONAR

    //verifier que le compte existe
    //verifier que la valeur existe


    return this.cashEntryService.save(cashEntryTO);

  }


  public Iterable<CashEntryPO> findAllByCurrencyOrderByValueDateAsc(int account, String currency) {

    return this.cashEntryService.findAllByAccountAndCurrencyOrderByValueDateAsc(account, currency);
  }
}
