 package com.chaschev.microbe;
 
 /**
 * User: chaschev
 * Date: 9/5/13
 */
 public abstract class ObjectFactory<T> {
     protected int granularity = -1;
 
    ObjectFactory() {
     }
 
    ObjectFactory(int granularity) {
         this.granularity = granularity;
     }
 
     public abstract T create(int trialIndex);
 
     public boolean isGranular(){
         return granularity != -1;
     }
 
     public int granularity(int trialIndex){
         if(granularity != -1){
             return granularity;
         }
         throw new UnsupportedOperationException("todo: if T is a list, then this method should return it's size");
     }
 
     public ObjectFactory<T> setGranularity(int granularity) {
         this.granularity = granularity;
         return this;
     }
 }
