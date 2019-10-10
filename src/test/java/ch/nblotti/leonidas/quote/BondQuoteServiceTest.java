package ch.nblotti.leonidas.quote;

import ch.nblotti.leonidas.asset.AssetPO;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;


@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:applicationtest.properties")
public class BondQuoteServiceTest {


  @MockBean
  private CacheManager cacheManager;

  @MockBean(name = "quoteDateTimeFormatter")
  private DateTimeFormatter quoteDateTimeFormatter;

  @MockBean(name = "dateTimeFormatter")
  private DateTimeFormatter dateTimeFormatter;

  @TestConfiguration
  static class QuoteServiceTestContextConfiguration {


    @Bean
    public BondQuoteService quoteService() {

      return new BondQuoteService();

    }
  }

  @MockBean
  private RestTemplate rt;

  @Autowired
  private BondQuoteService bondQuoteService;


  @Value("${spring.application.eod.api.key}")
  private String eodApiToken;
  @Value("${spring.application.eod.asset.url}")
  private String quoteUrl;


  @Test
  public void getQuotesFromCache() {


    Map<String, Map<LocalDate, BondQuoteDTO>> cacheByBondId = mock(HashMap.class);
    Map<LocalDate, BondQuoteDTO> quotes = mock(Map.class);
    String symbol = "FB";
    Cache cache = mock(ConcurrentMapCache.class);
    Cache.ValueWrapper vW = mock(Cache.ValueWrapper.class);

    when(cacheManager.getCache(BondQuoteService.BONDS_QUOTES)).thenReturn(cache);
    when(cache.get(symbol)).thenReturn(vW);
    //on obtient la map des quotes par security
    when(vW.get()).thenReturn(quotes);

//on obtient la liste des quotes par security
    when(quotes.size()).thenReturn(3);

    when(cacheByBondId.containsKey(symbol)).thenReturn(true);


    Map<LocalDate, BondQuoteDTO> returnedQuotes = bondQuoteService.getBondQuotes(symbol);
    Assert.assertEquals(3, returnedQuotes.size());

  }


  @Test
  public void getQuotesNoCache() {

    BondQuoteService spyBondQuoteService = spy(bondQuoteService);
    Map<String, Map<LocalDate, BondQuoteDTO>> cacheByBondId = mock(HashMap.class);
    Map<LocalDate, BondQuoteDTO> quotes = mock(Map.class);
    String symbol = "FB";
    Cache cache = mock(ConcurrentMapCache.class);
    ResponseEntity responseEntity = mock(ResponseEntity.class);
    BondQuoteDTO bondDTO01 = mock(BondQuoteDTO.class);
    BondQuoteDTO bondDTO02 = mock(BondQuoteDTO.class);
    when(bondDTO01.getDate()).thenReturn("2000-01-01");
    when(bondDTO02.getDate()).thenReturn("2001-01-01");
    BondQuoteDTO[] quotesArr = new BondQuoteDTO[]{bondDTO01, bondDTO02};
    when(responseEntity.getBody()).thenReturn(quotesArr);


    when(cacheManager.getCache(BondQuoteService.BONDS_QUOTES)).thenReturn(cache);
    when(cache.get(symbol)).thenReturn(null);


    when(rt.getForEntity(anyString(), any())).thenReturn(responseEntity);


    //on obtient la liste des quotes par security
    when(quotes.size()).thenReturn(3);

    when(cacheByBondId.containsKey(symbol)).thenReturn(true);


    doReturn(DateTimeFormatter.ofPattern("dd.MM.yyyy")).when(spyBondQuoteService).getDateTimeFormatter();
    doReturn(DateTimeFormatter.ofPattern("yyyy-MM-dd")).when(spyBondQuoteService).getQuoteDateTimeFormatter();


    Map<LocalDate, BondQuoteDTO> returnedQuotes = spyBondQuoteService.getBondQuotes(symbol);
    Assert.assertEquals(2, returnedQuotes.size());
    verify(responseEntity, times(1)).getBody();

  }




  @Test
  public void clearCache() {
    Cache cache = mock(ConcurrentMapCache.class);
    Cache.ValueWrapper vW = mock(Cache.ValueWrapper.class);

    when(cacheManager.getCache(BondQuoteService.BONDS_QUOTES)).thenReturn(cache);
    bondQuoteService.clearCache();

    verify(cacheManager, times(1)).getCache(any());
    verify(cache, times(1)).clear();

  }

  @Test
  public void getDateTimeFormatter() {
    DateTimeFormatter df = bondQuoteService.getDateTimeFormatter();
    Assert.assertEquals(df, dateTimeFormatter);

  }

  @Test
  public void getQuoteDateTimeFormatter() {
    DateTimeFormatter df = bondQuoteService.getQuoteDateTimeFormatter();
    Assert.assertEquals(df, quoteDateTimeFormatter);
  }


  @Test(expected = IllegalStateException.class)
  public void getQuoteForDateNoDateMatch() {

    String symbol = "FB";
    LocalDate now = LocalDate.now();
    BondQuoteDTO quoteDTO1 = mock(BondQuoteDTO.class);
    BondQuoteDTO quoteDTO2 = mock(BondQuoteDTO.class);
    AssetPO assetPO2 = mock(AssetPO.class);
    BondQuoteDTO[] quoteDTOS = new BondQuoteDTO[]{quoteDTO1, quoteDTO2};
    Map<LocalDate, BondQuoteDTO> quoteDTOMap = Maps.newHashMap();

    BondQuoteService spyQuoteService = spy(bondQuoteService);

    when(quoteDTO1.getDate()).thenReturn("01.01.1900");
    when(quoteDTO2.getDate()).thenReturn("01.01.1901");

    quoteDTOMap.put(LocalDate.parse(quoteDTO1.getDate(),DateTimeFormatter.ofPattern("dd.MM.yyyy")),quoteDTO1);
    quoteDTOMap.put(LocalDate.parse(quoteDTO2.getDate(),DateTimeFormatter.ofPattern("dd.MM.yyyy")),quoteDTO2);


    doReturn(quoteDTOMap).when(spyQuoteService).getBondQuotes(symbol);

    doReturn(DateTimeFormatter.ofPattern("dd.MM.yyyy")).when(spyQuoteService).getDateTimeFormatter();
    doReturn(DateTimeFormatter.ofPattern("yyyy-MM-dd")).when(spyQuoteService).getQuoteDateTimeFormatter();

    spyQuoteService.getQuoteForDate(symbol, LocalDate.parse("01.02.1900", DateTimeFormatter.ofPattern("dd.MM.yyyy")));


  }


  @Test
  public void getQuoteForDateMatch() {

    String exchange = "US";
    String symbol = "FB";
    LocalDate now = LocalDate.now();
    BondQuoteDTO quoteDTO1 = mock(BondQuoteDTO.class);
    BondQuoteDTO quoteDTO2 = mock(BondQuoteDTO.class);
    AssetPO assetPO2 = mock(AssetPO.class);
    BondQuoteDTO[] quoteDTOS = new BondQuoteDTO[]{quoteDTO1, quoteDTO2};

    Map<LocalDate, BondQuoteDTO> quoteDTOMap = Maps.newHashMap();


    BondQuoteService spyQuoteService = spy(bondQuoteService);


    when(quoteDTO1.getDate()).thenReturn("01.01.1900");
    when(quoteDTO1.getYield()).thenReturn("4.2");
    when(quoteDTO2.getDate()).thenReturn(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    when(quoteDTO2.getYield()).thenReturn("2.4");
    quoteDTOMap.put(LocalDate.parse(quoteDTO1.getDate(),DateTimeFormatter.ofPattern("dd.MM.yyyy")),quoteDTO1);
    quoteDTOMap.put(LocalDate.parse(quoteDTO2.getDate(),DateTimeFormatter.ofPattern("yyyy-MM-dd")),quoteDTO2);


    doReturn(quoteDTOMap).when(spyQuoteService).getBondQuotes(symbol);

    doReturn(DateTimeFormatter.ofPattern("dd.MM.yyyy")).when(spyQuoteService).getDateTimeFormatter();
    doReturn(DateTimeFormatter.ofPattern("yyyy-MM-dd")).when(spyQuoteService).getQuoteDateTimeFormatter();

    BondQuoteDTO returned = spyQuoteService.getQuoteForDate(symbol, now);

    Assert.assertEquals(quoteDTO2.getYield(), returned.getYield());
  }


}
