 /*
  * Copyright 2012 CoreMedia AG
  *
  * This file is part of Joala.
  *
  * Joala is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Joala is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Joala.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.joala.bdd.reference;
 
 import javax.annotation.Nonnull;
 
 /**
  * <p>
  * Factory methods for creating references. These factory methods are meant to provide
  * a shortcut to create new instances.
  * </p>
  *
 * @since 9/27/12
  * @see net.joala.bdd
  */
 public final class References {
   /**
    * Utility class - don't instantiate.
    */
   private References() {
   }
 
   /**
    * <p>
    * Factory method for simple (unlogged) references.
    * </p>
   * <p>
   * <strong>Usage:</strong>
   * </p>
   * <pre>{@code
    * Reference<String> myString = References.ref();
   * }</pre>
    *
    * @param <T> the type of value contained in the reference
    * @return a reference
    */
   @Nonnull
   public static <T> Reference<T> ref() {
     return new ReferenceImpl<T>();
   }
 
   /**
    * <p>
    * Create a reference which is self-describing. This is especially meant to be
   * used together with steps logging.
   * </p>
   * <p>
   * <strong>Usage:</strong>
    * </p>
   * <pre>{@code
    * Reference<String> myString = References.ref("aName");
   * }</pre>
    *
    * @param name the name (or description) of the reference used for logging
    * @param <T>  the type of value contained in the reference
    * @return a reference
    */
   @Nonnull
   public static <T> Reference<T> ref(final String name) {
     return new SelfDescribingReferenceImpl<T>(name);
   }
 }
