 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.persist.migrate.db;
 
 import static org.oobium.persist.migrate.defs.Column.*;
 import static org.oobium.persist.Relation.CASCADE;
 import static org.oobium.persist.Relation.NO_ACTION;
 import static org.oobium.persist.Relation.RESTRICT;
 import static org.oobium.persist.Relation.SET_DEFAULT;
 import static org.oobium.persist.Relation.SET_NULL;
 import static org.oobium.utils.StringUtils.join;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.oobium.logging.Logger;
 import org.oobium.persist.PersistService;
 import org.oobium.persist.db.DbPersistService;
 import org.oobium.persist.migrate.AbstractMigrationService;
 import org.oobium.persist.migrate.defs.Change;
 import org.oobium.persist.migrate.defs.Column;
 import org.oobium.persist.migrate.defs.Index;
 import org.oobium.persist.migrate.defs.Table;
 import org.oobium.persist.migrate.defs.changes.AddColumn;
 import org.oobium.persist.migrate.defs.changes.AddForeignKey;
 import org.oobium.persist.migrate.defs.changes.AddIndex;
 import org.oobium.persist.migrate.defs.changes.RemoveColumn;
 import org.oobium.persist.migrate.defs.changes.RemoveForeignKey;
 import org.oobium.persist.migrate.defs.changes.RemoveIndex;
 import org.oobium.persist.migrate.defs.changes.Rename;
 import org.oobium.persist.migrate.defs.columns.ForeignKey;
 import org.oobium.persist.migrate.defs.columns.PrimaryKey;
 
 public abstract class DbMigrationService extends AbstractMigrationService {
 
 	protected DbPersistService persistor;
 
 	public DbMigrationService() {
 		super();
 	}
 	
 	public DbMigrationService(String client, Logger logger) {
 		super(client, logger);
 	}
 
 	public void addColumn(Table table, AddColumn change) throws SQLException {
 		exec(
 				getAddColumnSql(table, change.column),
 				"adding column " + change.column.name + " to table " + table.name,
 				"added column " + change.column.name + " to table " + table.name
 			);
 	}
 	
 	public void addForeignKey(Table table, AddForeignKey change) throws SQLException {
 		exec(
 				getCreateForeignKeySql(table, change.fk),
 				"creating foreign key (" + change.fk.column + " -> " + change.fk.reference + ") for table " + table.name,
 				"created foreign key (" + change.fk.column + " -> " + change.fk.reference + ") for table " + table.name
 			);
 	}
 
 	public void addIndex(Table table, AddIndex change) throws SQLException {
 		createIndex(table, change.index);
 	}
 
 	@Override
 	public void create(Table table) throws SQLException {
 		createTable(table);
 		for(Index index : table.getIndexes()) {
 			createIndex(table, index);
 		}
 	}
 
 	@Override
 	public void createDatabase() throws SQLException {
 		logger.info("Creating database...");
 		persistor.createDatabase(client);
 	}
 
 	protected void createIndex(Table table, Index index) throws SQLException {
 		exec(
 				getCreateIndexSql(table, index),
 				"creating index [" + join(index.columns, ',') + "] for table " + table.name,
 				"created index [" + join(index.columns, ',') + "] for table " + table.name
 			);
 	}
 
 	protected void createTable(Table table) throws SQLException {
 		exec(
 				getCreateTableSql(table),
 				"creating " + table.name,
 				"created " + table.name
 			);
 	}
 	
 	@Override
 	public void drop(Table table) throws SQLException {
 		exec(
 				"DROP TABLE " + table.name,
 				"dropping " + table.name,
 				"dropped " + table.name
 			);
 	}
 	
 	@Override
 	public void dropDatabase() throws SQLException {
 		logger.info("Dropping database...");
 		persistor.dropDatabase(client);
 	}
 
 	protected void exec(String sql, String opener, String closer) throws SQLException {
 		// these are all long running operations compared to string concatenation, so don't worry about checking logging level
 		logger.info(opener + "...");
 		long start = System.currentTimeMillis();
 		
 		Connection connection = persistor.getConnection();
 		Statement stmt = connection.createStatement();
 		try {
 			logger.info(sql);
 			stmt.executeUpdate(sql);
 			long total = System.currentTimeMillis() - start;
 			logger.info(closer + " in " + total + "ms");
 		} finally {
 			try {
 				stmt.close();
 			} catch(SQLException e) {
 				// discard
 			}
 		}
 	}
 
 	@Override
 	public List<Map<String, Object>> executeQuery(String sql, Object...values) throws SQLException {
 		return persistor.executeQuery(sql, values);
 	}
 
 	@Override
 	public List<List<Object>> executeQueryLists(String sql, Object...values) throws SQLException {
 		return persistor.executeQueryLists(sql, values);
 	}
 
 	@Override
 	public Object executeQueryValue(String sql, Object...values) throws SQLException {
 		return persistor.executeQueryValue(sql, values);
 	}
 	
 	@Override
 	public int executeUpdate(String sql, Object...values) throws SQLException {
 		return persistor.executeUpdate(sql, values);
 	}
 	
 	@Override
 	public Table find(String table) {
 		throw new UnsupportedOperationException("not yet implemented");
 	}
 
 	@Override
 	public List<Table> findAll() {
 		throw new UnsupportedOperationException("not yet implemented");
 	}
 
 	protected String getAddColumnSql(Table table, Column column) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("ALTER TABLE ").append(getSqlSafe(table.name));
 		sb.append(" ADD COLUMN ").append(getColumnDefinitionSql(column));
 		return sb.toString();
 	}
 
 	protected String getColumnDefinitionSql(Column column) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(getSqlSafe(column.name));
 		sb.append(' ');
 		sb.append(getSqlType(column.type));
 		if(DECIMAL.equals(getSqlType(column.type))) {
 			String precision = column.options.get("precision", 2).toString();
 			String scale = column.options.get("scale", 8).toString();
 			sb.append("(").append(precision).append(",").append(scale).append(")");
 		}
 		if(column.options.get("unique", false)) {
 			sb.append(" UNIQUE");
 		}
 		if(column.options.get("required", false)) {
 			sb.append(" NOT NULL");
 		}
 		if(column.options.has("default")) {
 			sb.append(" DEFAULT ").append(column.options.get("default"));
 		} else if(column.options.has("primitive")) {
 			sb.append(" DEFAULT ").append(getSqlForPrimitive(column.type));
 		}
 		if(column.options.has("check")) {
 			sb.append(" CHECK(").append(column.options.get("check")).append(")");
 		}
 		return sb.toString();
 	}
 
 	protected abstract String getCreateForeignKeyColumnSql(ForeignKey fk);
 	
 	/**
 	 * Generate the SQL to create a foreign key constraint on an existing column.
 	 * @param table
 	 * @param fk
 	 * @return
 	 */
 	protected String getCreateForeignKeySql(Table table, ForeignKey fk) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("ALTER TABLE ").append(getSqlSafe(table.name)).append(" ADD");
 		sb.append(" CONSTRAINT ").append(getSqlSafe(fk.name));
 		sb.append(" Foreign Key (").append(getSqlSafe(fk.column)).append(")");
		sb.append(" REFERENCES ").append(getSqlSafe(fk.reference)).append(" (id)");
 		switch(fk.options.get("onDelete", -1)) {
 		case CASCADE:		sb.append(" ON DELETE CASCADE");	break;
 		case NO_ACTION:		sb.append(" ON DELETE NO ACTION");	break;
 		case RESTRICT:		sb.append(" ON DELETE RESTRICT");	break;
 		case SET_DEFAULT:	sb.append(" ON DELETE SET DEFAULT");break;
 		case SET_NULL:		sb.append(" ON DELETE SET NULL");	break;
 		}
 		switch(fk.options.get("onUpdate", -1)) {
 		case CASCADE:		sb.append(" ON UPDATE CASCADE");	break;
 		case NO_ACTION:		sb.append(" ON UPDATE NO ACTION");	break;
 		case RESTRICT:		sb.append(" ON UPDATE RESTRICT");	break;
 		case SET_DEFAULT:	sb.append(" ON UPDATE SET DEFAULT");break;
 		case SET_NULL:		sb.append(" ON UPDATE SET NULL");	break;
 		}
 		return sb.toString();
 	}
 
 	protected String getCreateIndexSql(Table table, Index index) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("CREATE ");
 		if(index.unique) {
 			sb.append("UNIQUE ");
 		}
 		sb.append("INDEX ").append(index.name);
 		sb.append(" ON ").append(getSqlSafe(table.name)).append('(');
 		for(int i = 0; i < index.columns.length; i++) {
 			if(i != 0) {
 				sb.append(',');
 			}
 			sb.append(getSqlSafe(index.columns[i]));
 		}
 		sb.append(')');
 		return sb.toString();
 	}
 	
 	protected abstract String getCreatePrimaryKeySql(PrimaryKey pk);
 	
 	protected abstract String getCreateTableOptionsSql(Table table);
 	
 	protected String getCreateTableSql(Table table) {
 		List<Column> columns = table.getColumns();
 		String options = getCreateTableOptionsSql(table);
 
 		StringBuilder sb = new StringBuilder();
 		sb.append("CREATE TABLE ").append(getSqlSafe(table.name)).append('(');
 		if(table.hasPrimaryKey()) {
 			sb.append(getCreatePrimaryKeySql(table.getPrimaryKey()));
			if(!columns.isEmpty() || options != null) sb.append(',');
 		}
 		for(Iterator<Column> iter = columns.iterator(); iter.hasNext(); ) {
 			Column column = iter.next();
 			switch(column.ctype) {
 			case Column:	 sb.append(getColumnDefinitionSql(column));	break;
 			case ForeignKey: sb.append(getCreateForeignKeyColumnSql((ForeignKey) column)); break;
 			}
 			if(iter.hasNext()) sb.append(',');
 		}
 		sb.append(")");
 		if(options != null) {
 			sb.append(' ').append(options);
 		}
 		return sb.toString();
 	}
 	
 	@Override
 	public int getCurrentRevision() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	protected String getRemoveColumnSql(Table table, String column) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("ALTER TABLE ").append(getSqlSafe(table.name));
 		sb.append(" DROP COLUMN ").append(getSqlSafe(column));
 		return sb.toString();
 	}
 	
 	public String getRemoveForeignKeySql(Table table, String name) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("ALTER TABLE ").append(getSqlSafe(table.name)).append(" DROP");
 		sb.append(" Foreign Key ").append(getSqlSafe(name));
 		return sb.toString();
 	}
 
 	protected String getRemoveIndexSql(Table table, String name) {
 		return "ALTER TABLE " + table.name + " DROP INDEX " + name;
 	}
 	
 	protected String getSqlForPrimitive(String type) {
 		return Column.BOOLEAN.equals(type) ? "false" : "0";
 	}
 	
 	protected abstract String getSqlSafe(String rawString);
 
 	protected abstract String getSqlType(String migrationType);
 
 	public void removeColumn(Table table, RemoveColumn change) throws SQLException {
 		exec(
 				getRemoveColumnSql(table, change.column),
 				"removing column " + change.column + " from table " + table.name,
 				"removed column " + change.column + " from table " + table.name
 			);
 	}
 
 	public void removeForeignKey(Table table, RemoveForeignKey change) throws SQLException {
 		exec(
 				getRemoveForeignKeySql(table, change.name),
 				"removing foreign key " + change.name + " from table " + table.name,
 				"removed foreign key " + change.name + " from table " + table.name
 			);
 	}
 
 	public void removeIndex(Table table, RemoveIndex change) throws SQLException {
 		exec(
 				getRemoveIndexSql(table, change.name),
 				"removing index " + change.name + " from table " + table.name,
 				"removed index " + change.name + " from table " + table.name
 			);
 	}
 
 	protected String getRenameColumnSql(Table table, String from, String to) {
 		return "ALTER TABLE " + table.name + " RENAME COLUMN " + from + " TO " + to;
 	}
 	
 	protected String getRenameTableSql(Table table, String to) {
 		return "ALTER TABLE " + table.name + " RENAME TO " + to;
 	}
 	
 	public void renameColumn(Table table, Rename change) throws SQLException {
 		exec(
 				getRenameColumnSql(table, change.from, change.to),
 				"renaming column from " + change.from + " to " + change.to + " in table " + table.name,
 				"renamed column from " + change.from + " to " + change.to + " in table " + table.name
 			);
 	}
 	
 	public void renameTable(Table table, Rename change) throws SQLException {
 		exec(
 				getRenameTableSql(table, change.to),
 				"renaming table from " + table.name + " to " + change.to,
 				"renamed table from " + table.name + " to " + change.to
 			);
 	}
 
 	@Override
 	public void setPersistService(PersistService service) {
 		if(service instanceof DbPersistService) {
 			this.persistor = (DbPersistService) service;
 		} else {
 			throw new IllegalStateException("Migration cannot run without a DbPersistService: " + service);
 		}
 	}
 
 	@Override
 	public void update(Table table) throws SQLException {
 		for(Change change : table.getChanges()) {
 			switch(change.ctype) {
 			case AddColumn:			addColumn(table, 		(AddColumn) change); 		break;
 			case AddForeignKey:		addForeignKey(table,	(AddForeignKey) change); 	break;
 			case AddIndex:			addIndex(table,			(AddIndex) change); 		break;
 //			case ChangeDefault:		changeDefault(table, change); 		break;
 			case RemoveColumn:		removeColumn(table,		(RemoveColumn) change); 	break;
 			case RemoveForeignKey:	removeForeignKey(table,	(RemoveForeignKey) change); break;
 			case RemoveIndex:		removeIndex(table,		(RemoveIndex) change); 		break;
 			case RenameColumn:		renameColumn(table,		(Rename) change); 			break;
 			case RenameTable:		renameTable(table,		(Rename) change); 			break;
 			}
 		}
 	}
 
 }
