 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.epsilony.tsmf.shape_func;
 
 import gnu.trove.list.array.TDoubleArrayList;
 import java.util.List;
 import net.epsilony.tsmf.util.Math2D;
 
 /**
  *
  * @author epsilon
  */
 public class Linear2D implements ShapeFunction {
 
     @Override
     public TDoubleArrayList[] values(double[] xy, List<double[]> coords, TDoubleArrayList influcenceRads, TDoubleArrayList[] dists, TDoubleArrayList[] output) {
         if (null != output) {
             output[0].resetQuick();
         } else {
             output = new TDoubleArrayList[]{new TDoubleArrayList(2)};
         }
         double len = Math2D.distance(coords.get(0), coords.get(1));
        double v2 = Math2D.distance(xy, coords.get(0)) / len;
        double v1 = 1 - v2;
         output[0].add(v1);
         output[1].add(v2);
         return output;
     }
 
     public static double[] values(double[] xy, double[] hCoord, double[] rCoord, double[] output) {
         if (null == output) {
             output = new double[2];
         }
         double len = Math2D.distance(hCoord, rCoord);
        double v2 = Math2D.distance(xy, hCoord) / len;
        double v1 = 1 - v2;
         output[0] = v1;
         output[1] = v2;
         return output;
     }
 
     @Override
     public int getDiffOrder() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void setDiffOrder(int diffOrder) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 }
