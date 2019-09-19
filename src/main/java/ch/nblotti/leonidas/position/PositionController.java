package ch.nblotti.leonidas.position;


import ch.nblotti.leonidas.position.cash.CashPositionService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class PositionController {


  private final CashPositionService cashPositionService;


  PositionController(CashPositionService cashPositionService) {

    this.cashPositionService = cashPositionService;

  }


  @PostMapping(value = "/position")
  public Iterable<PositionPO> save(@Valid @RequestBody List<PositionPO> positionPOS) {
    return cashPositionService.saveAll(positionPOS);
  }


}
