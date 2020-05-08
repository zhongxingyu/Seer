 package netcracker.lab1;
 
 // dima.halushko@ukr.net
 // Tu16_14_Pogoda
 
 import org.jetbrains.annotations.NotNull;
 
 import java.io.FileNotFoundException;
 import java.io.PrintStream;
 import java.util.Random;
 
 /**
  * Created by IntelliJ IDEA.
  * User: mpogoda
  * Date: 15/11/11
  * Time: 16:50
  * <p/>
  * Main. ITS MAIN
  *
  * @author Michael Pogoda
  * @version 0.4.0
  */
 final public class Main {
 
     /**
      * Maximum difference between two items in generated sorted array
      */
     private final static int MAX_DIFF = 50;
     /**
      * Maximum number in arrays
      */
     private final static int MAX_NUM = Integer.MAX_VALUE;
     /**
      * Maximum length of array
      */
     private final static int MAX_SIZE = 1000;
 
     /**
      * Generate sorted array (each element (except last) have larger successor
      *
      * @return sorted array of size MAX_SIZE
      */
     @NotNull
     private static Integer[] generateSortedArray() {
         @NotNull final Integer[] result = new Integer[MAX_SIZE];
         @NotNull final Random generator = new Random(System.nanoTime());
         Integer current = 0;
         result[0] = current;
         for (int i = 1; i < MAX_SIZE; ++i) {
             current += generator.nextInt(MAX_DIFF);
             result[i] = current;
         }
 
         return result;
     }
 
     /**
      * generate the array sorted in reverse way (each element (except last) have smaller successor)
      *
      * @return array of size MAX_SIZE
      */
     @NotNull
     private static Integer[] generateReverseSortedArray() {
         @NotNull final Integer[] result = new Integer[MAX_SIZE];
         @NotNull final Random generator = new Random(System.nanoTime());
         Integer current = 0;
         result[Main.MAX_SIZE - 1] = current;
         for (int i = Main.MAX_SIZE - 2; i >= 0; --i) {
             current += generator.nextInt(MAX_DIFF);
             result[i] = current;
         }
 
         return result;
     }
 
     /**
      * Initialize array with random elements
      *
      * @return array of size MAX_SIZE
      */
     @NotNull
     private static Integer[] generateRandomArray() {
         @NotNull final Integer[] result = new Integer[MAX_SIZE];
         @NotNull final Random generator = new Random(System.nanoTime());
         for (int i = 0; i < Main.MAX_SIZE; ++i) {
             result[i] = generator.nextInt(MAX_NUM);
         }
 
         return result;
     }
 
     /**
      * Benchmark sortMethod on array
      *
      * @param array      Array to be sorted
      * @param sortMethod Sorting method
      * @param length     The length of array
      * @return the time sortMethod took to sort array in nanoseconds
      */
     private static long benchmark(@NotNull final Integer[] array, @NotNull final AbstractSort sortMethod,
                                   final int length) {
         final long currentTime = System.nanoTime();
         sortMethod.sort(array, length);
         return System.nanoTime() - currentTime;
     }
 
     /**
      * Benchmark sortMethod on all subarrays, started from 1 first element.
      * No element are changed during benchmarking.
      * All results are printed to stdout
      *
      * @param array      array, which will be sorted
      * @param sortMethod sorting method
      */
     private static void benchmarkWithoutChangeOfArray(@NotNull final Integer[] array, @NotNull final AbstractSort sortMethod) {
         for (int i = 1; i < MAX_SIZE; ++i) {
             System.out.print(i);
             System.out.print('\t');
             System.out.println(benchmark(array, sortMethod, i));
             System.gc(); //I DO NOT WANT TO GC when BENCHMARKING
         }
     }
 
     /**
      * cases ACD
      * Simply call benchmarkWithoutChangeOfArray on every sortMethod
      * and redirect output to file
      *
      * @param array      array to be sorted during benchmarking
      * @param caseLetter case letter ('a', 'c' or 'd')
      * @throws FileNotFoundException just in case
      */
     private static void benchmarkCaseACD(@NotNull final Integer[] array, final char caseLetter)
             throws FileNotFoundException {
         System.err.println("Benchmark case" + caseLetter);
         for (Sort sortName : Sort.values()) {
             System.err.println("Begin benchmark " + sortName.toString());
             @NotNull AbstractSort sortMethod = SortFactory.getSort(sortName);
 
            System.setOut(new PrintStream("case" + caseLetter + "_" + sortMethod.toString()));
             benchmarkWithoutChangeOfArray(array, sortMethod);
             System.out.close();
 
             System.err.println("End benchmark " + sortMethod.toString());
         }
         System.err.println("End benchmark case" + caseLetter);
     }
 
     /**
      * Benchmark all sorting methods with sorted array
      *
      * @throws FileNotFoundException just in case
      */
     private static void caseA() throws FileNotFoundException {
         benchmarkCaseACD(generateSortedArray(), 'A');
     }
 
     /**
      * Benchmark sortMethod on array, with changing last element to random element.
      *
      * @param array      array, which will be sorted
      * @param sortMethod sorting method
      */
     private static void benchmarkArrayAndMethodForCaseB(@NotNull final Integer[] array, @NotNull final AbstractSort sortMethod) {
         @NotNull final Random generator = new Random(System.nanoTime());
         @NotNull final Integer[] arrayCopy = new Integer[MAX_SIZE];
         System.arraycopy(array, 0, arrayCopy, 0, MAX_SIZE);
 
         for (int i = MAX_SIZE - 1; i > 0; --i) {
             arrayCopy[i] = generator.nextInt(MAX_NUM);
             System.out.print(i);
             System.out.print('\t');
             System.out.println(benchmark(arrayCopy, sortMethod, i));
             System.gc();
         }
     }
 
     /**
      * Benchmark all sorting methods on arrays, which are sorted, except last element
      *
      * @throws FileNotFoundException just in case
      */
     private static void caseB() throws FileNotFoundException {
         @NotNull final Integer[] array = generateSortedArray();
 
         System.err.println("Benchmark caseB");
         for (Sort sortName : Sort.values()) {
             System.err.println("Begin benchmark " + sortName.toString());
             @NotNull AbstractSort sortMethod = SortFactory.getSort(sortName);
 
            System.setOut(new PrintStream("caseB_" + sortMethod.toString()));
             benchmarkArrayAndMethodForCaseB(array, sortMethod);
             System.out.close();
 
             System.err.println("End benchmark " + sortMethod.toString());
         }
         System.err.println("End benchmark caseB");
     }
 
     /**
      * Benchmark all sorting methods on array, that sorted in reverse direction
      *
      * @throws FileNotFoundException just in case
      */
     private static void caseC() throws FileNotFoundException {
         benchmarkCaseACD(generateReverseSortedArray(), 'c');
     }
 
     /**
      * Benchmark all sorting methods on random array
      *
      * @throws FileNotFoundException just in case
      */
     private static void caseD() throws FileNotFoundException {
         benchmarkCaseACD(generateRandomArray(), 'd');
     }
 
     /**
      * It's MAIN
      *
      * @param argv command line arguments
      */
     public static void main(String[] argv) {
         try {
             caseA();
             caseB();
             caseC();
             caseD();
         } catch (FileNotFoundException e) {
             System.out.println("oops");
         } catch (OutOfMemoryError e) {
             System.out.println("JAVA F.CKED UP");
         }
     }
 }
