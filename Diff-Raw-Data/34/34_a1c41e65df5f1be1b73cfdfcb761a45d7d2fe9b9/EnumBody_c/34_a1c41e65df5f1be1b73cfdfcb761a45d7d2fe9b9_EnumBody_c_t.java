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
 
            for (int j = i+1; j < l.size(); j++){
                 EnumConstant ej = (EnumConstant) l.get(j);
 
                 if (ei.name().equals(ej.name())){
                    throw new SemanticException("Duplicate enum constant \"" + ej + "\" at ", ej.position());
                 }
             }
         }
     }
 
    public void prettyPrint(CodeWriter w, PrettyPrinter tr){
        if (!members.isEmpty()) {
            w.newline(4);
            w.begin(0);

            boolean lastWasEnum = false;
            for (Iterator i = members.iterator(); i.hasNext(); ) {
                ClassMember member = (ClassMember) i.next();
                if (!(member instanceof EnumConstant) && lastWasEnum){
                    w.write(";");
                    w.newline(0);
                    lastWasEnum = false;
                }
                printBlock(member, w, tr);
                if (member instanceof EnumConstant){
                    w.write(",");
                    lastWasEnum = true;
                }
                if (i.hasNext()) {
                    w.newline(0);
                    w.newline(0);
                }
            }

            w.end();
            w.newline(0);
        } 
    }
 }
