package ch.nblotti.leonidas.security.performance;


import ch.nblotti.leonidas.security.account.Account;
import ch.nblotti.leonidas.security.account.AccountService;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjuster;
import java.util.List;
import java.util.logging.Logger;

import static java.time.temporal.TemporalAdjusters.firstDayOfYear;

@RestController
public class PerformanceController {


  private final static Logger LOGGER = Logger.getLogger("PerformanceController");

  @Autowired
  private PerformanceRepository performanceRepository;

  @Autowired
  DateTimeFormatter dateTimeFormatter;


  @GetMapping(value = "/performance/ytd/{accountID}/")
  public List<Performance> getTwrPerfByAccount(@PathVariable int accountID) throws NotFoundException {


    return this.performanceRepository.getTwrPerfByAccount( accountID);

  }

}
