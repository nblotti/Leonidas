package ch.nblotti.leonidas.security.asset;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static java.util.stream.Collectors.groupingBy;

@RestController
public class AssetController {


  @Autowired
  private AssetService assetService;


  @GetMapping(value = "/assets/{exchange}/{symbol}")
  public Iterable<Asset> getSymbol(@PathVariable String exchange, @PathVariable String symbol) {

    return assetService.findSymbol(exchange, symbol);


  }


}
