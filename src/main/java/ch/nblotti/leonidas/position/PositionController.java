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


  @RequestMapping(value = "/position", method = RequestMethod.POST)
  public Iterable<Position> save(@Valid @RequestBody List<Position> positions) {
    return cashPositionService.saveAll(positions);
  }


}
