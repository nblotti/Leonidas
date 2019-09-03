package ch.nblotti.leonidas;

import ch.nblotti.leonidas.account.Account;
import ch.nblotti.leonidas.asset.Asset;
import ch.nblotti.leonidas.quote.Quote;
import ch.nblotti.leonidas.entry.DEBIT_CREDIT;
import ch.nblotti.leonidas.order.ORDER_TYPE;
import ch.nblotti.leonidas.order.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


@ShellComponent
public class ShellCommand {


  private static final Logger LOGGER = Logger.getLogger("ShellCommand");

  @Value("${spring.application.order.url}")
  private String orderUrl;
  @Value("${spring.application.quote.url}")
  private String quoteUrl;
  @Value("${spring.application.asset.url}")
  private String assetUrl;
  @Value("${spring.application.account.url}")
  private String accountUrl;

  @Autowired
  DateTimeFormatter dateTimeFormatter;

  @Autowired
  private RestTemplate rt;


  /* exemple :
  acc --date 15.06.2019 --copy 1
 * */
  @ShellMethod("Create account")
  public String acc(
    @ShellOption(defaultValue = "") String date,
    @ShellOption(defaultValue = "CHF") String pcu

  ) {
    Account returns;
    Account a = new Account();

    a.setPerformanceCurrency(pcu);
    if (!date.isEmpty()) {

      a.setEntryDate(LocalDate.parse(date, dateTimeFormatter));
      returns = rt.postForObject(accountUrl, a, Account.class);
      return String.format("Created Account with id %s at date %s", returns.getId(), dateTimeFormatter.format(returns.getEntryDate()));
    } else {
      a.setEntryDate(LocalDate.now());
      returns = rt.postForObject(accountUrl, a, Account.class);
      return String.format("Created Account with id %s ", returns.getId());
    }
  }

  @ShellMethod("Duplicate account")
  public String dup(
    @ShellOption(defaultValue = "") String date,
    @ShellOption(defaultValue = "") String copy,
    @ShellOption(defaultValue = "") String pcu

  ) {
    Account returns;
    Account a = new Account();
    a.setPerformanceCurrency(pcu);
    a.setEntryDate(LocalDate.parse(date, dateTimeFormatter));
    returns = rt.postForObject(String.format("%sduplicateAccount/%s/", accountUrl, copy), a, Account.class);
    return String.format("Duplicated Account with id %s at date %s", returns.getId(), dateTimeFormatter.format(returns.getEntryDate()));
  }


  @ShellMethod("Create cash transaction")
  public String ordc(
    @ShellOption String id,
    @ShellOption int acc,
    @ShellOption String cur,
    @ShellOption int side,
    @ShellOption int amo,
    @ShellOption(defaultValue = "-1") String dtv) {

    LocalDate ld = dtv.equalsIgnoreCase("-1") ? LocalDate.now() : LocalDate.parse(dtv, dateTimeFormatter);

    Order u = new Order();
    u.setcIOrdID(id);
    u.setAccountId(acc);
    u.setAmount(amo);
    u.setCashCurrency(cur);
    u.setSide(DEBIT_CREDIT.fromType(side));
    u.setType(ORDER_TYPE.CASH_ENTRY);
    u.setTransactTime(ld);
    Order returns = rt.postForObject(orderUrl, u, Order.class);

    return String.format("Created Order with id %s", returns.getId());

  }

  /* exemple :
  acc
  ordm --acc 1 --id test1 --exch US --symb FB --side 2 --qty 100 --dtv 07.06.2019
  ordm --acc 1 --id test1 --exch US --symb FB --side 2 --qty 100 --dtv 11.06.2019
  ordm --acc 1 --id test1 --exch US --symb FB --side 2 --qty 50 --dtv 25.06.2019
  ordm --acc 1 --id test1 --exch US --symb FB --side 1 --qty 200 --dtv 12.07.2019


    ordm --acc 1 --id test1 --exch PA --symb AI --side 2 --qty 100 --dtv 07.06.2019
  * */
  @ShellMethod("Create order")
  public String ordm(
    @ShellOption String id,
    @ShellOption int acc,
    @ShellOption String exch,
    @ShellOption String symb,
    @ShellOption int side,
    @ShellOption int qty,
    @ShellOption(defaultValue = "-1") String dtv

  ) {

    LocalDate ld = dtv.equalsIgnoreCase("-1") ? LocalDate.now() : LocalDate.parse(dtv, dateTimeFormatter);

    Order u = new Order();
    u.setcIOrdID(id);
    u.setAccountId(acc);
    u.setExchange(exch);
    u.setSymbol(symb);
    u.setSide(DEBIT_CREDIT.fromType(side));
    u.setOrderQtyData(qty);
    u.setType(ORDER_TYPE.MARKET_ORDER);
    u.setTransactTime(ld);
    Order returns = rt.postForObject(orderUrl, u, Order.class);


    return String.format("Created Order with id %s", returns.getId());

  }

  /* exemple : val --id 1*/
  @ShellMethod("Translate text from one language to another.")
  public void val(@ShellOption String id) {

    rt.put(String.format("%s/validate/%s", orderUrl, id), null);

  }

  @ShellMethod("read all Order")
  public void rea() {
    ResponseEntity<Order[]> responseEntity = rt.getForEntity(orderUrl, Order[].class);
    List<Order> objects = Arrays.asList(responseEntity.getBody());

    objects.stream().forEach(i -> LOGGER.log(Level.FINE, String.format("id: %s, symbol: %s, status %s", i.getAccountId(), i.getSymbol(), i.getStatus())));

  }

  //ex qot --symbol GSPC.INDX
  @ShellMethod("Get quote for a symbol")
  public void qot(@ShellOption String symbol) {
    ResponseEntity<Quote[]> responseEntity = rt.getForEntity(String.format("%s/%s", quoteUrl, symbol), Quote[].class);
    List<Quote> objects = Arrays.asList(responseEntity.getBody());

    objects.stream().forEach(i -> LOGGER.log(Level.FINE, String.format("id: %s, symbol: %s, adjustedClose %s", symbol, i.getDate(), i.getAdjustedClose())));


  }

  //ex ass --symbol GO
  @ShellMethod("Get quote for a symbol")
  public void ass(@ShellOption String symbol) {
    ResponseEntity<Asset[]> responseEntity = rt.getForEntity(String.format("%s/%s", assetUrl, symbol), Asset[].class);
    List<Asset> objects = Arrays.asList(responseEntity.getBody());

    objects.stream().forEach(i -> LOGGER.log(Level.FINE, String.format("code: %s, exchange %s, name %s", i.getCode(), i.getExchange(), i.getName())));


  }


}
