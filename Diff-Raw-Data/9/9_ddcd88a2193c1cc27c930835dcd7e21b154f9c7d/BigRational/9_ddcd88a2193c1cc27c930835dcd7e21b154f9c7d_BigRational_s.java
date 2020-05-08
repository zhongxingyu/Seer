 /*
  * Created on 	23.03.2010
  * @author 		BrM / MD nach Vorlagen von Hue / FdR / Sid / Weigend
  * Zweck:		Klasse Rational mit vielfältigen Methoden
  */
 package ro.inf.p2.uebung05;
 
 import java.math.BigInteger;
 import java.util.StringTokenizer;
 
 
 /**
  * Einfache Implementierung für rationale Zahlen als Vorbereitung auf die Klasse BigRational.
  * Diese Klasse ist immutable. Instanzen können nach der Konstruktion nicht mehr verändert werden
  * (es gibt keine set-Methoden).
  */
 public class BigRational implements Comparable<BigRational> {
     // Konstanten
     public static final String DELIMITER = "/"; // Bruchstrich
     private final static long PRECISION = 10000000l;  // am Ende l wie long
 
 
     //  Zähler und Nenner
     private BigInteger numerator;
     private BigInteger denominator;
 
     // private Hilfsmethoden
 
     /**
      * Ermittlung des größten gemeinsamen Teilers (GCD = greatest common devisor) mittels des Euclidschen Alg
      *
      * @param a (positive) ganze Zahl
      * @param b (positive) ganze Zahl
      * @return größter gemeinsamer Teiler
      */
     private static BigInteger gcd(BigInteger a, BigInteger b) {
         a = a.abs(); // sonst wirft mod() eine ArithmeticException
         b = b.abs();
         if (a.compareTo(b) < 0) return gcd(b, a);
         if (b.compareTo(BigInteger.ZERO) == 0)
             return a; // Null wird von jeder Zahl geteilt
         BigInteger c = BigInteger.ONE;
         while (c.compareTo(BigInteger.ZERO) != 0) {
             c = a.mod(b);
             a = b;
             b = c;
         }
         return a;
     }
 
     /**
      * normiert den Bruch (kürzt und macht den Nenner positiv)
      */
     private void norm() {
         BigInteger gcd = gcd(numerator, denominator);
         // kürzen
 
         numerator = numerator.divide(gcd);
 
         denominator = denominator.divide(gcd);
 
         // Nenner positiv machen
 
         if (denominator.compareTo(BigInteger.ZERO) < 0) {
             numerator = numerator.negate();
             denominator = denominator.negate();
         }
     }
 
 
     // Konstruktoren
 
     /**
      * default Vorbelegung (0/1)
      */
     public BigRational() {
 
         numerator = BigInteger.ZERO;
         denominator = BigInteger.ONE;
 
     }
 
     /**
      * Rationalzahl mit Zähler und Nenner vom Typ long
      *
      * @param num Zähler
      * @param den Nenner
      */
     public BigRational(long num, long den) {
 
         numerator = BigInteger.valueOf(num);
         denominator = BigInteger.valueOf(den);
 
         norm();
     }
 
     /**
      * Rationalzahl mit Zähler und Nenner vom Typ BigInteger
      *
      * @param num Zähler
      * @param den Nenner
      */
     public BigRational(BigInteger num, BigInteger den) {
 
         numerator = num;
         denominator = den;
 
         norm();
     }
 
     /**
      * Rationalzahl aus Long-Integer mit Nenner 1
      *
      * @param val long
      */
     public BigRational(long val) {
         this(val, 1);
     }
 
     /**
      * Rationalzahl aus double
      *
      * @param val double-Wert
      */
     public BigRational(double val) {
         this((long) (val * PRECISION), PRECISION);
     }
 
     /**
      * Rationalzahl aus String der Form "x/y"
      *
      * @param val String
      */
     public BigRational(String val) {
         StringTokenizer tok = new StringTokenizer(val, DELIMITER);
 
         numerator = new BigInteger(tok.nextToken());
         denominator = new BigInteger(tok.nextToken());
 
         norm();
     }
 
     // get-Methoden
 
     /**
      * gibt den privaten Zähler
      *
      * @return Zähler
      */
     public BigInteger getNumerator() {
 
         return numerator;
 
     }
 
     /**
      * gibt den privaten Nenner
      *
      * @return Nenner
      */
     public BigInteger getDenominator() {
 
         return denominator;
 
     }
 
     // einfache Umwandlungen
 
     /**
      * Gibt die Rationalzahl als double zurück indem der Zähler durch den Nenner dividiert wird.
      *
      * @return die Rationalzahl als Double
      */
     public double doubleValue() {
         return numerator.doubleValue() / denominator.doubleValue();
     }
 
     /**
      * Ermittlung der Stringdarstellung
      *
      * @return eine Stringdarstellung der Rationalzahl der Form "a/b"
      */
     public String toString() {
         return numerator + "/" + denominator;
     }
 
     /**
      * Negation = Multiplikation mit -1
      *
      * @return den negativen Bruch
      */
     public BigRational negate() {
         return new BigRational(numerator.negate(), denominator);
     }
 
     /**
      * Kehrbruch
      *
      * @return den Kehrbruch (aus a/b wird b/a)
      */
     public BigRational invert() {
         return new BigRational(denominator, numerator);
     }
 
     // Bruchrechnen
 
     /**
      * Zwei Brüche (a,b) werden addiert. Dabei wird der Bruch erweitert
      *
      * @param val der Bruch (b), welcher addiert werden soll.
      * @return Eine neue Rationalzahl als Ergebnis der Operation
      */
     public BigRational add(BigRational val) {
         return new BigRational(
                 numerator.multiply(val.getDenominator()).add(val.getNumerator().multiply(denominator)),
                 denominator.multiply(val.getDenominator())
         );
         // die Normierung erfolgt im Konstruktor
     }
 
 
     /**
      * Zieht von einem Bruch einen anderen ab (a-b) indem mit dem negierten Bruch
      * addiert wird. Dieses Methode ist ein Beispiel für gute, wenig redundante Programmierung,
      * indem die substract-Methode auf eine bestehende Methode abgebildet wird, ohne die Logik
      * erneut zu programmieren.
      *
      * @param val der Bruch (b), der abgezogen wird
      * @return Eine neue Rationalzahl als Ergebnis der Operation
      */
     public BigRational subtract(BigRational val) {
         return new BigRational(
                 numerator.multiply(val.getDenominator()).subtract(val.getNumerator().multiply(denominator)),
                 denominator.multiply(val.getDenominator())
         );
     }
 
     /**
      * Multipliziert zwei Brüche (a,b), indem die Zähler und Nenner jeweils miteinander
      * multipliziert werden.
      *
      * @param val der Bruch (b)
      * @return Eine neue Rationalzahl als Ergebnis der Operation
      */
     public BigRational multiply(BigRational val) {
         return new BigRational(
                 numerator.multiply(val.getNumerator()),
                 denominator.multiply(val.getDenominator())
         );
     }
 
     /**
      * Teilt einen Bruch durch einen anderen, indem mit dem Kehrbruch multipliziert wird.
      *
      * @param val der Bruch durch den geteilt wird.
      * @return Eine neue Rationalzahl als Ergebnis der Operation
      */
     public BigRational divide(BigRational val) {
         return new BigRational(
                 numerator.multiply(val.getDenominator()),
                 denominator.multiply(val.getNumerator())
         );
     }
 
     @Override
     public boolean equals(Object x) {
         if (this == x) return true;
         if (!(x instanceof BigRational)) return false;
 
         BigRational that = (BigRational) x;
 
        if (denominator != null ? !denominator.equals(that.denominator) : that.denominator != null) return false;
        if (numerator != null ? !numerator.equals(that.numerator) : that.numerator != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
        int result = numerator != null ? numerator.hashCode() : 0;
        result = 31 * result + (denominator != null ? denominator.hashCode() : 0);
         return result;
     }
 
     public int compareTo(BigRational x) {
         return 0;
     }
 }
