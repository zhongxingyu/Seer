 package polyglot.ext.jl5.ast;
 
 import java.util.*;
 import polyglot.ext.jl.ast.*;
 import polyglot.util.*;
 import polyglot.ast.*;
 import polyglot.ext.jl5.types.*;
 import polyglot.types.*;
 import polyglot.visit.*;
 
 public class EnumBody_c extends ClassBody_c implements EnumBody {
 
     public EnumBody_c(Position pos, List members) {
         super(pos, members);
     }
 
     public Node typeCheck(TypeChecker tc) throws SemanticException {
         duplicateEnumConstantCheck(tc); 
         return super.typeCheck(tc);
     }
 
     protected void duplicateEnumConstantCheck(TypeChecker tc) throws SemanticException {
         JL5ParsedClassType type = (JL5ParsedClassType)tc.context().currentClass();
 
         ArrayList l = new ArrayList(type.enumConstants());
         for (int i = 0; i < l.size(); i++){
             EnumConstant ei = (EnumConstant) l.get(i);
 
            for (int j = 0; j < l.size(); j++){
                 EnumConstant ej = (EnumConstant) l.get(j);
 
                 if (ei.name().equals(ej.name())){
                    throw new SemanticException("Duplicate enum constant \"" + ej + "\" at " + ej.position());
                 }
             }
         }
     }
 
 }
