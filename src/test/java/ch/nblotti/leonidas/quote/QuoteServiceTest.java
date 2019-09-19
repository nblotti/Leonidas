package ch.nblotti.leonidas.quote;


import ch.nblotti.leonidas.asset.AssetPO;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:applicationtest.properties")
public class QuoteServiceTest {


  @MockBean
  private CacheManager cacheManager;

  @MockBean(name = "quoteDateTimeFormatter")
  private DateTimeFormatter quoteDateTimeFormatter;

  @MockBean(name = "dateTimeFormatter")
  private DateTimeFormatter dateTimeFormatter;

  @TestConfiguration
  static class QuoteServiceTestContextConfiguration {


    @Bean
    public QuoteService quoteService() {

      return new QuoteService();

    }
  }

  @MockBean
  private RestTemplate rt;

  @Autowired
  private QuoteService quoteService;


  @Value("${spring.application.eod.api.key}")
  private String eodApiToken;
  @Value("${spring.application.eod.asset.url}")
  private String quoteUrl;

  @Test
  public void getQuotesFromCache() {


    Map<String, List<QuoteDTO>> cacheByExchange = mock(HashMap.class);
    List<QuoteDTO> quotes = mock(List.class);
    String exchange = "US";
    String symbol = "FB";
    Cache cache = mock(ConcurrentMapCache.class);
    Cache.ValueWrapper vW = mock(Cache.ValueWrapper.class);

    when(cacheManager.getCache(QuoteService.QUOTES)).thenReturn(cache);
    when(cache.get(exchange)).thenReturn(vW);
    //on obtient la map des quotes par security
    when(vW.get()).thenReturn(cacheByExchange);

    when(quotes.size()).thenReturn(3);
//on obtient la liste des quotes par security
    when(cacheByExchange.get(symbol)).thenReturn(quotes);

    when(cacheByExchange.containsKey(symbol)).thenReturn(true);


    List<QuoteDTO> returnedQuotes = quoteService.getQuotes(exchange, symbol);
    Assert.assertEquals(3,returnedQuotes.size());

  }

  @Test
  public void getQuotesNoCache() {


    Map<String, List<QuoteDTO>> cacheByExchange = mock(HashMap.class);
    List<QuoteDTO> quotes = mock(List.class);
    String exchange = "US";
    String symbol = "FB";
    Cache cache = mock(ConcurrentMapCache.class);
    Cache.ValueWrapper vW = mock(Cache.ValueWrapper.class);
    ResponseEntity responseEntity = mock(ResponseEntity.class);

    QuoteDTO quoteDTO01 = mock(QuoteDTO.class);
    QuoteDTO quoteDTO02 = mock(QuoteDTO.class);
    QuoteDTO[] quotesArr = new QuoteDTO[]{quoteDTO01, quoteDTO02};

    when(cacheManager.getCache(QuoteService.QUOTES)).thenReturn(cache);
    when(cache.get(exchange)).thenReturn(null);
    //on obtient la map des quotes par security
    when(vW.get()).thenReturn(cacheByExchange);

    when(quotes.size()).thenReturn(3);
//on obtient la liste des quotes par security
    when(cacheByExchange.get(symbol)).thenReturn(quotes);

    when(cacheByExchange.containsKey(symbol)).thenReturn(true);
    //when(rt.getForEntity(String.format(quoteUrl, symbol + "." + exchange, eodApiToken), QuoteDTO[].class)).thenReturn(responseEntity);
    when(rt.getForEntity(anyString(), any())).thenReturn(responseEntity);

    when(responseEntity.getBody()).thenReturn(quotesArr);


    List<QuoteDTO> returnedQuotes = quoteService.getQuotes(exchange, symbol);
    Assert.assertEquals(2, returnedQuotes.size());
    verify(responseEntity, times(1)).getBody();

  }


  @Test
  public void clearCache() {
    Cache cache = mock(ConcurrentMapCache.class);
    Cache.ValueWrapper vW = mock(Cache.ValueWrapper.class);

    when(cacheManager.getCache(QuoteService.QUOTES)).thenReturn(cache);
    quoteService.clearCache();

    verify(cacheManager, times(1)).getCache(any());
    verify(cache, times(1)).clear();

  }

  @Test
  public void getDateTimeFormatter() {
    DateTimeFormatter df = quoteService.getDateTimeFormatter();
    Assert.assertEquals(df, dateTimeFormatter);

  }

  @Test
  public void getQuoteDateTimeFormatter() {
    DateTimeFormatter df = quoteService.getQuoteDateTimeFormatter();
    Assert.assertEquals(df, quoteDateTimeFormatter);
  }


  @Test(expected = IllegalStateException.class)
  public void getQuoteForDateNoDateMatch() {

    String exchange = "US";
    String symbol = "FB";
    LocalDate now = LocalDate.now();
    QuoteDTO quoteDTO1 = mock(QuoteDTO.class);
    QuoteDTO quoteDTO2 = mock(QuoteDTO.class);
    AssetPO assetPO2 = mock(AssetPO.class);
    QuoteDTO[] quoteDTOS = new QuoteDTO[]{quoteDTO1, quoteDTO2};


    QuoteService newQuoteService = new QuoteService();
    QuoteService spyQuoteService = spy(newQuoteService);

    when(quoteDTO1.getDate()).thenReturn("01.01.1900");
    when(quoteDTO2.getDate()).thenReturn("01.01.1901");

    doReturn(Arrays.asList(quoteDTOS)).when(spyQuoteService).getQuotes(exchange, symbol);

    doReturn(DateTimeFormatter.ofPattern("dd.MM.yyyy")).when(spyQuoteService).getDateTimeFormatter();
    doReturn(DateTimeFormatter.ofPattern("yyyy-MM-dd")).when(spyQuoteService).getQuoteDateTimeFormatter();

    spyQuoteService.getQuoteForDate(exchange, symbol, LocalDate.parse("01.02.1900", DateTimeFormatter.ofPattern("dd.MM.yyyy")));


  }


  @Test
  public void getQuoteForDateMatch() {

    String exchange = "US";
    String symbol = "FB";
    LocalDate now = LocalDate.now();
    QuoteDTO quoteDTO1 = mock(QuoteDTO.class);
    QuoteDTO quoteDTO2 = mock(QuoteDTO.class);
    AssetPO assetPO2 = mock(AssetPO.class);
    QuoteDTO[] quoteDTOS = new QuoteDTO[]{quoteDTO1, quoteDTO2};


    QuoteService newQuoteService = new QuoteService();
    QuoteService spyQuoteService = spy(newQuoteService);

    when(quoteDTO1.getDate()).thenReturn("01.01.1900");
    when(quoteDTO1.getAdjustedClose()).thenReturn("500");
    when(quoteDTO2.getDate()).thenReturn(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    when(quoteDTO2.getAdjustedClose()).thenReturn("1000");
    doReturn(Arrays.asList(quoteDTOS)).when(spyQuoteService).getQuotes(exchange, symbol);

    doReturn(DateTimeFormatter.ofPattern("dd.MM.yyyy")).when(spyQuoteService).getDateTimeFormatter();
    doReturn(DateTimeFormatter.ofPattern("yyyy-MM-dd")).when(spyQuoteService).getQuoteDateTimeFormatter();

    QuoteDTO returned = spyQuoteService.getQuoteForDate(exchange, symbol, now);

    Assert.assertEquals("1000", returned.getAdjustedClose());
  }


  @Test
  public void getLastQuote() {

    String exchange = "US";
    String symbol = "FB";
    LocalDate now = LocalDate.now();
    QuoteDTO quoteDTO1 = mock(QuoteDTO.class);
    QuoteDTO quoteDTO2 = mock(QuoteDTO.class);
    AssetPO assetPO2 = mock(AssetPO.class);
    QuoteDTO[] quoteDTOS = new QuoteDTO[]{quoteDTO1, quoteDTO2};


    QuoteService newQuoteService = new QuoteService();
    QuoteService spyQuoteService = spy(newQuoteService);

    when(quoteDTO1.getDate()).thenReturn("01.01.1900");
    when(quoteDTO2.getDate()).thenReturn("01.01.1901");

    doReturn(Arrays.asList(quoteDTOS)).when(spyQuoteService).getQuotes(exchange, symbol);

    doReturn(DateTimeFormatter.ofPattern("dd.MM.yyyy")).when(spyQuoteService).getDateTimeFormatter();
    doReturn(DateTimeFormatter.ofPattern("yyyy-MM-dd")).when(spyQuoteService).getQuoteDateTimeFormatter();

    QuoteDTO lastQuote = spyQuoteService.getLastQuote(exchange, symbol);

    Assert.assertEquals("01.01.1901", lastQuote.getDate());


  }


}
