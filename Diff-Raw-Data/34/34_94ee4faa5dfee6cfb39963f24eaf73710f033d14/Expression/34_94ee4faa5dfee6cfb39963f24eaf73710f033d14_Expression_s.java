 package zinara.ast.expression;
 import zinara.code_generator.*;
 
 import zinara.ast.type.Type;
 import zinara.exceptions.TypeClashException;
 
 import java.io.IOException;
 
 public abstract class Expression {
    private Type type;
     //@ ensures \result != null
     public abstract Type getType() throws TypeClashException;
     public abstract String toString();
     public abstract String tox86(Genx86 generate) throws IOException;
 }
