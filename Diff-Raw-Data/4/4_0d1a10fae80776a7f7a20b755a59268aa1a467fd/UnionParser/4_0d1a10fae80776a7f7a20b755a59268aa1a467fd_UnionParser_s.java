 /**
  * Copyright 2014 Eediom Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.logdb.query.parser;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import org.araqne.logdb.AbstractQueryCommandParser;
 import org.araqne.logdb.DefaultQuery;
 import org.araqne.logdb.Query;
 import org.araqne.logdb.QueryCommand;
 import org.araqne.logdb.QueryCommandPipe;
 import org.araqne.logdb.QueryContext;
 import org.araqne.logdb.QueryParserService;
 import org.araqne.logdb.QueryResult;
 import org.araqne.logdb.QueryResultCallback;
 import org.araqne.logdb.QueryResultConfig;
 import org.araqne.logdb.QueryResultFactory;
 import org.araqne.logdb.QueryResultSet;
 import org.araqne.logdb.QueryResultStorage;
 import org.araqne.logdb.Row;
 import org.araqne.logdb.RowBatch;
 import org.araqne.logdb.ThreadSafe;
 import org.araqne.logdb.query.command.Union;
 
 /**
  * @since 2.2.13
  * @author xeraph
  * 
  */
 public class UnionParser extends AbstractQueryCommandParser {
 
 	private QueryParserService queryParserService;
	private List<QueryCommand> subCommands;
 
 	public UnionParser(QueryParserService queryParserService) {
 		this.queryParserService = queryParserService;
 	}
 
 	@Override
 	public String getCommandName() {
 		return "union";
 	}
 
 	@Override
 	public QueryCommand parse(QueryContext context, String commandString) {
 		int b = commandString.indexOf('[');
 		int e = commandString.lastIndexOf(']');
 
 		String subQueryString = commandString.substring(b + 1, e).trim();
		subCommands = queryParserService.parseCommands(context, subQueryString);
 
 		Union union = new Union();
 		Query subQuery = new DefaultQuery(context, subQueryString, subCommands, new BypassResultFactory(union));
 		union.setSubQuery(subQuery);
 		return union;
 	}
 
 	private class BypassResult implements QueryResult {
 
 		private QueryCommand cmd;
 		private QueryCommandPipe pipe;
 
 		public BypassResult(QueryCommand cmd) {
 			this.cmd = cmd;
 			this.pipe = new QueryCommandPipe(cmd);
 		}
 
 		@Override
 		public boolean isThreadSafe() {
 			return cmd instanceof ThreadSafe;
 		}
 
 		@Override
 		public void onRow(Row row) {
 			pipe.onRow(row);
 		}
 
 		@Override
 		public void onRowBatch(RowBatch rowBatch) {
 			pipe.onRowBatch(rowBatch);
 		}
 
 		@Override
 		public Date getEofDate() {
 			return null;
 		}
 
 		@Override
 		public long getCount() {
 			return 0;
 		}
 
 		@Override
 		public void syncWriter() throws IOException {
 		}
 
 		@Override
 		public void closeWriter() {
 		}
 
 		@Override
 		public void purge() {
 		}
 
 		@Override
 		public boolean isStreaming() {
 			return false;
 		}
 
 		@Override
 		public void setStreaming(boolean streaming) {
 		}
 
 		@Override
 		public QueryResultSet getResultSet() throws IOException {
 			return null;
 		}
 
 		@Override
 		public Set<QueryResultCallback> getResultCallbacks() {
 			return null;
 		}
 	}
 
 	private class BypassResultFactory implements QueryResultFactory {
 		private QueryCommand cmd;
 
 		public BypassResultFactory(QueryCommand cmd) {
 			this.cmd = cmd;
 		}
 
 		@Override
 		public QueryResult createResult(QueryResultConfig config) throws IOException {
 			return new BypassResult(cmd);
 		}
 
 		@Override
 		public void registerStorage(QueryResultStorage storage) {
 		}
 
 		@Override
 		public void unregisterStorage(QueryResultStorage storage) {
 		}
 
 		@Override
 		public void start() {
 			// TODO Auto-generated method stub
 			
 		}
 	}
 }
