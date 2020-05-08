 package thomasmarkus.nl.freenet.graphdb;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.*;
 
 public class VertexIterator implements Iterator<Map<String, List<String>>> {
 
 	private ResultSet resultSet;
 	private boolean hasNext = true;
 	private long last_vertex = Long.MIN_VALUE;
 
 	
 	public VertexIterator(ResultSet resultSet) throws SQLException
 	{
 		this.resultSet = resultSet;
 		this.hasNext = resultSet.next();

		if (hasNext) //only assign field if we have at least one result in the resultSet
		{
			last_vertex = resultSet.getLong("vertex_id");	
		}
 	}
 	
 	@Override
 	public boolean hasNext() {
 		return hasNext;
 	}
 
 	@Override
 	public Map<String, List<String>> next() {
 		
 		Map<String, List<String>> properties = new HashMap<String, List<String>>();
 
 		try {
 			long current_vertex = Long.MIN_VALUE;
 			while(hasNext && (current_vertex == Long.MIN_VALUE || resultSet.getLong("vertex_id") == current_vertex)) 
 			{
 				final String name = resultSet.getString("name");
 				final String value = resultSet.getString("value");
 				if (!properties.containsKey(name)) properties.put(name, new LinkedList<String>());
 				properties.get(name).add(value);
 				
 				last_vertex = resultSet.getLong("vertex_id");
 				current_vertex = resultSet.getLong("vertex_id");
 				this.hasNext = resultSet.next();
 			}
 
 			if (!this.hasNext)
 			{
 				resultSet.close();
 			}
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 			this.hasNext = false;
 		}
 		
 		return properties;
 	}
 
 	public long getLastVertexId()
 	{
 		return last_vertex;
 	}
 	
 	@Override
 	public void remove() {
 		throw new UnsupportedOperationException("This iterator doesn't support removing elements in this manner");
 	}
 }
