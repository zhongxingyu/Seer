 package no.runsafe.framework.internal.database.jdbc;
 
 import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
 import no.runsafe.framework.api.database.IDatabase;
 import no.runsafe.framework.api.database.ITransaction;
 import no.runsafe.framework.api.log.IConsole;
 import no.runsafe.framework.api.log.IDebug;
 import no.runsafe.framework.internal.configuration.FrameworkConfiguration;
 
 import javax.annotation.Nullable;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.logging.Level;
 
 /**
  * Framework for handling database access
  */
 public final class Database extends QueryExecutor implements IDatabase
 {
 	public Database(IDebug output, IConsole console, FrameworkConfiguration config)
 	{
 		super(console, output);
 		String url = config.getConfigValueAsString("database.url");
 		String user = config.getConfigValueAsString("database.username");
 		String password = config.getConfigValueAsString("database.password");
 
 		source = new MysqlConnectionPoolDataSource();
 		source.setUrl(url);
 		source.setUser(user);
 		source.setPassword(password);
 		source.setAutoReconnectForConnectionPools(true);
 
 		testConnection();
 	}
 
 	@Override
 	@Nullable
 	public ITransaction isolate()
 	{
 		try
 		{
 			Connection transaction = source.getConnection();
 			if (transaction == null)
 				return null;
 			transaction.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
 			transaction.setAutoCommit(false);
 			return new Transaction(output, debugger, transaction);
 		}
 		catch (SQLException e)
 		{
 			output.outputToConsole(e.getMessage(), Level.SEVERE);
 			return null;
 		}
 	}
 
 	@Nullable
 	@Override
 	protected Connection getConnection()
 	{
 		try
 		{
 			return source.getConnection();
 		}
 		catch (SQLException e)
 		{
 			output.logException(e);
 			return null;
 		}
 	}
 
 	@Override
 	void close(PreparedStatement statement)
 	{
 		if (statement == null)
 			return;
 
 		try
 		{
 			Connection conn = statement.getConnection();
 			statement.close();
 			conn.close();
 		}
 		catch (SQLException e)
 		{
 			output.logException(e);
 		}
 	}
 
 	private void testConnection()
 	{
 		try
 		{
			if (queryRow("SELECT VERSION()") == null)
 				output.logFatal("Unable to connect to MySQL - Verify framework configuration!");
 		}
 		catch (Exception e)
 		{
 			output.logException(e);
 			output.logFatal("An error occurred while testing the MySQL connection - Verify framework configuration!");
 		}
 	}
 
 	private final MysqlConnectionPoolDataSource source;
 }
