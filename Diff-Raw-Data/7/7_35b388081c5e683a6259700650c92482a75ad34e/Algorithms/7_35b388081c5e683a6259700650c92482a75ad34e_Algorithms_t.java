 /*
  * Created on 26-Sep-2003
  */
 package uk.org.ponder.intutil;
 
 import java.util.Arrays;
 import java.util.Random;
 
 import uk.org.ponder.arrayutil.ArrayUtil;
 
 /**
  * @author Bosmon
  *
  * The class Algorithms incorporates several useful algorithms culled from the STL.
  */
 public class Algorithms {
  
   // QQQQQ crappy n^2 implementation until can think of some way to run
   // an intpair sort in Java.
   public static int[] invert_permutation(int[] indices, int maximum) {
     int[] togo = new int[maximum];
     for (int i = 0; i < maximum; ++ i) {
       togo[i] = ArrayUtil.indexOf(indices, i);
     }
     return togo;
   }
   
   public static void random_shuffle(intVector toshuf, int first, int last, Random random) {
     for (int i = first + 1; i < last; ++ i) {
       int swapind = first + random.nextInt(1 + i - first);
       int temp = toshuf.intAt(i);
       toshuf.setIntAt(i, toshuf.intAt(swapind));
       toshuf.setIntAt(swapind, temp);   
     }
   }
   
   public static void random_shuffle(int[] toshuf, Random random) {
     for (int i = 1; i < toshuf.length; ++ i) {
       int swapind = random.nextInt(1 + i);
       int temp = toshuf[i]; 
       toshuf[i] = toshuf[swapind];
       toshuf[swapind] = temp;
     }
   }
   
   public static int[] ensure_size(int[] array, int newsize) {
     if (array != null && array.length >= newsize) {
       return array;
     }
     else return new int[newsize*3 / 2];
   }
  
   public static boolean[] ensure_size(boolean[] array, int newsize) {
     if (array != null && array.length >= newsize) {
       return array;
     }
     else return new boolean[newsize*3 / 2];
   }
   
   public static int[] random_sample(int choose, int from, Random random) {
     int[] bits = new int[from];
     for (int i = 0; i < choose; ++ i) {
       bits[i] = 1;
     }
     random_shuffle(bits, random);
     int[] togo = new int[choose];
     int index = 0;
     for (int i = 0; i < from; ++ i) {
       if (bits[i] == 1) togo[index++] = i;
     }
     return togo;
   }
   
   public static boolean equals(int[] array1, int[] array2) {
     if (array1.length != array2.length) return false;
     for (int i = 0; i < array1.length; ++ i) {
       if (array1[i] != array2[i]) return false;
     }
     return true;
   }
   // I do not write this in the C style for "purely philosophical reasons"
   public static int lexicalCompare(int[] array1, int length1, int[] array2, int length2) {
     int i = 0;
     while (true) {
       if (i >= length1) { 
         // off the end of 1 means that 1 is possibly shorter
         if (length1 == length2) return 0;
         else return -1;
       }
       if (i >= length2) return 1;
       int a1 = array1[i];
       int a2 = array2[i];
       if (a1 < a2) return -1;
       else if (a1 > a2) return 1;
       ++i;
     }
   }
   
   public static int[] makeIota(int size, int start) {
     int[] togo = new int[size];
     for (int i = 0; i < size; ++ i) {
       togo[i] = start + i;
     }
     return togo;
   }
   
 
   /**
    * @param size
    * @param val
    * @return
    */
   public static int[] fill(int size, int val) { 
     int[] togo = new int[size];
     for (int i = 0; i < size; ++ i) {
       togo[i] = val;
     }
     return togo;
   }
   
  public static int[] fill(int[] target, int size, int val) {
    for (int i = 0; i < size; ++ i) {
      target[i] = val;
    }
    return target;
  }
  
   public static int[] copy(int[] tocopy) {
     int[] togo = new int[tocopy.length];
     System.arraycopy(tocopy, 0, togo, 0, tocopy.length);
     return togo;
   }
   
   /** Count the number of set bits in the argument, by the accepted method */
   public static int count_bits(int tocount) {
     int n = 0;
     for (; tocount != 0; n++) {
       tocount &= tocount - 1;
     }
     return n;
   }
   
   /*
   public static void reverse(intIterator first, intIterator last) {
     while (true)
       if (first == last || first == last.prev())
         return;
       else {
         int temp = first.getInt();
         first.setInt(last.getInt());
         iter_swap(__first++, __last);
         first.next();
       }
   }
 
   template <class _BidirectionalIter, class _Distance>
   _BidirectionalIter __rotate(_BidirectionalIter __first,
                               _BidirectionalIter __middle,
                               _BidirectionalIter __last,
                               _Distance*,
                               const bidirectional_iterator_tag &) {
     if (__first == __middle)
       return __last;
     if (__last  == __middle)
       return __first;
 
     __reverse(__first,  __middle, bidirectional_iterator_tag());
     __reverse(__middle, __last,   bidirectional_iterator_tag());
 
     while (__first != __middle && __middle != __last)
       swap (*__first++, *--__last);
 
     if (__first == __middle) {
       __reverse(__middle, __last,   bidirectional_iterator_tag());
       return __last;
     }
     else {
       __reverse(__first,  __middle, bidirectional_iterator_tag());
       return __first;
     }
   }
 */
 
 }
