 package pl.edu.pw.elka.pszt.inteligraph.model;
 
 /**
  * Klasa reprezentująca nazwę wierzchołka
  */
 public class VertexName
 {
 	private Integer name;
 
 	public VertexName(Integer name)
 	{
 		super();
 		this.name = name;
 	}
 
 	/**
 	 * @return Nazwa wierzchołka
 	 */
 	public Integer getName()
 	{
 		return name;
 	}

	@Override
	public String toString()
	{
		return this.name.toString();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VertexName other = (VertexName) obj;
		if (name == null)
		{
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
 	
 	
 }
