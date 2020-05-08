 /*
  * @(#)Skeleton.java
  */
 
 import de.tubs.cs.iti.jcrypt.protokoll.*;
 import de.tubs.cs.iti.jcrypt.chiffre.*;
 
 import java.math.*;
 import java.util.*;
 import java.io.*;
 import java.security.MessageDigest;
 
 /**
  *
  */
 
 public final class OT implements Protocol
 {
     /**
      *
      */
 
     static private int MinPlayer        = 2; // Minimal number of players
     static private int MaxPlayer        = 2; // Maximal number of players
     static private String NameOfTheGame = "ABBA";
     private Communicator com;
 
     public static final BigInteger ZERO = BigInteger.ZERO;
     public static final BigInteger ONE = BigInteger.ONE;
     public static final BigInteger NONE = BigInteger.ONE.negate();
     public static final BigInteger TWO = BigInteger.valueOf(2L);
     public static final BigInteger THREE = BigInteger.valueOf(3L);
 
     public void setCommunicator(Communicator com)
     {
         this.com = com;
     }
 
     /** This ia Alice. */
     public void sendFirst () {
         //send public key: p, g, y
         //send random m1,m2 between 0 and p. m1 != m2
         
         //receive q
         
         //k0 = decipher((q-m0) mod p)
         //k1 = decipher((q-m0) mod p)
         //S0 = sign(k0)
         //S1 = sign(k1)
         //select random s in {0,1}
         
        //send M_strich_0 := (M0 + k_{s xor 0}) mod p
        //send M_strich_1 := (M1 + k_{s xor 1}) mod p
         //send S0, S1
         //send s
     }
 
     /** This is Bob. */
     public void receiveFirst () {
         //receive p,g,y
         //receive m0,m1
         
         //select random b in {0,1}
         //select random k between 0 and p
         
         //send q:= (crypt(k) + m_b) mod p^2
         
         //receive M_strich_0, M_strich_1, S0,S1,s
         
         //compute M_{s ^ b} := M_strich_{s ^ b} - k
         //compute k_quer := M_strich_{s ^ b ^ 1} - M_{s ^ b}
         
         //check S_{b ^ 1} != k_quer (otherwise: betrayed!)
     }
     
     
 //    public static BigInteger crypt(BigInteger key, BigInteger msg) {
 //
 //    }
 //
 //    public static BigInteger decrypt(BigInteger key, BigInteger msg) {
 //
 //    }
 
     
     
     
     public String nameOfTheGame () {
         return NameOfTheGame;
     }
 
     public int minPlayer ()
     {
         return MinPlayer;
     }
 
     public int maxPlayer ()
     {
         return MaxPlayer;
     }
 }
