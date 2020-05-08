 // Scanner.java
 // 
 // Ein einfacher Scanner für Aufgabe 1, 8. Übungszettel GTI
 // Autor: Ulrich Kortenkamp 
 //
 // Übersetzen mit "javac Scanner.java"
 
 package blatt8;
 
 import java.io.*;
 
 public class Scanner {
 
     /** Konstanten für die möglichen Zeichenfolgen 
      */
 
     public final static int IF    = 0;
     public final static int THEN  = 1;
     public final static int FI    = 2;
     public final static int ELSE  = 3;
     public final static int WHILE = 4;
     public final static int DO    = 5;
     public final static int OD    = 6;
     public final static int BEGIN = 7;
     public final static int SEMI  = 8;
     public final static int END   = 9;
     public final static int ASSIGN = 10;
     public final static int PLUS   = 11;
     public final static int MINUS  = 12;
     public final static int ONE    = 13;
     public final static int ZERO   = 14;
     public final static int LETTER = 15;
     public final static int WHITESPACE = 16;
     public final static int EOF    = -1;
 
     /** Hier kommen die Daten her
      */
     private BufferedReader input;
 
     /** aktuelles Zeichen falls LETTER erkannt
      */
     private char currentChar;
 
     /** aktuelles Zeile
      */
     private String currentLine;
 
     /** Default-Konstruktor erzeugt Scanner für Konsole (stdin)
      */
 
     public Scanner() {
         this(System.in);
     }
 
     /** Konstruktor erzeugt Scanner für gegebenen InputStream
      */
     public Scanner(InputStream is) {
         input = new BufferedReader(new InputStreamReader(is));
     }
 
     /** Konstruktor erzeugt Scanner für gegebenen InputStreamReader
      */
     public Scanner(InputStreamReader ir) {
         input = new BufferedReader(ir);
     }
 
     /** Liefert nächstes token oder Scanner.EOF falls das Ende bereits
      erreicht wurde, whitespace wird nicht ignoriert */
 
     private int getNextTokenWS() {
         while (currentLine == null || currentLine.equals("")) {
             try {
                 currentLine = input.readLine();
             } catch (IOException ioexc) {
                 return EOF;
             }
             if (currentLine == null)
                 return EOF;
             currentLine = currentLine.toLowerCase();
         }
 
        if (check("!")) return EOF;
         if (check("if")) return IF;
         if (check("fi")) return FI;
         if (check("then")) return THEN;
         if (check("else")) return ELSE;
         if (check("while")) return WHILE;
         if (check("do")) return DO;
         if (check("od")) return OD;
         if (check("begin")) return BEGIN;
         if (check(";")) return SEMI;
         if (check("end")) return END;
         if (check(":=")) return ASSIGN;
         if (check("+")) return PLUS;
         if (check("-")) return MINUS;
         if (check("1")) return ONE;
         if (check("0")) return ZERO;
 
         char c = currentLine.charAt(0);
 
         if ((c >= 'a') && (c <= 'z')) {
             currentChar=c;
             currentLine=currentLine.substring(1);
             return LETTER;
         }
 
         currentLine=currentLine.substring(1);
         return WHITESPACE;
     }
 
     private boolean check(String x) {
         if (currentLine.startsWith(x)) {
             currentLine=currentLine.substring(x.length());
             return true;
         }
         return false;
     }
 
     /** Liefert nächstes token oder Scanner.EOF falls das Ende bereits
      erreicht wurde, whitespace wird ignoriert */
 
     public int getNextToken() {
         int i;
         while ((i = getNextTokenWS()) == WHITESPACE) {};
         return i;
     }
 
     /** Falls LETTER in getNextToken erkannt wurde, liefert
      getCurrentChar den gefundenen Character zurück (sonst
      undefiniert).
      */
 
     public char getCurrentChar() {
         return currentChar;
     }
 
 }
 	
 
     
