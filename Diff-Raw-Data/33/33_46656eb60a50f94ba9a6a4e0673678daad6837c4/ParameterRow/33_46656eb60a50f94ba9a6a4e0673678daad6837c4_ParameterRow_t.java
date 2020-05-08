 package compiler.parser.symbol;
 
 public class ParameterRow extends Row {
 
     private Type type;
  
    public ParameterRow(String lexeme, Classification classification, Type type) {
         super(lexeme, classification);
         this.type = type;
        
     }
 
     public Type getType() {
         return type;
     }
 
     public void setType(Type type) {
         this.type = type;
     }
 
    
 }
