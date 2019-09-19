package ch.nblotti.leonidas.performance;


import ch.nblotti.leonidas.process.MarketProcessService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
public class PerformanceController {


  @Autowired
  MarketProcessService marketProcessService;

  @Autowired
  private PerformanceRepository performanceRepository;

  @Autowired
  DateTimeFormatter dateTimeFormatter;


  @GetMapping(value = "/performance/{accountID}/")
  public List<PerformancePO> getTwrPerfByAccount(@PathVariable int accountID, HttpServletResponse response) {

    if (marketProcessService.isProcessForAccountRunning(accountID)) {
      response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
      return Lists.newArrayList();
    }
    return this.performanceRepository.getTwrPerfByAccount(accountID);

  }

}
