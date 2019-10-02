package ch.nblotti.leonidas.process.order;

import ch.nblotti.leonidas.position.PositionPO;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface MarketProcess {
  Class<?> entity();
  int postype() default 0;
}
