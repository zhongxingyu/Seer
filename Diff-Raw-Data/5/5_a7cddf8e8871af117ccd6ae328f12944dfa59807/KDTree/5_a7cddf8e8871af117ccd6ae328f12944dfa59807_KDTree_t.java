 package ru.spbau.bashorov;
 
 import java.io.Serializable;
import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 public class KDTree<T extends Comparable<T>> implements Serializable {
     private final KDNode root;
     private final DimensionChoicer dimensionChoicer;
     private final BinaryOperation<T[], T> distance;
 
     public KDTree(final List<T[]> data, DimensionChoicer dimensionChoicer, BinaryOperation<T[], T> distance) {
         this.dimensionChoicer = dimensionChoicer;
         this.distance = distance;
         root = new KDNode(data);
     }
 
     public List<T[]> getNearestK(T[] point, int k) {
         return root.getNearestK(point, k);
     }
 
     private class KDNode implements Serializable {
         private static final int MIN_COUNT = 10;
         private final List<T[]> data;
         private final KDNode left;
         private final KDNode right;
         private final int dimension;
         private final T middle;
         private T[] minBounds;
         private T[] maxBounds;
 
         KDNode(List<T[]> data) {
             dimension = dimensionChoicer.choice(data);
             Collections.sort(data, new Comparator<T[]>() {
                 @Override
                 public int compare(T[] l, T[] r) {
                     return l[dimension].compareTo(r[dimension]);
                 }
             });
 
             if (!data.isEmpty()) {
                 minBounds = data.get(0);
                 maxBounds = data.get(data.size() - 1);
             }
 
             if (data.isEmpty() || data.size() <= MIN_COUNT) {
                 this.data = data;
                 middle = null;
                 left = null;
                 right = null;
                 return;
             }
 
             this.data = null;
 
             final int middleIndex = data.size() / 2;
             middle = data.get(middleIndex)[dimension];
 
             final List<T[]> leftData = data.subList(0, middleIndex);
             final List<T[]> rightData = data.subList(middleIndex, data.size());
 
             left = new KDNode(leftData);
             right = new KDNode(rightData);
         }
 
         List<T[]> getNearestK(final T[] point, int k) {
             Comparator<T[]> comparator = new Comparator<T[]>() {
                 @Override
                 public int compare(T[] o1, T[] o2) {
                     return distance.eval(o1, point).compareTo(distance.eval(o2, point));
                 }
             };
 
             if (left == null || right == null) {
                 Collections.sort(data, comparator);
                 return data.subList(0, Math.min(k, data.size()));
             }
 
             boolean searchInLeft = point[dimension].compareTo(middle) < 0;
             List<T[]> result = searchInLeft ? left.getNearestK(point, k) : right.getNearestK(point, k);
             T worstDist = distance.eval(result.get(result.size() - 1), point);
 
             if (searchInLeft) {
                 if (result.size() < k || distance.eval(right.minBounds, point).compareTo(worstDist) < 0) {
                    result = new ArrayList<T[]>(result);
                     result.addAll(right.getNearestK(point, k));
                 }
             } else {
                 if (result.size() < k || distance.eval(point, left.maxBounds).compareTo(worstDist) < 0) {
                    result = new ArrayList<T[]>(result);
                     result.addAll(left.getNearestK(point, k));
                 }
             }
 
             Collections.sort(result, comparator);
             return result.subList(0, Math.min(k, result.size()));
         }
 
         @Override
         public String toString() {
             if (left != null && right != null) {
                 return left.toString() + right.toString();
             }
 
             StringBuilder builder = new StringBuilder();
             for (T[] d : data) {
                 builder.append(d);
                 builder.append("\n");
             }
 
             return builder.toString();
         }
     }
 
     @Override
     public String toString() {
         return "KDTree {" + root.toString() + '}';
     }
 }
