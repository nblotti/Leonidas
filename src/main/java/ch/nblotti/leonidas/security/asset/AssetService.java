package ch.nblotti.leonidas.security.asset;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;


@Component
public class AssetService {


  @Value("${spring.application.eod.api.key}")

  private String eodApiToken;
  @Value("${spring.application.eod.asset.url}")
  private String assetUrl;


  @Autowired
  private CacheManager cacheManager;

  @Autowired
  private RestTemplate rt;


  private List<Asset> getAssets(String exchange) {

    List<Asset> assetList;

    Map<String, List<Asset>> cachedAsset;

    if (cacheManager.getCache("assets").get("assetMap") == null)
      cacheManager.getCache("assets").put("assetMap", new HashMap<>());

    cachedAsset = (Map<String, List<Asset>>) cacheManager.getCache("assets").get("assetMap").get();


    if (cachedAsset.containsKey(exchange))
      assetList = cachedAsset.get(exchange);
    else {

      assetList = Arrays.asList(rt.getForEntity(String.format(assetUrl, exchange, eodApiToken), Asset[].class).getBody());
      cachedAsset = new HashMap<String, List<Asset>>();
      cachedAsset.put(exchange, assetList);
      cacheManager.getCache("assets").put("assetMap", cachedAsset);
    }
    return assetList;
  }


  @Scheduled(fixedRate = 10800000)
  public void clearCache() {
    cacheManager.getCache("assets").clear();

  }

  /**
   * Look up all employees, and transform them into a REST collection resource.
   */
  public Iterable<Asset> findSymbol(String exchange, String symbol) {

    return getAssets(exchange).stream().filter(c -> c.getCode().toLowerCase().contains(symbol.toLowerCase())).collect(Collectors.toList());


  }

  public Asset getSymbol(String exchange, String symbol) {


      Optional<Asset> asset =  getAssets(exchange).stream().filter(c -> c.getCode().equalsIgnoreCase(symbol)).reduce((a, b) -> {
      throw new IllegalStateException("Multiple elements: " + a + ", " + b);
    });

  if(asset.isPresent())
    return asset.get();
  else
    throw new IllegalStateException(String.format("No symbol found %s %s",exchange,symbol));


  }


  public int getValueDateForExchange(String exchangeid) {

    return 3;
  }


}
