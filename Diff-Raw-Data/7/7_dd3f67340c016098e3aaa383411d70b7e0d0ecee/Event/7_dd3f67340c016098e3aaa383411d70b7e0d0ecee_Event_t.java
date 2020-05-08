 package net.premereur.mvp.core;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 /**
  * Event annotations are used to mark methods on {@link EventBus}es. Calling
  * these event methods will cause the corresponding method in all
  * {@link Presenter}s enumerated in the Event annotation's argument to be
  * called. The corresponding method is a method with same arguments with its
  * name uppercased and prefixed with on.
  * 
  * E.g. method
  * 
  * <pre>
  * &#064;Event( { Presenter1.class, Presenter2.class })
  * void event(Integer val);
  * </pre>
  * 
  * will be forwarded to
  * 
  * <pre>
  * public void onEvent(Integer val)
  * </pre>
  * 
  * on both Presenter1 and Presenter2.
  * 
  * All arguments on the original are passed through.
  * 
  * Event methods should be public, have a void return type and only take
  * reference data types as arguments.
  * 
  * @author gpremer
  * 
  */
 @Retention(RetentionPolicy.RUNTIME)
 @Target(ElementType.METHOD)
 public @interface Event {
 
	/**
	 * The {@link Presenter}s to forward this event to
	 * 
	 * @return presenters
	 */
 	Class<? extends Presenter<? extends View, ? extends EventBus>>[] value();
 
 }
