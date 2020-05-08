 /*
  * Class Vertex for the graph.
  * Make a vertex for stack frames, objects, and primitive types.
  */
 public class Vertex {
 	
 	int id = 0;
 	String value = "";
 	//add function or object boolean
	Boolean isAFunction = false;
 	
 	public Vertex()
 	{
 		
 	}
 	
 	public Vertex(int id)
 	{
 		this.id = id;
 	}
 	
 	public Vertex(int id, String value)
 	{
 		this.id = id;
 		this.value = value;
 	}
 	
 	public Vertex(int id, String value, Boolean func)
 	{
 		this.id = id;
 		this.value = value;
		this.isAFunction = func;
 	}
 	
 	public void setName(String name)
 	{
 		this.value = name;
 	}
 	
 	public String getName()
 	{
 		return this.value;
 	}
 	
 	public int getID()
 	{
 		return this.id;
 	}
 	
 	public void displayVertex()
 	{
 		System.out.println("Vertex: " + this.id + " Name: " + this.value);
 	}
 	
 	public void resetVertex()
 	{
 		this.id = 0;
 		this.value = "";
 	}
 	
 }
