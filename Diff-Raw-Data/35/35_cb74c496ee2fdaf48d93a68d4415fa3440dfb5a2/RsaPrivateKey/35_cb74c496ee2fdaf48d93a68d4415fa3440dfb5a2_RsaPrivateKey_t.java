 package com.signr.rsa.core;
 
 import static com.signr.rsa.core.Rsa.DECIMAL_FORMATTER;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.concurrent.Immutable;
 
 /**
  * The private key of the RSA algorithm, containing the public key. The private key D can be
  * retrieved.
  * 
  * This is a thread-safe immutable class.
  * 
  * @author Marcello de Sales (marcello.desales@gmail.com)
  */
 @Immutable
 public final class RsaPrivateKey {
 
   /**
    * Reference to the public key
    */
   private final RsaPublicKey publicKey;
   /**
    * Private Key d
    */
   private final double d;
   /**
    * The private log for the calculator
    */
   private final List<String> log = new ArrayList<>();
 
   /**
    * Constructs a new Rsa private Key with a public key.
    * 
    * @param publicKey is the public key
    */
   private RsaPrivateKey(RsaPublicKey givenPublicKey) {
     publicKey = givenPublicKey;
 
     log.add(Rsa.LOG_ARROW + "Calculating private keys");
     d = this.generateDInverseE();
     log.add("");
     log.add("      Private Key (N,D) = (" + DECIMAL_FORMATTER.format(this.publicKey.getKeyN())
         + ", " + DECIMAL_FORMATTER.format(this.d) + ");");
   }
 
   /**
    * Constructs a new Private Key with a given keyD and the public keys associated with it
    * 
    * @param publicKey
    * @param keyD
    */
   private RsaPrivateKey(RsaPublicKey publicKey, double keyD) {
     this.publicKey = publicKey;
     this.d = keyD;
   }
 
   /**
    * @param publicKey the given public key
    * @return a new instance of the RSA private key with the given key.
    */
   public static RsaPrivateKey newInstance(RsaPublicKey publicKey) {
     return new RsaPrivateKey(publicKey);
   }
 
   /**
    * Factory method for the RSAPrivateKey based on a given key D and the public key.
    * 
    * @param publicKey is the public key object
    * @param keyD is the private keyD
    * @return a new instance of the RSAPrivateKey.
    */
   public static RsaPrivateKey newInstance(RsaPublicKey publicKey, double keyD) {
     return new RsaPrivateKey(publicKey, keyD);
   }
 
   /**
    * @return the reference to the public key
    */
   public RsaPublicKey getRSAPublicKey() {
     return this.publicKey;
   }
 
   /**
    * @return the value of the private key D
    */
   public double getKeyD() {
     return this.d;
   }
 
   /**
    * @return generates the private key D, based on the Inverse value of the public key E
    */
   private double generateDInverseE() {
     double u1 = 1;
     double u2 = 0;
     double u3 = this.publicKey.getFactorFI();
     double v1 = 0;
     double v2 = 1;
     double v3 = this.publicKey.getKeyE();
     double t1, t2, t3, vv, qq;
 
     log.add("         Initializing (p1,p2,p3) = (1, 0 , FI(n))");
     log.add("         Initializing (q1,q2,q3) = (0, 1 ,  E  ))");
     log.add("         While q3 != 0");
     log.add("             quoc = p3 / q3");
     log.add("             (t1,t2,t3) = (p1,p2,p3) - quoc * (q1,q2,q3)");
     log.add("             After, arrange the values:");
     log.add("             (p1,p2,p3) = (q1,q2,q3)");
     log.add("             (q1,q2,q3) = (t1,t2,t3)");
 
     while (v3 > 0) {
       this.log.add("");
       this.log.add("           (" + DECIMAL_FORMATTER.format(v3) + " <> 0) , then:");
       qq = Algebra.SINGLETON.getQuotient(u3, v3);
 
       this.log.add("             quoc = " + DECIMAL_FORMATTER.format(u3) + " / "
           + DECIMAL_FORMATTER.format(v3) + " = " + DECIMAL_FORMATTER.format(qq));
 
       t1 = u1 - qq * v1;
       t2 = u2 - qq * v2;
       t3 = u3 - qq * v3;
 
       u1 = v1;
       u2 = v2;
       u3 = v3;
       v1 = t1;
       v2 = t2;
       v3 = t3;
 
       this.log.add("             (t1,t2,t3) = (" + DECIMAL_FORMATTER.format(u1) + ","
           + DECIMAL_FORMATTER.format(u2) + "," + DECIMAL_FORMATTER.format(u3) + ") - "
           + DECIMAL_FORMATTER.format(qq) + " * (" + DECIMAL_FORMATTER.format(v1) + ","
           + DECIMAL_FORMATTER.format(v2) + "," + DECIMAL_FORMATTER.format(v3) + ") = ("
           + DECIMAL_FORMATTER.format(t1) + "," + DECIMAL_FORMATTER.format(t2) + ","
           + DECIMAL_FORMATTER.format(t3) + ")");
       this.log.add("             (p1,p2,p3) = (" + DECIMAL_FORMATTER.format(v1) + ","
           + DECIMAL_FORMATTER.format(v2) + "," + DECIMAL_FORMATTER.format(v3) + ")");
 
       this.log.add("             (q1,q2,q3) = (" + DECIMAL_FORMATTER.format(t1) + ","
           + DECIMAL_FORMATTER.format(t2) + "," + DECIMAL_FORMATTER.format(t3) + ")");
     }
     this.log.add("");
     this.log
         .add("         q3 is zero(0). Now, verify the value of p2. In case of negative, invert it by summing"
             + " it with FI. (represent the negative number of z(n) by a positive.)");
     this.log.add("");
     this.log.add("         u2 = " + DECIMAL_FORMATTER.format(u2) + ";");
 
     vv = u2;
     double inverse;
     if (vv < 0) {
       inverse = vv + this.publicKey.getFactorFI();
       this.log.add("          Since u2 is negative, we have:");
       this.log.add("          D = u2 + FI; D = " + DECIMAL_FORMATTER.format(u2) + " + "
           + DECIMAL_FORMATTER.format(this.publicKey.getFactorFI()) + " = "
           + DECIMAL_FORMATTER.format(inverse));
     } else {
       inverse = vv;
       this.log.add("         D = u2; D = " + DECIMAL_FORMATTER.format(u2));
     }
     return inverse;
   }
 
   @Override
   public boolean equals(Object obj) {
     if (!(obj instanceof RsaPrivateKey)) {
       return false;
     } else {
      RsaPrivateKey other = (RsaPrivateKey) obj;
      return other.d == this.d && other.publicKey.equals(this.publicKey);
     }
   }
 
   @Override
   public int hashCode() {
     return 31 + 31 * new Double(this.d).hashCode();
   }
 
   /**
    * @return the log entries for the creation of the RSA key.
    */
   public String[] getLogEntries() {
     List<String> logEntries = new ArrayList<>(this.log.size());
     for (String logEntry : this.log) {
       logEntries.add(logEntry);
     }
     return logEntries.toArray(new String[logEntries.size()]);
   }
 }
