 // hwRSA.java
 // Houses all required RSA *and* all required 0-Knowledge code.
 import java.math.*;
 import java.util.*;
 
 public class hwRSA
 {
     private static final int numberOfBits = 32768; // 2^15
     private static final BigInteger ONE = new BigInteger("1");
     
     
     public Random savedRandom = null;
     public BigInteger p;
     public BigInteger q;
     public BigInteger n; // n = p * q
     
     // RSA only?
     public BigInteger phiN;
     public BigInteger e; // for encryption; compute based on above.
     public BigInteger d; // for decryption; compute based on e.
     
     // 0 Knowledge only?
     public BigInteger S;
    public BigInteger V;
     
     public hwRSA(Random random)
     {
         savedRandom = random;
     
         p = getPrime(random); // p and q are arbitrary large primes
         q = getPrime(random);
         n = p.multiply(q);
         phiN = (p.subtract(ONE)).multiply(q.subtract(ONE));
         
         S = getPrime(random); // s is an arbitrary secret; we'll use a prime because they're BA!!!
        V = (S.multiply(S)).mod(n);
     }
     
     public void initialize(Random random)
     {
         // initialize all the variables
     }
 
     public void saveRSA()
     {
         // save out the RSA information
     }
     
     public void load()
     {
         // load all the variables from a saved source
     }
     
     private BigInteger getPrime(Random random)
     {
         return BigInteger.probablePrime(numberOfBits, random);
     }
 }
