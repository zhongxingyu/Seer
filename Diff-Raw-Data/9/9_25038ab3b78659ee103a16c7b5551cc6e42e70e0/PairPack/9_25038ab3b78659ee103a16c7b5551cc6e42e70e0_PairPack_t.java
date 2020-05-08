 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.epsilony.tsmf.util.pair;
 
 /**
  *
  * @author Man YUAN <epsilonyuan@gmail.com>
  */
public class PairPack<K, V> implements WithPair<K,V> {
 
     public K key;
     public V value;
 
     public PairPack(K value, V attach) {
         this.key = value;
         this.value = attach;
     }
 
     @Override
     public K getKey() {
         return key;
     }
 
     @Override
     public V getValue() {
         return value;
     }
 }
