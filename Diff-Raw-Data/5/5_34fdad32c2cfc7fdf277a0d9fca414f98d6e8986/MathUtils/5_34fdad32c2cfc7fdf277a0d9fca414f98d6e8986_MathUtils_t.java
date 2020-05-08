 package org.pillarone.riskanalytics.core.util;
 
 import umontreal.iro.lecuyer.rng.F2NL607;
 import umontreal.iro.lecuyer.rng.RandomStreamBase;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 public class MathUtils {
     public static RandomStreamBase RANDOM_NUMBER_GENERATOR_INSTANCE = new F2NL607();
     public static int DEFAULT_RANDOM_SEED = 2;
 
     public static double calculatePercentile(double[] values, double percentile) {
         Arrays.sort(values);
         return calculatePercentileOfSortedValues(values, percentile);
     }
 
     public static void initRandomStreamBase(Integer seed) {
         if (seed == null) {
             return;
         }
         F2NL607 generator = new F2NL607();
         generateSeed(generator, seed);
         RANDOM_NUMBER_GENERATOR_INSTANCE = generator;
     }
 
     public static void initDummyRandomStreamBase() {
         DummyRandomStreamBase oldStreamBase = (DummyRandomStreamBase) RANDOM_NUMBER_GENERATOR_INSTANCE;
         if (oldStreamBase != null) {
             oldStreamBase.invalidate();
         }
 
         RANDOM_NUMBER_GENERATOR_INSTANCE = new DummyRandomStreamBase();
 
     }
 
     private static void generateSeed(F2NL607 generator, int seed) {
         double source = Math.PI * (Math.log10(seed) + 1);
         ArrayList<Integer> seedsList = new ArrayList<Integer>();
         for (int i = 0; i < 19; i++) {
             seedsList.add((int) source);
            source = (source - seedsList.get(seedsList.size() - 1)) * (100 + Math.E);
         }
         int[] seeds = new int[seedsList.size()];
 
         for (int i = 0; i < seedsList.size(); i++) {
             seeds[i] = seedsList.get(i);
         }
         generator.setLinearSeed(seeds);
 
         seedsList = new ArrayList<Integer>();
         for (int i = 0; i < 3; i++) {
             seedsList.add((int) source);
            source = (source - seedsList.get(seedsList.size() - 1)) * (100 + Math.E);
         }
 
         seeds = new int[seedsList.size()];
 
         for (int i = 0; i < seedsList.size(); i++) {
             seeds[i] = seedsList.get(i);
         }
 
         generator.setNonLinearSeed(seeds);
     }
 
     /**
      * If an array is not yet sorted use Arrays.sort(values) before applying this function;
      *
      * @param sortedValues
      * @param percentile
      * @return
      */
     public static double calculatePercentileOfSortedValues(double[] sortedValues, double percentile) {
         int size = sortedValues.length;
         double n = (percentile / 100) * (size - 1.0) + 1.0;
         int k = (int) n;
         double d = n - (double) k;
 
         if (k - 1 == 0) {
             return sortedValues[0];
         } else if (k == size) {
             return sortedValues[size - 1];
         } else {
             double result = sortedValues[k - 1];
             result += d * (sortedValues[k] - sortedValues[k - 1]);
             return result;
         }
     }
 
     public static double calculateVar(double[] values, double var) {
         return calculateVar(values, var, calculateMean(values));
     }
 
     public static double calculateVarOfSortedValues(double[] sortedValues, double var, double mean) {
         return calculatePercentileOfSortedValues(sortedValues, var) - mean;
     }
 
     public static double calculateVar(double[] values, double var, double mean) {
         Arrays.sort(values);
         return calculateVarOfSortedValues(values, var, mean);
     }
 
     public static double calculateTvar(double[] values, double tvar) {
         Arrays.sort(values);
         return calculateTvarOfSortedValues(values, tvar);
     }
 
     /**
      * If an array is not yet sorted use Arrays.sort(values) before applying this function;
      *
      * @param sortedValues
      * @param tvar
      * @return
      */
     public static double calculateTvarOfSortedValues(double[] sortedValues, double tvar) {
         double size = (double) sortedValues.length;
         double n = (tvar / 100) * (size - 1.0) + 1.0;
         int k = (int) n;
         double d = n - (double) k;
 
         int index = 0;
         if (d == 0) {
             index = k - 1;
         } else {
             index = k;
         }
         double sum = 0;
         for (int i = index; i < size; i++) {
             sum += sortedValues[i];
         }
         double mean = sum / (size - index);
 
         return mean - (calculateSum(sortedValues) / size);
     }
 
     public static double calculateStandardDeviation(double[] values) {
         return calculateStandardDeviation(values, calculateMean(values));
     }
 
     public static double calculateStandardDeviation(double[] values, double mean) {
         double sqrtSum = 0.0;
         for (double value : values) {
             sqrtSum += Math.pow(value - mean, 2);
         }
         return Math.sqrt(sqrtSum / values.length);
     }
 
     public static double calculateMean(double[] values) {
         return calculateSum(values) / values.length;
     }
 
     public static double calculateSum(double[] values) {
         double sum = 0;
         for (double value : values) {
             sum += value;
         }
         return sum;
     }
 
     public static double max(double[] values) {
         Arrays.sort(values);
         return maxOfSortedValues(values);
     }
 
     public static double maxOfSortedValues(double[] sortedValues) {
         return sortedValues[sortedValues.length - 1];
     }
 
     public static double min(double[] values) {
         Arrays.sort(values);
         return minOfSortedValues(values);
     }
 
     public static double minOfSortedValues(double[] sortedValues) {
         return sortedValues[0];
     }
 }
