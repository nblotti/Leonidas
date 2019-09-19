package ch.nblotti.leonidas.asset;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.Equals;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.nblotti.leonidas.asset.AssetService.ASSET_MAP;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:applicationtest.properties")
public class AssetServiceTest {


  @TestConfiguration
  static class AssetServiceTestContextConfiguration {


    @Bean
    public AssetService assetService() {

      return new AssetService();

    }
  }

  @MockBean
  private CacheManager cacheManager;

  @MockBean
  private RestTemplate rt;

  @Autowired
  private AssetService assetService;


  @Value("${spring.application.eod.api.key}")
  private String eodApiToken;
  @Value("${spring.application.eod.asset.url}")
  private String assetUrl;

  @Before
  public void setUp() {


  }


  @Test
  public void getAssetsCacheNotExisting() {

    String exchange = "US";
    AssetPO assetPO1 = mock(AssetPO.class);
    AssetPO assetPO2 = mock(AssetPO.class);
    AssetPO[] assets = new AssetPO[]{assetPO1, assetPO2};
    Map<String, List<AssetPO>> cachedAsset = mock(HashMap.class);
    ResponseEntity responseEntity = mock(ResponseEntity.class);

    Cache cache = mock(ConcurrentMapCache.class);
    Cache.ValueWrapper vW = mock(Cache.ValueWrapper.class);

    when(cacheManager.getCache(AssetService.ASSETS)).thenReturn(cache);

    when(cache.get(ASSET_MAP)).thenReturn(null);

    when(rt.getForEntity(String.format(assetUrl, exchange, eodApiToken), AssetPO[].class)).thenReturn(responseEntity);//new ResponseEntity(assets, HttpStatus.OK));
    when(responseEntity.getBody()).thenReturn(assets);


    List<AssetPO> returnedAssets = assetService.getAssets(exchange);

//on teste uniquement le fait que l'on obtienne rien depuis le cache
    verify(vW, times(0)).get();
  }


  @Test
  public void getAssetsCacheExisting() {

    String exchange = "US";
    AssetPO assetPO1 = mock(AssetPO.class);
    AssetPO assetPO2 = mock(AssetPO.class);
    AssetPO[] assets = new AssetPO[]{assetPO1, assetPO2};
    Map<String, List<AssetPO>> cachedAsset = mock(HashMap.class);


    Cache cache = mock(ConcurrentMapCache.class);
    Cache.ValueWrapper vW = mock(Cache.ValueWrapper.class);

    when(cacheManager.getCache(AssetService.ASSETS)).thenReturn(cache);

    when(cache.get(ASSET_MAP)).thenReturn(vW);
    when(vW.get()).thenReturn(cachedAsset);
    when(cachedAsset.containsKey(exchange)).thenReturn(true);


    when(cachedAsset.get(exchange)).thenReturn(Arrays.asList(assets));

    when(cacheManager.getCache(AssetService.ASSETS)).thenReturn(cache);


    List<AssetPO> returnedAssets = assetService.getAssets(exchange);

//on teste uniquement le fait que l'on obtienne les données depuis le cache
    verify(vW, times(1)).get();
  }


  @Test
  public void getAssetsInCache() {
    String exchange = "US";
    AssetPO assetPO1 = mock(AssetPO.class);
    AssetPO assetPO2 = mock(AssetPO.class);
    AssetPO[] assets = new AssetPO[]{assetPO1, assetPO2};
    Map<String, List<AssetPO>> cachedAsset = mock(HashMap.class);
    ResponseEntity responseEntity = mock(ResponseEntity.class);

    Cache cache = mock(ConcurrentMapCache.class);
    Cache.ValueWrapper vW = mock(Cache.ValueWrapper.class);

    when(cacheManager.getCache(AssetService.ASSETS)).thenReturn(cache);

    when(cache.get(ASSET_MAP)).thenReturn(vW);
    when(cachedAsset.containsKey(exchange)).thenReturn(true);
    when(vW.get()).thenReturn(cachedAsset);

    when(rt.getForEntity(String.format(assetUrl, exchange, eodApiToken), AssetPO[].class)).thenReturn(responseEntity);//new ResponseEntity(assets, HttpStatus.OK));
    when(responseEntity.getBody()).thenReturn(assets);

    List<AssetPO> returnedAssets = assetService.getAssets(exchange);


    verify(cachedAsset, times(1)).get(exchange);
    verify(responseEntity, times(0)).getBody();


  }


  @Test
  public void getAssetsNotInCache() {
    String exchange = "US";
    AssetPO assetPO1 = mock(AssetPO.class);
    AssetPO assetPO2 = mock(AssetPO.class);
    AssetPO[] assets = new AssetPO[]{assetPO1, assetPO2};
    Map<String, List<AssetPO>> cachedAsset = mock(HashMap.class);
    ResponseEntity responseEntity = mock(ResponseEntity.class);

    Cache cache = mock(ConcurrentMapCache.class);
    Cache.ValueWrapper vW = mock(Cache.ValueWrapper.class);

    when(cacheManager.getCache(AssetService.ASSETS)).thenReturn(cache);

    when(cache.get(ASSET_MAP)).thenReturn(vW);
    when(cachedAsset.containsKey(exchange)).thenReturn(false);
    when(vW.get()).thenReturn(cachedAsset);

    when(rt.getForEntity(String.format(assetUrl, exchange, eodApiToken), AssetPO[].class)).thenReturn(responseEntity);//new ResponseEntity(assets, HttpStatus.OK));
    when(responseEntity.getBody()).thenReturn(assets);

    List<AssetPO> returnedAssets = assetService.getAssets(exchange);


    verify(cachedAsset, times(0)).get(exchange);
    verify(responseEntity, times(1)).getBody();
  }


  @Test
  public void clearCache() {
    Cache cache = mock(ConcurrentMapCache.class);
    Cache.ValueWrapper vW = mock(Cache.ValueWrapper.class);

    when(cacheManager.getCache(AssetService.ASSETS)).thenReturn(cache);
    assetService.clearCache();

    verify(cacheManager, times(1)).getCache(any());
    verify(cache, times(1)).clear();

  }

  @Test
  public void findSymbol() {

    String exchange = "US";
    AssetPO assetPO1 = mock(AssetPO.class);
    AssetPO assetPO2 = mock(AssetPO.class);
    AssetPO[] assets = new AssetPO[]{assetPO1, assetPO2};
    AssetService newAssetService = new AssetService();
    AssetService spyAssetService = spy(newAssetService);

    when(assetPO1.getCode()).thenReturn("FB");
    when(assetPO2.getCode()).thenReturn("IBM");

    doReturn(Arrays.asList(assets)).when(spyAssetService).getAssets(exchange);

    Iterable<AssetPO> returnedAssets = spyAssetService.findSymbol(exchange, "FB");
    List<AssetPO> assetPOs = Lists.newArrayList(returnedAssets);
    Assert.assertEquals(1,assetPOs.size());
    Assert.assertEquals("FB",assetPOs.get(0).getCode());

  }

  @Test
  public void getSymbol() {

    String exchange = "US";
    AssetPO assetPO1 = mock(AssetPO.class);
    AssetPO assetPO2 = mock(AssetPO.class);
    AssetPO[] assets = new AssetPO[]{assetPO1, assetPO2};
    AssetService newAssetService = new AssetService();
    AssetService spyAssetService = spy(newAssetService);

    when(assetPO1.getCode()).thenReturn("FB");
    when(assetPO2.getCode()).thenReturn("IBM");

    doReturn(Arrays.asList(assets)).when(spyAssetService).getAssets(exchange);

    AssetPO returnedAsset = spyAssetService.getSymbol(exchange, "FB");
    Assert.assertNotNull(returnedAsset);
  }

  @Test(expected = IllegalStateException.class)
  public void getSymbolNotPresent() {

    String exchange = "US";
    AssetPO assetPO1 = mock(AssetPO.class);
    AssetPO assetPO2 = mock(AssetPO.class);
    AssetPO[] assets = new AssetPO[]{assetPO1, assetPO2};
    AssetService newAssetService = new AssetService();
    AssetService spyAssetService = spy(newAssetService);

    when(assetPO1.getCode()).thenReturn("FB");
    when(assetPO2.getCode()).thenReturn("IBM");

    doReturn(Arrays.asList(assets)).when(spyAssetService).getAssets(exchange);

    AssetPO returnedAsset = spyAssetService.getSymbol(exchange, "GOOG");

  }

  @Test(expected = IllegalStateException.class)
  public void getSymbolMultiplePresent() {

    String exchange = "US";
    AssetPO assetPO1 = mock(AssetPO.class);
    AssetPO assetPO2 = mock(AssetPO.class);
    AssetPO[] assets = new AssetPO[]{assetPO1, assetPO2};
    AssetService newAssetService = new AssetService();
    AssetService spyAssetService = spy(newAssetService);

    when(assetPO1.getCode()).thenReturn("FBA");
    when(assetPO2.getCode()).thenReturn("FBA");

    doReturn(Arrays.asList(assets)).when(spyAssetService).getAssets(exchange);

    AssetPO returnedAsset = spyAssetService.getSymbol(exchange, "FBA");

  }

  @Test
  public void getValueDateForExchange() {
    int dV = assetService.getValueDateForExchange("US");
    Assert.assertEquals(3,dV );
  }


}
