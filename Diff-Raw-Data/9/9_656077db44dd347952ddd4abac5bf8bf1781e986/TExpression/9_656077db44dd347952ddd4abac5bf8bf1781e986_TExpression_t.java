 package spreadsheet.textual;
 import spreadsheet.Expression;
 import spreadsheet.Position;
 import java.util.ArrayList;
 
 //represents a textual expression
 
 public abstract class TExpression extends Expression {
   
   //returns a boolean representation of the textual value
 
   public boolean toBoolean() {
     return eval().isEmpty();
   }
   
  //returns a arithmetic representation of the textual value
 
   public int toInt() {
     return eval().length();
   }
 
   //returns the textual value
   
   public String toString() {
     return eval();
   }
   
   // returns the evaluation of the expression
   
   public abstract String eval();
   
   public abstract ArrayList<Position> getReferencedPositions();
 }
