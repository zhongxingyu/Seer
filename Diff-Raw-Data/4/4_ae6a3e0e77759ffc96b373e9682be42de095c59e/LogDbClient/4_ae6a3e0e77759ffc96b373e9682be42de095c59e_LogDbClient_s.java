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
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.zip.DataFormatException;
 import java.util.zip.Inflater;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 import org.araqne.codec.EncodingRule;
 import org.araqne.logdb.client.http.WebSocketTransport;
 import org.araqne.logdb.client.http.impl.TrapListener;
 import org.araqne.websocket.Base64;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @since 0.5.0
  * @author xeraph
  * 
  */
 public class LogDbClient implements TrapListener {
 	private Logger logger = LoggerFactory.getLogger(LogDbClient.class);
 	private LogDbTransport transport;
 	private LogDbSession session;
 	private int fetchSize = 10000;
 	private Map<Integer, LogQuery> queries = new HashMap<Integer, LogQuery>();
 
 	public LogDbClient() {
 		this(new WebSocketTransport());
 	}
 
 	public LogDbClient(LogDbTransport transport) {
 		this.transport = transport;
 	}
 
 	/**
 	 * @since 0.6.0
 	 */
 	public int getFetchSize() {
 		return fetchSize;
 	}
 
 	/**
 	 * @since 0.6.0
 	 */
 	public void setFetchSize(int fetchSize) {
 		this.fetchSize = fetchSize;
 	}
 
 	public boolean isClosed() {
 		return session == null || session.isClosed();
 	}
 
 	public List<LogQuery> getQueries() {
 		return new ArrayList<LogQuery>(queries.values());
 	}
 
 	public LogQuery getQuery(int id) {
 		return queries.get(id);
 	}
 
 	public void connect(String host, String loginName, String password) throws IOException {
 		connect(host, 80, loginName, password);
 	}
 
 	public void connect(String host, int port, String loginName, String password) throws IOException {
 		this.session = transport.newSession(host, port);
 		this.session.login(loginName, password, true);
 		this.session.addListener(this);
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<ArchiveConfig> listArchiveConfigs() throws IOException {
 		List<ArchiveConfig> configs = new ArrayList<ArchiveConfig>();
 		Message resp = session.rpc("org.logpresso.core.msgbus.ArchivePlugin.getConfigs");
 		List<Map<String, Object>> l = (List<Map<String, Object>>) resp.get("configs");
 		for (Map<String, Object> m : l) {
 			configs.add(parseArchiveConfig(m));
 		}
 
 		return configs;
 	}
 
 	@SuppressWarnings("unchecked")
 	public ArchiveConfig getArchiveConfig(String loggerName) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("logger", loggerName);
 
 		Message resp = session.rpc("org.logpresso.core.msgbus.ArchivePlugin.getConfig", params);
 		Map<String, Object> m = (Map<String, Object>) resp.getParameters().get("config");
 		return parseArchiveConfig(m);
 	}
 
 	@SuppressWarnings("unchecked")
 	private ArchiveConfig parseArchiveConfig(Map<String, Object> m) {
 		ArchiveConfig c = new ArchiveConfig();
 		c.setLoggerName((String) m.get("logger"));
 		c.setTableName((String) m.get("table"));
 		c.setHost((String) m.get("host"));
 		c.setEnabled((Boolean) m.get("enabled"));
 		c.setMetadata((Map<String, String>) m.get("metadata"));
 		return c;
 	}
 
 	public void createArchiveConfig(ArchiveConfig config) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("logger", config.getLoggerName());
 		params.put("table", config.getTableName());
 		params.put("host", config.getHost());
 		params.put("enabled", config.isEnabled());
 		params.put("metadata", config.getMetadata());
 		session.rpc("org.logpresso.core.msgbus.ArchivePlugin.createConfig", params);
 	}
 
 	public void removeArchiveConfig(String loggerName) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("logger", loggerName);
 		session.rpc("org.logpresso.core.msgbus.ArchivePlugin.removeConfig", params);
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<AccountInfo> listAccounts() throws IOException {
 		Message resp = session.rpc("org.araqne.logdb.msgbus.ManagementPlugin.listAccounts");
 		List<AccountInfo> accounts = new ArrayList<AccountInfo>();
 		List<Object> l = (List<Object>) resp.get("accounts");
 		for (Object o : l) {
 			Map<String, Object> m = (Map<String, Object>) o;
 			List<Object> pl = (List<Object>) m.get("privileges");
 
 			AccountInfo account = new AccountInfo();
 			String loginName = (String) m.get("login_name");
 			account.setLoginName(loginName);
 
 			for (Object o2 : pl) {
 				Map<String, Object> m2 = (Map<String, Object>) o2;
 				String tableName = (String) m2.get("table_name");
 				Privilege p = new Privilege(loginName, tableName);
 				account.getPrivileges().add(p);
 			}
 			accounts.add(account);
 		}
 
 		return accounts;
 	}
 
 	public void createAccount(AccountInfo account) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("login_name", account.getLoginName());
 		params.put("password", account.getPassword());
 
 		session.rpc("org.araqne.logdb.msgbus.ManagementPlugin.createAccount", params);
 	}
 
 	public void removeAccount(String loginName) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("login_name", loginName);
 
 		session.rpc("org.araqne.logdb.msgbus.ManagementPlugin.removeAccount", params);
 	}
 
 	public void changePassword(String loginName, String password) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("login_name", loginName);
 		params.put("password", password);
 
 		session.rpc("org.araqne.logdb.msgbus.ManagementPlugin.changePassword", params);
 	}
 
 	public void grantPrivilege(Privilege privilege) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("login_name", privilege.getLoginName());
 		params.put("table_name", privilege.getTableName());
 
 		session.rpc("org.araqne.logdb.msgbus.ManagementPlugin.grantPrivilege", params);
 	}
 
 	public void revokePrivilege(Privilege privilege) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("login_name", privilege.getLoginName());
 		params.put("table_name", privilege.getTableName());
 
 		session.rpc("org.araqne.logdb.msgbus.ManagementPlugin.revokePrivilege", params);
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<IndexTokenizerFactoryInfo> listIndexTokenizerFactories() throws IOException {
 		Message resp = session.rpc("com.logpresso.index.msgbus.ManagementPlugin.listIndexTokenizerFactories");
 
 		List<IndexTokenizerFactoryInfo> l = new ArrayList<IndexTokenizerFactoryInfo>();
 		for (Object o : (List<Object>) resp.getParameters().get("factories")) {
 			IndexTokenizerFactoryInfo f = parseIndexTokenizerFactory(o);
 			l.add(f);
 		}
 
 		return l;
 	}
 
 	public IndexTokenizerFactoryInfo getIndexTokenizerFactory(String name) throws IOException {
		Message resp = session.rpc("com.logpresso.index.msgbus.ManagementPlugin.getIndexTokenizerFactory");
 		return parseIndexTokenizerFactory(resp.getParameters().get("factory"));
 	}
 
 	@SuppressWarnings("unchecked")
 	private IndexTokenizerFactoryInfo parseIndexTokenizerFactory(Object o) {
 		Map<String, Object> m = (Map<String, Object>) o;
 		IndexTokenizerFactoryInfo f = new IndexTokenizerFactoryInfo();
 		f.setName((String) m.get("name"));
 		f.setConfigSpecs(parseIndexConfigList((List<Object>) m.get("config_specs")));
 		return f;
 	}
 
 	@SuppressWarnings("unchecked")
 	private List<IndexConfigSpec> parseIndexConfigList(List<Object> l) {
 		List<IndexConfigSpec> specs = new ArrayList<IndexConfigSpec>();
 
 		for (Object o : l) {
 			Map<String, Object> m = (Map<String, Object>) o;
 			IndexConfigSpec spec = new IndexConfigSpec();
 			spec.setKey((String) m.get("key"));
 			spec.setName((String) m.get("name"));
 			spec.setDescription((String) m.get("description"));
 			spec.setRequired((Boolean) m.get("required"));
 			specs.add(spec);
 		}
 
 		return specs;
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<IndexInfo> listIndexes(String tableName) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("table", tableName);
 
 		Message resp = session.rpc("com.logpresso.index.msgbus.ManagementPlugin.listIndexes", params);
 		List<IndexInfo> indexes = new ArrayList<IndexInfo>();
 
 		List<Object> l = (List<Object>) resp.getParameters().get("indexes");
 		for (Object o : l) {
 			Map<String, Object> m = (Map<String, Object>) o;
 			IndexInfo indexInfo = getIndexInfo(m);
 			indexes.add(indexInfo);
 		}
 
 		return indexes;
 	}
 
 	@SuppressWarnings("unchecked")
 	public IndexInfo getIndexInfo(String tableName, String indexName) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("table", tableName);
 		params.put("index", indexName);
 
 		Message resp = session.rpc("com.logpresso.index.msgbus.ManagementPlugin.getIndexInfo", params);
 		return getIndexInfo((Map<String, Object>) resp.getParameters().get("index"));
 	}
 
 	@SuppressWarnings("unchecked")
 	private IndexInfo getIndexInfo(Map<String, Object> m) {
 		IndexInfo index = new IndexInfo();
 		index.setTableName((String) m.get("table"));
 		index.setIndexName((String) m.get("index"));
 		index.setTokenizerName((String) m.get("tokenizer_name"));
 		index.setTokenizerConfigs((Map<String, String>) m.get("tokenizer_configs"));
 
 		try {
 			SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
 			String s = (String) m.get("min_index_day");
 			if (s != null)
 				index.setMinIndexDay(f.parse(s));
 		} catch (ParseException e) {
 		}
 
 		index.setUseBloomFilter((Boolean) m.get("use_bloom_filter"));
 		index.setBloomFilterCapacity0((Integer) m.get("bf_lv0_capacity"));
 		index.setBloomFilterErrorRate0((Double) m.get("bf_lv0_error_rate"));
 		index.setBloomFilterCapacity1((Integer) m.get("bf_lv1_capacity"));
 		index.setBloomFilterErrorRate1((Double) m.get("bf_lv1_error_rate"));
 		index.setBasePath((String) m.get("base_path"));
 		index.setBuildPastIndex((Boolean) m.get("build_past_index"));
 
 		return index;
 	}
 
 	public void createIndex(IndexInfo info) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("table", info.getTableName());
 		params.put("index", info.getIndexName());
 		params.put("tokenizer_name", info.getTokenizerName());
 		params.put("tokenizer_configs", info.getTokenizerConfigs());
 		params.put("base_path", info.getBasePath());
 		params.put("use_bloom_filter", info.isUseBloomFilter());
 		params.put("bf_lv0_capacity", info.getBloomFilterCapacity0());
 		params.put("bf_lv0_error_rate", info.getBloomFilterErrorRate0());
 		params.put("bf_lv1_capacity", info.getBloomFilterCapacity1());
 		params.put("bf_lv1_error_rate", info.getBloomFilterErrorRate1());
 		params.put("min_index_day", info.getMinIndexDay());
 		params.put("build_past_index", info.isBuildPastIndex());
 
 		session.rpc("com.logpresso.index.msgbus.ManagementPlugin.createIndex", params);
 	}
 
 	public void dropIndex(String tableName, String indexName) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("table", tableName);
 		params.put("index", indexName);
 
 		session.rpc("com.logpresso.index.msgbus.ManagementPlugin.dropIndex", params);
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<TableInfo> listTables() throws IOException {
 		Message resp = session.rpc("org.araqne.logdb.msgbus.ManagementPlugin.listTables");
 		List<TableInfo> tables = new ArrayList<TableInfo>();
 		Map<String, Object> m = (Map<String, Object>) resp.getParameters().get("tables");
 		for (String tableName : m.keySet()) {
 			Map<String, Object> params = (Map<String, Object>) m.get(tableName);
 			TableInfo tableInfo = getTableInfo(tableName, params);
 			tables.add(tableInfo);
 		}
 
 		return tables;
 	}
 
 	@SuppressWarnings("unchecked")
 	public TableInfo getTableInfo(String tableName) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("table", tableName);
 		Message resp = session.rpc("org.araqne.logdb.msgbus.ManagementPlugin.getTableInfo", params);
 
 		return getTableInfo(tableName, (Map<String, Object>) resp.get("table"));
 	}
 
 	private TableInfo getTableInfo(String tableName, Map<String, Object> params) {
 		Map<String, String> metadata = new HashMap<String, String>();
 		for (Entry<String, Object> pair : params.entrySet())
 			metadata.put(pair.getKey(), pair.getValue() == null ? null : pair.getValue().toString());
 		return new TableInfo(tableName, metadata);
 	}
 
 	public void setTableMetadata(String tableName, Map<String, String> config) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("table", tableName);
 		params.put("metadata", config);
 
 		session.rpc("org.araqne.logdb.msgbus.ManagementPlugin.setTableMetadata", params);
 	}
 
 	public void unsetTableMetadata(String tableName, Set<String> keySet) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("table", tableName);
 		params.put("keys", keySet);
 
 		session.rpc("org.araqne.logdb.msgbus.ManagementPlugin.unsetTableMetadata", params);
 	}
 
 	public void createTable(String tableName) throws IOException {
 		createTable(tableName, null);
 	}
 
 	public void createTable(String tableName, Map<String, String> metadata) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("table", tableName);
 		params.put("metadata", metadata);
 		session.rpc("org.araqne.logdb.msgbus.ManagementPlugin.createTable", params);
 	}
 
 	public void dropTable(String tableName) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("table", tableName);
 		session.rpc("org.araqne.logdb.msgbus.ManagementPlugin.dropTable", params);
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<LoggerFactoryInfo> listLoggerFactories() throws IOException {
 		Message resp = session.rpc("org.araqne.log.api.msgbus.LoggerPlugin.getLoggerFactories");
 
 		List<LoggerFactoryInfo> factories = new ArrayList<LoggerFactoryInfo>();
 		List<Object> l = (List<Object>) resp.get("factories");
 		for (Object o : l) {
 			parseLoggerFactoryInfo(factories, o);
 		}
 
 		return factories;
 	}
 
 	@SuppressWarnings("unchecked")
 	public LoggerFactoryInfo getLoggerFactoryInfo(String factoryName) throws IOException {
 		List<LoggerFactoryInfo> factories = listLoggerFactories();
 		LoggerFactoryInfo found = null;
 
 		for (LoggerFactoryInfo f : factories) {
 			if (f.getNamespace().equals("local") && f.getName().equals(factoryName)) {
 				found = f;
 				break;
 			}
 		}
 
 		if (found == null)
 			throw new IllegalStateException("logger factory not found: " + factoryName);
 
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("factory", factoryName);
 		Message resp2 = session.rpc("org.araqne.log.api.msgbus.LoggerPlugin.getFactoryOptions", params);
 		List<ConfigSpec> configSpecs = parseConfigList((List<Object>) resp2.get("options"));
 		found.setConfigSpecs(configSpecs);
 		return found;
 	}
 
 	@SuppressWarnings("unchecked")
 	private void parseLoggerFactoryInfo(List<LoggerFactoryInfo> factories, Object o) {
 		Map<String, Object> m = (Map<String, Object>) o;
 
 		LoggerFactoryInfo f = new LoggerFactoryInfo();
 		f.setFullName((String) m.get("full_name"));
 		f.setDisplayName((String) m.get("display_name"));
 		f.setNamespace((String) m.get("namespace"));
 		f.setName((String) m.get("name"));
 		f.setDescription((String) m.get("description"));
 
 		factories.add(f);
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<ParserFactoryInfo> listParserFactories() throws IOException {
 		Message resp = session.rpc("org.araqne.log.api.msgbus.LoggerPlugin.getParserFactories");
 		List<Object> l = (List<Object>) resp.get("factories");
 
 		List<ParserFactoryInfo> parsers = new ArrayList<ParserFactoryInfo>();
 		for (Object o : l) {
 			Map<String, Object> m = (Map<String, Object>) o;
 
 			ParserFactoryInfo f = new ParserFactoryInfo();
 			f.setName((String) m.get("name"));
 			f.setDisplayName((String) m.get("display_name"));
 			f.setDescription((String) m.get("description"));
 			f.setConfigSpecs(parseConfigList((List<Object>) m.get("options")));
 			parsers.add(f);
 		}
 
 		return parsers;
 	}
 
 	@SuppressWarnings("unchecked")
 	private List<ConfigSpec> parseConfigList(List<Object> l) {
 		List<ConfigSpec> specs = new ArrayList<ConfigSpec>();
 
 		for (Object o : l) {
 			Map<String, Object> m = (Map<String, Object>) o;
 			ConfigSpec spec = new ConfigSpec();
 			spec.setName((String) m.get("name"));
 			spec.setDescription((String) m.get("description"));
 			spec.setDisplayName((String) m.get("display_name"));
 			spec.setType((String) m.get("type"));
 			spec.setRequired((Boolean) m.get("required"));
 			spec.setDefaultValue((String) m.get("default_value"));
 			specs.add(spec);
 		}
 
 		return specs;
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<LoggerInfo> listLoggers() throws IOException {
 		Message resp = session.rpc("org.araqne.log.api.msgbus.LoggerPlugin.getLoggers");
 		List<Object> l = (List<Object>) resp.get("loggers");
 
 		List<LoggerInfo> loggers = new ArrayList<LoggerInfo>();
 		for (Object o : l) {
 			Map<String, Object> m = (Map<String, Object>) o;
 
 			LoggerInfo lo = new LoggerInfo();
 			lo.setNamespace((String) m.get("namespace"));
 			lo.setName((String) m.get("name"));
 			lo.setFactoryName((String) m.get("factory_full_name"));
 			lo.setDescription((String) m.get("description"));
 			lo.setPassive((Boolean) m.get("is_passive"));
 			lo.setInterval((Integer) m.get("interval"));
 			lo.setStatus((String) m.get("status"));
 			lo.setLastStartAt(parseDate((String) m.get("last_start")));
 			lo.setLastRunAt(parseDate((String) m.get("last_run")));
 			lo.setLastLogAt(parseDate((String) m.get("last_log")));
 			lo.setLogCount(Long.valueOf(m.get("log_count").toString()));
 
 			loggers.add(lo);
 		}
 
 		return loggers;
 	}
 
 	private Date parseDate(String s) {
 		if (s == null)
 			return null;
 
 		try {
 			SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
 			return f.parse(s);
 		} catch (ParseException e) {
 			return null;
 		}
 	}
 
 	public void createLogger(LoggerInfo logger) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("factory", logger.getFactoryName());
 		params.put("namespace", logger.getNamespace());
 		params.put("name", logger.getName());
 		params.put("description", logger.getDescription());
 		params.put("options", logger.getConfigs());
 
 		session.rpc("org.araqne.log.api.msgbus.LoggerPlugin.createLogger", params);
 	}
 
 	public void removeLogger(String fullName) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("logger", fullName);
 		session.rpc("org.araqne.log.api.msgbus.LoggerPlugin.removeLogger", params);
 	}
 
 	public void startLogger(String fullName, int interval) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("logger", fullName);
 		params.put("interval", interval);
 		session.rpc("org.araqne.log.api.msgbus.LoggerPlugin.startLogger", params);
 	}
 
 	public void stopLogger(String fullName, int waitTime) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("logger", fullName);
 		params.put("wait_time", waitTime);
 		session.rpc("org.araqne.log.api.msgbus.LoggerPlugin.stopLogger", params);
 	}
 
 	public LogCursor query(String queryString) throws IOException {
 		int id = createQuery(queryString);
 		startQuery(id);
 		LogQuery q = queries.get(id);
 		q.waitUntil(null);
 		long total = q.getLoadedCount();
 
 		return new LogCursorImpl(id, 0L, total, true, fetchSize);
 	}
 
 	private class LogCursorImpl implements LogCursor {
 
 		private int id;
 		private long offset;
 		private long limit;
 		private boolean removeOnClose;
 
 		private long p;
 		private Map<String, Object> cached;
 		private Long currentCacheOffset;
 		private Long nextCacheOffset;
 		private int fetchUnit;
 		private Map<String, Object> prefetch;
 
 		public LogCursorImpl(int id, long offset, long limit, boolean removeOnClose, int fetchUnit) {
 			this.id = id;
 			this.offset = offset;
 			this.limit = limit;
 			this.removeOnClose = removeOnClose;
 
 			this.p = offset;
 			this.nextCacheOffset = offset;
 			this.fetchUnit = fetchUnit;
 		}
 
 		@SuppressWarnings("unchecked")
 		@Override
 		public boolean hasNext() {
 			if (prefetch != null)
 				return true;
 
 			if (p < offset || p >= offset + limit)
 				return false;
 
 			try {
 				if (cached == null || p >= currentCacheOffset + fetchUnit) {
 					cached = getResult(id, nextCacheOffset, fetchUnit);
 					currentCacheOffset = nextCacheOffset;
 					nextCacheOffset += fetchUnit;
 				}
 
 				int relative = (int) (p - currentCacheOffset);
 				List<Object> l = (List<Object>) cached.get("result");
 				if (relative >= l.size())
 					return false;
 
 				prefetch = (Map<String, Object>) l.get(relative);
 				p++;
 				return true;
 			} catch (IOException e) {
 				logger.error("araqne logdb client: cannot fetch log query result", e);
 				return false;
 			}
 		}
 
 		@Override
 		public Map<String, Object> next() {
 			if (!hasNext())
 				throw new NoSuchElementException("end of log cursor");
 
 			Map<String, Object> m = prefetch;
 			prefetch = null;
 			return m;
 		}
 
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public void close() throws IOException {
 			if (removeOnClose)
 				removeQuery(id);
 		}
 	}
 
 	public int createQuery(String queryString) throws IOException {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("query", queryString);
 
 		Message resp = session.rpc("org.araqne.logdb.msgbus.LogQueryPlugin.createQuery", params);
 		int id = resp.getInt("id");
 		session.registerTrap("logstorage-query-" + id);
 		session.registerTrap("logstorage-query-timeline-" + id);
 
 		queries.put(id, new LogQuery(id, queryString));
 		return id;
 	}
 
 	public void startQuery(int id) throws IOException {
 		startQuery(id, 10, 10);
 	}
 
 	public void startQuery(int id, int pageSize, int timelineSize) throws IOException {
 		verifyQueryId(id);
 
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("id", id);
 		params.put("offset", 0);
 		params.put("limit", pageSize);
 		params.put("timeline_limit", timelineSize);
 
 		session.rpc("org.araqne.logdb.msgbus.LogQueryPlugin.startQuery", params);
 	}
 
 	public void stopQuery(int id) throws IOException {
 		verifyQueryId(id);
 
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("id", id);
 
 		session.rpc("org.araqne.logdb.msgbus.LogQueryPlugin.stopQuery", params);
 	}
 
 	public void removeQuery(int id) throws IOException {
 		verifyQueryId(id);
 
 		session.unregisterTrap("logstorage-query-" + id);
 		session.unregisterTrap("logstorage-query-timeline-" + id);
 
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("id", id);
 		session.rpc("org.araqne.logdb.msgbus.LogQueryPlugin.removeQuery", params);
 
 		queries.remove(id);
 	}
 
 	public void waitUntil(int id, Long count) {
 		verifyQueryId(id);
 		queries.get(id).waitUntil(count);
 	}
 
 	public Map<String, Object> getResult(int id, long offset, int limit) throws IOException {
 		verifyQueryId(id);
 
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("id", id);
 		params.put("offset", offset);
 		params.put("limit", limit);
 		params.put("binary_encode", true);
 
 		Message resp = session.rpc("org.araqne.logdb.msgbus.LogQueryPlugin.getResult", params);
 		if (resp.getParameters().size() == 0)
 			throw new MessageException("query-not-found", "", resp.getParameters());
 
 		// support backward compatibility
 		if (!resp.getParameters().containsKey("uncompressed_size"))
 			return resp.getParameters();
 
 		// decompress and decode
 		int uncompressedSize = (Integer) resp.getParameters().get("uncompressed_size");
 		String binary = (String) resp.getParameters().get("binary");
 		return decodeBinary(binary, uncompressedSize);
 	}
 
 	private Map<String, Object> decodeBinary(String binary, int uncompressedSize) {
 		byte[] b = Base64.decode(binary);
 		byte[] uncompressed = new byte[uncompressedSize];
 		uncompress(uncompressed, b);
 
 		Map<String, Object> m = EncodingRule.decodeMap(ByteBuffer.wrap(uncompressed));
 
 		Object[] resultArray = (Object[]) m.get("result");
 		List<Object> resultList = new ArrayList<Object>(resultArray.length);
 		for (int i = 0; i < resultArray.length; i++)
 			resultList.add(resultArray[i]);
 
 		m.put("result", resultList);
 
 		return m;
 	}
 
 	private void uncompress(byte[] output, byte[] b) {
 		Inflater inflater = new Inflater();
 		inflater.setInput(b, 0, b.length);
 		try {
 			inflater.inflate(output);
 			inflater.reset();
 		} catch (DataFormatException e) {
 			throw new IllegalStateException(e);
 		} finally {
 			inflater.end();
 		}
 	}
 
 	private void verifyQueryId(int id) {
 		if (!queries.containsKey(id))
 			throw new MessageException("query-not-found", "query [" + id + "] does not exist", null);
 	}
 
 	public void close() {
 		if (session != null)
 			session.close();
 	}
 
 	@Override
 	public void onTrap(Message msg) {
 		if (msg.getMethod().startsWith("logstorage-query-timeline")) {
 			int id = msg.getInt("id");
 			LogQuery q = queries.get(id);
 			q.updateCount(msg.getLong("count"));
 			if (msg.getString("type").equals("eof"))
 				q.updateStatus("Ended");
 		} else if (msg.getMethod().startsWith("logstorage-query")) {
 			int id = msg.getInt("id");
 			LogQuery q = queries.get(id);
 			if (msg.getString("type").equals("eof")) {
 				q.updateCount(msg.getLong("total_count"));
 				q.updateStatus("Ended");
 			} else if (msg.getString("type").equals("page_loaded")) {
 				q.updateCount(msg.getLong("count"));
 				q.updateStatus("Running");
 			} else if (msg.getString("type").equals("status_change")) {
 				q.updateCount(msg.getLong("count"));
 				q.updateStatus(msg.getString("status"));
 			}
 		}
 	}
 
 	@Override
 	public void onClose(Throwable t) {
 		close();
 	}
 }
