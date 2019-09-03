package ch.nblotti.leonidas.performance;


import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
public class PerformanceController {



  @Autowired
  private PerformanceRepository performanceRepository;

  @Autowired
  DateTimeFormatter dateTimeFormatter;


  @GetMapping(value = "/performance/{accountID}/")
  public List<PerformancePO> getTwrPerfByAccount(@PathVariable int accountID) throws NotFoundException {


    return this.performanceRepository.getTwrPerfByAccount( accountID);

  }

}
