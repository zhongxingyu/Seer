package orF.stardust.math.field;

import org.stardust.math.field.Field;
import org.stardust.math.field.FieldException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: evadrone
  * Date: 10/20/13
  * Time: 12:54 PM
  */
 public class FieldVerifier<T> {
 
    private Field<T> F;
 
     public FieldVerifier(Field<T> field) {
         this.F = field;
     }
 
     public boolean checkClosure(T a, T b) {
         if (F.contains(a) && F.contains(b)) {
             return F.contains(F.add(a, b))
                     && F.contains(F.multiply(a, b));
         } else {
             throw new FieldException("a and b must be elements of F");
         }
     }
 
     public boolean checkAssociativity(T a, T b, T c) {
         if (F.contains(a) && F.contains(b) && F.contains(c)) {
             return F.add(F.add(a, b), c).equals(F.add(a, F.add(b, c)))
                     && F.multiply(F.multiply(a, b), c).equals(F.multiply(a, F.multiply(b, c)));
         } else {
             throw new FieldException("a, b, and c must be elements of F");
         }
     }
 
     public boolean checkCommutativity(T a, T b) {
         if (F.contains(a) && F.contains(b)) {
             return F.add(a, b).equals(F.add(b, a))
                     && F.multiply(a, b).equals(F.multiply(b, a));
         } else {
             throw new FieldException("a and b must be elements of F");
         }
     }
 
     public boolean checkDistributivity(T a, T b, T c) {
         if (F.contains(a) && F.contains(b) && F.contains(c)) {
             return F.multiply(a, F.add(b, c)).equals(F.add(F.multiply(a, b), F.multiply(a, c)))
                     && F.multiply(F.add(a, b), c).equals(F.add(F.multiply(a, c), F.multiply(b, c)));
         } else {
             throw new FieldException("a, b, and c must be elements of F");
         }
     }
 
     public boolean checkAdditiveIdentity(T a) {
         if (F.contains(a)) {
             T zero = F.getAdditiveIdentity();
             T one = F.getMultiplicativeIdentity();
             return F.contains(zero)
                     && !zero.equals(one)
                     && a.equals(F.add(zero, a))
                     && a.equals(F.add(a, zero));
         } else {
             throw new FieldException("a must be an element of F");
         }
     }
 
     public boolean checkMultiplicativeIdentity(T a) {
         if (F.contains(a)) {
             T zero = F.getAdditiveIdentity();
             T one = F.getMultiplicativeIdentity();
             return F.contains(one)
                     && !zero.equals(one)
                     && a.equals(F.multiply(one, a))
                     && a.equals(F.multiply(a, one));
         } else {
             throw new FieldException("a must be an element of F");
         }
     }
 
     public boolean checkAdditiveInverse(T a) {
         if (F.contains(a)) {
             T zero = F.getAdditiveIdentity();
             T minusA = F.getAdditiveInverse(a);
             return F.contains(minusA)
                     && zero.equals(F.add(a, minusA))
                     && zero.equals(F.add(minusA, a));
         } else {
             throw new FieldException("a must be an element of F");
         }
     }
 
     public boolean checkMultiplicativeInverse(T a) {
         T zero = F.getAdditiveIdentity();
         if (!zero.equals(a) && F.contains(a)) {
             T one = F.getMultiplicativeIdentity();
             T aInv = F.getMultiplicativeInverse(a);
             return F.contains(aInv)
                     && one.equals(F.multiply(a, aInv))
                     && one.equals(F.multiply(aInv, a));
         } else {
             throw new FieldException("a must be a nonzero element of F");
         }
     }
 
 }
