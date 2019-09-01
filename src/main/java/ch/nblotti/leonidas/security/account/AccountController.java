package ch.nblotti.leonidas.security.account;


import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

@RestController
public class AccountController {


  private final static Logger LOGGER = Logger.getLogger("AccountController");

  @Autowired
  private AccountService accountService;

  @Autowired
  DateTimeFormatter dateTimeFormatter;


  /**
   * Look up all accounts, and transform them into a REST collection resource.
   */
  @GetMapping("/accounts")
  public Iterable<Account> findAll() {

    return this.accountService.findAll();

  }

  @RequestMapping(value = "/account", method = RequestMethod.POST)
  public Account save(@Valid @RequestBody Account account) {


    Account createdAccount = this.accountService.save(account);


    return createdAccount;
  }

  @RequestMapping(value = "/account/duplicateAccount/{id}/{date}", method = RequestMethod.POST)
  public Account duplicateAccount(@PathVariable String id, @PathVariable String date) {


    return accountService.duplicateAccount(Integer.valueOf(id), LocalDate.parse(date, dateTimeFormatter));
  }


  @RequestMapping(value = "/account//{id}/", method = RequestMethod.POST)
  public Account findAccountByID(@PathVariable String id) throws NotFoundException {


    Account createdAccount = this.accountService.findAccountById(Integer.valueOf(id));
    if (createdAccount == null)
      throw new NotFoundException(id);


    return createdAccount;
  }

}
