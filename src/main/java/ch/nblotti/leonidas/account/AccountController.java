package ch.nblotti.leonidas.account;


import ch.nblotti.leonidas.process.MarketProcessService;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
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
  public Iterable<AccountPO> findAll() {

    return this.accountService.findAll();

  }

  @PostMapping(value = "/account")
  public AccountPO save(@Valid @RequestBody AccountPO accountPO) {//NOSONAR


    return  this.accountService.save(accountPO);

  }

  @PostMapping(value = "/account/duplicateAccount/{id}/")
  public AccountPO duplicateAccount(@PathVariable int id, @Valid @RequestBody AccountPO accountPO, HttpServletResponse response) {

    if (marketProcessService.isProcessForAccountRunning(id)) {
      response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
      return null;
    }

    return accountService.duplicateAccount(id, accountPO);
  }

  @PostMapping(value = "/account/{id}/")
  public AccountPO findAccountByID(@PathVariable String id) throws NotFoundException {


    AccountPO createdAccountPO = this.accountService.findAccountById(Integer.valueOf(id));
    if (createdAccountPO == null)
      throw new NotFoundException(id);


    return createdAccountPO;
  }

}
