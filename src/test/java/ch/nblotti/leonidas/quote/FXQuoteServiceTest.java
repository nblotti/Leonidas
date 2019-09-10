package ch.nblotti.leonidas.quote;

import ch.nblotti.leonidas.asset.AssetPO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static ch.nblotti.leonidas.quote.FXQuoteService.QUOTES;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:applicationtest.properties")
public class FXQuoteServiceTest {


  @TestConfiguration
  static class FXQuoterviceTestContextConfiguration {


    @Bean
    public FXQuoteService fXQuoteService() {

      return new FXQuoteService();

    }
  }

  @MockBean
  private RestTemplate rt;
  @Autowired
  FXQuoteService fXQuoteService;

  @MockBean
  private CacheManager cacheManager;

  @MockBean(name = "dateTimeFormatter")
  private DateTimeFormatter dateTimeFormatter;


  @Test
  public void getCashName() {

    String quotes = fXQuoteService.getCashName();
    Assert.assertEquals(QUOTES, quotes);
  }

  @Test
  public void getFXQuotes() {

    List<QuoteDTO> quotes = mock(List.class);
    FXQuoteService spy = Mockito.spy(fXQuoteService);
    doReturn(quotes).when(spy).getQuotes(any(), any());
    List<QuoteDTO> returnedQuotes = spy.getFXQuotes("USDCHF");
    Assert.assertEquals(returnedQuotes, quotes);


  }

  @Test
  public void getFXQuoteForDateSameCurrency() {

    FXQuoteService spyFXQuoteService = spy(fXQuoteService);


    doReturn(DateTimeFormatter.ofPattern("dd.MM.yyyy")).when(spyFXQuoteService).getDateTimeFormatter();
    doReturn(DateTimeFormatter.ofPattern("yyyy-MM-dd")).when(spyFXQuoteService).getQuoteDateTimeFormatter();

    QuoteDTO quote = spyFXQuoteService.getFXQuoteForDate("CHF", "CHF", LocalDate.now());


    Assert.assertEquals("1", quote.getAdjustedClose());
    Assert.assertEquals("1", quote.getClose());
    Assert.assertEquals("1", quote.getHigh());
    Assert.assertEquals("1", quote.getLow());
    Assert.assertEquals("1", quote.getOpen());
    Assert.assertEquals("0", quote.getVolume());
    Assert.assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), quote.getDate());

  }

  @Test(expected = IllegalStateException.class)
  public void getFXQuoteForDateNoDateMatch() {

    String exchange = "US";
    String symbol = "FB";
    LocalDate now = LocalDate.now();
    QuoteDTO quoteDTO1 = mock(QuoteDTO.class);
    AssetPO assetPO2 = mock(AssetPO.class);
    QuoteDTO[] quoteDTOS = new QuoteDTO[]{quoteDTO1};


    FXQuoteService spyFXQuoteService = spy(fXQuoteService);

    when(quoteDTO1.getDate()).thenReturn("01.01.1900");

    doReturn(Arrays.asList(quoteDTOS)).when(spyFXQuoteService).getFXQuotes(String.format("%s%s", "CHF", "EUR"));

    doReturn(DateTimeFormatter.ofPattern("dd.MM.yyyy")).when(spyFXQuoteService).getDateTimeFormatter();
    doReturn(DateTimeFormatter.ofPattern("yyyy-MM-dd")).when(spyFXQuoteService).getQuoteDateTimeFormatter();

    spyFXQuoteService.getFXQuoteForDate("CHF", "EUR", LocalDate.parse("01.02.1900", DateTimeFormatter.ofPattern("dd.MM.yyyy")));


  }


  @Test
  public void getFXQuoteForDateMatch() {

    String exchange = "US";
    String symbol = "FB";
    LocalDate now = LocalDate.now();
    QuoteDTO quoteDTO1 = mock(QuoteDTO.class);
    QuoteDTO quoteDTO2 = mock(QuoteDTO.class);
    AssetPO assetPO2 = mock(AssetPO.class);
    QuoteDTO[] quoteDTOS = new QuoteDTO[]{quoteDTO1, quoteDTO2};


    FXQuoteService spyFXQuoteService = spy(fXQuoteService);


    when(quoteDTO1.getDate()).thenReturn("01.01.1900");
    when(quoteDTO1.getAdjustedClose()).thenReturn("500");
    when(quoteDTO2.getDate()).thenReturn(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    when(quoteDTO2.getAdjustedClose()).thenReturn("1000");
    doReturn(Arrays.asList(quoteDTOS)).when(spyFXQuoteService).getFXQuotes(String.format("%s%s", "CHF", "EUR"));

    doReturn(DateTimeFormatter.ofPattern("dd.MM.yyyy")).when(spyFXQuoteService).getDateTimeFormatter();
    doReturn(DateTimeFormatter.ofPattern("yyyy-MM-dd")).when(spyFXQuoteService).getQuoteDateTimeFormatter();

    QuoteDTO returned = spyFXQuoteService.getFXQuoteForDate("CHF", "EUR", LocalDate.now());

    Assert.assertEquals("1000", returned.getAdjustedClose());
  }


}
