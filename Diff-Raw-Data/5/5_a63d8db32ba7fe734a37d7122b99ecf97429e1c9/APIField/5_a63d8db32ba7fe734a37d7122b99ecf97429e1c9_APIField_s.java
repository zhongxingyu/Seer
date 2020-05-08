 package cz.cvut.fit.hybljan2.apitestingcg.apimodel;
 
 import com.sun.source.tree.Tree.Kind;
 import com.sun.tools.javac.tree.JCTree;
 import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
 import java.lang.reflect.Field;
 import java.util.Set;
 import javax.lang.model.element.Modifier;
 
 /**
  *
  * @author Jan Hýbl
  */
 public class APIField extends APIItem{
     private String varType;
 
     public APIField(JCVariableDecl jcvd) {
         this.name = jcvd.name.toString();
         this.varType = jcvd.vartype.toString();
         this.modifiers = jcvd.getModifiers().getFlags();
         this.kind = jcvd.getKind();
     }
 
     APIField(Field f) {
         this.name = f.getName();
         this.varType = f.getType().getSimpleName();
         this.modifiers = getModifiersSet(f.getModifiers());
        this.kind = Kind.OTHER;        // TODO: Find right kind
     }
 
     APIField(Class c) {
         this.name = "var";
         this.varType = c.getSimpleName();
         this.modifiers = getModifiersSet(c.getModifiers());
        this.kind = Kind.OTHER;        // TODO: Find right kind
     }
     
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         for(Modifier m : modifiers) sb.append(m).append(' ');
         sb.append(varType).append(' ');
         sb.append(name);
         return sb.toString();
     }        
 }
