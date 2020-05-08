 package zinara.ast.expression;
 import zinara.code_generator.*;
 
 import zinara.ast.type.Type;
import zinara.ast.type.StringType;
 
 public class ReadExp extends Expression {
     public ReadExp () { type = new StringType(); }
     public Type getType() { return type; }
     public String toString() { return "READ"; }
 
     public String tox86(Genx86 generate){
         return "";
     }
 }
