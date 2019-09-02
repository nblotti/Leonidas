package ch.nblotti.leonidas.asset;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AssetController {


  @Autowired
  private AssetService assetService;


  @GetMapping(value = "/assets/{exchange}/{symbol}")
  public Iterable<Asset> getSymbol(@PathVariable String exchange, @PathVariable String symbol) {

    return assetService.findSymbol(exchange, symbol);


  }


}
