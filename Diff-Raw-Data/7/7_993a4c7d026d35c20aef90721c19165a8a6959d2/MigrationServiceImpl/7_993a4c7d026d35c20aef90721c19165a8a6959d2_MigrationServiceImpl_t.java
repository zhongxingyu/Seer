 /* 
  * Copyright 2008 The Corner Team.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package corner.services.migration.impl;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.tapestry5.hibernate.HibernateSessionSource;
 import org.hibernate.Criteria;
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.cfg.Environment;
 import org.hibernate.cfg.Settings;
 import org.hibernate.connection.ConnectionProvider;
 import org.hibernate.dialect.Dialect;
 import org.hibernate.engine.SessionFactoryImplementor;
 import org.hibernate.mapping.Table;
 import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
 import org.hibernate.tool.hbm2ddl.TableMetadata;
 import org.hibernate.util.ArrayHelper;
 import org.slf4j.Logger;
 import org.springframework.jdbc.support.JdbcUtils;
 import org.springframework.orm.hibernate3.HibernateCallback;
 import org.springframework.orm.hibernate3.HibernateTemplate;
 
 import corner.services.migration.ConnectionAdapter;
 import corner.services.migration.ConnectionAdapterSource;
 import corner.services.migration.MigrateFragment;
 import corner.services.migration.MigrationService;
 import corner.services.migration.impl.fragment.AddColumnFragment;
 import corner.services.migration.impl.fragment.ChangeColumnFragment;
 import corner.services.migration.impl.fragment.CreateTableFragment;
 import corner.services.migration.impl.fragment.RemoveColumnFragment;
 import corner.services.migration.impl.fragment.RenameColumnFragment;
 
 /**
  * 数据库升级服务的实现类
  * 
  * @author <a href="jun.tsai@ganshane.net">Jun Tsai</a>
  * @version $Revision: 5317 $
  * @since 0.0.2
  */
 public class MigrationServiceImpl implements MigrationService {
 
 	/** 数据库的初始版本 * */
 	public static final int DB_NOTHING = -1;
 	/** 数据库中索引版本 * */
 	private static final int INDEX_NOTHING = -1;
 
 	public static final String defaultCatalog = Environment.getProperties()
 			.getProperty(Environment.DEFAULT_CATALOG);
 	public static final String defaultSchema = Environment.getProperties()
 			.getProperty(Environment.DEFAULT_SCHEMA);
 
 	public final SessionFactoryImplementor sessionFactory;
 	private Settings settings;
 	public Dialect dialect;
 	private Logger logger;
 	public ConnectionAdapter adapter;
 
 	private Configuration cfg;
 
 	public static final String SCHEMA_INFO_TABLE_NAME = "db_schema_info";
 
 	public MigrationServiceImpl(HibernateSessionSource sessionSource,
 			ConnectionAdapterSource adapterSource, Logger logger) {
 		this.sessionFactory = (SessionFactoryImplementor) sessionSource
 				.getSessionFactory();
 		this.settings = this.sessionFactory.getSettings();
 		this.cfg = sessionSource.getConfiguration();
 		this.dialect = this.settings.getDialect();
 		this.logger = logger;
 		adapter = adapterSource.getConnectionAdapter(dialect.getClass());
 		if (adapter == null) {
 			throw new RuntimeException("未能发现和Dialect["
 					+ dialect.getClass().getName() + "] 向匹配的数据库适配器!");
 		}
 
 	}
 
 	/**
 	 * 创建表，给定的表名来创建表格
 	 * 
 	 * @see corner.services.migration.MigrationService#createTable(java.lang.String)
 	 */
 	@Override
 	public void createTable(final String tableName) {
 		this.executeMigrateFragment(new CreateTableFragment(this.adapter,
 				dialect, sessionFactory, tableName));
 	}
 
 	public void addColumn(final String tableName) {
 		executeMigrateFragment(new AddColumnFragment(this.adapter, dialect,
 				sessionFactory, tableName));
 	}
 
 	/**
 	 * 修改Column内容
 	 * 
 	 * @param tableName
 	 *            表名
 	 * @param columnName
 	 *            列名
 	 */
 	public void changeColumn(final String tableName, final String columnName) {
 		executeMigrateFragment(new ChangeColumnFragment(this.adapter, dialect,
 				sessionFactory, tableName, columnName));
 	}
 
 	/**
 	 * 删除列的SQL
 	 * 
 	 * @param tableName
 	 *            表名
 	 * @param tableColumns
 	 *            待删除的列名
 	 * @since 0.0.2
 	 */
 	public void removeColumn(String tableName, String... tableColumns) {
 		executeMigrateFragment(new RemoveColumnFragment(this.adapter, dialect,
 				sessionFactory, tableName, tableColumns));
 	}
 
 	/**
 	 * 重命名列
 	 * 
 	 * @param tableName
 	 *            表名称
 	 * @param oldColumns
 	 *            旧的列名
 	 * @param newColumns
 	 *            新的列名
 	 * @since 0.0.2
 	 */
 	public void renameColumns(String tableName, String[] oldColumns,
 			String[] newColumns) {
 		executeMigrateFragment(new RenameColumnFragment(this.adapter, dialect,
 				sessionFactory, tableName, oldColumns, newColumns));
 	}
 
 	/**
 	 * 删除表
 	 * 
 	 * @param tableName
 	 *            表名
 	 * @since 0.0.2
 	 */
 	public void dropTable(String tableName) {
 		Table table = new Table(tableName);
 		this.executeSchemaScript(table.sqlDropString(this.dialect,
 				defaultCatalog, defaultSchema));
 	}
 
 	public void executeSchemaScript(String... sql) {
 		Connection conn = null;
 		try {
 			conn = this.settings.getConnectionProvider().getConnection();
 			this.executeSchemaScript(conn, sql);
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		} finally {
 			this.closeHibernateConnection(conn);
 
 		}
 
 	}
 
 	/**
 	 * 执行升级的脚本
 	 * 
 	 * @param fragment
 	 *            升级脚本
 	 * @since 0.0.2
 	 */
 	public void executeMigrateFragment(MigrateFragment fragment) {
		// 通过配置得到所有的表格映射集合
		Iterator tableMappings = this.cfg.getTableMappings();
 
 		while (tableMappings.hasNext()) {
 
 			// 得到表对象
 			Table table = (Table) tableMappings.next();
 
 			// 是否为待处理的表格
 			if (!fragment.filteTable(table)) {
 				continue;
 			}
 			Connection conn = null;
 			try {
 				conn = this.settings.getConnectionProvider().getConnection();
 
 				DatabaseMetadata meta = new DatabaseMetadata(conn, dialect);
 				TableMetadata tableMetadata = adapter.fetchTableInfo(meta,
 						table, defaultCatalog, defaultSchema);
 				// 通过当前的Table对象来产生升级的片段.
 				List<String> script = fragment.generateMigrationFragments(
 						table, tableMetadata);
 				final String[] sqls = ArrayHelper.toStringArray(script);
 
 				// 执行升级的SQL脚本
 				executeSchemaScript(conn, sqls);
 			} catch (SQLException e) {
 				throw new RuntimeException(e);
 			} finally {
 				closeHibernateConnection(conn);
 			}
 		}
 	}
 
 	/**
 	 * 关闭给定的{@link Connection}
 	 * <p>
 	 * 所有使用的{@link Connection}都来自{@link ConnectionProvider},因此调用{@link ConnectionProvider}的closeConnection方法关闭
 	 * 
 	 * @param conn
 	 */
 	private void closeHibernateConnection(Connection conn) {
 		if (conn != null) {
 			try {
 				this.settings.getConnectionProvider().closeConnection(conn);
 			} catch (SQLException e) {
 				logger.debug("when close connection get the Exception:" + e);
 			}
 		}
 	}
 
 	/**
 	 * Execute the given schema script on the given JDBC Connection.
 	 * <p>
 	 * Note that the default implementation will log unsuccessful statements and
 	 * continue to execute. Override the <code>executeSchemaStatement</code>
 	 * method to treat failures differently.
 	 * 
 	 * @param con
 	 *            the JDBC Connection to execute the script on
 	 * @param sql
 	 *            the SQL statements to execute
 	 * @throws SQLException
 	 *             if thrown by JDBC methods
 	 * @see #executeSchemaStatement
 	 * @param con
 	 *            the JDBC Connection to execute the script on
 	 * @param sql
 	 *            the SQL statements to execute
 	 * @throws SQLException
 	 *             if thrown by JDBC methods
 	 */
 	protected void executeSchemaScript(Connection con, String... sql)
 			throws SQLException {
 		if (sql != null && sql.length > 0) {
 			boolean oldAutoCommit = con.getAutoCommit();
 			if (!oldAutoCommit) {
 				con.setAutoCommit(false);
 			}
 			try {
 				Statement stmt = con.createStatement();
 				try {
 					for (int i = 0; i < sql.length; i++) {
 						try {
 							logger.info("[db-upgrade] " + sql[i]);
 							executeSchemaStatement(stmt, sql[i]);
 						} catch (SQLException se) {
 							logger.info("[db-upgrade] " + sql[i]+" error");
 							se.printStackTrace();
 							throw se;
 						}
 					}
 				} finally {
 					JdbcUtils.closeStatement(stmt);
 				}
 			} finally {
 				if (!oldAutoCommit) {
 					con.setAutoCommit(false);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Execute the given schema SQL on the given JDBC Statement.
 	 * <p>
 	 * Note that the default implementation will log unsuccessful statements and
 	 * continue to execute. Override this method to treat failures differently.
 	 * 
 	 * @param stmt
 	 *            the JDBC Statement to execute the SQL on
 	 * @param sql
 	 *            the SQL statement to execute
 	 * @throws SQLException
 	 *             if thrown by JDBC methods (and considered fatal)
 	 */
 	protected void executeSchemaStatement(Statement stmt, String sql)
 			throws SQLException {
 
 		try {
 			stmt.executeUpdate(sql);
 		} catch (SQLException ex) {
 
 		}
 	}
 
 	@Override
 	public SchemaInfo initSchemaInfo() {
 		this.executeMigrateFragment(new MigrateFragment() {
 
 			@Override
 			public boolean filteTable(Table table) {
 				return table.getName().equalsIgnoreCase(SCHEMA_INFO_TABLE_NAME);
 			}
 
 			@Override
 			public List<String> generateMigrationFragments(Table table,
 					TableMetadata tableInfo) {
 				List<String> script = new ArrayList<String>();
 				// 此数据库中，没有schema_info表，说明当前库为空库，直接创建所有的数据库
 				if (tableInfo == null) {
                     logger.info("数据库中无["+SCHEMA_INFO_TABLE_NAME+"],直接创建数据库!");
 					String[] sqls = cfg.generateSchemaCreationScript(dialect);
 					executeSchemaScript(sqls);
 				}
 				return script;
 			}
 		});
 		// 检查表中是否存在记录，如果没有，插入默认记录
 		HibernateTemplate hibernateTemplate = new HibernateTemplate(
 				this.sessionFactory);
 		SchemaInfo info = (SchemaInfo) hibernateTemplate
 				.execute(new HibernateCallback() {
 					public Object doInHibernate(Session session)
 							throws HibernateException, SQLException {
 						Criteria criteria = session
 								.createCriteria(SchemaInfo.class);
 						return criteria.uniqueResult();
 					}
 				});
 		if (info == null) {// 如果SchemaInfo表中无记录
 			// 增加以条默认记录,记录的version字段保存0
 			info = new SchemaInfo();
 			info.setDbversion(DB_NOTHING);
 			info.setIndexversion(INDEX_NOTHING);
 			hibernateTemplate.saveOrUpdate(info);
 		}
 		return info;
 	}
 
 	@Override
 	public void updateDbMaxVersion(int scriptType, int maxVersion) {
 		HibernateTemplate hibernateTemplate = new HibernateTemplate(
 				sessionFactory);
 		SchemaInfo info = (SchemaInfo) hibernateTemplate
 				.execute(new HibernateCallback() {
 					public Object doInHibernate(Session session)
 							throws HibernateException, SQLException {
 						Criteria criteria = session
 								.createCriteria(SchemaInfo.class);
 						return criteria.uniqueResult();
 					}
 				});
 
 		if (info != null) {
 			if (scriptType == AbstractDBMigrationInitializer.DB_SCRIPT_TYPE_STR) {
 				info.setDbversion(maxVersion);
 			} else if (scriptType == AbstractDBMigrationInitializer.INDEX_SCRIPT_TYPE_STR) {
 				info.setIndexversion(maxVersion);
 			}
 			hibernateTemplate.saveOrUpdate(info);
 		}
 	}
 
 	@Override
 	public void executeSql(String sql) {
 		this.executeSchemaScript(sql);
 	}
 }
