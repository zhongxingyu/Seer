 package com.saleem_siddiqui.java.util;
 
 /**
  * Created by Saleem Siddiqui on 10/16/12 at 4:29 PM
  * The filter interface, which determines if a given event should be accepted or rejected by the Observer
  * <p/>
  * Implementing classes <b>should</b> strive to provide a stateless implementation of the accept() method. That is,
 * the following <b>should</b> always be true, for two different event objects A and B and an Filter object F:
 * A.equals(B) == (F.accept(A) == F.accept(B))
  */
 public interface Filter<E> {
     boolean accept(E event);
 }
