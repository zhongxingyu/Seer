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
 
         // create ElGamal cipher
         BigInteger p;
         ElGamalCipher elGamalC_a;
         ElGamalSignature elGamalS_a;
         {
             // note: q is p in elgamal (too lazy to refactor that, "never touch a running system")
             // we just use a local p here, since we use it rather often anyways
             p = new BigInteger("4988735951183711405443349413015910122453507015594895638933838601555750189585703700647655985269637551634513770201277370413860951650702374379627998821919409");
             BigInteger g = new BigInteger("4403105895869798297264918950735787070665047406714785361037216842427722734684061748868589917485012596281820467352001338223691996653533143166890875549812531");
             BigInteger y = new BigInteger("3670294064109445804998782973709772470002041046377612489028768098078250713079795031354099562309432613560558383306865142781216201315104971340333690591679721");
             BigInteger x = new BigInteger("4589946301809196862611751989088793376762175950291076147544077975213763218505486754450017554342955014202444667772016113058406939298289857995054770609176615");
             elGamalC_a = new ElGamalCipher(p, g, y, x);
             elGamalS_a = new ElGamalSignature(p, g, y, x);
         }
 
         //send public key: p, g, y
         com.sendTo(1, p.toString(16));
         com.sendTo(1, elGamalC_a.g.toString(16));
         com.sendTo(1, elGamalC_a.y.toString(16));
 
         
         //send random m0, m1 between 0 and p. m0 != m1
         BigInteger m0, m1; {
             m0 = BigIntegerUtil.randomBetween(ZERO, p);
             do {
                 m1 = BigIntegerUtil.randomBetween(ZERO, p);
             } while(m0.equals(m1));
         }
 
         // send both
         com.sendTo(1, m0.toString(16));
         com.sendTo(1, m1.toString(16));
 
         // receive q
         BigInteger q = new BigInteger(com.receive(), 16);
         
         BigInteger s0, s1;
         BigInteger M0_dash, M1_dash;
         int s; {
 
             BigInteger k0, k1; {
 
                 // k0 = decipher((q-m0) mod p)
                 k0 = elGamalC_a.decipherBlock(q.subtract(m0).mod(p.pow(2)));
                 //k0 = (q.subtract(m0).mod(p)); //without elgamal
                 // k1 = decipher((q-m1) mod p)
                 k1 = elGamalC_a.decipherBlock(q.subtract(m1).mod(p.pow(2)));
                 //k1 = (q.subtract(m1).mod(p)); //without elgamal
             }
 
             System.out.println("k0'" + k0.toString(16));
             System.out.println("k1'" + k1.toString(16));
 
             //S0 = sign(k0)
             s0 = elGamalS_a.signBlock(k0);
             //S1 = sign(k1)
             s1 = elGamalS_a.signBlock(k1);
 
             System.out.println("s0'" + s0.toString(16));
             System.out.println("s1'" + s1.toString(16));
 
 
             // select random s in {0,1}
             s = new Random().nextBoolean() ? 1 : 0;
 
             // read messages to be send M0, M1 (between 0 and p)
             // Hamster. A dentist. Hardcode. Steven Seagal~
             BigInteger M0 = new BigInteger("1111111111111111111111111111111111111");
             BigInteger M1 = new BigInteger("2222222222222222222222222222222222222");
 
             // send M_strich_0 := (M0 + k_{s xor 0}) mod p
             M0_dash = M0.add(s == 1 ? k1 : k0).mod(p);
             // send M_strich_1 := (M1 + k_{s xor 1}) mod p
             M1_dash = M1.add(s == 0 ? k1 : k0).mod(p);
 
             System.out.println("M0'" + M0_dash.toString(16));
             System.out.println("M1'" + M1_dash.toString(16));
 
         }
 
         com.sendTo(1, M0_dash.toString(16));
         com.sendTo(1, M1_dash.toString(16));
 
         // send s0, s1
         com.sendTo(1, s0.toString(16));
         com.sendTo(1, s1.toString(16));
 
         // send s
         com.sendTo(1, Integer.toString(s));
 
     }
 
     /** This is Bob. */
     public void receiveFirst () {
         //receive p,g,y
         BigInteger p = new BigInteger(com.receive(),16);
         BigInteger g = new BigInteger(com.receive(),16);
         BigInteger y = new BigInteger(com.receive(),16);
 
         //receive m0,m1
         BigInteger[] m= new BigInteger[2];
         m[0] = new BigInteger(com.receive(), 16);
         m[1] = new BigInteger(com.receive(), 16);
 
         //select random b in {0,1}
         BigInteger b = new BigInteger(1, new Random());
         System.out.println("b=" + b.toString(16));
         
         //select random k between 0 and p
         BigInteger k;
         do {
             k = new BigInteger(p.bitLength(), new Random());
         } while (k.compareTo(p) >= 0);
         System.out.println("k=" + k.toString(16));
 
         
         //send q:= (crypt(k) + m_b) mod p^2
         ElGamalCipher elgamal = new ElGamalCipher(p,g,y);
         BigInteger q = elgamal.encipherBlock(k).add(m[b.intValue()]).mod(p.pow(2));
         //BigInteger q = k.add(m[b.intValue()]).mod(p); // without elgamal for debugging
 
         System.out.println("q=" + q.toString(16));
         com.sendTo(0, q.toString(16));
         
         //receive M_strich_0, M_strich_1
         BigInteger[] M_strich= new BigInteger[2];
         M_strich[0] = new BigInteger(com.receive(), 16);
         M_strich[1] = new BigInteger(com.receive(), 16);
         System.out.println("M0'=" + M_strich[0].toString(16));
         System.out.println("M1'=" + M_strich[1].toString(16));
 
         
         //receive S0,S1
         BigInteger[] S= new BigInteger[2];
         S[0] = new BigInteger(com.receive(), 16);
         S[1] = new BigInteger(com.receive(), 16);
         System.out.println("S0=" + S[0].toString(16));
         System.out.println("S1=" + S[1].toString(16));
 
         
         //receive s
         BigInteger s = new BigInteger(com.receive(), 16);
         System.out.println("s'=" + s.toString(16));
         
         //compute M_{s ^ b} := M_strich_{s ^ b} - k
         BigInteger M_sb = M_strich[s.xor(b).intValue()].mod(p).subtract(k).mod(p);
         
         //compute k_quer := M_strich_{s ^ b ^ 1} - M_{s ^ b}
         BigInteger k_quer = M_strich[s.xor(b).xor(ONE).intValue()].subtract(M_sb).mod(p);
         BigInteger k_quer2 = M_strich[s.xor(b).intValue()].subtract(M_sb).mod(p);
 
         System.out.println("k_quer=" + k_quer.toString(16));
         System.out.println("k_quer2=" + k_quer2.toString(16));
         
         //check S_{b ^ 1} != k_quer (otherwise: betrayed!)
        ElGamalSignature sign = new ElGamalSignature(p,q,y);
        
         if (sign.verifyBlock(k_quer, S[b.xor(ONE).intValue()])) {
             System.out.println("You have been betrayed!");
         }
         else {
             System.out.println("Congratulations! With a probability of 1/2 you were not betrayed!");
             System.out.println("The received secret is: " + M_sb);
             
             if (sign.verifyBlock(k_quer2, S[b.intValue()])) {
                 System.out.println("The signature was correct!");
             }
             else {
                 System.out.println("But the signature is wrong.");
             }
         }
     }
     
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
