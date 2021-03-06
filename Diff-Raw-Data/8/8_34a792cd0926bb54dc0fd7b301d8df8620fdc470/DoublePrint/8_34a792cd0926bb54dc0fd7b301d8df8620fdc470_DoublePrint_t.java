 class DoublePrint {
 
   static final double[] dcases = {
     0.0,
     Double.longBitsToDouble(0x8000000000000000L),
     Double.NaN,
     Double.POSITIVE_INFINITY,
     Double.NEGATIVE_INFINITY,
     Double.MIN_VALUE,
     Double.MAX_VALUE,
     123.75,
     -0.9e7,
     1e-3,
     1.05e9,
   };
 
   static final float[] fcases = {
     0.0f,
     Float.intBitsToFloat(0x80000000),
     Float.NaN,
     Float.POSITIVE_INFINITY,
     Float.NEGATIVE_INFINITY,
     Float.MIN_VALUE,
     Float.MAX_VALUE,
     123.75f,
     1e7f,
     -1e-3f,
   };
 
   public static void main(String[] args) {
     System.out.println("Double values:");
     System.out.println("  NaN -> " +
       Long.toHexString(
       Double.doubleToLongBits(
       Double.longBitsToDouble(0x7ff000abacab0000L))));
     for (int k = 0; k < dcases.length; k++) {
       System.out.println("  " + dcases[k]);
     }
     System.out.println("  0.0/0.0 = " + (dmeth() / dmeth()));
 
     System.out.println();
 
     System.out.println("Float values:");
     System.out.println("  NaN -> " +
       Integer.toHexString(
       Float.floatToIntBits(
       Float.intBitsToFloat(0x7f80cb01))));
     for (int k = 0; k < fcases.length; k++) {
       System.out.println("  " + fcases[k]);
     }
     System.out.println("  0.0/0.0 = " + (dmeth() / dmeth()));
   }
 
   public static double dmeth() {
     return 0.0;
   }
 
   public static float fmeth() {
     return 0.0f;
   }
 }
 
// Note: kaffe prints incorrect {Double,Float}.MIN_VALUE incorrectly

 /* Expected output:
 Double values:
   NaN -> 7ff8000000000000
   0.0
   -0.0
   NaN
   Infinity
   -Infinity
  4.9406564584124654E-324
   1.7976931348623157E308
   123.75
   -9000000.0
   0.001
   1.05E9
   0.0/0.0 = NaN
 
 Float values:
   NaN -> 7fc00000
   0.0
   -0.0
   NaN
   Infinity
   -Infinity
  1.4012985E-45
   3.4028235E38
   123.75
   1.0E7
   -0.001
   0.0/0.0 = NaN
 */
 
 // On Solaris/sparc with Sun JDK:
 /* (Un)Expected output:
 Double values:
   NaN -> 7ff8000000000000
   0.0
   -0.0
   NaN
   Infinity
   -Infinity
   4.9E-324
   1.7976931348623157E308
   123.75
   -9000000.0
   0.0010	               <--- JDK is wrong
   1.05E9
   0.0/0.0 = NaN
 
 Float values:
   NaN -> 7fc00000
   0.0
   -0.0
   NaN
   Infinity
   -Infinity
   1.4E-45
   3.4028235E38
   123.75
   1.0E7
   -0.0010	               <--- JDK is wrong
   0.0/0.0 = NaN
 */
