 package org.discordia.java8.monad;
 
 import java.util.function.Function;
 
 /**
  * @author robban
  */
 public interface Maybe<T> {
     public static <T> Maybe<T> toMaybe(T value) {
         return new Just<>(value);
     }
 
     @SuppressWarnings("unchecked")
     public default <T,B> Maybe<B> bind(Function<T, Maybe<B>> function) {
        Just<B> just = Type.as(Just.class, this);
 
        return (just == null) ? new Nothing<>() : function.apply((T) just.value());
     }
 }
 
 
