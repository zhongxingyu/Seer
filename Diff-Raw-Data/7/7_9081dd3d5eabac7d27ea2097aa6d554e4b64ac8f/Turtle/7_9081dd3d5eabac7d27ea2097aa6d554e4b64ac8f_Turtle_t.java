 package com.undeadscythes.metaturtle;
 
import com.undeadscythes.metaturtle.unique.MetaType;
 
 /**
 * {@link Turtle} is an implementation of {@link MetaType} that provides
 * {@link Turtle#ROOT ROOT} and {@link Turtle#DEFAULT DEFAULT} elements.
  *
  * @author UndeadScythes
  */
 public final class Turtle implements MetaType {
     /**
      * Root element to define a {@link MetaTurtle}.
      */
     public static final Turtle ROOT = new Turtle();
 
     /**
      * For use when no other {@link MetaType} is given.
      */
     public static final Turtle DEFAULT = new Turtle();
 
     private Turtle() {}
 }
