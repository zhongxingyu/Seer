 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.epsilony.tsmf.util.rangesearch;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import net.epsilony.tsmf.util.pair.PairPack;
 import net.epsilony.tsmf.util.pair.WithPair;
 import net.epsilony.tsmf.util.pair.WithPairComparator;
 
 /**
  * <p> A Layered Range Tree, a fractional cascading Range Tree. </br> the build
  * time is O(log<sup>d</sup>(n)n) </br> the search time is
  * O(log<sup>(d-1)</sup>(n)) </br> holdind a Layered Rang Tree need
  * O(log<sup>d</sup>(n)n) memory</br> in fact it's about
  * 72*log<sup>d</sup><sub>2</sub>(n)*n+32*log<sup>d-1</sup><sub>2</sub>*n+72*log<sub>2</sub>(n)*n+dataMemory
  * (bytes) where d means dimension;</br> </p> <p> The whole algorithm is
  * described minutely in Mark de Berg et. al. <i>Computational Geometry
  * Algorithms and Applications(Third Edition)</i> Ch5 </br> It should be pointed
  * out that the input keys should not contain duplicate objects.
  *
  * @param E
  * @author Man YUAN <epsilonyuan@gmail.com>
  */
 public class LayeredRangeTree<K, V> {
 
     TreeNode root;
     ArrayList<DictComparator<K>> dictComparators;
 
     /**
      *
      * @param keys
      * @param comparators
      */
     public LayeredRangeTree(Collection<? extends WithPair<K, V>> datas, List<? extends Comparator<K>> comparators) {
         buildTree(comparators, datas);
     }
 
     public LayeredRangeTree(List<? extends K> keys, List<? extends V> values, List<? extends Comparator<K>> comparators) {
         if (keys.size() != values.size()) {
             throw new IllegalArgumentException("Keys's size and values' size mismatch");
         }
 
         Iterator<? extends K> keyIter = keys.iterator();
         Iterator<? extends V> valIter = values.iterator();
         LinkedList<WithPair<K, V>> pairs = new LinkedList<>();
         while (keyIter.hasNext()) {
             K key = keyIter.next();
             V val = valIter.next();
             pairs.add(new PairPack<>(key, val));
         }
         buildTree(comparators, pairs);
     }
 
     public static <T> LayeredRangeTree<T, T> factory(List<? extends T> keys, List<? extends Comparator<T>> comparators) {
         return new LayeredRangeTree<>(keys, keys, comparators);
     }
 
     public void rangeSearch(Collection<? super V> results, K from, K to) {
         results.clear();
         root.rangeQuary(results, from, to);
     }
 
     private void buildTree(List<? extends Comparator<K>> comparators, Collection<? extends WithPair<K, V>> datas) throws IllegalArgumentException {
         dictComparators = new ArrayList<>(comparators.size());
         ArrayList<Comparator<K>> normComps = new ArrayList<>(comparators);
         ArrayList<ArrayList<WithPair<K, V>>> sortedPairLists = new ArrayList<>(comparators.size());
         for (int i = 0; i < comparators.size(); i++) {
             DictComparator<K> dictComparator = new DictComparator<>(normComps, false, i);
             dictComparators.add(dictComparator);
             ArrayList<WithPair<K, V>> sortedPair = new ArrayList<>(datas);
             Comparator<WithPair<K, V>> pairComp = new WithPairComparator<>(dictComparator);
             Collections.sort(sortedPair, pairComp);
             if (i == 0) {
                 for (int j = 1; j < sortedPair.size(); j++) {
                     if (0 == pairComp.compare(sortedPair.get(j - 1), sortedPair.get(j))) {
                         throw new IllegalArgumentException("The input datas contains two elements which are indistinguishable for each other.");
                     }
                 }
             }
             sortedPairLists.add(sortedPair);
         }
 
         root = new TreeNode(sortedPairLists, 0);
     }
 
     final class TreeNode {
 
         boolean isLeaf() {
             return left == null;
         }
         K key;
         V value;
         int mainKeyIndex;
 
         DictComparator<K> dictComparator() {
             return dictComparators.get(mainKeyIndex);
         }
         FraCasData fraCasData;
         TreeNode associateTree;
         TreeNode left;    //<=key
         TreeNode right;  //>key
 
         TreeNode(ArrayList<ArrayList<WithPair<K, V>>> sortedPairLists, int mainKeyIndex) {
             ArrayList<WithPair<K, V>> sortedPs = sortedPairLists.get(mainKeyIndex);
             this.mainKeyIndex = mainKeyIndex;
             if (sortedPs.size() == 1) {
                 WithPair<K, V> pair = sortedPs.get(0);
                 key = pair.getKey();
                 value = pair.getValue();
             } else {
                 int midIndex = (sortedPs.size() - 1) / 2;
                 WithPair<K, V> pair = sortedPs.get(midIndex);
                 key = pair.getKey();
                 value = pair.getValue();
                 ArrayList<ArrayList<WithPair<K, V>>> leftSorted = new ArrayList<>(sortedPairLists.size());
                 ArrayList<ArrayList<WithPair<K, V>>> rightSorted = new ArrayList<>(sortedPairLists.size());
 
                 for (int i = 0; i < sortedPairLists.size(); i++) {
                     ArrayList<WithPair<K, V>> lefts, rights;
                     if (i < mainKeyIndex) {
                         lefts = null;
                         rights = null;
                     } else {
                         List<WithPair<K, V>> sortedPairs = sortedPairLists.get(i);
                         lefts = new ArrayList<>(midIndex + 1);
                         rights = new ArrayList<>(sortedPs.size() - midIndex - 1);
                         for (WithPair<K, V> p : sortedPairs) {
                             if (dictComparator().compare(p.getKey(), key) <= 0) {
                                 lefts.add(p);
                             } else {
                                 rights.add(p);
                             }
                         }
                     }
                     leftSorted.add(lefts);
                     rightSorted.add(rights);
                 }
                 if (mainKeyIndex < dictComparators.size() - 2) {
                     associateTree = new TreeNode(sortedPairLists, mainKeyIndex + 1);
                 } else {
                     fraCasData = new FraCasData(
                             sortedPairLists.get(mainKeyIndex + 1),
                             leftSorted.get(mainKeyIndex + 1),
                             rightSorted.get(mainKeyIndex + 1));
                 }
                 left = new TreeNode(leftSorted, mainKeyIndex);
                 right = new TreeNode(rightSorted, mainKeyIndex);
             }
         }
 
         public int dictCompare(K o1, K o2) {
             return dictComparator().compare(o1, o2);
         }
 
         TreeNode getSplitNode(K from, K to) {
             TreeNode v = this;
             boolean b = dictCompare(to, v.key) <= 0;
             while (!v.isLeaf() && (b || dictCompare(from, v.key) > 0)) {
                 v = b ? v.left : v.right;
                 b = dictCompare(to, v.key) <= 0;
             }
             return v;
         }
 
         void rangeQuary(Collection<? super V> results, K from, K to) {
             TreeNode vs = getSplitNode(from, to);
             if (vs.isLeaf()) {
                 vs.checkTo(from, to, results);
             } else {
                 if (dictComparator().getMainKey() < dictComparator().getKeyDimensionSize() - 2) {
                     TreeNode v = vs.left;
                     while (!v.isLeaf()) {
                         if (dictCompare(from, v.key) <= 0) {
                             v.right.rangeQuary(results, from, to);
                             v = v.left;
                         } else {
                             v = v.right;
                         }
                     }
                     v.checkTo(from, to, results);
                     v = vs.right;
                     while (!v.isLeaf()) {
                         if (dictCompare(v.key, to) <= 0) {
                             v.left.rangeQuary(results, from, to);
                             v = v.right;
                         } else {
                             v = v.left;
                         }
                     }
                     v.checkTo(from, to, results);
                 } else {
                     TreeNode v = vs.left;
                     int casIndex = 0;
                     if (!v.isLeaf()) {
                         casIndex = v.fraCasData.searchCasIndex(from);
                     }
                     while (!v.isLeaf() && casIndex < v.fraCasData.keys.size()) {
                         if (dictCompare(from, v.key) <= 0) {
                             if (v.right.isLeaf()) {
                                 v.right.checkTo(from, to, results);
                             } else {
                                 v.right.fraCasData.checkTo(results, v.fraCasData.rightCas[casIndex], to);
                             }
                             casIndex = v.fraCasData.leftCas[casIndex];
                             v = v.left;
                         } else {
                             casIndex = v.fraCasData.rightCas[casIndex];
                             v = v.right;
                         }
                     }
                     if (v.isLeaf()) {
                         v.checkTo(from, to, results);
                     }
 
                     v = vs.right;
                     if (!v.isLeaf()) {
                         casIndex = v.fraCasData.searchCasIndex(from);
                     }
                     while (!v.isLeaf() && casIndex < v.fraCasData.keys.size()) {
                         if (dictCompare(v.key, to) <= 0) {
                             if (v.left.isLeaf()) {
                                 v.left.checkTo(from, to, results);
                             } else {
                                 v.left.fraCasData.checkTo(results, v.fraCasData.leftCas[casIndex], to);
                             }
                             casIndex = v.fraCasData.rightCas[casIndex];
                             v = v.right;
                         } else {
                             casIndex = v.fraCasData.leftCas[casIndex];
                             v = v.left;
                         }
                     }
                     if (v.isLeaf()) {
                         v.checkTo(from, to, results);
                     }
                 }
             }
         }
 
         void checkTo(K from, K to, Collection<? super V> results) {
            List<? extends Comparator<K>> comparators = dictComparator().getComparators();
             boolean b = true;
             for (int i = mainKeyIndex; i < comparators.size(); i++) {
                 if (comparators.get(i).compare(from, key) > 0 || comparators.get(i).compare(key, to) > 0) {
                     b = false;
                     break;
                 }
             }
             if (b) {
                 results.add(value);
             }
         }
     }
 
     /**
      * Fractional Cascading Data, acts as the range tree of the last dimension.
      */
     final class FraCasData {
 
         /**
          *
          * @param pairs
          * @param leftDatas
          * @param rightDatas
          */
         FraCasData(ArrayList<WithPair<K, V>> pairs, ArrayList<WithPair<K, V>> leftDatas, ArrayList<WithPair<K, V>> rightDatas) {
             this.keys = new ArrayList<>(pairs.size());
             this.values = new ArrayList<>(pairs.size());
             for (WithPair<K, V> pair : pairs) {
                 keys.add(pair.getKey());
                 values.add(pair.getValue());
             }
             leftCas = new int[pairs.size()];
             rightCas = new int[pairs.size()];
             int l = 0, r = 0;
             for (int i = 0; i < pairs.size(); i++) {
                 leftCas[i] = l;
                 rightCas[i] = r;
                 if (l < leftDatas.size() && pairs.get(i) == leftDatas.get(l)) {
                     l++;
                 } else if (r < rightDatas.size()) {
                     r++;
                 }
             }
         }
         //
 
         DictComparator<K> dictComparator() {
             return dictComparators.get(dictComparators.size() - 1);
         }
         ArrayList<K> keys;
         ArrayList<V> values;
         //fractional cascading keys:
         int[] leftCas;    //leftCase[i] is the smallest one that left.associate.keys[leftCas[i]]>=keys[i], if leftCas[i]>left.associate.data it should be -1
         int[] rightCas;  //like leftCase
 
         int searchCasIndex(K from) {
             int fromIndex = Collections.binarySearch(keys, from, dictComparator());
             if (fromIndex < 0) {
                 return -fromIndex - 1;
             }
             return fromIndex;
         }
 
         void checkTo(Collection<? super V> results, int fromIndex, K to) {
             Comparator<K> comp = dictComparator().comparators.get(dictComparator().getMainKey());
             for (int i = fromIndex; i < keys.size(); i++) {
                 K key = keys.get(i);
                 if (comp.compare(key, to) > 0) {
                     return;
                 }
                 results.add(values.get(i));
             }
         }
     }
 }
