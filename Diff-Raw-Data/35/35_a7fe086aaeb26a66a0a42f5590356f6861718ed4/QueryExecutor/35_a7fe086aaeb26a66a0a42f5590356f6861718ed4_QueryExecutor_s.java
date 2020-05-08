 package no.runsafe.framework.internal.database.jdbc;
 
 import no.runsafe.framework.api.database.IQueryExecutor;
 import no.runsafe.framework.internal.database.Row;
 import no.runsafe.framework.internal.database.Set;
 import no.runsafe.framework.internal.database.Value;
import no.runsafe.framework.api.IOutput;
 import org.joda.time.DateTime;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 abstract class QueryExecutor implements IQueryExecutor
 {
 	QueryExecutor(IOutput output)
 	{
 		this.output = output;
 	}
 
 	@Override
	public Set Query(String query, Object... params)
 	{
 		try
 		{
 			Connection conn = getConnection();
 			PreparedStatement statement = conn.prepareStatement(query);
 			for (int i = 0; i < params.length; i++)
 				statement.setObject(i + 1, params[i]);
 			return getSet(statement);
 		}
 		catch (SQLException e)
 		{
 			output.logException(e);
 			return null;
 		}
 	}
 
 	@Override
	public Row QueryRow(String query, Object... params)
 	{
 		try
 		{
 			Connection conn = getConnection();
 			PreparedStatement statement = conn.prepareStatement(query);
 			for (int i = 0; i < params.length; i++)
 				statement.setObject(i + 1, params[i]);
 			Set set = getSet(statement);
 			if (set == null || set.isEmpty())
 				return null;
 			return set.get(0);
 		}
 		catch (SQLException e)
 		{
 			output.logException(e);
 			return null;
 		}
 	}
 
 	@Override
	public List<Value> QueryColumn(String query, Object... params)
 	{
 		try
 		{
 			PreparedStatement statement = prepare(query);
 			setParams(statement, params);
 			return getValues(statement);
 		}
 		catch (SQLException e)
 		{
 			output.logException(e);
 			return null;
 		}
 	}
 
 	@Override
 	public boolean Execute(String query, Object... params)
 	{
 		try
 		{
 			PreparedStatement statement = prepare(query);
 			setParams(statement, params);
 			output.finer("Running SQL: %s", statement);
 			statement.execute();
 			return true;
 		}
 		catch (SQLException e)
 		{
 			output.logException(e);
 			return false;
 		}
 	}
 
 	@Override
 	public int Update(String query, Object... params)
 	{
 		try
 		{
 			PreparedStatement statement = prepare(query);
 			setParams(statement, params);
 			output.finer("Running SQL: %s", statement);
 			return statement.executeUpdate();
 		}
 		catch (SQLException e)
 		{
 			output.logException(e);
 			return -1;
 		}
 	}
 
 	protected Set getSet(PreparedStatement statement) throws SQLException
 	{
 		output.finer("Running SQL: %s", statement);
 		ResultSet result = statement.executeQuery();
 		if (!result.first())
 			return null;
 		ResultSetMetaData meta = result.getMetaData();
 		int cols = meta.getColumnCount();
 		if (cols == 0)
 			return null;
 		ArrayList<Row> results = new ArrayList<Row>();
 		while (!result.isAfterLast())
 		{
 			HashMap<String, Object> row = new HashMap<String, Object>();
 			for (int i = 0; i < cols; ++i)
 				row.put(meta.getColumnName(i + 1), result.getObject(i + 1));
 			results.add(new Row(row));
 			result.next();
 		}
 		return new Set(results);
 	}
 
	protected ArrayList<Value> getValues(PreparedStatement statement) throws SQLException
 	{
 		output.finer("Running SQL: %s", statement);
 		ResultSet result = statement.executeQuery();
 		if (!result.first())
 			return null;
 		ResultSetMetaData meta = result.getMetaData();
 		int cols = meta.getColumnCount();
 		if (cols == 0)
 			return null;
		ArrayList<Value> results = new ArrayList<Value>();
 		while (!result.isAfterLast())
 		{
 			results.add(new Value(result.getObject(1)));
 			result.next();
 		}
 		return results;
 	}
 
 	protected PreparedStatement prepare(String query) throws SQLException
 	{
 		Connection conn = getConnection();
 		return conn.prepareStatement(query);
 	}
 
 	protected void setParams(PreparedStatement statement, Object... params) throws SQLException
 	{
 		for (int i = 0; i < params.length; i++)
 		{
 			if (params[i] instanceof DateTime)
 				statement.setObject(i + 1, new Timestamp(((DateTime) params[i]).getMillis()));
 			else
 				statement.setObject(i + 1, params[i]);
 		}
 	}
 
 	protected abstract Connection getConnection();
 
 	protected final IOutput output;
 }
