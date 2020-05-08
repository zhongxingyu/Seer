 package thomasmarkus.nl.freenet.graphdb;
 
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 public class H2DB {
 
 	Connection con;
 
 	public H2DB(Connection con)
 	{
 		this.con = con;
 	}
 
 	public static void checkDB(Connection con) throws SQLException
 	{
 		try
 		{
 			final Statement stmt = con.createStatement();
 
 			//check whether it contains the right tables
 			DatabaseMetaData dbm = con.getMetaData();
 			ResultSet tables = dbm.getTables(null, null, null, new String[] {"TABLE"});
 
 			if (!tables.next()) {
 				System.out.println("Creating tables...");
 
 				//create the table
 				stmt.execute("CREATE TABLE vertices " +
 						"(id IDENTITY PRIMARY KEY)");
 
 				stmt.execute("CREATE TABLE edges " +
 						"(" +
 						"	id IDENTITY PRIMARY KEY," +
 						"	vertex_from_id LONG, " +
 						"	vertex_to_id LONG " +
 						")");
 
 				stmt.execute("CREATE INDEX vertex_from_index ON edges(vertex_from_id)");
 				stmt.execute("CREATE INDEX vertex_to_index ON edges(vertex_to_id)");
 
 				stmt.execute(	"CREATE TABLE vertex_properties " +
 						" (" +
 						"	vertex_id LONG, name VARCHAR(256), value VARCHAR(256), value_number INT" +
 						")"
 						); 
 
 				stmt.execute("CREATE INDEX vertex_properties_vertex_id_index ON vertex_properties(vertex_id)");
 				stmt.execute("CREATE INDEX vertex_properties_name_index ON vertex_properties(name)");
 				stmt.execute("CREATE INDEX vertex_properties_value_index ON vertex_properties(value)");
 				stmt.execute("CREATE INDEX vertex_properties_valuenumber_index ON vertex_properties(value_number)");
 
 				stmt.execute(	"CREATE TABLE edge_properties " +
 						" (" +
 						"	edge_id LONG, name VARCHAR(256), value VARCHAR(256), value_number INT " +
 						")"
 						); 
 
 				stmt.execute("CREATE INDEX edge_properties_edge_id ON edge_properties(edge_id)");
 				stmt.execute("CREATE INDEX edge_properties_name_index ON edge_properties(name)");
 				stmt.execute("CREATE INDEX edge_properties_value_index ON edge_properties(value)");
 				stmt.execute("CREATE INDEX edge_properties_valuenumber_index ON edge_properties(value_number)");
 
 				//foreign key constraints
 				stmt.execute("ALTER TABLE edges ADD FOREIGN KEY (vertex_from_id) REFERENCES vertices(ID) ON DELETE CASCADE");
 				stmt.execute("ALTER TABLE edges ADD FOREIGN KEY (vertex_to_id) REFERENCES vertices(ID) ON DELETE CASCADE");
 				stmt.execute("ALTER TABLE vertex_properties ADD FOREIGN KEY (vertex_id) REFERENCES vertices(ID) ON DELETE CASCADE");
 				stmt.execute("ALTER TABLE edge_properties ADD FOREIGN KEY (edge_id) REFERENCES edges(ID) ON DELETE CASCADE");
 			}
 
 			tables.close();
 			stmt.close();
 		}
 		finally
 		{
 			con.close();
 		}
 	}
 
 	public void close() throws SQLException
 	{
 		con.close();
 	}
 
 	public long insertVertex() throws SQLException
 	{
 		Statement st = null;
 		try
 		{
 			st = con.createStatement();
 
 			st.executeUpdate("INSERT into vertices () VALUES () ", Statement.RETURN_GENERATED_KEYS);
 			ResultSet results = st.getGeneratedKeys(); 
 
 			if (results.next())
 			{
 				return results.getLong(1); 	
 			}
 
 			throw new SQLException("Could not retrieve latest vertices.id");
 		}
 		finally
 		{
 			st.close();
 		}
 	}
 
 	public List<Long> getAllVerticesWithProperty(String name) throws SQLException
 	{
 		final PreparedStatement statement = con.prepareStatement("SELECT DISTINCT vertex_id FROM vertex_properties WHERE name = ?");
 		statement.setString(1, name);
 		final ResultSet resultSet = statement.executeQuery();
 
 		final List<Long> result = new LinkedList<Long>();
 		while(resultSet.next())
 		{
 			result.add(resultSet.getLong("vertex_id"));
 		}
 
 		resultSet.close();
 		statement.close();
 
 		return result;
 	}
 
 	public void insertVertexProperty(long vertex_id, String name, String value) throws SQLException
 	{
 		final PreparedStatement ps =  con.prepareStatement("INSERT INTO vertex_properties (vertex_id, name, value, value_number) VALUES (?, ?, ?, ?)");
 		ps.setLong(1, vertex_id);
 		ps.setString(2, name);
 		ps.setString(3, value);
 
 		try
 		{
 			ps.setInt(4, Integer.parseInt(value));	
 		}
 		catch(NumberFormatException e)
 		{
 			ps.setNull(4, java.sql.Types.INTEGER);
 		}
 
 		ps.execute();
 		ps.close();
 	}
 
 	public void updateVertexProperty(long vertex_id, String name, String value) throws SQLException
 	{
 
		PreparedStatement ps_select = con.prepareStatement("SELECT FROM vertex_properties WHERE vertex_id = ? AND name = ?");
 		ps_select.setLong(1, vertex_id);
 		ps_select.setString(2, name);	
 		
 		ResultSet result = ps_select.executeQuery();
 		
 		try
 		{
 			if (result.next()) //update
 			{
 				PreparedStatement ps_update = con.prepareStatement("UPDATE vertex_properties SET value = ? WHERE name = ? AND vertex_id = ?;");
 				ps_update.setString(1, value);
 				ps_update.setString(2, name);	
 				ps_update.setLong(3, vertex_id);
 				ps_update.execute();
 				ps_update.close();
 			}
 			else //insert
 			{
 				insertVertexProperty(vertex_id, name, value);
 			}
 		}
 		finally
 		{
 			ps_select.close();
 			result.close();
 		}
 	}
 
 	public void updateEdgeProperty(long edge_id, String name, String value) throws SQLException
 	{
		PreparedStatement ps_select = con.prepareStatement("SELECT FROM edge_properties WHERE edge_id = ? AND name = ?");
 		ps_select.setLong(1, edge_id);
 		ps_select.setString(2, name);	
 		
 		ResultSet result = ps_select.executeQuery();
 		
 		try
 		{
 			if (result.next()) //update
 			{
 				PreparedStatement ps_update = con.prepareStatement("UPDATE edge_properties SET value = ? WHERE name = ? AND edge_id = ?;");
 				ps_update.setString(1, value);
 				ps_update.setString(2, name);	
 				ps_update.setLong(3, edge_id);
 				ps_update.execute();
 				ps_update.close();
 			}
 			else //insert
 			{
 				insertEdgeProperty(edge_id, name, value);
 			}
 		}
 		finally
 		{
 			ps_select.close();
 			result.close();
 		}
 	}
 
 	public void insertEdgeProperty(long edge_id, String name, String value) throws SQLException
 	{
 		final PreparedStatement ps = con.prepareStatement("INSERT INTO edge_properties (edge_id, name, value, value_number) VALUES (?, ?, ?, ?)");
 		ps.setLong(1, edge_id);
 		ps.setString(2, name);
 		ps.setString(3, value);
 		try
 		{
 			ps.setInt(4, Integer.parseInt(value));	
 		}
 		catch(NumberFormatException e)
 		{
 			ps.setNull(4, java.sql.Types.INTEGER);
 		}
 		ps.execute();
 
 		ps.close();
 	}
 
 	public Map<String, List<String>> getEdgeProperties(long edge_id) throws SQLException
 	{
 		//lookup the properties for this edge and add them to the object
 		PreparedStatement ps_props = con.prepareStatement("SELECT name, value FROM edge_properties WHERE edge_id = ?");
 		ps_props.setLong(1, edge_id);
 		ResultSet propertiesValues = ps_props.executeQuery();
 
 		Map<String, List<String>> properties = new HashMap<String, List<String>>();
 		while(propertiesValues.next())
 		{
 			String name = propertiesValues.getString("name");
 			String value = propertiesValues.getString("value");
 
 			if (!properties.containsKey(name))	properties.put(name, new LinkedList<String>());
 			properties.get(name).add(value);
 		}
 
 		propertiesValues.close();
 		ps_props.close();
 
 		return properties;
 	}
 
 	/**
 	 * lookup the properties for this edge and add them to the object
 	 * @param edge_id
 	 * @param name
 	 * @return
 	 * @throws SQLException
 	 */
 
 	public String getEdgeProperty(long edge_id, String name) throws SQLException
 	{
 		PreparedStatement ps_props = con.prepareStatement("SELECT value FROM edge_properties WHERE edge_id = ? AND name = ? LIMIT 1");
 
 		try
 		{
 			ps_props.setLong(1, edge_id);
 			ps_props.setString(2, name);
 			ResultSet propertiesValues = ps_props.executeQuery();
 
 			while(propertiesValues.next())
 			{
 				return propertiesValues.getString("value");
 			}
 			return null;
 		}
 		finally
 		{
 			ps_props.close();
 		}
 	}
 
 
 	public List<Edge> getOutgoingEdges(long vertex_from_id) throws SQLException
 	{
 		PreparedStatement ps = con.prepareStatement("SELECT id, vertex_to_id FROM edges WHERE vertex_from_id = ?");
 		ps.setLong(1, vertex_from_id);
 
 		List<Edge> edges = new LinkedList<Edge>();
 		ResultSet results = ps.executeQuery();
 		while(results.next())
 		{
 			Edge edge = new Edge(this);
 			edge.id = results.getLong("id");
 			edge.vertex_from = vertex_from_id;
 			edge.vertex_to = results.getLong("vertex_to_id");
 			edges.add(edge);
 		}
 
 		results.close();
 		ps.close();
 
 		return edges;
 	}
 
 	public List<Edge> getIncomingEdges(long vertex_to_id) throws SQLException
 	{
 		PreparedStatement ps = con.prepareStatement("SELECT id, vertex_from_id FROM edges WHERE vertex_to_id = ?");
 		ps.setLong(1, vertex_to_id);
 
 		List<Edge> edges = new LinkedList<Edge>();
 		ResultSet results = ps.executeQuery();
 		while(results.next())
 		{
 			Edge edge = new Edge(this);
 			edge.id = results.getLong("id");
 			edge.vertex_from = results.getLong("vertex_from_id");
 			edge.vertex_to = vertex_to_id;
 			edges.add(edge);
 		}
 
 		results.close();
 		ps.close();
 
 		return edges;
 	}
 
 	public List<EdgeWithProperty> getOutgoingEdgesWithProperty(long vertex_from_id, String name) throws SQLException
 	{
 		final PreparedStatement ps_outgoing_edges_with_property = con.prepareStatement(
 				"SELECT value, id, vertex_to_id FROM edge_properties, edges " +
 						"WHERE " +
 						"edge_id IN (SELECT id FROM edges WHERE vertex_from_id = ?) AND " +
 						"edges.id = edge_id AND " +
 				"name = ?");
 
 
 		ps_outgoing_edges_with_property.setLong(1, vertex_from_id);
 		ps_outgoing_edges_with_property.setString(2, name);
 
 		final List<EdgeWithProperty> edges = new LinkedList<EdgeWithProperty>();
 		final ResultSet results = ps_outgoing_edges_with_property.executeQuery();
 		while(results.next())
 		{
 			EdgeWithProperty edge = new EdgeWithProperty();
 			edge.id = results.getLong("id");
 			edge.vertex_from = vertex_from_id;
 			edge.vertex_to = results.getLong("vertex_to_id");
 			edge.value = results.getString("value");
 			edges.add(edge);
 		}
 
 		results.close();
 		ps_outgoing_edges_with_property.close();
 
 		return edges;
 	}
 
 	public List<EdgeWithProperty> getIncomingEdgesWithProperty(long vertex_to_id, String name) throws SQLException
 	{
 		final PreparedStatement ps_incoming_edges_with_property = con.prepareStatement(
 				"SELECT value, id, vertex_from_id FROM edge_properties, edges " +
 						"WHERE " +
 						"edge_id IN (SELECT id FROM edges WHERE vertex_to_id = ?) AND " +
 						"edges.id = edge_id AND " +
 				"name = ?");
 
 		ps_incoming_edges_with_property.setLong(1, vertex_to_id);
 		ps_incoming_edges_with_property.setString(2, name);
 
 		final List<EdgeWithProperty> edges = new LinkedList<EdgeWithProperty>();
 		final ResultSet results = ps_incoming_edges_with_property.executeQuery();
 		while(results.next())
 		{
 			EdgeWithProperty edge = new EdgeWithProperty();
 			edge.id = results.getLong("id");
 			edge.vertex_from = vertex_to_id;
 			edge.vertex_to = results.getLong("vertex_to_id");
 			edge.value = results.getString("value");
 			edges.add(edge);
 		}
 
 		results.close();
 		ps_incoming_edges_with_property.close();
 
 		return edges;
 	}
 
 
 	public List<Edge> getEdgesByPropertyValue(String name, String value) throws SQLException
 	{
 		PreparedStatement ps = con.prepareStatement("SELECT edge_id, vertex_from_id, vertex_to_id FROM edge_properties, edges WHERE name = ? AND value = ? AND edge_id = edges.id");
 		ps.setString(1, name);
 		ps.setString(2, value);
 
 		List<Edge> edges = new LinkedList<Edge>();
 		ResultSet results = ps.executeQuery();
 		while(results.next())
 		{
 			Edge edge = new Edge(this);
 			edge.id = results.getLong("edge_id");
 			edge.vertex_from = results.getLong("vertex_from_id");
 			edge.vertex_to = results.getLong("vertex_to_id");
 			edges.add(edge);
 		}
 
 		results.close();
 		ps.close();
 
 		return edges;
 	}
 
 	public long getEdgeByVerticesAndProperty(long vertex_from, long vertex_to, String name) throws SQLException
 	{
 		PreparedStatement ps = con.prepareStatement("SELECT edge_id FROM edge_properties, edges WHERE vertex_from_id = ? AND vertex_to_id = ? AND name = ? AND edge_id = edges.id");
 		ps.setLong(1, vertex_from);
 		ps.setLong(2, vertex_to);
 		ps.setString(3, name);
 
 		ResultSet results = ps.executeQuery();
 		if (results.next())
 		{
 			return results.getLong("edge_id");
 		}
 		else
 		{
 			throw new SQLException("Could not retrieve edge with these vertices and property name");
 		}
 	}
 
 	public String getEdgeValueByVerticesAndProperty(long vertex_from, long vertex_to, String name) throws SQLException
 	{
 		PreparedStatement ps = con.prepareStatement("SELECT value FROM edge_properties, edges WHERE vertex_from_id = ? AND vertex_to_id = ? AND name = ? AND edge_id = edges.id");
 		ps.setLong(1, vertex_from);
 		ps.setLong(2, vertex_to);
 		ps.setString(3, name);
 
 		ResultSet results = ps.executeQuery();
 		if (results.next())
 		{
 			return results.getString("value");
 		}
 		else
 		{
 			throw new SQLException("Could not retrieve edge value with these vertices and property name");
 		}
 	}
 
 
 
 	public List<Long> getVertex(String name, String value) throws SQLException
 	{
 		PreparedStatement ps = con.prepareStatement("SELECT vertex_id FROM vertex_properties WHERE name = ? AND value = ?");
 		ps.setString(1, name);
 		ps.setString(2, value);
 
 		List<Long> vertices = new LinkedList<Long>();
 		ResultSet results = ps.executeQuery();
 		while(results.next())
 		{
 			vertices.add(results.getLong("vertex_id"));
 		}
 
 		results.close();
 		ps.close();
 
 		return vertices;
 	}
 
 	public List<Long> getVertexWithPropertyValueLargerThan(String name, long value) throws SQLException
 	{
 		PreparedStatement ps = con.prepareStatement("SELECT vertex_id FROM vertex_properties WHERE name = ? AND value_number > ?");
 		ps.setString(1, name);
 		ps.setLong(2, value);
 
 		List<Long> vertices = new LinkedList<Long>();
 		ResultSet results = ps.executeQuery();
 		while(results.next())
 		{
 			vertices.add(results.getLong("vertex_id"));
 		}
 
 		results.close();
 		ps.close();
 
 		return vertices;
 	}
 
 	/**
 	 * lookup the properties for this vertex and add them to the object
 	 * @param vertex_id
 	 * @return
 	 * @throws SQLException
 	 */
 
 	public Map<String, List<String>> getVertexProperties(long vertex_id) throws SQLException
 	{
 		PreparedStatement ps_props = con.prepareStatement("SELECT * FROM vertex_properties WHERE vertex_id = ? ORDER BY name");
 		ps_props.setLong(1, vertex_id);
 		ResultSet propertiesValues = ps_props.executeQuery();
 
 		Map<String, List<String>> properties = new HashMap<String, List<String>>();
 		while(propertiesValues.next())
 		{
 			String name = propertiesValues.getString("name");
 			String value = propertiesValues.getString("value");
 			if (!properties.containsKey(name))	properties.put(name, new LinkedList<String>());
 			properties.get(name).add(value);
 		}
 
 		propertiesValues.close();
 		ps_props.close();
 
 		return properties;
 	}
 
 
 	public long insertEdge(long vertex_from_id, long vertex_to_id) throws SQLException {
 
 		final PreparedStatement ps = con.prepareStatement("INSERT INTO edges (vertex_from_id, vertex_to_id) VALUES (?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
 
 		ps.setLong(1, vertex_from_id);
 		ps.setLong(2, vertex_to_id);
 		ps.executeUpdate();
 
 		ResultSet results =  ps.getGeneratedKeys(); 
 
 		if (results.next())
 		{
 			return results.getLong(1);	
 		}
 		throw new SQLException("Could not retrieve latest edges.id");
 	}
 
 	public void removeVertex(long vertex_id) throws SQLException
 	{
 		final PreparedStatement ps = con.prepareStatement("DELETE FROM vertices WHERE id = ?");
 		ps.setLong(1, vertex_id);
 		ps.execute();
 		ps.close();
 	}
 
 	public void removePropertyForAllVertices(String name) throws SQLException
 	{
 		PreparedStatement ps = con.prepareStatement("DELETE FROM vertex_properties WHERE name = ?");
 		ps.setString(1, name);
 		ps.execute();
 		ps.close();
 	}
 
 	public void removeVertexProperty(long vertex_id, String name) throws SQLException
 	{
 		PreparedStatement ps = con.prepareStatement("DELETE FROM vertex_properties WHERE vertex_id = ? AND name = ?");
 		ps.setLong(1, vertex_id);
 		ps.setString(2, name);
 		ps.execute();
 		ps.close();
 	}
 
 	public void removeVertexPropertyValue(long vertex_id, String name, String value) throws SQLException
 	{
 		final PreparedStatement ps = con.prepareStatement("DELETE FROM vertex_properties WHERE vertex_id = ? AND name = ? AND value = ?");
 		ps.setLong(1, vertex_id);
 		ps.setString(2, name);
 		ps.setString(3, value);
 		ps.execute();
 		ps.close();
 	}
 
 
 	public void removeEdge(long edge_id) throws SQLException
 	{
 		final PreparedStatement ps = con.prepareStatement("DELETE FROM edges WHERE id = ?");
 		ps.setLong(1, edge_id);
 		ps.execute();
 		ps.close();
 	}
 
 
 	public long countVertices() throws SQLException
 	{
 		final PreparedStatement ps_props = con.prepareStatement("SELECT COUNT(*) AS vertex_count FROM vertices");
 		final ResultSet count = ps_props.executeQuery();
 
 		if (count.next())
 		{
 			return count.getLong("vertex_count");
 		}
 		else
 		{
 			return -1l;
 		}
 	}
 
 	public long countEdges() throws SQLException
 	{
 		final PreparedStatement ps_props = con.prepareStatement("SELECT COUNT(*) AS edge_count FROM edges");
 		final ResultSet count = ps_props.executeQuery();
 
 		if (count.next())
 		{
 			return count.getLong("edge_count");
 		}
 		else
 		{
 			return -1l;
 		}
 	}
 }
