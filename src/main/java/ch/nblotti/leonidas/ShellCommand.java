package ch.nblotti.leonidas;

import ch.nblotti.leonidas.account.AccountPO;
import ch.nblotti.leonidas.accountrelation.AccountRelationPO;
import ch.nblotti.leonidas.asset.AssetPO;
import ch.nblotti.leonidas.entry.ACHAT_VENTE_TITRE;
import ch.nblotti.leonidas.order.ORDER_TYPE;
import ch.nblotti.leonidas.order.OrderPO;
import ch.nblotti.leonidas.quote.QuoteDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


@ShellComponent
public class ShellCommand {


  private static final Logger logger = Logger.getLogger("ShellCommand");

  @Value("${spring.application.order.url}")
  private String orderUrl;
  @Value("${spring.application.quote.url}")
  private String quoteUrl;
  @Value("${spring.application.asset.url}")
  private String assetUrl;
  @Value("${spring.application.account.url}")
  private String accountUrl;
  @Value("${spring.application.accountrelation.url}")
  private String accountrelationUrl;

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
    AccountPO returns;
    AccountPO a = new AccountPO();

    a.setPerformanceCurrency(pcu);
    if (!date.isEmpty()) {

      a.setEntryDate(LocalDate.parse(date, dateTimeFormatter));
      returns = rt.postForObject(accountUrl, a, AccountPO.class);
      return String.format("Created Account with id %s at date %s", returns.getId(), dateTimeFormatter.format(returns.getEntryDate()));
    } else {
      a.setEntryDate(LocalDate.now());
      returns = rt.postForObject(accountUrl, a, AccountPO.class);
      return String.format("Created Account with id %s ", returns.getId());
    }
  }

  //acc --date 15.05.2019 --pcu USD
  //acc --date 15.05.2019 --pcu USD
  //rel --date 01.11.2010 --first 1 --sec 2
  @ShellMethod("Create account relation")
  public String rel(
    @ShellOption(defaultValue = "") String date,
    @ShellOption() int first,
    @ShellOption() int sec

  ) {
    LocalDate openingDate = LocalDate.now();

    if (!date.isEmpty())
      openingDate = LocalDate.parse(date, dateTimeFormatter);
    AccountRelationPO a = new AccountRelationPO(first, sec, openingDate);

    rt.setErrorHandler(new ResponseErrorHandler() {
      @Override
      public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {

        return clientHttpResponse.getStatusCode() != HttpStatus.OK;
      }

      @Override
      public void handleError(ClientHttpResponse response) throws IOException {
        logger.fine(String.format("%s - %s", response.getStatusCode(), StreamUtils.copyToString(response.getBody(), Charset.defaultCharset())));
      }
    });
    AccountRelationPO returns = rt.postForObject(accountrelationUrl, a, AccountRelationPO.class);
    if (returns != null)
      return String.format("Created AccountRelation with id %s at date %s", returns.getId(), dateTimeFormatter.format(returns.getCreationDate()));
    else
      return "";

  }

  @ShellMethod("Duplicate account")
  public String dup(
    @ShellOption(defaultValue = "") String date,
    @ShellOption(defaultValue = "") String copy,
    @ShellOption(defaultValue = "") String pcu

  ) {
    AccountPO returns;
    AccountPO a = new AccountPO();
    a.setPerformanceCurrency(pcu);
    a.setEntryDate(LocalDate.parse(date, dateTimeFormatter));
    returns = rt.postForObject(String.format("%s/duplicateAccount/%s/", accountUrl, copy), a, AccountPO.class);
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

    OrderPO u = new OrderPO();
    u.setcIOrdID(id);
    u.setAccountId(acc);
    u.setAmount(amo);
    u.setCashCurrency(cur);
    u.setSide(ACHAT_VENTE_TITRE.fromType(side));
    u.setType(ORDER_TYPE.CASH_ENTRY);
    u.setTransactTime(ld);
    OrderPO returns = rt.postForObject(orderUrl, u, OrderPO.class);

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

    OrderPO u = new OrderPO();
    u.setcIOrdID(id);
    u.setAccountId(acc);
    u.setExchange(exch);
    u.setSymbol(symb);
    u.setSide(ACHAT_VENTE_TITRE.fromType(side));
    u.setOrderQtyData(qty);
    u.setType(ORDER_TYPE.MARKET_ORDER);
    u.setTransactTime(ld);
    OrderPO returns = rt.postForObject(orderUrl, u, OrderPO.class);


    return String.format("Created Order with id %s", returns.getId());

  }

  /* exemple : val --id 1*/
  @ShellMethod("Translate text from one language to another.")
  public void val(@ShellOption String id) {

    rt.put(String.format("%s/validate/%s", orderUrl, id), null);

  }

  @ShellMethod("read all Order")
  public void rea() {
    ResponseEntity<OrderPO[]> responseEntity = rt.getForEntity(orderUrl, OrderPO[].class);
    List<OrderPO> objects = Arrays.asList(responseEntity.getBody());

    objects.stream().forEach(i -> logger.log(Level.FINE, String.format("id: %s, symbol: %s, status %s", i.getAccountId(), i.getSymbol(), i.getStatus())));

  }

  //ex qot --symbol GSPC.INDX
  @ShellMethod("Get quote for a symbol")
  public void qot(@ShellOption String symbol) {
    ResponseEntity<QuoteDTO[]> responseEntity = rt.getForEntity(String.format("%s/%s", quoteUrl, symbol), QuoteDTO[].class);
    List<QuoteDTO> objects = Arrays.asList(responseEntity.getBody());

    objects.stream().forEach(i -> logger.log(Level.FINE, String.format("id: %s, symbol: %s, adjustedClose %s", symbol, i.getDate(), i.getAdjustedClose())));


  }

  //ex ass --symbol GO
  @ShellMethod("Get quote for a symbol")
  public void ass(@ShellOption String symbol) {
    ResponseEntity<AssetPO[]> responseEntity = rt.getForEntity(String.format("%s/%s", assetUrl, symbol), AssetPO[].class);
    List<AssetPO> objects = Arrays.asList(responseEntity.getBody());

    objects.stream().forEach(i -> logger.log(Level.FINE, String.format("code: %s, exchange %s, name %s", i.getCode(), i.getExchange(), i.getName())));


  }


}
