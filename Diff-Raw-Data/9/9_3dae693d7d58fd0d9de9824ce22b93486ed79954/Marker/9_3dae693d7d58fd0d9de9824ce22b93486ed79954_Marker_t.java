 package edu.mit.wi.plink;
 
 
 public class Marker {
 
     private short chromosome;
     private String markerID;
     private long position;
    private static String[] chroms = {"","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","X","Y","XY","MT"};
 
     public Marker(short chrom, String marker, long pos){
         chromosome = chrom;
         markerID = marker;
         position = pos;
     }
 
     public String getChromosome(){
         return chroms[chromosome];
     }
 
     public short getChromosomeIndex(){
         return chromosome;
     }
 
     public String getMarkerID(){
         return markerID;
     }
 
     public long getPosition(){
         return position;
     }
 
 }
