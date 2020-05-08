 /*
  * Sorts.java
  *
  * Created on November 6, 2007, 2:18 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package br.org.indt.ndg.mobile;
 
 import java.util.Vector;
 /**
  *
  * @author tdickes
  */
 public class SortsKeys {
 
     /** Creates a new instance of Sorts */
     public SortsKeys() {
     }
 
     //Vector of Strings version
     public void qsort(Vector list) {
         quicksort(list, 0, list.size()-1);
     }
 
     //Vector of Strings version
     private void quicksort(Vector list, int p, int r) {
         if (p < r) {
             int q = qpartition(list, p, r);
             if (q == r) {
                 q--;
             }
             quicksort(list, p, q);
             quicksort(list, q+1, r);
         }
     }
 
     //Vector of Strings version
     private int qpartition(Vector list, int p, int r) {
        int pivot = Integer.parseInt((String)list.elementAt(p));
         int lo = p;
         int hi = r;
 
         while (true) {
            while ( Integer.parseInt((String)list.elementAt(hi)) >= pivot && lo < hi) {
                 hi--;
             }
            while ( Integer.parseInt((String) list.elementAt(lo)) < pivot && lo < hi) {
                 lo++;
             }
             if (lo < hi) {
                 String T = (String) list.elementAt(lo);
                 list.setElementAt(list.elementAt(hi), lo);
                 list.setElementAt(T, hi);
             }
             else return hi;
         }
     }
 }
