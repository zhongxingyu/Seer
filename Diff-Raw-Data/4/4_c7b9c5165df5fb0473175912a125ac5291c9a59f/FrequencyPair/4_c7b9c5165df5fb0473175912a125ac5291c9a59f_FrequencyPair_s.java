 /*
  * Copyright (C) 2012 Fabian Hirschmann <fabian@hirschm.net>
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package com.github.fhirschmann.clozegen.lib.frequency;
 
 /**
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class FrequencyPair<T> implements Comparable<FrequencyPair<T>> {
     private Integer frequency;
     private T value;
 
     public FrequencyPair(final T value, final int frequency) {
         this.frequency = frequency;
         this.value = value;
     }
 
     @Override
     public int compareTo(final FrequencyPair<T> frequencyPair) {
         return frequency.compareTo(frequencyPair.getFrequency());
     }
 
     @Override
     public String toString() {
         return String.format("%s/%s", getValue(), getFrequency());
     }
 
     @Override
     public int hashCode() {
         return getValue().hashCode();
     }
 
     @Override
     public boolean equals(final Object other) {
        return (other instanceof ValueCount)
                && getValue().equals(((ValueCount) other).getValue());
     }
 
     /**
      * @return the value
      */
     public T getValue() {
         return value;
     }
 
     /**
      * @param value the value to set
      */
     public void setValue(T value) {
         this.value = value;
     }
 
     /**
      * @return the frequency
      */
     public int getFrequency() {
         return frequency;
     }
 
     /**
      * @param frequency the frequency to set
      */
     public void setFrequency(int frequency) {
         this.frequency = frequency;
     }
 
 }
