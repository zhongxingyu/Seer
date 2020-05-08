 package columbia.plt.tt.interpreter;
 
 import java.util.HashMap;
 import java.lang.String; 
 
 public class Scope extends HashMap<String, Symbol> {
   
     private final int id;
   
     public Scope(final int id) {
         this.id = id;
     }
     
     public int getID() {
       return this.id;
     }
     
     public void addSymbol(String name, String type, Object value) {
       this.put(name, new Symbol(type, value));
     }
     
     public Object getValue(String name)
     {
     	return this.get(name);
     }
     
     public Object getType(String name)
     {
    	return this.get(name);
    }    
 }
