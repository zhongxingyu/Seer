 /* Jedd - A language for implementing relations using BDDs
  * Copyright (C) 2005 Ondrej Lhotak
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the
  * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
  * Boston, MA 02111-1307, USA.
  */
 
 package jedd.internal;
 import java.util.*;
 import jedd.Numberer;
 
 public abstract class Domain {
     public abstract Numberer numberer();
     public abstract int maxBits();
     protected boolean[] usefulBits;
     public boolean[] usefulBits() {return usefulBits;}
     public int maxUsefulBit() {
         return usefulBits.length;
     }
     public int numUsefulBits() {
         return maxBits();
     }
 
     public Domain() {
         usefulBits = new boolean[maxBits()];
         for( int i = 0; i < numUsefulBits(); i++ ) {
             usefulBits[i] = true;
         }
     }
 
     public String name() {
         return this.getClass().getName();
     }
     public String toString() { return name(); }
     public void setBits( PhysicalDomain physDom, int[] bits, long value ) {
        long origValue = value;
         if(physDom.bits() < maxUsefulBit()) throw new RuntimeException("Physical domain "+physDom+" is too small for domain "+this );
         int bit = physDom.firstBit();
         for( int i = 0; i < maxUsefulBit(); i++ ) {
             if(usefulBits[i]) bits[bit] = (int) (value & 1L);
             bit++;
             value >>>= 1;
         }
        if( value != 0 ) throw new RuntimeException( "Value "+origValue+" was too large in domain "+name()+"!" );
     }
     public long readBits(PhysicalDomain physDom, int[] bits) {
         if(physDom.bits() < maxUsefulBit()) throw new RuntimeException("Physical domain "+physDom+" is too small for domain "+this );
         long ret = 0;
         int bit = physDom.firstBit()+maxUsefulBit()-1;
         for( int i = maxUsefulBit()-1; i >=0; i-- ) {
             ret <<= 1;
             if(usefulBits[i]) ret = ret | bits[bit];
             bit--;
         }
         return ret;
     }
 }
