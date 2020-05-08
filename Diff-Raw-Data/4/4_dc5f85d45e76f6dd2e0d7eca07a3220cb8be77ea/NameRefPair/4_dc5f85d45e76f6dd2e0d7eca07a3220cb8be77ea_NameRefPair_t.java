 
 
 
 package overwatch.gui;
 
 
 
 
 
 /**
  * Pairs a reference with a name.
  * Intended for GUI containers which use toString() to decide what will be shown.
  * Often you don't want the default toString() to be what is displayed.
  * 
  * @author Lee Coakley
 * @version 3
  */
 
 
 
 
 
 public class NameRefPair<T>
 {
 	public String name;
 	public T      ref;
 	
 	
 	
 	
 	
 	public NameRefPair( T ref, String displayedName )
 	{
 		this.ref  = ref;
 		this.name = displayedName;
 	}
 	
 	
 	
 	
 	
 	public boolean equals( Object other )
 	{
 		if (other == null)
 			return false;
 		
 		if (this.getClass() != other.getClass()) 
 			return false;
 		
		if (ref != null)
 		if (ref.equals( ((NameRefPair<T>)other).ref ))
 			return true;
 		
 		return false;
 	}
 	
 	
 	
 	
 	
 	public String toString() {
 		return name;
 	}
 }
