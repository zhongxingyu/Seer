 package cryptocast.crypto;
 
 import java.io.Serializable;
 import java.math.BigInteger;
 import java.util.Random;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Optional;
 
 import cryptocast.util.NativeUtils;
 
 // for the sqrt algorithm
 import org.bouncycastle.math.ec.ECFieldElement.Fp;
 
 /**
  * The field $\mathbb{Z}/p\mathbb{Z}$ of integers modulo a prime $p > 2$.
  */
 public class IntegersModuloPrime extends Field<BigInteger> 
                                  implements Serializable {
     private static final long serialVersionUID = -2607220779180962188L;
     private static final Logger log = LoggerFactory
             .getLogger(IntegersModuloPrime.class);
 
     private BigInteger p;
     private int n; // p.bitLength()

     // precomputed values used for Barrett reduction
     private BigInteger mu;
     private int alpha, beta;

     private static boolean haveNative = 
             NativeUtils.tryToLoadNativeLibOrLogFailure("IntegersModuloPrime", log);
 
     /**
      * Reduces an integer $a < p^2$ modulo $p$.
      * @param a ($a < p^2$)
      * @return $a \mod p$
      */
     public BigInteger reduce(BigInteger a) {
         // perform a barrett reduction
         BigInteger qq = a.shiftRight(n + beta).multiply(mu).shiftRight(alpha - beta);
         BigInteger z = a.subtract(qq.multiply(p));
         if (z.compareTo(p) >= 0) {
             return z.subtract(p);
         }
         return z;
     }
 
     /**
      * Initializes the field.
      * @param p A prime number (must not be $2$)
      */
     public IntegersModuloPrime(BigInteger p) {
         assert p.testBit(0);
         this.p = p;
         n = p.bitLength();
         alpha = n + 1;
         beta = -2;
         mu = BigInteger.ONE.shiftLeft(n + alpha).divide(p);
     }
 
     /** @return $p$ */
     public BigInteger getP() {
         return p;
     }
 
     @Override
     public BigInteger add(BigInteger a, BigInteger b) {
         return reduce(a.add(b));
     }
 
     @Override
     public BigInteger multiply(BigInteger a, BigInteger b) {
         return reduce(a.multiply(b));
     }
 
     @Override
     public BigInteger negate(BigInteger a) {
         if (a.signum() == 0) { return a; }
         return p.subtract(a);
     }
 
     @Override
     public BigInteger invert(BigInteger a) throws ArithmeticException {
         return a.modInverse(p);
     }
 
     @Override
     public BigInteger zero() {
         return BigInteger.ZERO;
     }
 
     @Override
     public BigInteger one() {
         return BigInteger.ONE;
     }
     
     @Override
     public BigInteger two() {
         return reduce(BigInteger.valueOf(2));
     }
     
     @Override
     public BigInteger three() {
         return reduce(BigInteger.valueOf(3));
     }
     
     @Override
     public BigInteger four() {
         return reduce(BigInteger.valueOf(4));
     }
 
     @Override
     public BigInteger subtract(BigInteger a, BigInteger b) {
        BigInteger x = a.subtract(b);
        if (x.signum() < 0) x = x.add(p);
        assert x.signum() >= 0;
        return reduce(x);
     }
 
     @Override
     public BigInteger pow(BigInteger a, BigInteger e) {
         if (haveNative) {
             return new BigInteger(nativeModPow(a.toByteArray(), e.toByteArray(), getP().toByteArray()));
         } else {
             return a.modPow(e, p);
         }
     }
 
     private static native byte[] nativeModPow(byte[] x, byte[] e, byte[] m);
     
     @Override
     public BigInteger randomElement(Random rnd) {
         BigInteger r;
         // TODO improve performance. Check out 
         // http://stackoverflow.com/questions/2290057/how-to-generate-a-random-biginteger-value-in-java
         do {
             r = new BigInteger(p.bitLength(), rnd);
         } while (r.compareTo(p) >= 0);
         return r;
     }
     
     @Override
     public boolean isZero(BigInteger a) {
         return a.signum() == 0;
     }
     
     @Override
     public boolean equals(Object other) {
         if (other == null || other.getClass() != getClass()) { return false; }
         return p.equals(((IntegersModuloPrime)other).p);
     }
 
     @Override
     public Optional<BigInteger> sqrt(BigInteger a) {
         Fp res = (Fp) new Fp(getP(), a).sqrt();
         return res == null ? Optional.<BigInteger>absent() : Optional.of(res.toBigInteger());
     }
 }
