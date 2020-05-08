 package no.runsafe.framework.internal.database;
 
 import no.runsafe.framework.api.database.IDatabase;
 import no.runsafe.framework.api.database.ISchemaChanges;
 import no.runsafe.framework.api.database.ITransaction;
import no.runsafe.framework.api.IOutput;
 import org.picocontainer.Startable;
 
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * This class handles database schema updates
  */
 public final class SchemaUpdater implements Startable
 {
 	/**
 	 * This constructor is required for plugins that do not provide any ISchemaChanges objects
 	 *
 	 * @param db     The database handler
 	 * @param output The console to log information to
 	 */
 	public SchemaUpdater(IDatabase db, IOutput output)
 	{
 		this(db, output, null);
 	}
 
 	/**
 	 * @param db             The database handler
 	 * @param output         The console to log information to
 	 * @param schemaUpdaters Schema updaters
 	 */
 	public SchemaUpdater(IDatabase db, IOutput output, ISchemaChanges[] schemaUpdaters)
 	{
 		this.schemaUpdaters = schemaUpdaters;
 		database = db;
 		console = output;
 		database.Execute(
 			"CREATE TABLE IF NOT EXISTS runsafe_schema (" +
 				"`table` varchar(255) NOT NULL," +
 				"`revision` int(11) NOT NULL," +
 				"PRIMARY KEY (`table`)" +
 				")"
 		);
 	}
 
 	public int getRevision(String table)
 	{
		Row row = database.QueryRow("SELECT `revision` FROM runsafe_schema WHERE `table`=?", table);
 		if (row == null)
 			return 0;
 		return row.Integer("revision");
 	}
 
 	public void setRevision(String table, int revision)
 	{
 		database.Update(
 			"INSERT INTO runsafe_schema (`table`,`revision`) VALUES (?,?)" +
 				"ON DUPLICATE KEY UPDATE `revision`=VALUES(`revision`)",
 			table, revision
 		);
 	}
 
 	@Override
 	public void start()
 	{
 		if (schemaUpdaters != null && schemaUpdaters.length > 0)
 			executeSchemaChanges();
 	}
 
 	@Override
 	public void stop()
 	{
 	}
 
 	private void executeSchemaChanges()
 	{
 		for (ISchemaChanges changes : schemaUpdaters)
 		{
 			int revision = getRevision(changes.getTableName());
 			HashMap<Integer, List<String>> queries = changes.getSchemaUpdateQueries();
 			for (Integer rev : queries.keySet())
 			{
 				if (rev > revision)
 				{
 					revision = executeSchemaChanges(changes.getTableName(), revision, rev, queries.get(rev));
 
 					// Update failed, abort now
 					if (revision < rev)
 						break;
 				}
 			}
 			setRevision(changes.getTableName(), revision);
 		}
 	}
 
 	private int executeSchemaChanges(String tableName, int oldRevision, int newRevision, List<String> queries)
 	{
 		String sqlQuery = null;
 		ITransaction transaction = database.Isolate();
 		console.write(String.format("Updating table %s from revision %d to revision %d", tableName, oldRevision, newRevision));
 		for (String sql : queries)
 		{
 			if (!transaction.Execute(sql))
 			{
 				console.writeColoured("Failed executing query:\n%s", sqlQuery);
 				console.writeColoured("&cRolling back transaction..");
 				transaction.Rollback();
 				return oldRevision;
 			}
 		}
 		transaction.Commit();
 		return newRevision;
 	}
 
 
 	private final IDatabase database;
 	private final IOutput console;
 	private final ISchemaChanges[] schemaUpdaters;
 }
