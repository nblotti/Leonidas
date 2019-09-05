package ch.nblotti.leonidas.asset;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@Component
public class AssetService {

  private static final Logger LOGGER = Logger.getLogger("AssetService");

  public static final String ASSETS = "assets";
  public static final String ASSET_MAP = "assetMap";

  @Value("${spring.application.value.date}")
  private int valueDate;

  @Value("${spring.application.eod.api.key}")

  private String eodApiToken;
  @Value("${spring.application.eod.asset.url}")
  private String assetUrl;


  @Autowired
  private CacheManager cacheManager;

  @Autowired
  private RestTemplate rt;


  List<AssetPO> getAssets(String exchange) {

    List<AssetPO> assetPOList;

    Map<String, List<AssetPO>> cachedAsset;

    if (cacheManager.getCache(ASSETS).get(ASSET_MAP) == null) {
      cachedAsset = new HashMap<>();
      cacheManager.getCache(ASSETS).put(ASSET_MAP, cachedAsset);
    } else {
      cachedAsset = (Map<String, List<AssetPO>>) cacheManager.getCache(ASSETS).get(ASSET_MAP).get();
    }

    if (cachedAsset.containsKey(exchange))
      assetPOList = cachedAsset.get(exchange);
    else {

      assetPOList = Arrays.asList(rt.getForEntity(String.format(assetUrl, exchange, eodApiToken), AssetPO[].class).getBody());
      cachedAsset = new HashMap<>();
      cachedAsset.put(exchange, assetPOList);
      cacheManager.getCache(ASSETS).put(ASSET_MAP, cachedAsset);
    }
    return assetPOList;
  }


  @Scheduled(fixedRate = 10800000)
  public void clearCache() {
    cacheManager.getCache(ASSETS).clear();

  }


  public Iterable<AssetPO> findSymbol(String exchange, String symbol) {

    return getAssets(exchange).stream().filter(c -> c.getCode().toLowerCase().contains(symbol.toLowerCase())).collect(Collectors.toList());


  }

  public AssetPO getSymbol(String exchange, String symbol) {


    Optional<AssetPO> asset = getAssets(exchange).stream().filter(c -> c.getCode().equalsIgnoreCase(symbol)).reduce((a, b) -> {
      throw new IllegalStateException("Multiple elements: " + a + ", " + b);
    });

    if (asset.isPresent())
      return asset.get();
    else
      throw new IllegalStateException(String.format("No symbol found %s %s", exchange, symbol));


  }


  public int getValueDateForExchange(String exchangeid) {

    LOGGER.log(Level.FINE, exchangeid);

    return valueDate;
  }


}
