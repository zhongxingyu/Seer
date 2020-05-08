 /*
  * Copyright 2013 Eediom Inc.
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
 package org.araqne.logdb.client;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.text.SimpleDateFormat;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 public class Console {
 	private BufferedReader br;
 	private LogDbClient client;
 	private String host;
 	private String loginName;
 	private String password;
 
 	public static void main(String[] args) throws IOException {
 		new Console().run();
 	}
 
 	public void run() throws IOException {
 		w("Araqne LogDB Console 0.6.0 (2013-06-01)");
 		w("Type \"help\" for more information");
 
 		br = new BufferedReader(new InputStreamReader(System.in));
 
 		try {
 			while (true) {
 				System.out.print(getPrompt());
 				String line = br.readLine();
 				if (line == null)
 					break;
 
 				if (line.trim().isEmpty())
 					continue;
 
 				String[] tokens = line.split(" ");
 				if (tokens.length == 0)
 					continue;
 
 				String cmd = tokens[0].trim();
 				if (cmd.equals("quit") || cmd.equals("exit"))
 					break;
 				else if (cmd.equals("help"))
 					help();
 				else if (cmd.equals("connect"))
 					connect(tokens);
 				else if (cmd.equals("disconnect"))
 					disconnect();
 				else if (cmd.equals("query"))
 					query(tokens);
 				else if (cmd.equals("create_query"))
 					createQuery(tokens);
 				else if (cmd.equals("start_query"))
 					startQuery(tokens);
 				else if (cmd.equals("stop_query"))
 					stopQuery(tokens);
 				else if (cmd.equals("remove_query"))
 					removeQuery(tokens);
 				else if (cmd.equals("fetch"))
 					fetch(tokens);
 				else if (cmd.equals("queries"))
 					queries();
 				else if (cmd.equals("query_status"))
 					queryStatus(tokens);
 				else if (cmd.equals("create_table"))
 					createTable(tokens);
 				else if (cmd.equals("drop_table"))
 					dropTable(tokens);
 				else if (cmd.equals("tables"))
 					listTables();
 				else if (cmd.equals("table"))
 					manageTable(tokens);
 				else if (cmd.equals("loggers"))
 					listLoggers();
 				else if (cmd.equals("logger_factories"))
 					listLoggerFactories();
 				else if (cmd.equals("parser_factories"))
 					listParserFactories();
 				else if (cmd.equals("parser_factory"))
 					getParserFactoryInfo(tokens);
 				else if (cmd.equals("parsers"))
 					listParsers();
 				else if (cmd.equals("transformer_factories"))
 					listTransformerFactories();
 				else if (cmd.equals("transformer_factory"))
 					getTransformerFactoryInfo(tokens);
 				else if (cmd.equals("transformers"))
 					listTransformers();
 				else if (cmd.equals("create_transformer"))
 					createTransformer(tokens);
 				else if (cmd.equals("remove_transformer"))
 					removeTransformer(tokens);
 				else if (cmd.equals("create_parser"))
 					createParser(tokens);
 				else if (cmd.equals("remove_parser"))
 					removeParser(tokens);
 				else if (cmd.equals("create_logger"))
 					createLogger(tokens);
 				else if (cmd.equals("remove_logger"))
 					removeLogger(tokens);
 				else if (cmd.equals("start_logger"))
 					startLogger(tokens);
 				else if (cmd.equals("stop_logger"))
 					stopLogger(tokens);
 				else if (cmd.equals("index_tokenizers"))
 					listIndexTokenizers(tokens);
 				else if (cmd.equals("indexes"))
 					listIndexes(tokens);
 				else if (cmd.equals("index"))
 					getIndexInfo(tokens);
 				else if (cmd.equals("create_index"))
 					createIndex(tokens);
 				else if (cmd.equals("drop_index"))
 					dropIndex(tokens);
 				else if (cmd.equals("accounts"))
 					listAccounts(tokens);
 				else if (cmd.equals("create_account"))
 					createAccount(tokens);
 				else if (cmd.equals("remove_account"))
 					removeAccount(tokens);
 				else if (cmd.equals("passwd"))
 					changePassword(tokens);
 				else if (cmd.equals("grant"))
 					grantPrivilege(tokens);
 				else if (cmd.equals("revoke"))
 					revokePrivilege(tokens);
 				else if (cmd.equals("archives"))
 					listArchiveConfigs(tokens);
 				else if (cmd.equals("create_archive"))
 					createArchiveConfig(tokens);
 				else if (cmd.equals("remove_archive"))
 					removeArchiveConfig(tokens);
 				else
 					w("syntax error");
 
 			}
 		} finally {
 			if (client != null) {
 				w("closing logdb connection...");
 				client.close();
 				w("bye!");
 			}
 		}
 
 	}
 
 	private void connect(String[] tokens) {
 		if (tokens.length < 3) {
 			w("Usage: connect <host:port> <loginname> [<password>]");
 			return;
 		}
 
 		if (client != null) {
 			w("already connected");
 			return;
 		}
 
 		String addr = tokens[1];
 		String[] addrTokens = addr.split(":");
 
 		host = addrTokens[0];
 		int port = 80;
 		if (addrTokens.length > 1)
 			port = Integer.valueOf(addrTokens[1]);
 
 		try {
 			InetAddress.getByName(host);
 		} catch (UnknownHostException e) {
 			w("invalid hostname " + host + ", connect failed");
 			return;
 		}
 
 		loginName = tokens[2];
 
 		password = "";
 		if (tokens.length > 3)
 			password = tokens[3];
 
 		try {
 			client = new LogDbClient();
 			client.connect(host, port, loginName, password);
 			w("connected to " + host + " as " + loginName);
 		} catch (Throwable t) {
 			w(t.getMessage());
 			if (client != null) {
 				try {
 					client.close();
 				} catch (IOException e) {
 				}
 				client = null;
 			}
 		}
 	}
 
 	private void disconnect() {
 		if (client == null) {
 			w("not connected yet");
 			return;
 		}
 
 		w("closing connection...");
 		try {
 			client.close();
 		} catch (IOException e) {
 		}
 		w("disconnected");
 		client = null;
 	}
 
 	private void queries() {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 		try {
 			List<LogQuery> queries = client.getQueries();
 			if (queries.size() == 0) {
 				w("no result");
 				return;
 			}
 
 			for (LogQuery query : queries) {
 				w(query.toString());
 			}
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void queryStatus(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 2) {
 			w("Usage: query_status <query_id>");
 			return;
 		}
 
 		try {
 			LogQuery query = client.getQuery(Integer.valueOf(tokens[1]));
 			if (query == null) {
 				w("query not found");
 				return;
 			}
 
 			w(query.toString());
 			for (LogQueryCommand cmd : query.getCommands())
 				w("\t" + cmd);
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void query(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		long begin = System.currentTimeMillis();
 		String queryString = join(tokens);
 		w("querying [" + queryString + "] ...");
 
 		long count = 0;
 		LogCursor cursor = null;
 		try {
 			cursor = client.query(queryString);
 			while (cursor.hasNext()) {
 				Object o = cursor.next();
 				w(o.toString());
 				count++;
 			}
 
 			long end = System.currentTimeMillis();
 			w("total " + count + " row(s), elapsed " + (end - begin) + "ms");
 		} catch (Throwable t) {
 			if (client != null && client.isClosed())
 				client = null;
 
 			w("query failed: " + t.getMessage());
 		} finally {
 			if (cursor != null) {
 				try {
 					cursor.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 
 	private void createQuery(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 2) {
 			w("Usage: create_query <query_string>");
 			return;
 		}
 
 		try {
 			String queryString = join(tokens);
 			int id = client.createQuery(queryString);
 			w("created query " + id);
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private String join(String[] tokens) {
 		StringBuilder sb = new StringBuilder();
 		int p = 0;
 		for (int i = 1; i < tokens.length; i++) {
 			String t = tokens[i];
 			if (p++ != 0)
 				sb.append(" ");
 			sb.append(t);
 		}
 
 		return sb.toString();
 	}
 
 	private void startQuery(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 2) {
 			w("Usage: start_query <query_id>");
 			return;
 		}
 
 		try {
 			int id = Integer.valueOf(tokens[1]);
 			client.startQuery(id);
 			w("started query " + id);
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void stopQuery(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 2) {
 			w("Usage: stop_query <query_id>");
 			return;
 		}
 
 		try {
 			int id = Integer.valueOf(tokens[1]);
 			client.stopQuery(id);
 			w("stopped query " + id);
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void removeQuery(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 2) {
 			w("Usage: remove_query <query_id>");
 			return;
 		}
 		try {
 			int id = Integer.valueOf(tokens[1]);
 			client.removeQuery(id);
 			w("removed query " + id);
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void fetch(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 4) {
 			w("Usage: fetch <query_id> <offset> <limit>");
 			return;
 		}
 
 		int id = Integer.valueOf(tokens[1]);
 		long offset = Long.valueOf(tokens[2]);
 		int limit = Integer.valueOf(tokens[3]);
 
 		try {
 			Map<String, Object> page = client.getResult(id, offset, limit);
 			List<Object> rows = (List<Object>) page.get("result");
 			for (Object row : rows)
 				w(row.toString());
 			w(rows.size() + " row(s)");
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void createTable(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 2) {
 			w("Usage: create_table <table_name>");
 			return;
 		}
 
 		try {
 			client.createTable(tokens[1]);
 			w("created");
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void dropTable(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 2) {
 			w("Usage: drop_table <table_name>");
 			return;
 		}
 
 		try {
 			client.dropTable(tokens[1]);
 			w("dropped");
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void listTables() {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		try {
 			for (TableInfo table : client.listTables()) {
 				w("Table [" + table.getName() + "]");
 				for (Entry<String, String> e : table.getMetadata().entrySet())
 					w(" * " + e.getKey() + "=" + e.getValue());
 			}
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void manageTable(String[] tokens) throws IOException {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 2) {
 			w("Usage: table <table_name> [<key>] [<value>]");
 			return;
 		}
 
 		try {
 			String tableName = tokens[1];
 			if (tokens.length == 2) {
 				TableInfo table = client.getTableInfo(tableName);
 				w("Table [" + table.getName() + "]");
 				if (table.getMetadata().isEmpty()) {
 					w("no metadata");
 					return;
 				}
 
 				for (Entry<String, String> e : table.getMetadata().entrySet())
 					w(" * " + e.getKey() + "=" + e.getValue());
 			} else {
 				String key = tokens[2];
 				if (tokens.length == 3) {
 					Set<String> keys = new HashSet<String>();
 					keys.add(key);
 					client.unsetTableMetadata(tableName, keys);
 					w("unset");
 				} else if (tokens.length == 4) {
 					Map<String, String> config = new HashMap<String, String>();
 					String value = tokens[3];
 					config.put(key, value);
 					client.setTableMetadata(tableName, config);
 					w("set");
 				}
 			}
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void listLoggers() {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		try {
 			w("Loggers");
 			w("---------");
 			for (LoggerInfo logger : client.listLoggers())
 				w(logger.toString());
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void listLoggerFactories() {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		try {
 			w("Logger Factories");
 			w("------------------");
 			for (LoggerFactoryInfo f : client.listLoggerFactories())
 				w(f.toString());
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void listParserFactories() {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		try {
 			w("Parser Factories");
 			w("------------------");
 			for (ParserFactoryInfo f : client.listParserFactories())
 				w(f.toString());
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void getParserFactoryInfo(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 2) {
 			w("Usage: parser_factory <factory name>");
 			return;
 		}
 
 		try {
 			w("Parser Factory");
 			w("------------------");
 			ParserFactoryInfo f = client.getParserFactoryInfo(tokens[1]);
 			w(f.toString());
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void listParsers() {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		try {
 			List<ParserInfo> parsers = client.getParsers();
 			if (parsers.size() == 0) {
 				w("no result");
 				return;
 			}
 
 			w("Parsers");
 			w("----------");
 			for (ParserInfo parser : parsers)
 				w(parser.toString());
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void createParser(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 3) {
 			w("Usage: create_parser <factory_name> <name>");
 			return;
 		}
 
 		try {
 			ParserFactoryInfo f = client.getParserFactoryInfo(tokens[1]);
 
 			ParserInfo p = new ParserInfo();
 			p.setFactoryName(tokens[1]);
 			p.setName(tokens[2]);
 
 			for (ConfigSpec type : f.getConfigSpecs()) {
 				inputOption(p, type);
 			}
 
 			client.createParser(p);
 			w("created");
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void inputOption(ParserInfo parser, ConfigSpec spec) throws IOException {
 		String directive = spec.isRequired() ? "(required)" : "(optional)";
 		System.out.print(spec.getDisplayName() + " " + directive + "? ");
 		String value = br.readLine();
 		if (!value.isEmpty())
 			parser.getConfigs().put(spec.getName(), value);
 
 		if (value.isEmpty() && spec.isRequired()) {
 			inputOption(parser, spec);
 		}
 	}
 
 	private void removeParser(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 2) {
 			w("Usage: remove_parser <name>");
 			return;
 		}
 
 		try {
 			client.removeParser(tokens[1]);
 			w("removed");
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void listTransformerFactories() {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		try {
 			w("Transformer Factories");
 			w("------------------");
 			for (TransformerFactoryInfo f : client.listTransformerFactories())
 				w(f.toString());
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void getTransformerFactoryInfo(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 2) {
 			w("Usage: transformer_factory <factory name>");
 			return;
 		}
 
 		try {
 			w("Transformer Factory");
 			w("------------------");
 			TransformerFactoryInfo f = client.getTransformerFactoryInfo(tokens[1]);
 			w(f.toString());
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void listTransformers() {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		try {
 			List<TransformerInfo> transformers = client.getTransformers();
 			if (transformers.size() == 0) {
 				w("no result");
 				return;
 			}
 
			w("Parsers");
			w("----------");
 			for (TransformerInfo transformer : transformers)
 				w(transformer.toString());
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void createTransformer(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 3) {
 			w("Usage: create_transformer <factory_name> <name>");
 			return;
 		}
 
 		try {
 			TransformerFactoryInfo f = client.getTransformerFactoryInfo(tokens[1]);
 
 			TransformerInfo p = new TransformerInfo();
 			p.setFactoryName(tokens[1]);
 			p.setName(tokens[2]);
 
 			for (ConfigSpec type : f.getConfigSpecs()) {
 				inputOption(p, type);
 			}
 
 			client.createTransformer(p);
 			w("created");
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void inputOption(TransformerInfo parser, ConfigSpec spec) throws IOException {
 		String directive = spec.isRequired() ? "(required)" : "(optional)";
 		System.out.print(spec.getDisplayName() + " " + directive + "? ");
 		String value = br.readLine();
 		if (!value.isEmpty())
 			parser.getConfigs().put(spec.getName(), value);
 
 		if (value.isEmpty() && spec.isRequired()) {
 			inputOption(parser, spec);
 		}
 	}
 
 	private void removeTransformer(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 2) {
 			w("Usage: remove_transformer <name>");
 			return;
 		}
 
 		try {
 			client.removeTransformer(tokens[1]);
 			w("removed");
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void createLogger(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 4) {
 			w("Usage: create_logger <factory name> <namespace> <name>");
 			return;
 		}
 
 		try {
 			LoggerInfo logger = new LoggerInfo();
 			logger.setFactoryName(tokens[1]);
 			logger.setNamespace(tokens[2]);
 			logger.setName(tokens[3]);
 
 			LoggerFactoryInfo f = client.getLoggerFactoryInfo(tokens[1]);
 
 			for (ConfigSpec type : f.getConfigSpecs()) {
 				inputOption(logger, type);
 			}
 
 			client.createLogger(logger);
 			w("created");
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void inputOption(LoggerInfo logger, ConfigSpec spec) throws IOException {
 		String directive = spec.isRequired() ? "(required)" : "(optional)";
 		System.out.print(spec.getDisplayName() + " " + directive + "? ");
 		String value = br.readLine();
 		if (!value.isEmpty())
 			logger.getConfigs().put(spec.getName(), value);
 
 		if (value.isEmpty() && spec.isRequired()) {
 			inputOption(logger, spec);
 		}
 	}
 
 	private void removeLogger(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 2) {
 			w("Usage: remove_logger <logger fullname>");
 			return;
 		}
 
 		try {
 			client.removeLogger(tokens[1]);
 			w("removed");
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void startLogger(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 3) {
 			w("Usage: start_logger <logger fullname> <interval (millisec)>");
 			return;
 		}
 
 		try {
 			client.startLogger(tokens[1], Integer.valueOf(tokens[2]));
 			w("started with interval " + tokens[2] + "ms");
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void listIndexTokenizers(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		try {
 			for (IndexTokenizerFactoryInfo tokenizer : client.listIndexTokenizerFactories()) {
 				w(tokenizer.toString());
 			}
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void listIndexes(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 2) {
 			w("Usage: indexes <table name>");
 			return;
 		}
 
 		try {
 			for (IndexInfo index : client.listIndexes(tokens[1])) {
 				w(index.toString());
 			}
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void getIndexInfo(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 3) {
 			w("Usage: index <table name> <index name>");
 			return;
 		}
 
 		try {
 			IndexInfo index = client.getIndexInfo(tokens[1], tokens[2]);
 			w(index.toString());
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void stopLogger(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 2) {
 			w("Usage: stop_logger <logger fullname>");
 			return;
 		}
 
 		try {
 			client.stopLogger(tokens[1], 5000);
 			w("stopped");
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void createIndex(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 3) {
 			w("Usage: create_index <table name> <index name>");
 			return;
 		}
 
 		try {
 			String tableName = tokens[1];
 			String indexName = tokens[2];
 
 			IndexInfo index = new IndexInfo();
 			index.setTableName(tableName);
 			index.setIndexName(indexName);
 
 			w("Available Index Tokenizers");
 			w("----------------------------");
 			List<IndexTokenizerFactoryInfo> tokenizers = client.listIndexTokenizerFactories();
 			for (IndexTokenizerFactoryInfo tokenizer : tokenizers) {
 				w(tokenizer.toString());
 			}
 
 			System.out.print("select tokenizer? ");
 			String tokenizerName = br.readLine().trim();
 
 			IndexTokenizerFactoryInfo selected = null;
 			for (IndexTokenizerFactoryInfo tokenizer : tokenizers) {
 				if (tokenizer.getName().equals(tokenizerName))
 					selected = tokenizer;
 			}
 
 			if (selected == null) {
 				w("invalid index tokenizer");
 				return;
 			}
 
 			index.setTokenizerName(tokenizerName);
 
 			for (IndexConfigSpec type : selected.getConfigSpecs()) {
 				inputOption(index, type);
 			}
 
 			System.out.print("use bloom filter (y/N)? ");
 			index.setUseBloomFilter(br.readLine().trim().equalsIgnoreCase("y"));
 
 			if (index.isUseBloomFilter()) {
 				System.out.print("bloom filter lv0 capacity (enter to use default)? ");
 				String t = br.readLine().trim();
 				if (!t.isEmpty())
 					index.setBloomFilterCapacity0(Integer.valueOf(t));
 
 				System.out.print("bloom filter lv0 error rate (0<x<1, enter to use default)? ");
 				t = br.readLine().trim();
 				if (!t.isEmpty())
 					index.setBloomFilterErrorRate0(Double.valueOf(t));
 
 				System.out.print("bloom filter lv1 capacity (enter to use default)? ");
 				if (!t.isEmpty())
 					index.setBloomFilterCapacity1(Integer.valueOf(t));
 
 				System.out.print("bloom filter lv1 error rate (0<x<1, enter to use default)? ");
 				if (!t.isEmpty())
 					index.setBloomFilterErrorRate1(Double.valueOf(t));
 			}
 
 			System.out.print("base path (optional)? ");
 			String basePath = br.readLine().trim();
 			if (basePath.isEmpty())
 				basePath = null;
 			index.setBasePath(basePath);
 
 			System.out.print("build past index (y/n)? ");
 			String s = br.readLine().trim();
 			index.setBuildPastIndex(s.equalsIgnoreCase("y"));
 
 			if (index.isBuildPastIndex()) {
 				System.out.print("min day (yyyymmdd or enter to skip)? ");
 				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
 				String minDayStr = br.readLine().trim();
 				if (minDayStr != null)
 					index.setMinIndexDay(dateFormat.parse(minDayStr));
 			}
 
 			client.createIndex(index);
 			w("created");
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void inputOption(IndexInfo index, IndexConfigSpec spec) throws IOException {
 		String directive = spec.isRequired() ? "(required)" : "(optional)";
 		System.out.print(spec.getName() + " " + directive + "? ");
 		String value = br.readLine();
 		if (!value.isEmpty())
 			index.getTokenizerConfigs().put(spec.getKey(), value);
 
 		if (value.isEmpty() && spec.isRequired()) {
 			inputOption(index, spec);
 		}
 	}
 
 	private void dropIndex(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 3) {
 			w("Usage: drop_index <table name> <index name>");
 			return;
 		}
 
 		try {
 			client.dropIndex(tokens[1], tokens[2]);
 			w("dropped");
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void listAccounts(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		try {
 			for (AccountInfo account : client.listAccounts())
 				w(account.toString());
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 
 	}
 
 	private void createAccount(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 3) {
 			w("Usage: create_account <login name> <password>");
 			return;
 		}
 
 		try {
 			AccountInfo account = new AccountInfo();
 			account.setLoginName(tokens[1]);
 			account.setPassword(tokens[2]);
 			client.createAccount(account);
 			w("created");
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void removeAccount(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 2) {
 			w("Usage: remove_account <login name>");
 			return;
 		}
 
 		try {
 			client.removeAccount(tokens[1]);
 			w("removed");
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 
 	}
 
 	private void changePassword(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 3) {
 			w("Usage: passwd <login name> <password>");
 			return;
 		}
 
 		try {
 			client.changePassword(tokens[1], tokens[2]);
 			w("changed");
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 
 	}
 
 	private void grantPrivilege(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 3) {
 			w("Usage: grant <login name> <table name>");
 			return;
 		}
 
 		try {
 			client.grantPrivilege(new Privilege(tokens[1], tokens[2]));
 			w("granted");
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 
 	}
 
 	private void revokePrivilege(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 3) {
 			w("Usage: revoke <login name> <table name>");
 			return;
 		}
 
 		try {
 			client.revokePrivilege(new Privilege(tokens[1], tokens[2]));
 			w("revoked");
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 
 	}
 
 	private void listArchiveConfigs(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		try {
 			w("Archive Configs");
 			w("-----------------");
 			for (ArchiveConfig config : client.listArchiveConfigs()) {
 				w(config.toString());
 			}
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void createArchiveConfig(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 3) {
 			w("Usage: create_archive <logger fullname> <table name> [<host name>]");
 			return;
 		}
 
 		try {
 			ArchiveConfig config = new ArchiveConfig();
 			config.setLoggerName(tokens[1]);
 			config.setTableName(tokens[2]);
 			if (tokens.length > 3)
 				config.setHost(tokens[3]);
 			config.setEnabled(true);
 
 			client.createArchiveConfig(config);
 			w("created");
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private void removeArchiveConfig(String[] tokens) {
 		if (client == null) {
 			w("connect first please");
 			return;
 		}
 
 		if (tokens.length < 2) {
 			w("Usage: remove_archive <logger fullname>");
 			return;
 		}
 
 		try {
 			client.removeArchiveConfig(tokens[1]);
 			w("removed");
 		} catch (Throwable t) {
 			w(t.getMessage());
 		}
 	}
 
 	private String getPrompt() {
 		if (client != null)
 			return "logdb@" + host + "> ";
 		return "logdb> ";
 	}
 
 	private void help() {
 		w("connect <host> <loginname> <password>");
 		w("\tconnect to specified araqne logdb instance");
 
 		w("disconnect");
 		w("\tdisconnect database connection");
 
 		w("queries");
 		w("\tprint all queries initiated by this session");
 
 		w("query <query string>");
 		w("\tcreate, start and fetch query result at once");
 
 		w("create_query <query string>");
 		w("\tcreate query with specified query string, and return allocated query id");
 
 		w("start_query <query id>");
 		w("\tstart query");
 
 		w("stop_query <query_id>");
 		w("\tstop running query");
 
 		w("remove_query <query_id>");
 		w("\tstop and remove query");
 
 		w("fetch <query_id> <offset> <limit>");
 		w("\tfetch result set of specified window. you can fetch partial result before query is ended");
 	}
 
 	private static void w(String s) {
 		System.out.println(s);
 	}
 }
