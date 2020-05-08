 /*
  * Copyright 2013 Future Systems
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
 package org.araqne.logdb.query.engine;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.araqne.logdb.FunctionRegistry;
 import org.araqne.logdb.QueryCommand;
 import org.araqne.logdb.QueryCommandParser;
 import org.araqne.logdb.QueryCommandPipe;
 import org.araqne.logdb.QueryContext;
 import org.araqne.logdb.QueryParseException;
 import org.araqne.logdb.QueryParserService;
 import org.araqne.logdb.QueryStopReason;
 import org.araqne.logdb.query.parser.QueryTokenizer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Component(name = "logdb-query-parser-service")
 @Provides
 public class QueryParserServiceImpl implements QueryParserService {
 	private final Logger slog = LoggerFactory.getLogger(QueryParserServiceImpl.class);
 	private ConcurrentMap<String, QueryCommandParser> commandParsers = new ConcurrentHashMap<String, QueryCommandParser>();
 
 	@Requires
 	private FunctionRegistry functionRegistry;
 
 	// support unit test
 	public void setFunctionRegistry(FunctionRegistry functionRegistry) {
 		this.functionRegistry = functionRegistry;
 	}
 
 	@Override
 	public QueryCommandParser getCommandParser(String name) {
 		return commandParsers.get(name);
 	}
 
 	@Override
 	public List<QueryCommand> parseCommands(QueryContext context, String queryString) {
 		List<QueryCommand> commands = new ArrayList<QueryCommand>();
 
 		try {
 			for (String q : QueryTokenizer.parseCommands(queryString)) {
 				q = q.trim();
 
				StringTokenizer tok = new StringTokenizer(q, " \t");
 				String commandType = tok.nextToken();
 				QueryCommandParser parser = commandParsers.get(commandType);
 				if (parser == null)
 					throw new QueryParseException("unsupported-command", -1, "command is [" + commandType + "]");
 
 				QueryCommand cmd = parser.parse(context, q);
 				commands.add(cmd);
 			}
 		} catch (QueryParseException t) {
 			closePrematureCommands(commands);
 			throw t;
 		} catch (Throwable t) {
 			closePrematureCommands(commands);
 			slog.debug("QueryParserServiceImpl", t);
 			throw new QueryParseException("parse failure", -1, t.toString());
 		}
 
 		if (commands.isEmpty())
 			throw new IllegalArgumentException("empty query");
 
 		for (int i = 0; i < commands.size(); i++) {
 			QueryCommand command = commands.get(i);
 			if (i < commands.size() - 1)
 				command.setOutput(new QueryCommandPipe(commands.get(i + 1)));
 		}
 
 		return commands;
 	}
 
 	private void closePrematureCommands(List<QueryCommand> commands) {
 		for (QueryCommand cmd : commands) {
 			try {
 				slog.debug("araqne logdb: parse failed, closing command [{}]", cmd.toString());
 				cmd.onClose(QueryStopReason.CommandFailure);
 			} catch (Throwable t2) {
 				slog.error("araqne logdb: cannot close command", t2);
 			}
 		}
 	}
 
 	@Override
 	public void addCommandParser(QueryCommandParser parser) {
 		parser.setQueryParserService(this);
 		commandParsers.putIfAbsent(parser.getCommandName(), parser);
 	}
 
 	@Override
 	public void removeCommandParser(QueryCommandParser parser) {
 		commandParsers.remove(parser.getCommandName(), parser);
 	}
 
 	@Override
 	public FunctionRegistry getFunctionRegistry() {
 		return functionRegistry;
 	}
 
 }
