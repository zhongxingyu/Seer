 package org.oobium.persist.migrate.db;
 
 import static org.oobium.persist.migrate.db.defs.Column.BINARY;
 import static org.oobium.persist.migrate.db.defs.Column.BOOLEAN;
 import static org.oobium.persist.migrate.db.defs.Column.DATE;
 import static org.oobium.persist.migrate.db.defs.Column.DATESTAMPS;
 import static org.oobium.persist.migrate.db.defs.Column.DECIMAL;
 import static org.oobium.persist.migrate.db.defs.Column.DOUBLE;
 import static org.oobium.persist.migrate.db.defs.Column.FLOAT;
 import static org.oobium.persist.migrate.db.defs.Column.INTEGER;
 import static org.oobium.persist.migrate.db.defs.Column.LONG;
 import static org.oobium.persist.migrate.db.defs.Column.STRING;
 import static org.oobium.persist.migrate.db.defs.Column.TEXT;
 import static org.oobium.persist.migrate.db.defs.Column.TIME;
 import static org.oobium.persist.migrate.db.defs.Column.TIMESTAMP;
 import static org.oobium.persist.migrate.db.defs.Column.TIMESTAMPS;
 import static org.oobium.utils.StringUtils.tableName;
 import static org.oobium.utils.StringUtils.tableNames;
 
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Map;
 
 import org.oobium.logging.Logger;
 import org.oobium.persist.migrate.Migration;
 import org.oobium.persist.migrate.MigrationService;
 import org.oobium.persist.migrate.db.defs.Change;
 import org.oobium.persist.migrate.db.defs.Column;
 import org.oobium.persist.migrate.db.defs.Index;
 import org.oobium.persist.migrate.db.defs.Table;
 import org.oobium.persist.migrate.db.defs.changes.AddColumn;
 import org.oobium.persist.migrate.db.defs.changes.AddForeignKey;
 import org.oobium.persist.migrate.db.defs.changes.AddIndex;
 import org.oobium.persist.migrate.db.defs.changes.RemoveColumn;
 import org.oobium.persist.migrate.db.defs.changes.RemoveForeignKey;
 import org.oobium.persist.migrate.db.defs.changes.RemoveIndex;
 import org.oobium.persist.migrate.db.defs.changes.Rename;
 import org.oobium.persist.migrate.db.defs.columns.ForeignKey;
 
 public abstract class AbstractDbMigration implements Migration {
 
 	protected Logger logger;
 	private DbMigrationService service;
 	
 
 	public Change add(String type, String column) {
 		return add(type, column, null);
 	}
 
 	public Change add(String type, String column, Map<String, ? extends Object> options) {
 		return new AddColumn(type, column, options);
 	}
 
 	public Change addBinary(String name) {
 		return add(BINARY, name);
 	}
 	
 	public Change addBinary(String name, Map<String, ? extends Object> options) {
 		return add(BINARY, name, options);
 	}
 	
 	public Change addBoolean(String name) {
 		return add(BOOLEAN, name);
 	}
 	
 	public Change addBoolean(String name, Map<String, ? extends Object> options) {
 		return add(BOOLEAN, name, options);
 	}
 
 	public Change addDate(String name) {
 		return add(DATE, name);
 	}
 	
 	public Change addDate(String name, Map<String, ? extends Object> options) {
 		return add(DATE, name, options);
 	}
 	
 	public Change addDatestamps() {
     	return add(DATESTAMPS, null);
     }
 	
 	public Change addDecimal(String name) {
 		return add(DECIMAL, name);
 	}
 	
 	public Change addDecimal(String name, Map<String, ? extends Object> options) {
 		return add(DECIMAL, name, options);
 	}
 
 	public Change addFloat(String name) {
 		return add(FLOAT, name);
 	}
 	
 	public Change addFloat(String name, Map<String, ? extends Object> options) {
 		return add(FLOAT, name, options);
 	}
 	
 	public Change addForeignKey(String column, String reference) {
 		return addForeignKey(column, reference, null);
 	}
 	
 	public Change addForeignKey(String column, String reference, Map<String, ? extends Object> options) {
 		return new AddForeignKey(column, reference, options);
 	}
 	
     public Change addIndex(String...columns) {
     	return new AddIndex(columns, false);
     }
     
     public Change addInteger(String name) {
 		return add(INTEGER, name);
 	}
     
     public Change addInteger(String name, Map<String, ? extends Object> options) {
 		return add(INTEGER, name, options);
 	}
     
     public Change addString(String name) {
 		return add(STRING, name);
 	}
     
     public Change addString(String name, Map<String, ? extends Object> options) {
 		return add(STRING, name, options);
 	}
     
     public Change addText(String name) {
 		return add(TEXT, name);
 	}
     
     public Change addText(String name, Map<String, ? extends Object> options) {
 		return add(TEXT, name, options);
 	}
 
     public Change addTime(String name) {
 		return add(TIME, name);
 	}
 
     public Change addTime(String name, Map<String, ? extends Object> options) {
 		return add(TIME, name, options);
 	}
     
     public Change addTimestamp(String name) {
 		return add(TIMESTAMP, name);
 	}
 
 	public Change addTimestamp(String name, Map<String, ? extends Object> options) {
 		return add(TIMESTAMP, name, options);
 	}
 
     public Change addTimestamps() {
     	return add(TIMESTAMPS, null);
     }
 
     public Change addUniqueIndex(String...columns) {
     	return new AddIndex(columns, true);
     }
 
     public Column Binary(String name) {
 		return Column(BINARY, name);
 	}
 
     public Column Binary(String name, Map<String, ? extends Object> options) {
 		return Column(BINARY, name, options);
 	}
 
     public Column Boolean(String name) {
 		return Column(BOOLEAN, name);
 	}
 
     public Column Boolean(String name, Map<String, ? extends Object> options) {
 		return Column(BOOLEAN, name, options);
 	}
 
     public Change change(String column, String type, Map<String, ? extends Object> options) {
     	// TODO
     	throw new UnsupportedOperationException("not yet implemented");
     }
 
     public void changeColumn(String table, String column, String type, Map<String, ? extends Object> options) {
     	// TODO
     	throw new UnsupportedOperationException("not yet implemented");
     }
 
     public Change changeDefault(String column, String newDefault) {
     	// TODO
     	throw new UnsupportedOperationException("not yet implemented");
     }
 
 	public Table changeTable(String name, Change...changes) throws SQLException {
 		Table table = new Table(getService(), name, null);
 		for(Change change : changes) {
 			table.add(change);
 		}
 		table.update();
 		return table;
 	}
     
 	public Column Column(String type, String name) {
 		return Column(type, name, null);
 	}
 
     public Column Column(String type, String name, Map<String, ? extends Object> options) {
 		return new Column(type, name, options);
 	}
 
     public boolean columnExists(String table, String column) {
     	// TODO
     	throw new UnsupportedOperationException("not yet implemented");
 	}
 
     public void createColumn(String tableName, String column, String type) throws SQLException {
 		createColumn(tableName, column, type, null);
 	}
     
     public void createColumn(String tableName, String column, String type, Map<String, ? extends Object> options) throws SQLException {
     	Table table = new Table(getService(), tableName, null);
     	table.add(new AddColumn(type, column, options));
     	table.update();
     }
     
     public void createForeignKey(String tableName, String column, String reference) throws SQLException {
     	Table table = new Table(getService(), tableName, null);
     	table.addForeignKey(column, reference);
     	table.update();
     }
 
     public void createIndex(String tableName, String...columns) throws SQLException {
     	Table table = new Table(getService(), tableName, null);
     	table.addIndex(columns);
     	table.update();
 	}
 
     public void dropJoinTable(String table1, String column1, String table2, String column2) throws SQLException {
 		String name = tableName(table1, column1, table2, column2);
 		Table table = new Table(getService(), name, null);
 		table.destroy();
     }
     
     public void createJoinTable(String table1, String column1, String table2, String column2) throws SQLException {
 		String name = tableName(table1, column1, table2, column2);
 		String[] references = tableNames(table1, table2);
 
 		Table table = new Table(getService(), name, null);
 		table.setPrimaryKey(false);
 		table.add(ForeignKey("a", references[1]));
 		table.add(ForeignKey("b", references[0]));
 		table.add(Index("a"));
 		table.add(Index("b"));
 		table.create();
 	}
 
     public void createJoinTable(Table table1, String column1, Table table2, String column2) throws SQLException {
     	createJoinTable(table1.name, column1, table2.name, column2);
     }
     
     public Table createSessions() throws SQLException {
     	return createTable("sessions",
 	    			String("uuid"),
	    			String("data"),
 	    			Timestamp("expiration")
 	    		);
     }
     
     public Table createTable(String name, Column...elements) throws SQLException {
 		return createTable(name, null, elements);
 	}
 
     public Table createTable(String name, Map<String, ? extends Object> options, Column...elements) throws SQLException {
 		Table table = new Table(getService(), name, options);
 		for(Column element : elements) {
 			table.add(element);
 		}
 		table.create();
 		return table;
 	}
 
     public void createUniqueIndex(String tableName, String...columns) throws SQLException {
     	Table table = new Table(getService(), tableName, null);
     	table.addUniqueIndex(columns);
     	table.update();
 	}
     
     public Column Date(String name) {
 		return Column(DATE, name);
 	}
     
     public Column Date(String name, Map<String, ? extends Object> options) {
 		return Column(DATE, name, options);
 	}
     
     public Column Datestamps() {
     	return Column(DATESTAMPS, null, null);
     }
     
     public Column Decimal(String name) {
 		return Column(DECIMAL, name);
 	}
     
     public Column Decimal(String name, Map<String, ? extends Object> options) {
 		return Column(DECIMAL, name, options);
 	}
     
     public void destroyColumn(String tableName, String column) throws SQLException {
     	Table table = new Table(getService(), tableName, null);
     	table.add(new RemoveColumn(column));
     	table.update();
 	}
 
 	public void destroyColumns(String tableName, String...columns) throws SQLException {
     	Table table = new Table(getService(), tableName, null);
     	for(String column : columns) {
     		table.add(new RemoveColumn(column));
     	}
     	table.update();
 	}
 
     public void destroyForeignKey(String tableName, String column) throws SQLException {
     	Table table = new Table(getService(), tableName, null);
     	table.add(new RemoveForeignKey(column));
     	table.update();
 	}
     
     public void destroyIndex(String tableName, String...columns) throws SQLException {
     	Table table = new Table(getService(), tableName, null);
     	table.add(new RemoveIndex(columns));
     	table.update();
 	}
     
 	public void destroyTable(String name) throws SQLException {
 		Table table = new Table(getService(), name, null);
 		table.destroy();
 	}
 	
 	public Column Double(String name) {
     	return Column(DOUBLE, name);
     }
 	
 	public Column Double(String name, Map<String, ? extends Object> options) {
     	return Column(DOUBLE, name, options);
     }
 	
 	@Override
 	public abstract void down() throws Exception;
 	
 	public void dropSessions() throws SQLException {
 		dropTable("sessions");
 	}
 	
 	/**
 	 * Synonym for {@link #destroyTable(String)}
 	 */
 	public void dropTable(String name) throws SQLException {
 		destroyTable(name);
 	}
 	
 	public List<Map<String, Object>> executeQuery(String sql, Object...values) throws SQLException {
 		return getService().executeQuery(sql, values);
 	}
 	
 	public List<List<Object>> executeQueryLists(String sql, Object...values) throws SQLException {
 		return getService().executeQueryLists(sql, values);
 	}
 	
 	public Object executeQueryValue(String sql, Object...values) throws SQLException {
 		return getService().executeQueryValue(sql, values);
 	}
 	
 	public int executeUpdate(String sql, Object...values) throws SQLException {
 		return getService().executeUpdate(sql, values);
 	}
 
 	public Column Float(String name) {
 		return Column(FLOAT, name);
 	}
 	
 	public Column Float(String name, Map<String, ? extends Object> options) {
 		return Column(FLOAT, name, options);
 	}
 
 	public Column ForeignKey(String column, String reference) {
     	return ForeignKey(column, reference, null);
     }
 
 	public Column ForeignKey(String column, String reference, Map<String, ? extends Object> options) {
     	return new ForeignKey(column, reference, options);
     }
 
 	private DbMigrationService getService() {
 		return service;
 	}
 	
 	@Override
 	public void setLogger(Logger logger) {
 		this.logger = logger;
 	}
 	
 	@Override
 	public void setService(MigrationService service) {
 		this.service = (DbMigrationService) service;
 	}
 
 	public Index Index(String...columns) {
     	return new Index(columns, false);
     }
 	
 	public Index Index(String column, boolean unique) {
     	return new Index(column, unique);
     }
 	
 	public boolean indexExists(String table, String index) {
     	// TODO
     	throw new UnsupportedOperationException("not yet implemented");
 	}
 
 	public Column Integer(String name) {
 		return Column(INTEGER, name);
 	}
 	
 	public Column Integer(String name, Map<String, ? extends Object> options) {
 		return Column(INTEGER, name, options);
 	}
 
 	public Column Long(String name) {
 		return Column(LONG, name);
 	}
 	
 	public Column Long(String name, Map<String, ? extends Object> options) {
 		return Column(LONG, name, options);
 	}
 
 	public Change remove(String column) {
     	return new RemoveColumn(column);
     }
 	
 	public Change removeDatestamps() {
     	return remove(DATESTAMPS);
     }
 	
 	public Change removeForeignKey(String column) {
     	return new RemoveForeignKey(column);
     }
 	
 	public Change removeIndex(String...columns) {
     	return new RemoveIndex(columns);
     }
 
 	public Change removeTimestamps() {
     	return remove(TIMESTAMPS);
     }
 	
 	public Change rename(String to) {
 		return new Rename(to);
 	}
 	
 	public Change rename(String from, String to) {
 		return new Rename(from, to);
 	}
 	
 	public void rename(String tableName, String column, String to) throws SQLException {
 		Table table = new Table(getService(), tableName, null);
 		table.rename(column, to);
 		table.update();
     }
 	
 	public void renameTable(String from, String to) throws SQLException {
 		Table table = new Table(getService(), from, null);
 		table.rename(to);
 		table.update();
 	}
 	
     public Column String(String name) {
 		return String(name, null);
 	}
 
     public Column String(String name, Map<String, ? extends Object> options) {
 		return new Column(STRING, name, options);
 	}
 
 	public Column Text(String name) {
 		return Column(TEXT, name);
 	}
 
     public Column Text(String name, Map<String, ? extends Object> options) {
 		return Column(TEXT, name, options);
 	}
 
     public Column Time(String name) {
 		return Column(TIME, name);
 	}
 
     public Column Time(String name, Map<String, ? extends Object> options) {
 		return Column(TIME, name, options);
 	}
 
     public Column Timestamp(String name) {
 		return Column(TIMESTAMP, name);
 	}
 
     public Column Timestamp(String name, Map<String, ? extends Object> options) {
 		return Column(TIMESTAMP, name, options);
 	}
 
     public Column Timestamps() {
 		return Column(TIMESTAMPS, null);
     }
 
     public Index UniqueIndex(String...columns) {
     	return new Index(columns, true);
     }
 
 	@Override
 	public abstract void up() throws Exception;
     
 }
