package ch.nblotti.leonidas.security.position;


import ch.nblotti.leonidas.security.position.cash.CashPositionService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.websocket.server.PathParam;
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
