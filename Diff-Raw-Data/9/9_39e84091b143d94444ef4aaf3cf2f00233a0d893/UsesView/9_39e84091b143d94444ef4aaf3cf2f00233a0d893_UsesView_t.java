 package net.premereur.mvp.core;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 /**
 * Used on MVP {@link Presenter}s so that the {@link net.premereur.mvp.core.EventBusFactory} knows which view to inject in the Presenter.
  * 
  * @author gpremer
  * 
  */
 @Retention(RetentionPolicy.RUNTIME)
 @Target(ElementType.TYPE)
 public @interface UsesView {
 
    /**
     * The view the presenter manages.
     */
     Class<? extends View> value();
 }
