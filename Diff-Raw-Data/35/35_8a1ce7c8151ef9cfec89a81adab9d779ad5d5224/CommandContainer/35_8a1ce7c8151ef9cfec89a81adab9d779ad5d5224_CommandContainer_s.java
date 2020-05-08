 /*
  * Copyright 2004-2009 H2 Group. Multiple-Licensed under the H2 License,
  * Version 1.0, and under the Eclipse Public License, Version 1.0
  * (http://h2database.com/html/license.html).
  * Initial Developer: H2 Group
  */
 package org.h2.command;
 
 import java.rmi.RemoteException;
 import java.sql.SQLException;
 
 import org.h2.command.dml.Select;
 import org.h2.engine.Constants;
 import org.h2.expression.Parameter;
 import org.h2.result.LocalResult;
import org.h2.test.h2o.H2OTest;
 import org.h2.util.ObjectArray;
 import org.h2.value.Value;
 import org.h2o.db.query.QueryProxyManager;
 
 import uk.ac.standrews.cs.nds.util.Diagnostic;
 import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
 import uk.ac.standrews.cs.nds.util.ErrorHandling;
 
 /**
  * Represents a single SQL statements. It wraps a prepared statement.
  */
 public class CommandContainer extends Command {
 
 	private Prepared prepared;
 
 	private QueryProxyManager proxyManager = null;
 
 	CommandContainer(Parser parser, String sql, Prepared prepared) {
 		super(parser, sql);
 		prepared.setCommand(this);
 		this.prepared = prepared;
 
 		/*
 		 * If this command is part of a larger transaction then this query proxy manager will be over-written later on by a call from the
 		 * Command list class.
 		 */
 		if (!session.getApplicationAutoCommit() && session.getCurrentTransactionLocks() != null) {
 			// Diagnostic.traceNoEvent(DiagnosticLevel.FULL,
 			// "Using an existing proxy manager.");
 			this.proxyManager = session.getCurrentTransactionLocks();
 		} else {
 			// Diagnostic.traceNoEvent(DiagnosticLevel.FULL,
 			// "Creating a new proxy manager.");
 			this.proxyManager = new QueryProxyManager(parser.getSession().getDatabase(), getSession());
 			session.setCurrentTransactionLocks(this.proxyManager);
 		}
 	}
 
 	public ObjectArray getParameters() {
 		return prepared.getParameters();
 	}
 
 	public boolean isTransactional() {
 		return prepared.isTransactional();
 	}
 
 	public boolean isQuery() {
 		return prepared.isQuery();
 	}
 
 	public int executeUpdate() throws SQLException {
 		return executeUpdate(false);
 	}
 
 	private void recompileIfRequired() throws SQLException {
 		if (prepared.needRecompile()) {
 			// TODO test with 'always recompile'
 			prepared.setModificationMetaId(0);
 			String sql = prepared.getSQL();
 			ObjectArray oldParams = prepared.getParameters();
 			Parser parser = new Parser(session, false);
 			prepared = parser.parseOnly(sql);
 			long mod = prepared.getModificationMetaId();
 			prepared.setModificationMetaId(0);
 			ObjectArray newParams = prepared.getParameters();
 			for (int i = 0; i < newParams.size(); i++) {
 				Parameter old = (Parameter) oldParams.get(i);
 				if (old.isValueSet()) {
 					Value v = old.getValue(session);
 					Parameter p = (Parameter) newParams.get(i);
 					p.setValue(v);
 				}
 			}
 			prepared.prepare();
 			prepared.setModificationMetaId(mod);
 		}
 	}
 
 	public int update() throws SQLException, RemoteException {
 		int resultOfUpdate = update(false);
 
 		// if (!getSession().getAutoCommit()){
 		// ErrorHandling.hardError("Unexpected code path. This shouldn't be called when auto-commit is off.");
 		// }
 
 		return resultOfUpdate;
 	}
 
 	public LocalResult query(int maxrows) throws SQLException {
 		return query(maxrows, false);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.h2.command.Command#query(int, boolean)
 	 */
 	@Override
 	protected LocalResult query(int maxrows, boolean partOfMultiQueryTransaction) throws SQLException {
 		recompileIfRequired();
 
 		start();
 		prepared.checkParameters();
 
 		/*
 		 * If this is a SELECT query that does not target any meta-tables then locks must be acquired. If it is something else then no locks
 		 * are needed.
 		 */
 		if (!prepared.sqlStatement.contains("H2O.") && !prepared.sqlStatement.contains("INFORMATION_SCHEMA.")
 				&& !prepared.sqlStatement.contains("SYSTEM_RANGE") && !prepared.sqlStatement.contains("information_schema.")
 				&& prepared instanceof Select) {
 
 			getLock();
 
 			if (!proxyManager.hasAllLocks()) {
 				// TODO implement lock request timeout - look at TableData.doLock().
 				throw new SQLException("Couldn't obtain locks for all tables involved in query.");
 			}
 		}
 
 		try {
 			LocalResult result = prepared.query(maxrows);
 			prepared.trace(startTime, result.getRowCount());
 			proxyManager.endTransaction(null, true);
 			return result;
 		} catch (SQLException e) {
 			proxyManager.endTransaction(null, true);
 			throw e;
 		}
 	}
 
 	private void getLock() throws SQLException {
 		try {
 			doLock();
 		} finally {
 			session.setWaitForLock(null);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.h2.command.Command#update(boolean)
 	 */
 	@Override
 	protected int update(boolean partOfMultiQueryTransaction) throws SQLException, RemoteException {
 		recompileIfRequired();
 		start();
 		prepared.checkParameters();
 		int updateCount;
 
 		boolean singleQuery = !partOfMultiQueryTransaction, transactionCommand = prepared.isTransactionCommand();
 
 		if (!transactionCommand) { // Not a prepare or commit.
 			assert (proxyManager != null);
 
 			getLock(); // this throws an SQLException if no lock is found.
 
 			if (Diagnostic.getLevel() == DiagnosticLevel.FULL) {
 				proxyManager.addSQL(prepared.getSQL());
 			}
 
 			try {
 
 				updateCount = prepared.update(proxyManager.getTransactionName());
 
 				boolean commit = true; // An exception would already have been thrown if it should have been a rollback.
 
 				H2OTest.createTableFailure();
 
 				if (singleQuery && session.getApplicationAutoCommit()) {
 					/*
 					 * If it is one of a number of queries in the transaction then we must wait for the entire transaction to finish.
 					 */
 
 					proxyManager.commit(commit, true, session.getDatabase());
 					session.setCurrentTransactionLocks(null);
 				} else {
 					session.setCurrentTransactionLocks(proxyManager);
 				}
 
 			} catch (SQLException e) {
 				proxyManager.commit(false, true, session.getDatabase());
 				session.setCurrentTransactionLocks(null);
 				throw e;
 			}
 		} else {
 			/*
 			 * This is a transaction command. No need to commit such a query.
 			 */
 
 			try {
 				updateCount = prepared.update();
 
 				session.setCurrentTransactionLocks(null);
 			} catch (SQLException e) {
 				ErrorHandling.errorNoEvent("Transaction not found for query: " + prepared.getSQL());
 
 				e.printStackTrace();
 				throw e;
 			}
 		}
 
 		prepared.trace(startTime, updateCount);
 		return updateCount;
 	}
 
 	public boolean isReadOnly() {
 		return prepared.isReadOnly();
 	}
 
 	public LocalResult queryMeta() throws SQLException {
 		return prepared.queryMeta();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.h2.command.Command#acquireLocks()
 	 */
 	@Override
 	public void acquireLocks(QueryProxyManager queryProxyManager2) throws SQLException {
 		prepared.acquireLocks(proxyManager);
 	}
 
 	/**
 	 * @return
 	 */
 	public String getTableName() {
 		return prepared.table.getFullName();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.h2.command.Command#shouldBePropagated()
 	 */
 	@Override
 	public boolean shouldBePropagated() {
 		return prepared.shouldBePropagated();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.h2.command.Command#addQueryProxyManager(org.h2.h2o.comms. QueryProxyManager)
 	 */
 	@Override
 	public void addQueryProxyManager(QueryProxyManager proxyManager) {
 		if (proxyManager == null)
 			return;
 		this.proxyManager = proxyManager;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.h2.command.CommandInterface#getQueryProxyManager()
 	 */
 	@Override
 	public QueryProxyManager getQueryProxyManager() {
 		return proxyManager;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.h2.command.CommandInterface#isPreparedStatement(boolean)
 	 */
 	@Override
 	public void setIsPreparedStatement(boolean preparedStatement) {
 		prepared.setPreparedStatement(preparedStatement);
 	}
 
 	private void doLock() throws SQLException {
 		long max = System.currentTimeMillis() + session.getLockTimeout();
 
 		while (true) {
 
 			/*
 			 * Check if lock has been obtained.
 			 */
 			this.acquireLocks(proxyManager);
 
 			if (proxyManager.hasAllLocks())
 				return;
 
 			ErrorHandling.errorNoEvent("No lock obtained yet: " + prepared.getSQL());
 
 			/*
 			 * Check current time.. wait.
 			 */
 			long now = System.currentTimeMillis();
 			if (now >= max) {
 				throw new SQLException("Couldn't obtain locks for all tables involved in query.");
 			}
 			try {
 
 				for (int i = 0; i < 20; i++) {
 					long free = Runtime.getRuntime().freeMemory();
 					System.gc();
 					long free2 = Runtime.getRuntime().freeMemory();
 					if (free == free2) {
 						break;
 					}
 				}
 
 				// don't wait too long so that deadlocks are detected early
 				long sleep = Math.min(Constants.DEADLOCK_CHECK, max - now);
 				if (sleep == 0) {
 					sleep = 1;
 				}
 				Thread.sleep(sleep);
 			} catch (InterruptedException e) {
 				// ignore
 			}
 		}
 	}
 }
