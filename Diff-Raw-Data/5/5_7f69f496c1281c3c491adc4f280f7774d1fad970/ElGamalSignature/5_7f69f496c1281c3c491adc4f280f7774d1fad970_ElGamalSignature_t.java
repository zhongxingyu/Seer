 /*
  * jCrypt - Programmierumgebung für das Kryptologie-Praktikum
  * Studienarbeit am Institut für Theoretische Informatik der
  * Technischen Universität Braunschweig
  * 
  * Datei:        ElGamalSignature.java
  * Beschreibung: Dummy-Implementierung des ElGamal-Public-Key-Signaturverfahrens
  * Erstellt:     30. März 2010
  * Autor:        Martin Klußmann
  */
 
 package task4;
 
 import java.io.*;
 import java.math.*;
 import java.util.*;
 
 import de.tubs.cs.iti.jcrypt.chiffre.*;
 
 /**
  * Dummy-Klasse für das ElGamal-Public-Key-Signaturverfahren.
  *
  * @author Martin Klußmann
  * @version 1.1 - Sat Apr 03 22:14:47 CEST 2010
  */
 public final class ElGamalSignature extends Signature {
 
     // did I do this public private thing right? :)
     // public key part
     public BigInteger g, q, y;
     // private key part
     private BigInteger x;
 
     public static final BigInteger ZERO = BigInteger.ZERO;
     public static final BigInteger ONE = BigInteger.ONE;
     public static final BigInteger NONE = BigInteger.ONE.negate();
     public static final BigInteger TWO = BigInteger.valueOf(2L);
     public static final BigInteger THREE = BigInteger.valueOf(3L);
 
     /**
      * Erzeugt einen neuen Schlüssel.
      * 
      * @see #readKey readKey
      * @see #writeKey writeKey
      */
     public void makeKey() {
         Random rand = new Random();
 
         // trivial algorithm: get a 512 bit random number, check if it's a prime. rinse and repeat.
         do {
             BigInteger p = new BigInteger(511, rand); // p = random 512 bit number
             q = p.multiply(TWO).add(ONE); // q = 2p+1
         } while(!q.isProbablePrime(42));
 
         // same algorithm to find a generator
         BigInteger qMinusOne = q.subtract(ONE);
         // this is p. don't ask.
         BigInteger qMinusOneDivTwo = qMinusOne.divide(TWO);
         do {
             // choose 2 < g < q, we should have a 50% probability of hitting a generating number here.
             g = BigIntegerUtil.randomBetween(THREE, qMinusOne, rand);
             // check if the required criteria for a generator of G applies
         } while(!g.modPow(qMinusOneDivTwo, q).equals(qMinusOne));
 
         // choose random x
         x = BigIntegerUtil.randomBetween(TWO, q.subtract(TWO));
 
         // also, y.
         y = g.modPow(x, q);
 
     }
 
     /**
      * Liest den Schlüssel mit dem Reader <code>key</code>.
      * 
      * @param key
      * Der Reader, der aus der Schlüsseldatei liest.
      * @see #makeKey makeKey
      * @see #writeKey writeKey
      */
     public void readKey(BufferedReader key) {
         try {
 
             {
                 String in_public = key.readLine();
                 BufferedReader in = new BufferedReader(new FileReader(in_public));
 
                 q = new BigInteger(in.readLine());
                 g = new BigInteger(in.readLine());
                 y = new BigInteger(in.readLine());
 
             }
 
             {
                 String in_private = key.readLine();
                 BufferedReader in = new BufferedReader(new FileReader(in_private));
 
                 x = new BigInteger(in.readLine());
             }
 
             key.close();
         } catch (Exception e) {
             // let's try to exit graceffuuuuOHSHIKILLITWITHFIREOMGOMGOMG
             e.printStackTrace();
             System.exit(1);
         }
     }
 
     /**
      * Schreibt den Schlüssel mit dem Writer <code>key</code>.
      * 
      * @param key
      * Der Writer, der in die Schlüsseldatei schreibt.
      * @see #makeKey makeKey
      * @see #readKey readKey
      */
     public void writeKey(BufferedWriter key) {
         try {
 
             String out_public = "<Ihr Accountname>.secr.public";
             String out_private = "<Ihr Accountname>.secr.private";
 
             {
                 BufferedWriter out = new BufferedWriter(new FileWriter(new File(out_public)));
                 out.write(q.toString()); out.newLine();
                 out.write(g.toString()); out.newLine();
                 out.write(y.toString()); out.newLine();
                 out.close();
             }
 
             {
                 BufferedWriter out = new BufferedWriter(new FileWriter(new File(out_private)));
                 out.write(x.toString()); out.newLine();
                 out.close();
             }
 
             key.write(out_public); key.newLine();
             key.write(out_private); key.newLine();
             key.close();
         } catch(Exception e) {
             // let's try to exit graceffuuuuOHSHIKILLITWITHFIREOMGOMGOMG
             e.printStackTrace();
             System.exit(1);
         }
     }
 
     public BigInteger signBlock(BigInteger M) {
        // Algorithm 7.8
         BigInteger k;
         do {
             k = BigIntegerUtil.randomBetween(TWO, q.subtract(TWO));
         } while(!k.gcd(q.subtract(ONE)).equals(ONE));
         BigInteger r = g.modPow(k, q);
        BigInteger b = M.subtract(x.multiply(r));
         BigInteger s = b.multiply(k.modInverse(q.subtract(ONE))).mod(q.subtract(ONE));
         return r.add(s.multiply(q));
     }
 
     /**
      * Signiert den durch den FileInputStream <code>cleartext</code> gegebenen
      * Klartext und schreibt die Signatur in den FileOutputStream
      * <code>ciphertext</code>.
      * <p>Das blockweise Lesen des Klartextes soll mit der Methode {@link
      * #readClear readClear} durchgeführt werden, das blockweise Schreiben der
      * Signatur mit der Methode {@link #writeCipher writeCipher}.</p>
      * 
      * @param cleartext
      * Der FileInputStream, der den Klartext liefert.
      * @param ciphertext
      * Der FileOutputStream, in den die Signatur geschrieben werden soll.
      */
     public void sign(FileInputStream cleartext, FileOutputStream ciphertext) {
         int bitLen = q.bitLength();
         int blockLen = (bitLen - 1) / 8;
 
         BigInteger clear = readClear(cleartext,blockLen);
         System.out.println(clear);
         while (clear != null ) {
             writeCipher(ciphertext, signBlock(clear));
             clear = readClear(cleartext, blockLen);
         }
         try {
             ciphertext.close();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     public boolean verifyBlock(BigInteger clear, BigInteger cipher) {
         //split cipher back into r and s parts
         BigInteger r = cipher.mod(q);
         BigInteger s = cipher.divide(q);
 
         //decipher message
         BigInteger first = y.modPow(r, q).multiply(r.modPow(s, q)).mod(q);
         BigInteger second = g.modPow(clear, q);
         return first.equals(second);
     }
 
     /**
      * Überprüft die durch den FileInputStream <code>ciphertext</code> gegebene
      * Signatur auf den vom FileInputStream <code>cleartext</code> gelieferten
      * Klartext.
      * <p>Das blockweise Lesen der Signatur soll mit der Methode {@link
      * #readCipher readCipher} durchgeführt werden, das blockweise Lesen des
      * Klartextes mit der Methode {@link #readClear readClear}.</p>
      *
      * @param ciphertext
      * Der FileInputStream, der die zu prüfende Signatur liefert.
      * @param cleartext
      * Der FileInputStream, der den Klartext liefert, auf den die Signatur
      * überprüft werden soll.
      */
     public void verify(FileInputStream ciphertext, FileInputStream cleartext) {
         int bitLen = q.bitLength();
         int blockLen = (bitLen - 1) / 8;
         
         BigInteger cipher = readCipher(ciphertext);
         BigInteger clear = readClear(cleartext, blockLen);
 
         //decipher blockwise
         while (cipher != null ) {
 
             if(!verifyBlock(clear, cipher)) {
                 System.out.println("Wrong signature!");
                 return;
             }
 
             cipher = readCipher(ciphertext);
             clear = readClear(cleartext, blockLen);
         }
 
         System.out.println("Correct signature!");
     }
     
     
     public static void main(String[]args) {
         ElGamalSignature eg = new ElGamalSignature();
         eg.makeKey();
         
         BigInteger clear = new BigInteger("1");
         BigInteger cipher = eg.signBlock(clear);
         assert(eg.verifyBlock(clear, cipher));
     }
 
 }
