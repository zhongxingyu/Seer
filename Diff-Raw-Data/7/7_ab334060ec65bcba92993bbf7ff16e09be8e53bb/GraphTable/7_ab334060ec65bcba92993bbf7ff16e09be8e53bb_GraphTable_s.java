import java.util.HashMap;
import java.util.Map;
import java.util.List;
 
 /**
  * Extends a HashMap, only rewriting the toString function.
  */
 public class GraphTable extends HashMap<String, Node> {
    // Eventually we want to write a topological sort based toString.
 
    public String toString() { 
       String ret = "";
       Set<String> keys = keySet();
 
       for (String s : keys) {
          ret += get(s) + "\n";
       }
    }
 }
