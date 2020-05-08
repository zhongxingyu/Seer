 package no.runsafe.framework.internal.database.jdbc;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 import no.runsafe.framework.api.ILocation;
 import no.runsafe.framework.api.IWorld;
 import no.runsafe.framework.api.database.IQueryExecutor;
 import no.runsafe.framework.api.database.IRow;
 import no.runsafe.framework.api.database.ISet;
 import no.runsafe.framework.api.database.IValue;
 import no.runsafe.framework.api.log.IConsole;
 import no.runsafe.framework.api.log.IDebug;
 import no.runsafe.framework.api.player.IPlayer;
 import no.runsafe.framework.internal.database.Row;
 import no.runsafe.framework.internal.database.Set;
 import no.runsafe.framework.internal.database.Value;
 import org.joda.time.DateTime;
 import org.joda.time.ReadableInstant;
 
 import javax.annotation.Nullable;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 abstract class QueryExecutor implements IQueryExecutor
 {
 	QueryExecutor(IConsole output, IDebug debugger)
 	{
 		this.output = output;
 		this.debugger = debugger;
 	}
 
 	@Override
 	public ISet Query(String query, Object... params)
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
 			return Set.Empty;
 		}
 	}
 
 	@Override
 	public IRow QueryRow(String query, Object... params)
 	{
 		try
 		{
 			Connection conn = getConnection();
 			PreparedStatement statement = conn.prepareStatement(query);
 			for (int i = 0; i < params.length; i++)
 				statement.setObject(i + 1, params[i]);
 			ISet set = getSet(statement);
 			if (set.isEmpty())
 				return Row.Empty;
 			return set.get(0);
 		}
 		catch (SQLException e)
 		{
 			output.logException(e);
 			return Row.Empty;
 		}
 	}
 
 	@Override
 	public List<String> QueryStrings(String query, Object... params)
 	{
 		return Lists.transform(
 			QueryColumn(query, params),
 			new Function<IValue, String>()
 			{
 				@Override
 				public String apply(@Nullable IValue value)
 				{
 					assert value != null;
 					return value.String();
 				}
 			}
 		);
 	}
 
 	@Override
 	public List<Integer> QueryIntegers(String query, Object... params)
 	{
 		return Lists.transform(
 			QueryColumn(query, params),
 			new Function<IValue, Integer>()
 			{
 				@Override
 				public Integer apply(@Nullable IValue value)
 				{
 					assert value != null;
 					return value.Integer();
 				}
 			}
 		);
 	}
 
 	@Override
 	public List<Long> QueryLongs(String query, Object... params)
 	{
 		return Lists.transform(
 			QueryColumn(query, params),
 			new Function<IValue, Long>()
 			{
 				@Override
 				public Long apply(@Nullable IValue value)
 				{
 					assert value != null;
 					return value.Long();
 				}
 			}
 		);
 	}
 
 	@Override
 	public List<Double> QueryDoubles(String query, Object... params)
 	{
 		return Lists.transform(
 			QueryColumn(query, params),
 			new Function<IValue, Double>()
 			{
 				@Override
 				public Double apply(@Nullable IValue value)
 				{
 					assert value != null;
 					return value.Double();
 				}
 			}
 		);
 	}
 
 	@Override
 	public List<Float> QueryFloats(String query, Object... params)
 	{
 		return Lists.transform(
 			QueryColumn(query, params),
 			new Function<IValue, Float>()
 			{
 				@Override
 				public Float apply(@Nullable IValue value)
 				{
 					assert value != null;
 					return value.Float();
 				}
 			}
 		);
 	}
 
 	@Override
 	public List<DateTime> QueryDateTimes(String query, Object... params)
 	{
 		return Lists.transform(
 			QueryColumn(query, params),
 			new Function<IValue, DateTime>()
 			{
 				@Override
 				public DateTime apply(@Nullable IValue value)
 				{
 					assert value != null;
 					return value.DateTime();
 				}
 			}
 		);
 	}
 
 	@Override
 	public List<IPlayer> QueryPlayers(String query, Object... params)
 	{
 		return Lists.transform(
 			QueryColumn(query, params),
 			new Function<IValue, IPlayer>()
 			{
 				@Override
 				public IPlayer apply(@Nullable IValue value)
 				{
 					assert value != null;
 					return value.Player();
 				}
 			}
 		);
 	}
 
 	@Override
 	public List<IWorld> QueryWorlds(String query, Object... params)
 	{
 		return Lists.transform(
 			QueryColumn(query, params),
 			new Function<IValue, IWorld>()
 			{
 				@Override
 				public IWorld apply(@Nullable IValue value)
 				{
 					assert value != null;
 					return value.World();
 				}
 			}
 		);
 	}
 
 	@Override
 	public List<ILocation> QueryLocations(String query, Object... params)
 	{
 		return Lists.transform(
 			Query(query, params),
 			new Function<IRow, ILocation>()
 			{
 				@Override
 				public ILocation apply(@Nullable IRow row)
 				{
 					assert row != null;
 					return row.Location();
 				}
 			}
 		);
 	}
 
 	@Override
 	public String QueryString(String query, Object... params)
 	{
 		return QueryValue(query, params).String();
 	}
 
 	@Override
 	public Integer QueryInteger(String query, Object... params)
 	{
 		return QueryValue(query, params).Integer();
 	}
 
 	@Override
 	public Long QueryLong(String query, Object... params)
 	{
 		return QueryValue(query, params).Long();
 	}
 
 	@Override
 	public Double QueryDouble(String query, Object... params)
 	{
 		return QueryValue(query, params).Double();
 	}
 
 	@Override
 	public Float QueryFloat(String query, Object... params)
 	{
 		return QueryValue(query, params).Float();
 	}
 
 	@Override
 	public DateTime QueryDateTime(String query, Object... params)
 	{
 		return QueryValue(query, params).DateTime();
 	}
 
 	@Override
 	public IPlayer QueryPlayer(String query, Object... params)
 	{
 		return QueryValue(query, params).Player();
 	}
 
 	@Override
 	public IWorld QueryWorld(String query, Object... params)
 	{
 		return QueryValue(query, params).World();
 	}
 
 	@Override
 	public ILocation QueryLocation(String query, Object... params)
 	{
 		return QueryRow(query, params).Location();
 	}
 
 	@Override
 	public boolean Execute(String query, Object... params)
 	{
 		try
 		{
 			PreparedStatement statement = prepare(query);
 			setParams(statement, params);
 			debugger.debugFiner("Running SQL: %s", statement);
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
 			debugger.debugFiner("Running SQL: %s", statement);
 			return statement.executeUpdate();
 		}
 		catch (SQLException e)
 		{
 			output.logException(e);
 			return -1;
 		}
 	}
 
 	List<IValue> QueryColumn(String query, Object... params)
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
 			return Lists.newArrayList();
 		}
 	}
 
 	IValue QueryValue(String query, Object... params)
 	{
 		try
 		{
 			Connection conn = getConnection();
 			PreparedStatement statement = conn.prepareStatement(query);
 			for (int i = 0; i < params.length; i++)
 				statement.setObject(i + 1, params[i]);
 			List<IValue> set = getValues(statement);
 			if (set.isEmpty())
 				return Value.Empty;
 			return set.get(0);
 		}
 		catch (SQLException e)
 		{
 			output.logException(e);
 			return Value.Empty;
 		}
 	}
 
 	@SuppressWarnings("MethodWithMultipleLoops")
 	ISet getSet(PreparedStatement statement) throws SQLException
 	{
 		debugger.debugFiner("Running SQL: %s", statement);
 		ResultSet result = statement.executeQuery();
 		if (!result.first())
 			return Set.Empty;
 		ResultSetMetaData meta = result.getMetaData();
 		int cols = meta.getColumnCount();
 		if (cols == 0)
 			return Set.Empty;
 		ArrayList<Row> results = new ArrayList<Row>(1);
 		while (!result.isAfterLast())
 		{
 			HashMap<String, Object> row = new HashMap<String, Object>(cols);
 			for (int i = 0; i < cols; ++i)
 				row.put(meta.getColumnName(i + 1), result.getObject(i + 1));
 			results.add(new Row(row));
 			result.next();
 		}
 		return new Set(results);
 	}
 
 	List<IValue> getValues(PreparedStatement statement) throws SQLException
 	{
 		debugger.debugFiner("Running SQL: %s", statement);
 		ResultSet result = statement.executeQuery();
 		if (!result.first())
 			return Lists.newArrayList();
 		ResultSetMetaData meta = result.getMetaData();
 		int cols = meta.getColumnCount();
 		if (cols == 0)
 			return Lists.newArrayList();
 		List<IValue> results = new ArrayList<IValue>(0);
 		while (!result.isAfterLast())
 		{
 			results.add(new Value(result.getObject(1)));
 			result.next();
 		}
 		return results;
 	}
 
 	PreparedStatement prepare(String query) throws SQLException
 	{
 		Connection conn = getConnection();
 		return conn.prepareStatement(query);
 	}
 
 	static void setParams(PreparedStatement statement, Object... params) throws SQLException
 	{
 		for (int i = 0; i < params.length; i++)
 		{
 			if (params[i] instanceof ReadableInstant)
 				statement.setObject(i + 1, new Timestamp(((ReadableInstant) params[i]).getMillis()));
 			else
 				statement.setObject(i + 1, params[i]);
 		}
 	}
 
 	protected abstract Connection getConnection();
 
 	protected final IConsole output;
 	protected final IDebug debugger;
 }
