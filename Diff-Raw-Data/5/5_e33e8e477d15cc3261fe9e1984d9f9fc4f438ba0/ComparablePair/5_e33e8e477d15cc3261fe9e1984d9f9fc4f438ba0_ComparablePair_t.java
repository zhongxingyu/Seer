 /*
 This file is part of opensearch.
 Copyright © 2009, Dansk Bibliotekscenter a/s, 
 Tempovej 7-11, DK-2750 Ballerup, Denmark. CVR: 15149043
 
 opensearch is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 opensearch is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with opensearch.  If not, see <http://www.gnu.org/licenses/>.
  */
 /**
 * \brief  This is an implementation of the Pair class extended with Comparable to guarantee that elements can be compared, e.g. using Collection.sort
  * \file
  */
 
 
 package dk.dbc.opensearch.common.types;
 
 /**
 * Use this class if you want a {@link Pair} class that can be sorted. It
  * sorts on the first element and only considers the second if the two
  * first elements are equal. If the client needs sorting on the second
  * element and not the first, please consider using a
  * {@code http://java.sun.com/javase/6/docs/api/java/util/Comparator.html}
  * Please bear in mind, that {@link java.lang.Comparable} is used to define
  * the natural sort order of a class. In contrast,
  * java.util.Comparator is used to define an alternative sort order for
  * a class.
  *
  * @param <E>
  * @param <V>
  */
 public final class ComparablePair<E extends Comparable<E>, V extends Comparable<V>>  extends Pair<E, V> implements Comparable<ComparablePair<E, V>>
 {
 
     /**
      * Constructs the {@link ComparablePair} with arguments E as first member of
      * the pair and V as the second member.
      *
      * The {@link ComparablePair} is immutable and as such usable as a key in
      * Hash structures.
      *
      * @param first
      * @param second
      */
     public ComparablePair( E first, V second )
     {
         super( first, second );
     }
 
 
     /**
      * Renders as {@code ComparablePair< E, V >}
      * @return a String representation of the object
      */
     @Override
     public String toString()
     {
         return String.format( "ComparablePair< %s, %s >", getFirst().toString(), getSecond().toString() );
     }
 
 
     /**
      * Compares the object with a {@link ComparablePair} returning values in
      * accordance with the {@link Comparable#compareTo(java.lang.Object)}
      * specification
      * 
      * @param pair the {@link ComparablePair} to compare
      * @return -1 if {@code pair} is smaller than {@code this} 0 if they're equal
      * and 1 otherwise
      */
     @Override
     public int compareTo( ComparablePair<E, V> pair )
     {
         if( !(pair instanceof ComparablePair<?, ?>) )
         {
             throw new UnsupportedOperationException( String.format( "Type %s is not a comparable type", pair.toString() ) );
         }
 
         ComparablePair<E, V> newpair = pair;
 
         if( !(newpair.getFirst().getClass() == getFirst().getClass()) )
         {
             throw new UnsupportedOperationException( String.format( "Type %s is not a comparable to type %s", newpair.getFirst().getClass(), getFirst().getClass() ) );
         }
 
         if( getFirst().equals( newpair.getFirst() ) )
         {
             return getSecond().compareTo( newpair.getSecond());
         }
 
         return getFirst().compareTo( newpair.getFirst());
     }
 }
