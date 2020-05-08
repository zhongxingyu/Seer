 /**
  *  Copyright (c) 2011, The Staccato-Commons Team
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as published by
  *  the Free Software Foundation; version 3 of the License.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  */
 package net.sf.staccatocommons.lang.thunk;
 
 import java.util.Date;
 import java.util.concurrent.Callable;
 
 import net.sf.staccatocommons.check.Ensure;
 import net.sf.staccatocommons.defs.Thunk;
 import net.sf.staccatocommons.defs.function.Function;
 import net.sf.staccatocommons.iterators.thriter.internal.ConstantThunk;
 import net.sf.staccatocommons.lang.SoftException;
 import net.sf.staccatocommons.lang.function.AbstractFunction;
 import net.sf.staccatocommons.lang.thunk.internal.DateThunk;
 import net.sf.staccatocommons.lang.thunk.internal.FailThunk;
 import net.sf.staccatocommons.lang.thunk.internal.NullThunk;
 import net.sf.staccatocommons.lang.thunk.internal.UndefinedThunk;
 import net.sf.staccatocommons.restrictions.Constant;
 import net.sf.staccatocommons.restrictions.check.NonNull;
 
import com.sun.istack.internal.NotNull;
 
 /**
  * Class factory methods for some common {@link Thunk}s
  * 
  * @author flbulgarelli
  * 
  */
 public class Thunks {
 
   private Thunks() {}
 
   /**
    * Returns a thunk that returns always the given value
    * 
    * @param <A>
    * @param value
    *          the value the constant thunk will return as when invoking
    *          {@link Thunk#value()}
    * @return a new thunk
    */
   public static <A> Thunk<A> constant(A value) {
     return new ConstantThunk<A>(value);
 
   }
 
   /**
    * Returns a constant {@link Thunk} that always provides <code>null</code>
    * 
    * @param <A>
    * @return a constant thunk of nulls
    */
   @Constant
   public static <A> Thunk<A> null_() {
     return NullThunk.null_();
   }
 
   /**
    * Returns a {@link Thunk} that provides the current date
    * 
    * @return a constant thunk that provides <code>new Date()</code>
    */
   @Constant
   public static Thunk<Date> currentDate() {
     return DateThunk.INSTANCE;
   }
 
   /**
    * Returns a thunk whose value is retrieved sending {@link Callable#call()} to
    * the given {@link Callable}
    * 
    * @param <A>
    * @param callable
    * @return a new {@link Thunk} that wraps the given callable
    */
   public static <A> Thunk<A> from(@NonNull final Callable<A> callable) {
     return new Thunk<A>() {
       public A value() {
         return SoftException.callOrSoften(callable);
       }
     };
   }
 
   /**
    * Returns a thunk that when evaluated throws a {@link RuntimeException}. This
    * Thunk is said to have an undefined element.
    * 
    * @param <A>
    * @return a {@link Constant} undefined thunk
    */
   @Constant
   public static <A> Thunk<A> undefined() {
     return UndefinedThunk.undefined();
   }
   
   /**
    * Answers a {@link Thunk} that, when evaluated, throws an exception with the given 
    * formatted message as {@link Ensure#fail(String, Object...)}
    * 
    * @param message the message
    * @param args the message arguments
    * @return a new {@link Thunk} that fails with the given message 
    */
  public static <A> Thunk<A> fail(@NotNull String message, Object... args) {
     return new FailThunk<A>(message, args);
   }
 
   /**
    * Returns a cell that provides not actual value, but a side effect instead,
    * by sending {@link Runnable#run()} to the given <code>runnable</code>
    * 
    * @param runnable
    * @return a new {@link Thunk} that wraps the given {@link Runnable}
    */
   public static Thunk<Void> from(@NonNull final Runnable runnable) {
     return new Thunk<Void>() {
       public Void value() {
         runnable.run();
         return null;
       }
     };
   }
 
   /**
    * Returns a {@link Function} that evaluates its Thunk argument
    * 
    * @param <A>
    * @return a constant {@link Function}
    */
   @Constant
   public static <A> Function<Thunk<A>, A> value() {
     return new AbstractFunction<Thunk<A>, A>() {
       public A apply(Thunk<A> arg) {
         return arg.value();
       }
     };
   }
 }
