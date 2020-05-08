 /*
    Copyright 2012 Mattias Jiderhamn
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 package se.jiderhamn;
 
 /**
  * Utility class that provides more readable syntax for {@link java.lang.Comparable#compareTo(Object)}.
  * 
  * Examples:
  * <code>
  *   import static se.jiderhamn.CompareTo.is;
  *
  *...
  *
  *  boolean oneIsZero = is(1).equalTo(0);
  *  ...
  *    
  *  boolean value1LessThanValue2 = is(value1).lessThan(value2);
  *  ...
  *    
  *  if(is(a).lessThanOrEqualTo(b)) {
  *    ...
  *  }
  *
  *  boolean date1AfterDate2 = is(date1).greaterThan(date2);
  *  ...
  *
  *  if(is(a).greaterThanOrEqualTo(a)) {
  *    ...
  *  }
  *
 *  // Short syntax 
  *
 *  boolean oneIsZero = is(1).eq(0));
  *  ...
  *
  *  boolean value1LessThanValue2 = is(value1).lt(value2);
  *  ...
  *
  *  if(is(a).le(b)) {
  *    ...
  *  }
  *
  *  boolean date1AfterDate2 = is(date1).gt(date2);
  *  ...
  *
  *  if(is(a).ge(a)) {
  *    ...
  *  }
  *
  * </code>
  * @author Mattias Jiderhamn
  */
 
 public class CompareTo<T extends Comparable<T>> {
   
   private final Comparable<T> comparable;
 
   private CompareTo(Comparable<T> comparable) {
     this.comparable = comparable;
   }
   
   //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   // Equality
   
   /** Create new instance that allows for chained comparison */
   public static <C extends Comparable<C>> CompareTo<C> is(C comparable) {
     return new CompareTo<C>(comparable);
   }
   
   /** Is the owner equal to the argument? */
   public boolean equalTo(T that) {
     return this.comparable.compareTo(that) == 0;
   }
   
   /** Is the owner equal to the argument? */
   public boolean eq(T that) {
     return this.comparable.compareTo(that) == 0;
   }
   
   //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   // Less
   
   /** Is the owner less than the argument? */
   public boolean lessThan(T that) {
     return this.comparable.compareTo(that) < 0;
   }
 
   /** Is the owner less than or equal to the argument? */
   public boolean lessThanOrEqualTo(T that) {
     return this.comparable.compareTo(that) <= 0;
   }
 
   /** Is the owner less than the argument? */
   public boolean lt(T that) {
     return this.comparable.compareTo(that) < 0;
   }
 
   /** Is the owner less than or equal to the argument? */
   public boolean le(T that) {
     return this.comparable.compareTo(that) <= 0;
   }
   
   //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   // Greater
   
   /** Is the owner greater than the argument? */
   public boolean greaterThan(T that) {
     return this.comparable.compareTo(that) > 0;
   }
 
   /** Is the owner greater than or equal to the argument? */
   public boolean greaterThanOrEqualTo(T that) {
     return this.comparable.compareTo(that) >= 0;
   }
 
   /** Is the owner greater than the argument? */
   public boolean gt(T that) {
     return this.comparable.compareTo(that) > 0;
   }
 
   /** Is the owner greater than or equal to the argument? */
   public boolean ge(T that) {
     return this.comparable.compareTo(that) >= 0;
   }
   
 }
