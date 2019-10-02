package ch.nblotti.leonidas.process.order;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface MarketProcess {
  Class<?> entity();
}
