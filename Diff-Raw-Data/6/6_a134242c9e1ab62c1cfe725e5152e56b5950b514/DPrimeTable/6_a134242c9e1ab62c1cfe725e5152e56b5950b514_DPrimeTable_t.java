 package edu.mit.wi.haploview;
 
 import java.util.Vector;
 
 /**
  * Created by IntelliJ IDEA.
  * User: julian
  * Date: Aug 12, 2004
  * Time: 1:35:31 PM
  * To change this template use File | Settings | File Templates.
  */
 public class DPrimeTable {
     private PairwiseLinkage[][] theTable;
 
     public DPrimeTable(int numMarkers){
         theTable = new PairwiseLinkage[numMarkers][];
     }
 
 
     public void addMarker(Vector marker, int pos){
        theTable[pos] = (PairwiseLinkage[]) marker.toArray(new PairwiseLinkage[0]);
     }
 
 
     public PairwiseLinkage getLDStats(int pos1, int pos2){
         //we need to convert the input of an absolute position into the relative position
         //to index into the DP array. here we jump through the additional hoop of un-filtering the input
         //numbers
         int x = Chromosome.realIndex[pos1];
         int y = Chromosome.realIndex[pos2] - x - 1;
         if (x < theTable.length-1){
             if (y < theTable[x].length){
                 return theTable[x][y];
             }else{
                 return null;
             }
         }else{
             return null;
         }
     }
 
    /* public int getLength(int whichMarker){
         //this is the number of markers in one "row" of the table
         //that is, for whichmarker, the num of other markers it is compared to
         return theTable[whichMarker].length;
     }*/
 
     public int getLength(int x){
         //same as above but for the filtered dataset
         int whichMarker = Chromosome.realIndex[x];
         if (whichMarker >= theTable.length-1) return 0;
         for (int m = theTable[whichMarker].length+whichMarker; m > whichMarker; m--){
             if (Chromosome.filterIndex[m] != -1){
                 //length of array is one greater than difference of first and last elements
                 return Chromosome.filterIndex[m] - Chromosome.filterIndex[whichMarker] + 1;
             }
         }
         return 0;
     }
 
 
 }
