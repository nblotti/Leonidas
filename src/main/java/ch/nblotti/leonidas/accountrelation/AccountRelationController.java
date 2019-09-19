package ch.nblotti.leonidas.accountrelation;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.format.DateTimeFormatter;

@RestController
public class AccountRelationController {


  @Autowired
  private AccountRelationService accountRelationService;

  @Autowired
  DateTimeFormatter dateTimeFormatter;


  /**
   * Look up all accounts, and transform them into a REST collection resource.
   */
  @GetMapping("/accountrelation")
  public Iterable<AccountRelationPO> findAll() {

    return this.accountRelationService.findAll();

  }

  @PostMapping(value = "/accountrelation")
  public ResponseEntity save(@Valid @RequestBody AccountRelationPO accountRelationPO) {//NOSONAR

    try {
      AccountRelationPO returned = this.accountRelationService.save(accountRelationPO);
      return ResponseEntity.status(HttpStatus.OK).body(returned);

    } catch (
      IllegalStateException ex) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

  }
}
