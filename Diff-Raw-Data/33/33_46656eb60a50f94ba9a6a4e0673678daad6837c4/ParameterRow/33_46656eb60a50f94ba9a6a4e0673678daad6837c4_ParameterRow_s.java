 package compiler.parser.symbol;
 
 public class ParameterRow extends Row {
 
     private Type type;

    private Attribute[] attributes;
    
    public ParameterRow(String lexeme, Classification classification, Type type, Attribute[] attributes) {
         super(lexeme, classification);
         this.type = type;
        this.attributes = attributes;
     }
 
     public Type getType() {
         return type;
     }
 
     public void setType(Type type) {
         this.type = type;
     }
 
    public Attribute[] getAttributes()
    {
        return attributes;
    }
 }
