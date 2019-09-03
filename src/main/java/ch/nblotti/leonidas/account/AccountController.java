package ch.nblotti.leonidas.account;


import ch.nblotti.leonidas.process.MarketProcessService;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
public class AccountController {



  @Autowired
  private AccountService accountService;

  @Autowired
  DateTimeFormatter dateTimeFormatter;



  @Autowired
  MarketProcessService marketProcessService;
  /**
   * Look up all accounts, and transform them into a REST collection resource.
   */
  @GetMapping("/accounts")
  public Iterable<Account> findAll() {

    return this.accountService.findAll();

  }

  @PostMapping(value = "/account")
  public Account save(@Valid @RequestBody Account account) {//NOSONAR


    return  this.accountService.save(account);

  }

  @PostMapping(value = "/account/duplicateAccount/{id}/")
  public Account duplicateAccount(@PathVariable int id, @Valid @RequestBody Account account, HttpServletResponse response) {

    if (marketProcessService.isProcessForAccountRunning(id)) {
      response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
      return null;
    }

    return accountService.duplicateAccount(id, account);
  }

  @PostMapping(value = "/account/{id}/")
  public Account findAccountByID(@PathVariable String id) throws NotFoundException {


    Account createdAccount = this.accountService.findAccountById(Integer.valueOf(id));
    if (createdAccount == null)
      throw new NotFoundException(id);


    return createdAccount;
  }

}
