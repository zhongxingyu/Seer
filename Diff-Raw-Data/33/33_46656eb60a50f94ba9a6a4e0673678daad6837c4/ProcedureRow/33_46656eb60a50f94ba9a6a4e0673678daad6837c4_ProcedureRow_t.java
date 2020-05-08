 package compiler.parser.symbol;
 
 public class ProcedureRow extends Row {
 
    private Attribute[] attributes;
    
    public ProcedureRow(String lexeme, Classification classification, Attribute[] attributes) {
         super(lexeme, classification);
        this.attributes = attributes;
     }
 
    public Attribute[] getAttributes()
    {
        return attributes;
    }
 }
