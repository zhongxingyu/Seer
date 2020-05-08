 /**
  * @author Bogdan Gutey
  */
 package epam.ph.sg.models.sb;
 
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 
 public class SbJSLoader {
 
 	//Ліст скріптів
 	private Set<String> scripts = new LinkedHashSet<String>();
 	
 	public Set<String> getScripts() {
 		return scripts;
 	}
 
 	public void addScript(String script)
 	{
 		scripts.add("js/"+script+".js");
 	}
	public void removeScript(String script)
	{
		scripts.remove(script);
	}
	
	
	
 	public String toString()
 	{
 		StringBuilder sb = new StringBuilder();
 		Iterator<String> it = scripts.iterator();
 		while(it.hasNext())
 		{
 			sb.append(it.next()).append("\n");
 			
 		}
 		return sb.toString();
 	}
 }
