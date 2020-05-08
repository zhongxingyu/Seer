 /*
  * Copyright (c) 2008 Hidenori Sugiyama
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
 
 /**
  * 
  */
 package org.madogiwa.plaintable.impl;
 
 import org.madogiwa.plaintable.AtomicAction;
 import org.madogiwa.plaintable.PlainTableException;
 import org.madogiwa.plaintable.Row;
 import org.madogiwa.plaintable.Session;
 import org.madogiwa.plaintable.criteria.*;
 import org.madogiwa.plaintable.criteria.bool.Bools;
 import org.madogiwa.plaintable.criteria.value.NumericAggregation;
 import org.madogiwa.plaintable.criteria.value.NumericExpression;
 import org.madogiwa.plaintable.dialect.Dialect;
 import org.madogiwa.plaintable.handler.ListHandler;
 import org.madogiwa.plaintable.handler.RowHandler;
 import org.madogiwa.plaintable.handler.SingleHandler;
 import org.madogiwa.plaintable.mapper.RowMapper;
 import org.madogiwa.plaintable.provider.BeanRowProvider;
 import org.madogiwa.plaintable.provider.RowProvider;
 import org.madogiwa.plaintable.schema.Column;
 import org.madogiwa.plaintable.schema.Schema;
 import org.madogiwa.plaintable.util.JdbcUtils;
 
 import javax.sql.DataSource;
 import java.sql.*;
 import java.util.List;
 import java.util.logging.Logger;
 
 /**
  * @author Hidenori Sugiyama
  * 
  */
 public class SessionImpl implements Session {
 
 	private static Logger logger = Logger
 			.getLogger(SessionImpl.class.getName());
 
 	private DatabaseManagerImpl databaseManager;
 
 	private DataSource dataSource = null;
 
 	private Connection connection = null;
 
 	private Dialect dialect = null;
 
 	private boolean delayedOpen = false;
 
 	private boolean readOnly = false;
 
     private boolean autoCommit = false;
 
 	private TransactionMode transactionMode;
 
     private boolean isDirty = false;
 
 	/**
 	 * @param databaseManager
 	 * @param dataSource
 	 * @param dialect
 	 */
 	public SessionImpl(DatabaseManagerImpl databaseManager, DataSource dataSource, Dialect dialect) {
 		this.databaseManager = databaseManager;
 		this.dataSource = dataSource;
 		this.dialect = dialect;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.madogiwa.plaintable.Session#open()
 	 */
 	public void open() throws PlainTableException {
 		if (connection != null) {
 			throw new PlainTableException("session already opened");
 		}
 
 		try {
 			connection = dataSource.getConnection();
 
 			if (readOnly) {
 				connection.setReadOnly(true);
 
 			} else {
 				connection.setReadOnly(false);
 			}
 
 			if (transactionMode != null) {
 				switch (transactionMode) {
 				case READ_COMMITTED:
 					connection
 							.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
 					break;
 				case SERIALIZABLE:
 					connection
 							.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
 					break;
 				case CONNECTOR_DEFAULT:
 					// don't set transaction level
 					break;
 				}
 			} else {
 				connection
 						.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
 			}
 
 			connection.setAutoCommit(autoCommit);
 
             this.isDirty = false;
 		} catch (SQLException e) {
 			connection = null;
 			throw new PlainTableException(e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.madogiwa.plaintable.Session#close()
 	 */
 	public void close() throws PlainTableException {
         try {
             if (!getAutoCommit() && this.isDirty) {
                 logger.warning("autoCommit is disabled and session is dirty. trying automatic rollback...");
                 connection.rollback();
                 logger.warning("automatic rollback successful");
             }
         } catch (SQLException e) {
             logger.severe("automatic rollback failed: " + e.getMessage());
         }
 
 		try {
 			if (connection != null) {
 				connection.close();
 			}
 		} catch (SQLException e) {
 			throw new PlainTableException(e);
 		} finally {
 			connection = null;
 		}
 	}
 
     /*
 	 * (non-Javadoc)
 	 *
 	 * @see org.madogiwa.plaintable.Session#getTransactionMode()
 	 */
     public TransactionMode getTransactionMode() {
         return transactionMode;
     }
 
     /*
      * (non-Javadoc)
      *
      * @see
      * org.madogiwa.plaintable.Session#setTransactionMode(org.madogiwa.plaintable
      * .Session.TransactionMode)
      */
 	public Session setTransactionMode(TransactionMode mode) {
 		this.transactionMode = mode;
         return this;
 	}
 
     /*
 	 * (non-Javadoc)
 	 *
 	 * @see org.madogiwa.plaintable.Session#getDelayedOpen()
 	 */
     public boolean getDelayedOpen() {
         return delayedOpen;
     }
 
 
     /*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.madogiwa.plaintable.Session#setDelayedOpen(boolean)
 	 */
 	public Session setDelayedOpen(boolean delayedOpen) {
 		this.delayedOpen = delayedOpen;
         return this;
 	}
 
     /*
 	 * (non-Javadoc)
 	 *
 	 * @see org.madogiwa.plaintable.Session#getReadOnly()
 	 */
     public boolean getReadOnly() {
         return readOnly;
     }
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.madogiwa.plaintable.Session#setReadOnly(boolean)
 	 */
 	public Session setReadOnly(boolean readOnly) {
 		this.readOnly = readOnly;
         return this;
 	}
 
     /*
      * (non-Javadoc)
      *
      * @see org.madogiwa.plaintable.Session#getAutoCommit()
      */
     public boolean getAutoCommit() {
         return autoCommit;
     }
 
     /*
      * (non-Javadoc)
      *
      * @see org.madogiwa.plaintable.Session#setAutoCommit(boolean)
      */
     public Session setAutoCommit(boolean autoCommit) {
         this.autoCommit = autoCommit;
         return this;
     }
 
     /*
      * (non-Javadoc)
      *
      * @see org.madogiwa.plaintable.Session#commit()
      */
 	public void commit() throws PlainTableException {
 		checkAndOpenSession();
 
 		try {
 			connection.commit();
             this.isDirty = false;
 		} catch (SQLException e) {
 			throw new PlainTableException(e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.madogiwa.plaintable.Session#rollback()
 	 */
 	public void rollback() throws PlainTableException {
 		checkAndOpenSession();
 
 		try {
 			connection.rollback();
             this.isDirty = false;
         } catch (SQLException e) {
 			throw new PlainTableException(e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.madogiwa.plaintable.Session#doAtomic(org.madogiwa.plaintable.AtomicAction
 	 * )
 	 */
 	public void doAtomic(AtomicAction action) throws PlainTableException {
 		try {
 			Savepoint savepoint = connection.setSavepoint();
 			try {
 				action.doAtomic(this);
 			} catch (PlainTableException e) {
 				try {
 					connection.rollback(savepoint);
 				} catch (SQLException e2) {
 					throw new RuntimeException(e2);
 				}
 				throw e;
 			} finally {
 				connection.releaseSavepoint(savepoint);
 			}
 		} catch (SQLException e) {
 			throw new PlainTableException(e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.madogiwa.plaintable.Session#isOpened()
 	 */
 	public boolean isOpened() throws PlainTableException {
 		try {
 			if (connection == null || connection.isClosed()) {
 				return false;
 			} else {
 				return true;
 			}
 		} catch (SQLException e) {
 			throw new PlainTableException(e);
 		}
 	}
 
 	/**
 	 * @throws PlainTableException
 	 */
 	private void checkAndOpenSession() throws PlainTableException {
 		if (!isOpened()) {
 			if (delayedOpen) {
 				open();
 			} else {
 				throw new PlainTableException("session is not opened");
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.madogiwa.plaintable.Session#load(org.madogiwa.plaintable.schema.Schema
 	 * , long, org.madogiwa.plaintable.mapper.Mapper)
 	 */
 	public <T> T load(Schema schema, long id, RowMapper<T> mapper)
 			throws PlainTableException {
 
 		Query criteria = new Query(schema);
 		criteria.getRestriction().add(Bools.eq(schema.getPrimaryKey(), id));
 
 		SingleHandler<T> handler = new SingleHandler<T>(mapper);
 		select(criteria, handler);
 		return handler.getResult();
 	}
 
 	public <T> T loadForUpdate(org.madogiwa.plaintable.schema.Schema schema, long id,
 							   org.madogiwa.plaintable.mapper.RowMapper<T> mapper)
 			throws org.madogiwa.plaintable.PlainTableException {
 
 		checkAndOpenSession();
 
 		Query query = new Query(schema);
 		query.getRestriction().add(Bools.eq(schema.getPrimaryKey(), id));
 
 		Context context = new Context(dialect, Context.Mode.SELECT);
 		StatementBuilder builder = databaseManager.createStatementBuilder();
 		String sql = builder.buildSelectForUpdateSql(context, query);
 
 		SingleHandler<T> handler = new SingleHandler<T>(mapper);
 		doSelect(context, sql, handler, query.getProjection(), new Window());
 
 		return handler.getResult();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see
 	 * org.madogiwa.plaintable.Session#count(org.madogiwa.plaintable.schema.
 	 * Schema, org.madogiwa.plaintable.criteria.Restriction)
 	 */
 	public long count(Schema schema, Restriction restriction)
 			throws PlainTableException {
 
 		return count(schema, restriction, null);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.madogiwa.plaintable.Session#count(org.madogiwa.plaintable.schema.
 	 * Schema, org.madogiwa.plaintable.criteria.Restriction,
 	 * org.madogiwa.plaintable.schema.Column)
 	 */
 	public long count(Schema schema, Restriction restriction, Column column)
 			throws PlainTableException {
 
 		Query query = new Query(schema);
 		query.setRestriction(restriction);
 		if (column != null) {
 			query.getProjection().add(
 				new NumericAggregation(column,
 						NumericAggregation.Function.COUNT),
 				String.format("count(%s)", column.getName().toLowerCase()));
 		} else {
 			query.getProjection().add(
 				new NumericExpression() {
 					public String getSQLString(Context context) {
 						return "count(*)";
 					}
 				}, "count");
 		}
 
 		SingleHandler<Long> handler = new SingleHandler<Long>(
 				new RowMapper<Long>() {
 
 					public Long map(Row row) {
 						try {
 							return row.getLong(0);
 						} catch (PlainTableException e) {
 							throw new RuntimeException(e);
 						}
 					}
 
 				});
 		select(query, handler);
 		return handler.getResult();
 	}
 
 	public long count(IQuery query)
 			throws PlainTableException {
 
 		Query countQuery = new Query(new QuerySource(query, "query"));
 		countQuery.getProjection().add(
 				new NumericExpression() {
 					public String getSQLString(Context context) {
 						return "count(*)";
 					}
 				}, "count");
 
 		SingleHandler<Long> handler = new SingleHandler<Long>(
 				new RowMapper<Long>() {
 
 					public Long map(Row row) {
 						try {
 							return row.getLong(0);
 						} catch (PlainTableException e) {
 							throw new RuntimeException(e);
 						}
 					}
 
 				});
 		select(countQuery, handler);
 		return handler.getResult();
 	}
 
 	public <T> List<T> select(IQuery query, RowMapper<T> mapper) throws PlainTableException {
 		return select(query, mapper, new Window());
 	}
 
 	public <T> List<T> select(IQuery query, RowMapper<T> mapper, Window window) throws PlainTableException {
 		ListHandler handler = new ListHandler<T>(mapper);
 		select(query, handler, window);
 		return handler.getList();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.madogiwa.plaintable.Session#select(org.madogiwa.plaintable.criteria
 	 * .IQuery, org.madogiwa.plaintable.handler.RowHandler)
 	 */
 	public void select(IQuery query, RowHandler handler)
 			throws PlainTableException {
 		select(query, handler, new Window());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.madogiwa.plaintable.Session#select(org.madogiwa.plaintable.criteria
 	 * .IQuery, org.madogiwa.plaintable.handler.RowHandler,
 	 * org.madogiwa.plaintable.criteria.Window)
 	 */
 	public void select(IQuery query, RowHandler handler, Window window)
 			throws PlainTableException {
 		checkAndOpenSession();
 
 		StatementBuilder builder = databaseManager.createStatementBuilder();
 		Context context = new Context(dialect, Context.Mode.SELECT);
 		String sql = builder.buildSelectSql(context, query, window);
 		doSelect(context, sql, handler, query.getProjection(), window);
 	}
 
 	/**
 	 * @param context
 	 * @param sql
 	 * @param handler
 	 * @param projection
 	 * @param window
 	 * @throws PlainTableException
 	 */
 	private void doSelect(Context context, String sql, RowHandler handler,
 			Projection projection, Window window) throws PlainTableException {
 		logger.fine(sql);
 
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 		try {
 			statement = connection.prepareStatement(sql);
 			if (!dialect.isLimitSupported() && window.getLimit() != Window.UNLIMITED) {
				statement.setFetchSize((int) window.getLimit());
 			}
 			context.resolveParameters(statement);
 			resultSet = statement.executeQuery();
 
 			if (!dialect.isLimitSupported()) {
 				for (int i = 0; i < window.getOffset(); i++) {
 					resultSet.next();
 				}
 			}
 
 			handler.begin();
 			while (resultSet.next()) {
 				RowImpl row = new RowImpl(projection, resultSet);
 				row.begin();
 				handler.handle(row);
 				row.end();
 			}
 			handler.end();
 		} catch (SQLException e) {
 			throw new PlainTableException(e);
 		} finally {
 			JdbcUtils.closeResultSet(resultSet);
 			JdbcUtils.closeStatement(statement);
 		}
 	}
 
 	public <T> long insert(T bean) throws PlainTableException {
 		BeanRowProvider<T> provider = new BeanRowProvider<T>(bean);
 		return insert(provider);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.madogiwa.plaintable.Session#insert(org.madogiwa.plaintable.schema
 	 * .Schema, org.madogiwa.plaintable.provider.RowProvider)
 	 */
 	public long insert(RowProvider provider) throws PlainTableException {
 		checkAndOpenSession();
 
 		StatementBuilder builder = databaseManager.createStatementBuilder();
 		Context context = new Context(dialect, Context.Mode.INSERT);
 		String sql = builder.buildInsertSql(context, provider.getSchema(),
 				provider);
 		return doInsert(context, sql);
 	}
 
 	private long doInsert(Context context, String sql)
 			throws PlainTableException {
 		logger.fine("insert: " + sql);
 
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 		try {
 			statement = connection.prepareStatement(sql.toString(),
 					Statement.RETURN_GENERATED_KEYS);
 			context.resolveParameters(statement);
 			statement.executeUpdate();
             this.isDirty = true;
 
 			resultSet = statement.getGeneratedKeys();
 			resultSet.next();
 			return resultSet.getLong(1);
 		} catch (SQLException e) {
 			throw new PlainTableException(e);
 		} finally {
 			JdbcUtils.closeResultSet(resultSet);
 			JdbcUtils.closeStatement(statement);
 		}
 
 	}
 
 	public boolean update(RowProvider provider, Long id)
 			throws PlainTableException {
 
 		Restriction restriction = new Restriction();
 		restriction.add(Bools.eq(provider.getSchema().getPrimaryKey(), id));
 		long result = update(provider, restriction);
 		return (result == 1) ? true : false;
 	}
 
 	public long update(RowProvider provider, Restriction restriction)
 			throws PlainTableException {
 		checkAndOpenSession();
 
 		StatementBuilder builder = databaseManager.createStatementBuilder();
 		Context context = new Context(dialect, Context.Mode.UPDATE);
 		String sql = builder.buildUpdateSql(context, provider.getSchema(),
 				restriction, provider);
 		return doUpdate(context, sql);
 	}
 
 	private long doUpdate(Context context, String sql)
 			throws PlainTableException {
 		logger.fine("update: " + sql);
 
 		PreparedStatement statement = null;
 		try {
 			statement = connection.prepareStatement(sql);
 			context.resolveParameters(statement);
 			long count = statement.executeUpdate();
             this.isDirty = true;
             return count;
 		} catch (SQLException e) {
 			throw new PlainTableException(e);
 		} finally {
 			JdbcUtils.closeStatement(statement);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.madogiwa.plaintable.Session#delete(org.madogiwa.plaintable.schema
 	 * .Schema, Long)
 	 */
 	public boolean delete(Schema schema, Long id) throws PlainTableException {
 
 		Restriction restriction = new Restriction();
 		restriction.add(Bools.eq(schema.getPrimaryKey(), id));
 		long count = delete(schema, restriction);
 		return (count == 1) ? true : false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.madogiwa.plaintable.Session#delete(org.madogiwa.plaintable.schema
 	 * .Schema, Long[])
 	 */
 	public long delete(Schema schema, Long[] ids) throws PlainTableException {
 
 		Restriction restriction = new Restriction();
 		restriction.add(Bools.in(schema.getPrimaryKey(), ids));
 		return delete(schema, restriction);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.madogiwa.plaintable.Session#delete(org.madogiwa.plaintable.schema
 	 * .Schema, org.madogiwa.plaintable.criteria.Restriction)
 	 */
 	public long delete(Schema schema, Restriction restriction)
 			throws PlainTableException {
 		checkAndOpenSession();
 
 		StatementBuilder builder = databaseManager.createStatementBuilder();
 		Context context = new Context(dialect, Context.Mode.DELETE);
 		String sql = builder.buildDeleteSql(context, schema, restriction);
 		return doDelete(context, sql);
 	}
 
 	/**
 	 * @param context
 	 * @param sql
 	 * @return
 	 * @throws PlainTableException
 	 */
 	private long doDelete(Context context, String sql)
 			throws PlainTableException {
 		logger.fine("delete: " + sql);
 
 		PreparedStatement statement = null;
 		try {
 			statement = connection.prepareStatement(sql);
 			context.resolveParameters(statement);
 			long count = statement.executeUpdate();
             this.isDirty = true;
             return count;
 		} catch (SQLException e) {
 			throw new PlainTableException(e);
 		} finally {
 			JdbcUtils.closeStatement(statement);
 		}
 	}
 
 }
