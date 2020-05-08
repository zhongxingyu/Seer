 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.epsilony.simpmeshfree.sfun;
 
 import gnu.trove.list.array.TDoubleArrayList;
 import net.epsilony.simpmeshfree.utils.CommonUtils;
 import net.epsilony.simpmeshfree.utils.SomeFactory;
 
 /**
  *
  * @author epsilonyuan@gmail.com
  */
 public class WeightFunctions {
 
     public static SomeFactory<WeightFunction> factory(final SomeFactory<WeightFunctionCore> coreFunFactory) {
         return factory(coreFunFactory, 2);
 
     }
 
     public static SomeFactory<WeightFunction> factory(final SomeFactory<WeightFunctionCore> coreFunFactory, final int dim) {
         return new SomeFactory<WeightFunction>() {
             @Override
             public WeightFunction produce() {
                 WeightFunctionImp imp = new WeightFunctionImp(coreFunFactory.produce(), dim);
                 return imp;
             }
         };
     }
 
     public static WeightFunction weightFunction(WeightFunctionCore coreFun, int dim) {
         return new WeightFunctionImp(coreFun, dim);
     }
 
     public static WeightFunction weightFunction(WeightFunctionCore coreFun) {
         return new WeightFunctionImp(coreFun);
     }
 
     static class WeightFunctionImp implements WeightFunction {
 
         private int diffOrder;
         WeightFunctionCore coreFun;
         private final int dim;
         private double[] coreVals;
 
         private WeightFunctionImp(WeightFunctionCore coreFun, int dim) {
             this.coreFun = coreFun;
             this.dim = dim;
         }
 
         private WeightFunctionImp(WeightFunctionCore coreFun) {
             this.coreFun = coreFun;
             this.dim = 2;
         }
 
         private TDoubleArrayList[] initResults(TDoubleArrayList[] ori) {
 
             int len = 0;
             switch (diffOrder) {
                 case 0:
                     len = 1;
                     break;
                 case 1:
                     switch (dim) {
                         case 1:
                             len = 2;
                             break;
                         case 2:
                             len = 3;
                             break;
                         case 3:
                             len = 4;
                             break;
                         default:
                             throw new UnsupportedOperationException();
                     }
                     break;
                 default:
                     throw new UnsupportedOperationException();
             }
 
             if (ori == null) {
                 ori = new TDoubleArrayList[len];
                 for (int i = 0; i < ori.length; i++) {
                     ori[i] = new TDoubleArrayList();
                 }
             } else {
                 for (int i = 0; i < len; i++) {
                     ori[i].resetQuick();
                 }
             }
 
             return ori;
         }
 
         @Override
         public TDoubleArrayList[] values(TDoubleArrayList[] distsSqs, TDoubleArrayList rads, TDoubleArrayList[] results) {
             int size = distsSqs[0].size();
             boolean uniqRads = false;
             double radSq = 0;
             if (rads.size() == 1) {
                 uniqRads = true;
                 radSq = rads.getQuick(0);
                 radSq *= radSq;
             }
             results = initResults(results);
             for (int i = 0; i < size; i++) {
                 if (!uniqRads) {
                     radSq = rads.getQuick(i);
                     radSq *= radSq;
                 }
                 double distSq = distsSqs[0].get(i);
                 coreFun.valuesByNormalisedDistSq(distSq / radSq, coreVals);
                 results[0].add(coreVals[0]);
                 
                 if (diffOrder < 1) {
                     continue;
                 }
 
                 double distSq_x = distsSqs[1].get(i);
                 double distSq_y = distsSqs[2].get(i);
                 results[1].add(coreVals[1] * distSq_x / radSq);
                 results[2].add(coreVals[1] * distSq_y / radSq);
                 if (dim == 3) {
                     double distSq_z = distsSqs[3].get(i);
                     results[3].add(coreVals[1] * distSq_z / radSq);
                 }
                 
             }
             return results;
         }
 
         @Override
         public void setDiffOrder(int order) {
             if (order < 0 || order >= 2) {
                 throw new UnsupportedOperationException();
             }
             this.diffOrder = order;
             coreFun.setDiffOrder(order);
             coreVals = new double[diffOrder + 1];
         }
 
         @Override
         public int getDiffOrder() {
             return diffOrder;
         }
         
         private double[] initValueResult(double[] ori){
             if(null==ori){
                 return new double[CommonUtils.lenBase(dim, diffOrder)];
             }else{
                 return ori;
             }
         }
         
 
         @Override
         public double[] value(double[] distSq, double rad, double[] result) {
             result=initValueResult(result);
            coreFun.valuesByNormalisedDistSq(distSq[0]/rad, result);
             if(diffOrder<1){
                 return result;
             }
             double dw_dwDistSq=result[1];
            result[1]=dw_dwDistSq*distSq[1];
            result[2]=dw_dwDistSq*distSq[2];
             return result;       
         }
     }
 }
