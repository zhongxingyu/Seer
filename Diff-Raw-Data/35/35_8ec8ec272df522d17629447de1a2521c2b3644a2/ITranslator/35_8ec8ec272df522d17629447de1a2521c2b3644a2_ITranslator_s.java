 package org.nohope;
 
 /**
  * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
  * @since 11/15/12 6:16 PM
  */
 public interface ITranslator<S, T> {
     T translate(S source);
    Class<T> getTargetClass();
 }
