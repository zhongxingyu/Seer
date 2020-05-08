 package com.yahoo.dtf.util;
 
 import java.util.Random;
 
 /**
  * @dtf.feature Generating Random Data
  * 
  * @dtf.feature.group Random Data
  * 
  * @dtf.feature.desc
  * <p>
  * This is the random generator used by DTF and it can be easily reproduced in 
  * other languages so that the random generation of bytes can be similar and 
  * data validation can be easily done without having to worry about the data
  * generated in another language not matching the same pseudo random method that
  * is within DTF.
  * </p>
  * <p>
  * <b>NOTE: This implementation is NOT thread-safe. So make a new instance
  *          of DTFRandom for each of your threads.</b>
  * </p>
  * <p> 
  * The following is based on information taken from 
  * <a href="http://www.javamex.com/tutorials/random_numbers/xorshift.shtml">here</a>
  * and re-implemented within DTF. Based on a solution that was found in 2003 by 
  * George Marsaglia in the form of XORShift Random Number Generators. Marsaglia 
  * showed that a generator involving three shifts and three XOR operations 
  * generates a "full" period and that the resulting values satisfy various 
  * statistical tests of randomness that even the LCGs fail (LCG algorithm is 
  * currently used by Java).
  * </p> 
  * <p>
  * The "magic" values of 21, 35 and 4 have been found to produce good results. 
  * With these values, the generator has a full period of 2^64-1, and the 
  * resulting values pass Marsaglia's "Diehard battery" of statistical tests for 
  * randomness.
  * </p>
  * <p>
  * Here is the way that random data is currently generated within DTF:
  * </p>
  * </br> 
  * <b>Java</b>
  * <p>
  * Sample code of how we generate the pseudo random data within DTF, this file
  * can be found at <b>src/java/com/dtf/yahoo/util/DTFRandom.java</b>.
  * </p>
  * <pre>
  * public void nextBytes(byte[] bytes) {
  *     for (int i = 0, len = bytes.length; i < len; ) {
  *         for (int rnd = nextInt(),
  *              n = Math.min(len - i, Integer.SIZE/Byte.SIZE);
  *              n-- > 0; rnd >>= Byte.SIZE) {
  *             // not allowing $ because then a property could be accidentally
  *             // created, and this would lead to unnecessary issues.
  *             byte b = (byte)rnd;
  *             if ( b == '$' ) continue;
  *             bytes[i++] = b;
  *         }
  *     }
  * }
  * 
  * public int nextInt() {
  *     return next(Integer.SIZE);
  * }
  * 
  * public boolean nextBoolean() {
  *     return next(1) != 0;
  * }
  *   
  * protected int next(int nbits) {
  *     long x = this.seed;
  *     x ^= (x << 21);
  *     // unsigned shift so we don't drag the signal
  *     x ^= (x >>> 35);
  *     x ^= (x << 4);
  *     x &= ((1L << nbits) - 1);
  *     return (int) x;
  * }
  * </pre>
  * </br> 
  * <p>
  * The following is an example of how to mimic the same random byte generator 
  * that is currently implemented in DTF in C. This same method can be used in 
  * C++. The source file containing this sample can be found in DTF at
  * <b>src/native/random.c</b> and should have a few comments on how to build it 
  * and run the sample test that exists.
  * </p>
  * <b>C/C++</b>
  * <pre> 
  * const int INTEGER_SIZE = 32;
  * const int BYTE_SIZE = 4;
  * 
  * #define LONG64 long long
  * 
  * static long rseed = 1234567890L;
  * long min(long a, long b) {
  *     return (a < b ? a : b);
  * }
  * 
  * void nextBytes(char* bytes, int length) {
  *    int i,rnd,len,n;
  *
  *    for (i = 0, len = length; i < len; ) {
  *         for (rnd = nextInt(),
  *              n = min(len - i, INTEGER_SIZE/BYTE_SIZE);
  *              n-- > 0; rnd >>= BYTE_SIZE) {
  *            // not allowing $ because then a property could be accidentally
  *            // created, and this would lead to unnecessary issues.
  *            char b = (char)rnd;
  *            if ( b == '$' ) continue;
  *            bytes[i++] = b;
  *         }
  *     }
  * }
  * 
  * int nextInt() {
  *     return next(INTEGER_SIZE);
  * }
  * 
  * unsigned int nextBoolean() {
  *     return next(1) != 0;
  * }
  *  
  * int next(int nbits) {
  *     LONG64 x = rseed++;
  *     x ^= (x << 21);
  *     x ^= (x >> 35);
  *     x ^= (x << 4);
  *     return x;
  * }
  *  
  * void random(long seed) {
  *     rseed = seed;
  * }
  * </pre>
  */
 public class DTFRandom extends Random {
 
     private long seed = 0;
 
     public DTFRandom() {
         this.seed = System.nanoTime();
     }
 
     public DTFRandom(long seed) {
         this.seed = seed;
     }
     
     /**
      * This function is implemented here so that other developers wanting to 
      * simulate this same behavior can easily copy the follow function and mimic
      * the behavior of DTF. The nice thing about this implementation is that it
      * takes advantage of every Integer generated to break it into n number of
      * characters and in that manner use the nextInt() function very 
      * efficiently. 
      * 
      * The only special thing being done in this function is that we're skipping
      * to generate the sequence ${ because this could result in a property 
      * being generated that DTF would later try to resolve.
      */
     public void nextBytes(byte[] bytes) {
        for (int i = 0, prev = 0, len = bytes.length; i < len; ) {
             for (int rnd = nextInt(),
                  n = Math.min(len - i, Integer.SIZE/Byte.SIZE);
                  n-- > 0; rnd >>= Byte.SIZE) {
                 byte b = (byte)rnd;
                if ( b == '{' && prev == '$' ) continue;
                 bytes[i++] = b;
                prev = b;
             }
         }
     }
   
     /**
      * The following methods are useful as an example of how to use this 
      * generator to create other types. We'll add more methods as we find that
      * necessary.
      */
     public int nextInt() {
         return next(Integer.SIZE);
     }
     
     public boolean nextBoolean() {
         return next(1) != 0;
     }
     
     protected int next(int nbits) {
         long x = seed++;
         x ^= (x << 21);
         // unsigned shift so we don't drag the signal
         x ^= (x >>> 35);
         x ^= (x << 4);
         x &= ((1L << nbits) - 1);
         return (int) x;
     }
 }
