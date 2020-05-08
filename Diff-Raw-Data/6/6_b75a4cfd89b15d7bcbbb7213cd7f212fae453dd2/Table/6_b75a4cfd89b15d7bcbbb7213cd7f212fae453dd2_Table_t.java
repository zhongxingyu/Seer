 /*
  * Copyright 2011 Future Systems
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
 package org.araqne.logdb.query.command;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.araqne.log.api.LogParser;
 import org.araqne.log.api.LogParserFactory;
 import org.araqne.log.api.LogParserFactoryRegistry;
 import org.araqne.log.api.LoggerConfigOption;
 import org.araqne.logdb.LogMap;
 import org.araqne.logdb.LogQueryCommand;
 import org.araqne.logstorage.Log;
 import org.araqne.logstorage.LogSearchCallback;
 import org.araqne.logstorage.LogStorage;
 import org.araqne.logstorage.LogTableRegistry;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Table extends LogQueryCommand {
 	private final Logger logger = LoggerFactory.getLogger(Table.class);
 	private LogStorage storage;
 	private LogTableRegistry tableRegistry;
 	private LogParserFactoryRegistry parserFactoryRegistry;
 
 	private List<String> tableNames;
 	private long offset;
 	private long limit;
 	private Date from;
 	private Date to;
 	private long found;
 
 	public Table(List<String> tableNames) {
 		this(tableNames, 0);
 	}
 
 	public Table(List<String> tableNames, long limit) {
 		this(tableNames, limit, null, null);
 	}
 
 	public Table(List<String> tableNames, Date from, Date to) {
 		this(tableNames, 0, from, to);
 	}
 
 	public Table(List<String> tableNames, long limit, Date from, Date to) {
 		this(tableNames, 0, 0, from, to);
 	}
 
 	public Table(List<String> tableNames, long offset, long limit, Date from, Date to) {
 		this.tableNames = tableNames;
 		this.offset = offset;
 		this.limit = limit;
 		this.from = from;
 		this.to = to;
 	}
 
 	public List<String> getTableNames() {
 		return tableNames;
 	}
 
 	public void setTableNames(List<String> tableNames) {
 		this.tableNames = tableNames;
 	}
 
 	public LogStorage getStorage() {
 		return storage;
 	}
 
 	public void setStorage(LogStorage storage) {
 		this.storage = storage;
 	}
 
 	public LogTableRegistry getTableRegistry() {
 		return tableRegistry;
 	}
 
 	public void setTableRegistry(LogTableRegistry tableRegistry) {
 		this.tableRegistry = tableRegistry;
 	}
 
 	public LogParserFactoryRegistry getParserFactoryRegistry() {
 		return parserFactoryRegistry;
 	}
 
 	public void setParserFactoryRegistry(LogParserFactoryRegistry parserFactoryRegistry) {
 		this.parserFactoryRegistry = parserFactoryRegistry;
 	}
 
 	public long getOffset() {
 		return offset;
 	}
 
 	public void setOffset(long offset) {
 		this.offset = offset;
 	}
 
 	public long getLimit() {
 		return limit;
 	}
 
 	public void setLimit(long limit) {
 		this.limit = limit;
 	}
 
 	public Date getFrom() {
 		return from;
 	}
 
 	public Date getTo() {
 		return to;
 	}
 
 	@Override
 	public void start() {
 		try {
 			status = Status.Running;
 
 			for (String tableName : tableNames) {
 				String parserName = tableRegistry.getTableMetadata(tableName, "logparser");
 				LogParserFactory parserFactory = parserFactoryRegistry.get(parserName);
 				LogParser parser = null;
 				if (parserFactory != null) {
 					Properties prop = new Properties();
 					for (LoggerConfigOption configOption : parserFactory.getConfigOptions()) {
 						String optionName = configOption.getName();
 						String optionValue = tableRegistry.getTableMetadata(tableName, optionName);
						if (configOption.isRequired() && optionValue == null)
 							throw new IllegalArgumentException("require table metadata " + optionName);
 						prop.put(optionName, optionValue);
 					}
 					parser = parserFactory.createParser(prop);
 				}
 
 				long needed = limit - found;
 				if (limit != 0 && needed <= 0)
 					break;
 
				found += storage.search(tableName, from, to, offset, limit == 0 ? 0 : needed,
						new LogSearchCallbackImpl(parser));
 				if (offset > 0) {
 					if (found > offset) {
 						found -= offset;
 						offset = 0;
 					} else {
 						offset -= found;
 						found = 0;
 					}
 				}
 			}
 		} catch (InterruptedException e) {
 			logger.trace("araqne logdb: query interrupted");
 		} catch (Exception e) {
 			logger.error("araqne logdb: table exception", e);
 		} catch (Error e) {
 			logger.error("araqne logdb: table error", e);
 		}
 		eof();
 	}
 
 	@Override
 	public void push(LogMap m) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public boolean isReducer() {
 		return false;
 	}
 
 	private class LogSearchCallbackImpl implements LogSearchCallback {
 		private LogParser parser;
 
 		public LogSearchCallbackImpl(LogParser parser) {
 			this.parser = parser;
 		}
 
 		@Override
 		public void onLog(Log log) {
 			Map<String, Object> m = null;
 
 			if (parser != null) {
 				Map<String, Object> parsed = parser.parse(log.getData());
 				if (parsed != null) {
 					parsed.put("_table", log.getTableName());
 					parsed.put("_id", log.getId());
 					parsed.put("_time", log.getDate());
 					m = parsed;
 				} else {
 					logger.debug("araqne logdb: cannot parse log [{}]", log.getData());
 					return;
 				}
 			} else {
 				m = log.getData();
 				m.put("_table", log.getTableName());
 				m.put("_id", log.getId());
 				m.put("_time", log.getDate());
 			}
 
 			write(new LogMap(m));
 		}
 
 		@Override
 		public void interrupt() {
 			eof();
 		}
 
 		@Override
 		public boolean isInterrupted() {
 			return status.equals(Status.End);
 		}
 	}
 }
