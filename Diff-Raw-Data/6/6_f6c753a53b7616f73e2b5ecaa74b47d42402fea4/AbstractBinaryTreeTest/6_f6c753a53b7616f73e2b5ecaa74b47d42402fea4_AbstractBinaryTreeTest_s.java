 package adt.datastruct;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.TreeMap;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 public abstract class AbstractBinaryTreeTest {
     private AbstractBinaryTree tree;
     
     protected AbstractBinaryTreeTest(AbstractBinaryTree tree) {
         this.tree = tree;
     }
     
     @Test
     public void testEmpty() {
         Assert.assertTrue(tree.check());
     }
     
     @Test
     public void testInsertAndRemoveSimple() {
         for (int i = 0; i < 10; ++i)
             tree.insert(i);
         
         Assert.assertTrue(tree.check());
         Assert.assertEquals(10, tree.size());
                 
         for (int i = 0; i < 10; ++i)
             Assert.assertTrue(tree.contains(i));
         
         for (int i = 0; i < 10; ++i)
             tree.remove(i);
         
         Assert.assertTrue(tree.check());
         Assert.assertEquals(0, tree.size());        
     }
     
     @Test
     public void testInsertSmallAll() {
         int[] vs = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
         
         do {
             tree.clear();
             for (int i = 0; i < vs.length; ++i)
                 tree.insert(vs[i]);
             
             Assert.assertTrue(tree.check());
         } while (nextPermutation(vs));
     }
     
     @Test
     public void testInsertAndRemoveAllNormalOrder() {
         int[] vs = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
         
         do {
             tree.clear();
             for (int i = 0; i < vs.length; ++i)
                 tree.insert(vs[i]);
             
             for (int i = 0; i < vs.length; ++i)
                 tree.remove(vs[i]);
             
             Assert.assertTrue(tree.check());
         } while (nextPermutation(vs));        
     }
     
     @Test
     public void testInsertAndRemoveAllReverseOrder() {
         int[] vs = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
         
         do {
             tree.clear();
             for (int i = 0; i < vs.length; ++i)
                 tree.insert(vs[i]);
             
             for (int i = vs.length - 1; i >= 0; --i)
                 tree.remove(vs[i]);
             
             Assert.assertTrue(tree.check());
         } while (nextPermutation(vs));        
     }
 
     
     @Test
     public void testInsertAndRemoveRandomSmall() {
        //for (int i = 0; i < 1000; ++i) {
            int i = 44;
             Random random = new Random(i);
             f(20, random);
        //}
     }
 
     private ArrayList<Integer> createReadableRandomArray(int N, Random random) {
         TreeMap<Integer, Integer> tm = new TreeMap<Integer, Integer>();
         for (int i = 0; i < N; ++i) {
             int v = random.nextInt();
             tm.put(v, i);
         }
         
         ArrayList<Integer> vs = new ArrayList<Integer>(20);
         for (Iterator<Entry<Integer, Integer>> it = tm.entrySet().iterator(); it.hasNext(); ) {
             Entry<Integer, Integer> entry = it.next();
             vs.add(entry.getValue());
         }
         
         return vs;
     }
     
     private void f(int N, Random random) {
         ArrayList<Integer> vs = createReadableRandomArray(N, random);
         for (int i = 0; i < N; ++i)
             tree.insert(vs.get(i));
         
         Assert.assertTrue(tree.check());
         Assert.assertEquals(N, tree.size());
 
         Collections.shuffle(vs, random);
         
         for (int i = 0; i < N; ++i)
             tree.remove(vs.get(i));
         
         Assert.assertEquals(0, tree.size());        
     }
     
     @Test
     public void testInsertAndRemoveRandomLarge() {
         Random random = new Random(253);
         ArrayList<Integer> vs = new ArrayList<Integer>();
         final int N = 1000;
         
         for (int i = 0; i < N; ++i) {
             int v = random.nextInt();
             vs.add(v);
             tree.insert(v);
         }
         
         Assert.assertTrue(tree.check());
         Assert.assertEquals(N, tree.size());
 
         Collections.shuffle(vs, random);
         
         for (int i = 0; i < N; ++i) {
             tree.remove(vs.get(i));
         }
         
         Assert.assertEquals(0, tree.size());
     }
     
     
     public static boolean nextPermutation(int[] a) {
         for (int i = a.length - 1; i > 0; --i) {
             if (a[i - 1] < a[i]) {
                 int swapIndex = find(a[i - 1], a, i, a.length - 1);
                 int temp = a[swapIndex];
                 a[swapIndex] = a[i - 1];
                 a[i - 1] = temp;
                 Arrays.sort(a, i, a.length);
                 return true;
             }
         }
         return false;
     }
 
     // destより大きい要素の中で最小のもののインデックスを二分探索で探す
     private static int find(int dest, int[] a, int s, int e) {
         if (s == e) {
             return s;
         }
         int m = (s + e + 1) / 2;
         return a[m] <= dest ? find(dest, a, s, m - 1) : find(dest, a, m, e);
     }
 }
