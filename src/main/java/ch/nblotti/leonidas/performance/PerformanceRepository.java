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

  private final String sql = "SELECT * from  ACCOUNT_TWR_PERF WHERE ACCOUNT_ID = %s;";


  public List<Performance> getTwrPerfByAccount(int accountID) {

    List<Performance> performance = Lists.newArrayList();


    performance.addAll(jdbcTemplate.query(
      String.format(sql, accountID),
      (rs, rowNum) ->
        new Performance(
          rs.getInt(1),
          rs.getDate(2).toLocalDate(),
          rs.getInt(3),
          rs.getDouble(4)
        )
    ));

    return performance;

  }

}
