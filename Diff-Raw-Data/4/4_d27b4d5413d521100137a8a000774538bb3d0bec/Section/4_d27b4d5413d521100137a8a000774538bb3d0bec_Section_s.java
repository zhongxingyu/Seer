 package com.silviaterra;
 
 import java.util.ArrayList;
 
 /**
  *
  * @author vincent
  */
 public class Section
 {    
     ArrayList<ArrayList<Double>> pixelList;
     String volumeShape;
     int upper;
     int middle;
     int lower;
     double volume;
 
     public Section(ArrayList pixelList, int id)
     {
         this.pixelList = pixelList;
         upper = id * 2 - 2;
         middle = id * 2 -1;
         lower = id * 2;
         
 //        System.out.println(upper + " - " + middle + " - " + lower); 
     }
     
     public void getVolumeShape()
     {
 //        System.out.println(pixelList.get(upper).get(2));
 //        System.out.println(pixelList.get(middle).get(2));
 //        System.out.println(pixelList.get(lower).get(2));
         
         if((pixelList.get(middle).get(2) - pixelList.get(upper).get(2)) > (pixelList.get(lower).get(2) - pixelList.get(middle).get(2)))
         {
             volumeShape = "paraboloid";
             calculateParaboloidVolume();
         }
         else
         {
             volumeShape = "neiloid";
             calculateNeiloidVolume();
         }
         System.out.println(volumeShape);
     }
     
     
    // http://www2.latech.edu/~strimbu/Teaching/FOR306/T4.pdfhttp://www2.latech.edu/~strimbu/Teaching/FOR306/T4.pdf
     private void calculateNeiloidVolume()
     {
         double h;
         double a1;
         double a2;
         
         h = pixelList.get(upper).get(5) - pixelList.get(lower).get(5);
         a1 = Math.pow(pixelList.get(upper).get(4) / 2, 2) * Math.PI;
         a2 = Math.pow(pixelList.get(lower).get(4) / 2, 2) * Math.PI;
         
        volume = (h/4) + (a1 + Math.pow(Math.pow(a1, 2) * a2, 1 / 3) + Math.pow(Math.pow(a2, 2) * a1, 1 / 3) + a2);
     }
     
     private void calculateParaboloidVolume()
     {
         double h;
         double a1;
         double a2;
         
         h = pixelList.get(upper).get(5) - pixelList.get(lower).get(5);
         a1 = Math.pow(pixelList.get(upper).get(4) / 2, 2) * Math.PI;
         a2 = Math.pow(pixelList.get(lower).get(4) / 2, 2) * Math.PI;
         
         volume = ((a1 + a2) / 2) * h;
     }
     
     public double getVolume()
     {
         return volume;
     }
 }
