 package de.skuzzle.polly.parsing.util;
 
 /**
  * Predefines several useful fields for usage with a matrix. Please note that the set
  * of Integers does not form a valid field as not every item has a valid multiplicative
  * inverse.
  * 
  * @author Simon
  *
  */
 public class Fields {
 
     
     /**
      * Defines a {@link Field} over the residue class set of integers modulo 5.
      */
     public final static Field<Integer> INT_MOD_5 = integerModulo(5);
     
     
     
     /**
      * Defines a {@link Field} over the residue class set of integers modulo 7. 
      */
     public final static Field<Integer> INT_MOD_7 = integerModulo(7);
     
     
 
     /**
      * Defines a {@link Field} over the set of Doubles.
      */
     public final static Field<Double> DOUBLE = new Field<Double>() {
         
         @Override
         public Double multiply(Double left, Double right) {
             return left * right;
         }
         
         
         
         @Override
         public Double getMultiplicativeNeutral() {
             return 1.0;
         }
         
         
         
         @Override
         public Double getMultiplicativeInverse(Double element) {
             return 1.0 / element;
         }
         
         
         
         @Override
         public Double getAdditiveNeutral() {
             return 0.0;
         }
         
         
         
         @Override
         public Double getAdditiveInverse(Double element) {
             return -element;
         }
         
         
         
         @Override
         public Double add(Double left, Double right) {
             return left + right;
         }
     };
     
     
     
     
     private static Integer[] extendedEuklid(Integer a, Integer b) {
         if (b == 0) {
             return new Integer[] {a, 1, 0};
         }
         Integer[] tmp = extendedEuklid(b, a % b);
         return new Integer[] {tmp[0], tmp[2], (tmp[1] - (a/b) * tmp[2]) };
     }
     
     
     
     /**
      * Creates a {@link Field} over the residue class of integers modulo p. Please note 
      * that in order for the result to be a valid field, p must be a prime number. 
      * Otherwise, there won't exist a valid multiplicative inverse for each element.
      * 
      * @param p A prime number of which the residue classes are calculated.
      * @return A {@link Field} over the residue class modulo p.
      */
     public static Field<Integer> integerModulo(final int p) {
         return new Field<Integer>() {
 
             @Override
             public Integer getAdditiveNeutral() {
                 return 0;
             }
             
             
 
             @Override
             public Integer getAdditiveInverse(Integer element) {
                 return p - element;
             }
             
             
 
             @Override
             public Integer add(Integer left, Integer right) {
                 return Math.abs(left + right) % p;
             }
             
             
 
             @Override
             public Integer getMultiplicativeNeutral() {
                 return 1;
             }
             
             
 
             @Override
             public Integer getMultiplicativeInverse(Integer element) {
                 Integer[] e = extendedEuklid(element, p);
                 Integer k = e[1];
                 if (k < 0) {
                     return p + k;
                 } else {
                     return k;
                 }
             }
 
             
 
             @Override
             public Integer multiply(Integer left, Integer right) {
                int r = (left * right) % p;
                if (r < 0) {
                    r = p + r;
                }
                return r;
             }
             
             
             
         };
     }
     
     
     
     private Fields() {};
     
 }
