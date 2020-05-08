 package algo.exercise;
 
 import static java.lang.Math.max;
 
 /**
  * Baumknoten und Token zur Darstellung eines Operators.
  *
  * @author Sandro Kuckert
  */
 public class Op extends Token 
 {
 
     Token left;
     Token right;
     
     /**
      * Erzeugt einen neuen Operator-Knoten.
      * 
      * @param t der Typ des Knotens: '+', '-', '*' oder '/'
      * @param l der linke Unterbaum
      * @param r der rechte Unterbaum
      */
     public Op(char t, Token l, Token r) 
     {
         type = t;
         left = l;
         right = r;
     }
     
     /**
      * Erzeugt einen neuen Operator-Token.
      * 
      * @param t der Typ des Tokens: '+', '-', '*' oder '/'
      */
     public Op(char t) 
     {
         type = t;
         left = null;
         right = null;
     }
 
     public Token left() { return left; }
     public Token right() { return right; }
 
     /**
      * Funktion zum Auswerten des Operators und der Ergebnisse der zwei Unterbäume
      *
      * @return int Ergebnis der Operation
      */
     public int eval() 
     {
         switch(this.type)
         {
             case '+':
                 return this.left.eval() + this.right.eval();
             case '-':
                 return this.left.eval() - this.right.eval();
             case '*':
                 return this.left.eval() * this.right.eval();
             case '/':
                 return this.left.eval() / this.right.eval();
             default:
                 return 0;
         }
 
     }
 
     /**
      * Funktion zum Erstellen der Präfixnotation aus dem Operator, dem linken und rechten Unterbaum
      *
      * @return String der Operation
      */
     public String prefix() 
     {
         return String.valueOf(this.type) + " " + this.left.prefix() + this.right.prefix();
     }
 
     /**
      * Funktion zum Erstellen der Infixnotation aus linken Unterbaum, dem Operator und dem rechten Unterbaum
      *
      * @return String der Operation
      */
     public String infix() 
     {
        return "(" + this.left.eval() + " " + this.type + " " + this.right.eval() + ") ";
     }
 
     /**
      * Funktion zum Erstellen der Postfixnotation aus linken, rechten Unterbaum  und dem Operator
      *
      * @return String der Operation
      */
     public String postfix() 
     {
        return this.left.infix() + this.right.infix() + String.valueOf(this.type) + " ";
     }
 
     /**
      * Funktion gibt die Anzhal der Knoten zurück inklusive dem Operator
      *
      * @return int Anzahl der Knoten
      */
     public int nodes() 
     {
         return this.left.nodes() + this.right.nodes() + 1;
     }
 
     /**
      * Funktion zum Ermitteln der Tiefe des Baumes
      * Ermittelt wird die Länge der Unterbäume, der Tiefere Unterbaum +1 wird zurück gegeben.
      *
      * @return int Tiefe des Baumes
      */
     public int depth() 
     {
         int leftDepth = this.left.depth();
         int rightDepth = this.right.depth();
 
         return max(leftDepth,rightDepth) + 1;
     }
 
     /**
      * Numeriert den Baum ausgehend vom aktuellen Knoten unter Verwendung eines Zaehlers in Infix-Reihenfolge durch (wichtig fuer die Visualisierung).
      * 
      * @param o der Zaehler
      */
     public void order(Order o) 
     {
         left.order(o);
         setOrd(++o.counter);
         right.order(o);
     }
 }
