package ch.nblotti.leonidas.entry.security;


import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecurityEntryController {


  private final SecurityEntryRepository repository;


  SecurityEntryController(SecurityEntryRepository repository) {

    this.repository = repository;

  }


  public SecurityEntry save(SecurityEntry entry) {
    return null;
  }
}
