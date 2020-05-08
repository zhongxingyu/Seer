 /***********************************************************************
  * cs3517.solutions.mud.Vertex
  ***********************************************************************/
 
 package cs3517.hale.mud;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Vector;
 import java.util.Iterator;
 
 // Represents a location in the MUD (a vertex in the graph).
 class Vertex
 {
   public String _name;             // Vertex name
   public String _msg = "";         // Message about this location
   public Map<String,Edge> _routes; // Association between direction
   // (e.g. "north") and a path
   // (Edge)
   public List<String> _things;     // The things (e.g. players) at
   // this location
 
   public Vertex( String nm )
   {
     _name = nm;
     _routes = new HashMap<String,Edge>(); // Not synchronised
     _things = new Vector<String>();       // Synchronised
   }
 
   public String toString()
   {
     String summary = "\n";
     summary += _msg + "\n";
     Iterator iter = _routes.keySet().iterator();
     String direction;
     while (iter.hasNext()) {
       direction = (String)iter.next();
      summary += "To the " + direction + " there is " + ((Edge)_routes.get( direction ))._view + "\n";
     }
     iter = _things.iterator();
     if (iter.hasNext()) {
       summary += "You can see: ";
       do {
         summary += iter.next() + " ";
       } while (iter.hasNext());
     }
     summary += "\n\n";
     return summary;
   }
 }
 
