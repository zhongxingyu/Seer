 import com.krypto.elGamal.ElGamal;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 
 import de.tubs.cs.iti.jcrypt.chiffre.BigIntegerUtil;
 import de.tubs.cs.iti.krypto.protokoll.*;
 
 public final class Vertrag implements Protocol {
 
   static private int MinPlayer = 2; // Minimal number of players
   static private int MaxPlayer = 2; // Maximal number of players
   static private String NameOfTheGame = "Geheimnisaustausch";
   private Communicator Com;
 
   private BigInteger ZERO = BigIntegerUtil.ZERO;
   private BigInteger ONE = BigIntegerUtil.ONE;
   private BigInteger TWO = BigIntegerUtil.TWO;
 
   private boolean betray = false;
 
   public void setCommunicator(Communicator com) {
     Com = com;
   }
 
   /**
    * Aktionen der beginnenden Partei. Bei den 2-Parteien-Protokollen seien dies die Aktionen von
    * Alice.
    */
   public void sendFirst() {
     System.out.println("-- Alice --");
     if (betray) {
       System.out.println("ACHTUNG: Betrugsmodus aktiv!!!");
     }
 
     // ElGamal Austausch ---------------------------------------------------------------
 
     // Hard coded ElGamal
     BigInteger p_A = new BigInteger("7789788965135663714690749102453072297748091458564354001035945418057913886819451721947477667556269500246451521462308030406227237346483679855991947569361139");
     BigInteger g_A = new BigInteger("6064211169633122201619014531987050083527855665630754543345421103270545526304595525644519493777291154802011605984321393354028831270292432551124003674426238");
     BigInteger y_A = new BigInteger("3437627792030969437324738830672923365331058766427964788898937390314623633227168012908665090706697391878208866573481456022491841700034626290242749535475902");
     // private:
     BigInteger x_A = new BigInteger("3396148360179732969395840357777168909721385739804535508222449486018759668590512304433229713789117927644143586092277750293910884717312503836910153525557232");
     // Objekt initialisieren mit priv key
     ElGamal elGamal_A = new ElGamal(p_A, g_A, y_A, x_A);
 
     // Alice sendet ElGamal public key an Bob
     Com.sendTo(1, elGamal_A.p.toString(16)); // S1
     Com.sendTo(1, elGamal_A.g.toString(16)); // S2
     Com.sendTo(1, elGamal_A.y.toString(16)); // S3
 
     // Alice empfängt Bobs ElGamal pub key
     BigInteger p_B = new BigInteger(Com.receive(), 16); // R1
     BigInteger g_B = new BigInteger(Com.receive(), 16); // R2
     BigInteger y_B = new BigInteger(Com.receive(), 16); // R3
     // ElGamal Objekt ohne priv key bauen
     ElGamal elGamal_B = new ElGamal(p_B, g_B, y_B);
 
     // Vertrag einlesen ---------------------------------------------------------------
 
   }
 
   /**
    * Aktionen der uebrigen Parteien. Bei den 2-Parteien-Protokollen seien dies die Aktionen von Bob.
    */
   public void receiveFirst() {
     System.out.println("-- Bob --");
     if (betray) {
       System.out.println("ACHTUNG: Betrugsmodus aktiv!!!");
     }
 
     // ElGamal Austausch ---------------------------------------------------------------
 
     // Hard coded ElGamal
     BigInteger p_B = new BigInteger("7789788965135663714690749102453072297748091458564354001035945418057913886819451721947477667556269500246451521462308030406227237346483679855991947569361139");
     BigInteger g_B = new BigInteger("6064211169633122201619014531987050083527855665630754543345421103270545526304595525644519493777291154802011605984321393354028831270292432551124003674426238");
     BigInteger y_B = new BigInteger("3437627792030969437324738830672923365331058766427964788898937390314623633227168012908665090706697391878208866573481456022491841700034626290242749535475902");
     // private:
     BigInteger x_B = new BigInteger("3396148360179732969395840357777168909721385739804535508222449486018759668590512304433229713789117927644143586092277750293910884717312503836910153525557232");
     // Objekt initialisieren mit priv key
     ElGamal elGamal_B = new ElGamal(p_B, g_B, y_B, x_B);
 
     // Bob empfängt Alice ElGamal pub key
     BigInteger p_A = new BigInteger(Com.receive(), 16); // R1
     BigInteger g_A = new BigInteger(Com.receive(), 16); // R2
     BigInteger y_A = new BigInteger(Com.receive(), 16); // R3
     // ElGamal Objekt ohne priv key bauen
     ElGamal elGamal_A = new ElGamal(p_A, g_A, y_A);
 
     // Bob sendet ElGamal public key an Alice
     Com.sendTo(0, elGamal_B.p.toString(16)); // S1
     Com.sendTo(0, elGamal_B.g.toString(16)); // S2
     Com.sendTo(0, elGamal_B.y.toString(16)); // S3
 
     // Vertrag einlesen ---------------------------------------------------------------
 
   }
 
   public void obliviousSend(int sendTo, BigInteger M_0, BigInteger M_1) {
     System.out.println("Oblivious Transfer");
 
     // Hard coded messages M_0 and M_1
     BigInteger[] M = new BigInteger[2];
     M[0] = M_0;
     M[1] = M_1;
 
     if (betray) {
       M[1] = M[0];
     }
 
     // Hard coded ElGamal
     BigInteger p_A = new BigInteger("7789788965135663714690749102453072297748091458564354001035945418057913886819451721947477667556269500246451521462308030406227237346483679855991947569361139");
     BigInteger g_A = new BigInteger("6064211169633122201619014531987050083527855665630754543345421103270545526304595525644519493777291154802011605984321393354028831270292432551124003674426238");
     BigInteger y_A = new BigInteger("3437627792030969437324738830672923365331058766427964788898937390314623633227168012908665090706697391878208866573481456022491841700034626290242749535475902");
     // private:
     BigInteger x_A = new BigInteger("3396148360179732969395840357777168909721385739804535508222449486018759668590512304433229713789117927644143586092277750293910884717312503836910153525557232");
     // Objekt initialisieren mit priv key
     ElGamal elGamal_A = new ElGamal(p_A, g_A, y_A, x_A);
 
     BigInteger p = elGamal_A.p;
 
     // Alice sendet ElGamal public key an Bob
     Com.sendTo(sendTo, elGamal_A.p.toString(16)); // S1
     Com.sendTo(sendTo, elGamal_A.g.toString(16)); // S2
     Com.sendTo(sendTo, elGamal_A.y.toString(16)); // S3
 
     // Alice wählt zufällig zwei Nachrichten m_0, m_1 in Z_p, 1 <= m < p
     BigInteger[] m = new BigInteger[2];
     m[0] = BigIntegerUtil.randomBetween(ONE, p);
     m[1] = BigIntegerUtil.randomBetween(ONE, p);
     // System.out.println("m_0: " + m[0]);
     // System.out.println("m_1: " + m[1]);
 
     // Alice sendet m_0, m_1 an Bob
     Com.sendTo(sendTo, m[0].toString(16)); // S4
     Com.sendTo(sendTo, m[1].toString(16)); // S5
 
     // Alice empfängt q
     BigInteger q = new BigInteger(Com.receive(), 16); // R6
 
     // Alice berechnet k_0', k_1', hier k_A[0] und k_A[1] genannt
     BigInteger[] k_strich = new BigInteger[2];
     for (int i = 0; i < 2; i++) {
       k_strich[i] = elGamal_A.decipher((q.subtract(m[i])).mod(p.multiply(p))); // D_A((q-m_i) mod
                                                                                // p^2)
     }
     // System.out.println("k_strich[0]: " + k_strich[0]);
     // System.out.println("k_strich[1]: " + k_strich[1]);
 
     // zufällig s wählen
     int s = BigIntegerUtil.randomBetween(ZERO, TWO).intValue();
     // System.out.println("s: " + s);
 
     BigInteger[] send = new BigInteger[2];
     send[0] = M[0].add(k_strich[s]).mod(p);
     send[1] = M[1].add(k_strich[s ^ 1]).mod(p);
 
     // System.out.println("send_0: " + send[0]);
     // System.out.println("send_1: " + send[1]);
 
     int r = -1;
     if (betray) { // try to find right r :D
       r = BigIntegerUtil.randomBetween(ZERO, TWO).intValue();
       System.out.println("guessed r: " + r);
     }
 
     // Signatur berechnen
     BigInteger[] S = new BigInteger[2];
     for (int i = 0; i < 2; i++) {
       if (betray) {
         if (i == r) { // gefälschte signatur
           S[i] = BigIntegerUtil.randomBetween(BigIntegerUtil.TWO, p.multiply(p));
         } else {
           S[i] = elGamal_A.sign(k_strich[i]);
         }
       } else { // no betraying
         S[i] = elGamal_A.sign(k_strich[i]);
       }
     }
     // System.out.println("S_0: " + S[0]);
     // System.out.println("S_1: " + S[1]);
 
     // Alice sendet send_0, send_1, s, S[0], S[1]
     Com.sendTo(sendTo, send[0].toString(16)); // S7
     Com.sendTo(sendTo, send[1].toString(16)); // S8
     Com.sendTo(sendTo, s + ""); // S9
     Com.sendTo(sendTo, S[0].toString(16)); // S10
     Com.sendTo(sendTo, S[1].toString(16)); // S11
   }
 
   public BigInteger obliviousReceive(int sendTo) {
     System.out.println("Oblivious Transfer");
 
     // Bob empfängt Alice ElGamal pub key
     BigInteger p_A = new BigInteger(Com.receive(), 16); // R1
     BigInteger g_A = new BigInteger(Com.receive(), 16); // R2
     BigInteger y_A = new BigInteger(Com.receive(), 16); // R3
     // ElGamal Objekt ohne priv key bauen
     ElGamal elGamal_A = new ElGamal(p_A, g_A, y_A);
 
     BigInteger p = elGamal_A.p;
 
     // Bob empfängt m_0 und m_1
     BigInteger[] m = new BigInteger[2];
     m[0] = new BigInteger(Com.receive(), 16); // R4
     m[1] = new BigInteger(Com.receive(), 16); // R5
 
     // Bob wählt zufällig ein r in {0,1} und k in Z_p
     int r = BigIntegerUtil.randomBetween(ZERO, TWO).intValue();
     // System.out.println("r: " + r);
     BigInteger k = BigIntegerUtil.randomBetween(ONE, p);
     // System.out.println("k: " + k);
 
     // Bob berechnet q
     BigInteger q = elGamal_A.encipher(k).add(m[r]); // E_A(k) + m_r
     q = q.mod(p.multiply(p)); // mod p^2
     // System.out.println("q: " + q);
     // Bob sendet q
     Com.sendTo(sendTo, q.toString(16)); // S6
 
     // Bob empfängt send_0, send_1, s, S[0], S[1]
     BigInteger[] send = new BigInteger[2];
     send[0] = new BigInteger(Com.receive(), 16); // R7
     send[1] = new BigInteger(Com.receive(), 16); // R8
     int s = Integer.valueOf(Com.receive()); // R9
     BigInteger[] S = new BigInteger[2];
     S[0] = new BigInteger(Com.receive(), 16); // R10
     S[1] = new BigInteger(Com.receive(), 16); // R11
     // System.out.println("S_0: " + S[0]);
     // System.out.println("S_1: " + S[1]);
 
     // System.out.println("s: " + s);
     // System.out.println("r: " + r);
 
     BigInteger M = send[s ^ r].subtract(k).mod(p); // M = M_{s xor r}
 
     BigInteger k_quer = send[s ^ r ^ 1].subtract(M).mod(p);
 
     BigInteger k_quer2 = send[s ^ r].subtract(M).mod(p);
 
     // System.out.println("S[r^1]: " + S[r ^ 1]);
     // System.out.println("k_dach: " + k_quer);
 
     if (elGamal_A.verify(k_quer, S[r ^ 1])) {
       System.out.println("Betrug!!!!!!!!");
       System.exit(0);
 
       return null;
     } else {
       if (elGamal_A.verify(k_quer2, S[r])) {
         System.out.println("Alles OK!");
         System.out.println("Message choosen: M_" + (s ^ r) + ": " + M);
 
         return M;
       } else {
         System.out.println("Betrug!!!!!!!!");
         System.exit(0);
 
         return null;
       }
     }
   }
 
   public String nameOfTheGame() {
     return NameOfTheGame;
   }
 
   public int minPlayer() {
     return MinPlayer;
   }
 
   public int maxPlayer() {
     return MaxPlayer;
   }
   
   /**
    * Tafel: 1.)1.3
    */
   private BigInteger computeSHA(String text){
     MessageDigest sha = null;
     byte[] digest;
 
     try {
       sha = MessageDigest.getInstance("SHA");
     } catch (Exception e) {
       System.out.println("Could not create message digest! Exception " + e.toString());
       System.exit(0);
     }
     
     sha.update(text.getBytes());
     digest = sha.digest();
     
     return new BigInteger(digest); //TODO ka ob das korrekt ist
   }
   
   /**
    * Tafel: 1.)1.3
    */
   private BigInteger sign(ElGamal el, BigInteger message){
     return el.sign(message);
   }
   
   /**
    * Tafel: 1.)3.2
    */
   private boolean verify(ElGamal el, BigInteger message, BigInteger signature){
     return el.verify(message, signature);
   }
   
   /**
    * Tafel: 1.)1.3
    */
   private String erklaerungAlice(){
     String a = "Die Symbole A'_i,j bezeichnen Loesungen der zugehoerigen S-Puzzles ";
     String b = "C_(A_i,j), i.element{1,...,n}, j.element{1,2}. ";
     String c = "Der untenstehende Vertrag ist von mir unterzeichnet, ";
     String d = "wenn Bob fuer ein i.element{1,...,n} die beiden Schluessel ";
     String e = "A'_i,1 und A'_i,2 nennen kann, d.h., wenn er die Loesung ";
     String f = "des (i,1)-ten und (i,2)-ten Puzzles kennt.";
     
     return a+b+c+d+e+f;
   }
   
   /**
    * Tafel: 1.)1.3
    */
   private String erklaerungBob(){
     String a = "Die Symbole A'_i,j bezeichnen Loesungen der zugehoerigen S-Puzzles ";
     String b = "C_(A_i,j), i.element{1,...,n}, j.element{1,2}. ";
     String c = "Der untenstehende Vertrag ist von mir unterzeichnet, ";
     String d = "wenn Alice fuer ein i.element{1,...,n} die beiden Schluessel ";
     String e = "B'_i,1 und B'_i,2 nennen kann, d.h., wenn er die Loesung ";
     String f = "des (i,1)-ten und (i,2)-ten Puzzles kennt.";
     
     return a+b+c+d+e+f;
   }
   
   private String vertragString(File file){
     
     String output = "";
     try {
       BufferedReader in = new BufferedReader(new FileReader(file));
       
       String inputLine;
       while ((inputLine = in.readLine()) != null) {
         output += inputLine;
       }
     } catch (FileNotFoundException e) {
       System.out.println("Dude! Vertrag nicht vorhanden!");
       System.exit(0);
       e.printStackTrace();
     } catch (IOException e) {
       System.out.println("Dude! Vertrag nicht vorhanden!");
       System.exit(0);
       e.printStackTrace();
     }
     
     return output;
   }
 
   /*
    * Testet ob gcd(val1, val2) = 1 ist und liefert TRUE falls ja
    */
   private boolean checkGGT(BigInteger val1, BigInteger val2) {
     boolean result = false;
 
     BigInteger gcd = val1.gcd(val2);
     
     if(gcd.compareTo(ONE) == 0){
       result = true;
     }
 
     return result;
   }
 
   /**
    * Tafel: 1.)1.1
    */
   private BigInteger[][] getDoubleArray(int n, BigInteger p) { // p_A oder p_B
     BigInteger[][] array = new BigInteger[n][2];
 
     for (int i = 0; i < n; i++) {
       boolean check = false;
       while (!check) {
         array[i][0] = computePrime(p);
         check = checkGGT(array[i][0], p.subtract(ONE));
       }
       check = false;
       while (!check) {
         array[i][1] = computePrime(p);
         check = checkGGT(array[i][1], p.subtract(ONE));
       }
     }
 
     return array;
   }
   
   /**
    * Tafel: 1.)1.1
    */
  private BigInteger[] get_C_Array(BigInteger M, BigInteger[][] array){
     int length1 = array.length;
     int length2 = array[0].length;
     BigInteger[] output = new BigInteger[length1*length2];
     
     int counter = 0;
     for(int i=0; i<length1; i++){
       for(int j=0; j<length2; j++){
        output[counter++] = M.modPow(array[i][j], M);
       }
     }
     
     return output;
   }
 
   /**
    * Tafel: 0.)3.1
    */
   private BigInteger computeMessage(BigInteger modulus) { // modulus = M
     BigInteger output = null;
 
     // TODO output < modulus (kein Primzahltest noetig)
 
     return output;
   }
 
   /**
    * Tafel: 0.)3.3
    */
   private boolean isPrime(BigInteger val) {
     boolean result = false;
 
     // TODO check if val is Prime!
 
     return result;
   }
 
   /**
    * Tafel: 0.)3.1 , 0.)4.1
    */
   private BigInteger computePrime(BigInteger modulus) { // p_A , Modulus 2^52
     BigInteger output = null;
 
     while (!isPrime(output)) { // Solange keine Primzahl gefunden wurde
       output = null; // TODO Generate new prime
     }
 
     return output;
   }
 
   /**
    * Tafel: 0.)4.2
    */
   private BigInteger computePrimeBetween(BigInteger M, BigInteger modulus) { // p_B, Modulus 2^52
     BigInteger output = computePrime(modulus);
 
     // Bedingung: M < output < 2^52
     while ((output.compareTo(M) == 1) || (output.compareTo(M) == 0)) {
       output = computePrime(modulus);
     }
 
     return output;
 
   }
 
 }
