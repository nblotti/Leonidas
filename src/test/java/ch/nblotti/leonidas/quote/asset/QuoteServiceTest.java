package ch.nblotti.leonidas.quote.asset;


import ch.nblotti.leonidas.asset.AssetPO;
import ch.nblotti.leonidas.asset.AssetService;
import ch.nblotti.leonidas.quote.QuoteDTO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
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

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.nblotti.leonidas.asset.AssetService.ASSET_MAP;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:applicationtest.properties")
public class QuoteServiceTest {




  @MockBean
  private CacheManager cacheManager;

  @MockBean
  private DateTimeFormatter quoteDateTimeFormatter;


  @TestConfiguration
  static class AccountServiceTestContextConfiguration {


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
    Assert.assertEquals(returnedQuotes.size(), 3);

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
    Assert.assertEquals(2,returnedQuotes.size());
    verify(responseEntity,times(1)).getBody();

  }


}
