 package net.premereur.mvp.core;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
import net.premereur.mvp.core.basic.BasicEventBusFactory;

 /**
 * Used on MVP {@link Presenter}s so that the {@link BasicEventBusFactory} knows which view to inject in the Presenter.
  * 
  * @author gpremer
  * 
  */
 @Retention(RetentionPolicy.RUNTIME)
 @Target(ElementType.TYPE)
 public @interface UsesView {
 
     Class<? extends View> value();
 }
