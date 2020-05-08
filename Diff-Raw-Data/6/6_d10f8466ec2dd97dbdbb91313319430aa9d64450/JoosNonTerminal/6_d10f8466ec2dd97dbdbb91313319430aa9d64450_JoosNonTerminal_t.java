 package cs444.parser.symbols;
 
 import java.util.HashSet;
 import java.util.Set;
 
 public class JoosNonTerminal extends NonTerminal{
     public static final Set<String> noCollapse = new HashSet<String>();
    public static final Set<String> specialNoDie = new HashSet<String>();
 
     static{
         noCollapse.add("MODIFIERS");
         noCollapse.add("NAME");
         noCollapse.add("INTERFACETYPE");
         noCollapse.add("CLASSTYPE");
         noCollapse.add("TYPEDECLARATION");
         noCollapse.add("TYPE");
         noCollapse.add("ARRAYTYPE");
         noCollapse.add("VARIABLEINITIALIZER");
         noCollapse.add("VARIABLEDECLARATOR");
         noCollapse.add("CONSTRUCTORDECLARATION");
         noCollapse.add("FORMALPARAMETERLIST");
         noCollapse.add("CONSTRUCTORBODY");
         noCollapse.add("METHODBODY");
         noCollapse.add("METHODDECLARATION");
         noCollapse.add("METHODDECLARATOR");
         noCollapse.add("CONSTRUCTORDECLARATOR");
         noCollapse.add("INTERFACEMEMBERDECLARATIONS");
         noCollapse.add("CLASSBODYDECLARATIONS");
        specialNoDie.add("EMPTYSTATEMENT");
     }
 
     public JoosNonTerminal(String name, ISymbol[] children) {
         super(name, children);
     }
 
     @Override
     public Set<String> noCollapse() {
         return noCollapse;
     }
 
     @Override
     public boolean empty() {
        return children.size() == 0 && !specialNoDie.contains(name.toUpperCase());
     }
 }
