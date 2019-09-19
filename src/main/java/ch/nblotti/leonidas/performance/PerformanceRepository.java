package ch.nblotti.leonidas.performance;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;


@Transactional
@Repository
public class PerformanceRepository {


  @Autowired
  DateTimeFormatter dateTimeFormatter;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private static final String SQL = "SELECT * from  ACCOUNT_TWR_PERF WHERE ACCOUNT_ID = %s;";


  public List<PerformancePO> getTwrPerfByAccount(int accountID) {

    List<PerformancePO> performancePO = Lists.newArrayList();


    performancePO.addAll(jdbcTemplate.query(
      String.format(SQL, accountID),
      (rs, rowNum) ->
        new PerformancePO(
          rs.getInt(1),
          rs.getDate(2).toLocalDate(),
          rs.getInt(3),
          rs.getDouble(4)
        )
    ));

    return performancePO;

  }

}
