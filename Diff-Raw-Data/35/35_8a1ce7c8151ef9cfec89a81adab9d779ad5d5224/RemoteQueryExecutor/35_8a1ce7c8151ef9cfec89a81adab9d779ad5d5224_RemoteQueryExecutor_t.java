 /*
  * Copyright (C) 2009-2010 School of Computer Science, University of St Andrews. All rights reserved.
  * Project Homepage: http://blogs.cs.st-andrews.ac.uk/h2o
  *
  * H2O is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * H2O is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with H2O.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.h2o.db.query.asynchronous;
 
 import java.rmi.RemoteException;
 import java.sql.SQLException;
 
 import org.h2.command.Command;
 import org.h2.command.Parser;
 import org.h2o.db.id.TableInfo;
 import org.h2o.db.wrappers.DatabaseInstanceWrapper;
import org.h2o.test.H2OTest;
 
 public class RemoteQueryExecutor extends Thread {
 
 	private String query;
 
 	private String transactionName;
 
 	private DatabaseInstanceWrapper databaseWrapper;
 
 	private boolean local;
 
 	private Parser parser;
 
 	private boolean commitOperation;
 
 	private int updateID;
 
 	private final TableInfo tableInfo;
 
 	/**
 	 * 
 	 * @param query
 	 * @param transactionName
 	 * @param replica
 	 * @param updateID 
 	 * 			  The current update ID of the table involved in this query.
 	 * @param parser
 	 *            Only used to execute the transaction if local is true.
 	 * @param local
 	 *            Whether the transaction is to be executed locally, or sent
 	 *            remotely.
 	 * @param commitOperation
 	 *            True if this is a COMMIT, false if it is another type of
 	 *            query. If it is false a PREPARE command will be executed to
 	 *            get ready for the eventual commit.
 	 */
 	public RemoteQueryExecutor(String query, String transactionName, DatabaseInstanceWrapper replica, int updateID, Parser parser,
 			boolean local, boolean commitOperation, TableInfo tableInfo) {
 		this.query = query;
 		this.transactionName = transactionName;
 		this.databaseWrapper = replica;
 		this.parser = parser;
 		this.local = local;
 		this.commitOperation = commitOperation;
 		this.updateID = updateID;
 		this.tableInfo = tableInfo;
 	}
 
 	public QueryResult executeQuery() {
 		if (local) {
 			return executeLocal();
 		} else {
 			return executeRemote();
 		}
 	}
 
 	private QueryResult executeLocal() {
 		QueryResult qr = null;
 		try {
 			int result = 0;
 
 			if (!commitOperation) {
 
 				// Execute query.
 				Command command = parser.prepareCommand(query);
 				command.executeUpdate(true); // True because it may need to wait
 												// for the remote machine to
 												// commit.
 
 				// Prepare query for commit.
 				command = parser.prepareCommand("PREPARE COMMIT "
 						+ transactionName);
 				result = command.executeUpdate();
 			} else {
 				// Prepare for commit.
 				Command prepare = parser.prepareCommand("PREPARE COMMIT "
 						+ transactionName);
 				prepare.executeUpdate();
 
 				// Execute query.
 				Command command = parser.prepareCommand(query);
 				result = command.executeUpdate();
 			}
 
 			qr = new QueryResult(result, databaseWrapper, updateID, tableInfo);
 
 		} catch (SQLException e) {
 			qr = new QueryResult(e, databaseWrapper, updateID, tableInfo);
 		}
 
 		return qr;
 	}
 
 	private QueryResult executeRemote() {
 		QueryResult qr = null;
 		try {
 			H2OTest.rmiFailure(databaseWrapper);
 
 			int result = databaseWrapper.getDatabaseInstance().execute(query,
 					transactionName, commitOperation);
 
 			qr = new QueryResult(result, databaseWrapper, updateID, tableInfo);
 
 		} catch (RemoteException e) {
 			qr = new QueryResult(new SQLException(e.getMessage()), databaseWrapper, updateID, tableInfo);
 		} catch (SQLException e) {
 			qr = new QueryResult(e, databaseWrapper, updateID, tableInfo);
 		}
 
 		return qr;
 	}
 
 }
