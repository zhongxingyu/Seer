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
 package org.araqne.logstorage.engine;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentMap;
 
 import org.araqne.codec.FastEncodingRule;
 import org.araqne.logstorage.CachedRandomSeeker;
 import org.araqne.logstorage.Log;
 import org.araqne.logstorage.LogMarshaler;
 import org.araqne.logstorage.LogTableRegistry;
 import org.araqne.logstorage.file.LogFileReader;
 import org.araqne.logstorage.file.LogRecord;
 
 /**
  * not thread-safe
  * 
  * @author xeraph
  * @since 0.9
  */
 public class CachedRandomSeekerImpl implements CachedRandomSeeker {
 	private boolean closed;
 	private LogTableRegistry tableRegistry;
 	private LogFileFetcher fetcher;
 
 	private ConcurrentMap<OnlineWriterKey, OnlineWriter> onlineWriters;
 	private Map<OnlineWriterKey, List<Log>> onlineBuffers;
 	private Map<TabletKey, LogFileReader> cachedReaders;
 
 	public CachedRandomSeekerImpl(LogTableRegistry tableRegistry, LogFileFetcher fetcher,
 			ConcurrentMap<OnlineWriterKey, OnlineWriter> onlineWriters) {
 		this.tableRegistry = tableRegistry;
 		this.fetcher = fetcher;
 		this.onlineWriters = onlineWriters;
 		this.onlineBuffers = new HashMap<OnlineWriterKey, List<Log>>();
 		this.cachedReaders = new HashMap<TabletKey, LogFileReader>();
 	}
 
 	private Log getLogFromOnlineWriter(String tableName, int tableId, Date day, long id) {
 		OnlineWriterKey onlineKey = new OnlineWriterKey(tableName, day, tableId);
 		List<Log> buffer = onlineBuffers.get(onlineKey);
 		if (buffer == null) {
 			// try load on demand
 			OnlineWriter writer = onlineWriters.get(onlineKey);
 			if (writer != null) {
 				buffer = writer.getBuffer();
 				onlineBuffers.put(onlineKey, buffer);
 			}
 		}
 
 		if (buffer != null) {
 			for (Log r : buffer)
 				if (r.getId() == id) {
 					return r;
 				}
 		}
 		return null;
 	}
 
 	private List<Log> getLogsFromOnlineWriter(String tableName, int tableId, Date day, List<Long> ids) {
 		OnlineWriterKey onlineKey = new OnlineWriterKey(tableName, day, tableId);
 		List<Log> buffer = onlineBuffers.get(onlineKey);
 		if (buffer == null) {
 			// try load on demand
 			OnlineWriter writer = onlineWriters.get(onlineKey);
 			if (writer != null) {
 				buffer = writer.getBuffer();
 				onlineBuffers.put(onlineKey, buffer);
 			}
 		}
 		
 		List<Log> ret = new ArrayList<Log>();
 		if (buffer != null) {
 			for (Log r : buffer) {
 				if (Collections.binarySearch(ids, r.getId(), Collections.reverseOrder()) >= 0) {
 					ret.add(r);
 				}
 			}
 		}
 		
		Collections.sort(ret, Collections.reverseOrder());
 		return ret;
 	}
 	
 	// TODO : remove duplicated method convert (LogStorageEngine.convert())
 	private LogRecord convert(Log log) {
 		ByteBuffer bb = new FastEncodingRule().encode(log.getData());
 		LogRecord logdata = new LogRecord(log.getDate(), log.getId(), bb);
 		log.setBinaryLength(bb.remaining());
 		return logdata;
 	}
 	
 	private LogFileReader getReader(String tableName, int tableId, Date day) throws IOException {
 		TabletKey key = new TabletKey(tableId, day);
 		LogFileReader reader = cachedReaders.get(key);
 		if (reader == null) {
 			reader = fetcher.fetch(tableName, day);
 			cachedReaders.put(key, reader);
 		}
 		return reader;
 	}
 
 	@Override
 	public LogRecord getLogRecord(String tableName, Date day, long id) throws IOException {
 		if (closed)
 			throw new IllegalStateException("already closed");
 
 		int tableId = tableRegistry.getTableId(tableName);
 
 		// check memory buffer (flush waiting)
 		Log bufferedLog = getLogFromOnlineWriter(tableName, tableId, day, id);
 		if (bufferedLog != null) {
 			return convert(bufferedLog);
 		}
 
 		LogFileReader reader = getReader(tableName, tableId, day);
 		return reader.find(id);
 	}
 	
 	private List<Long> getFileLogIds(List<Log> onlineLogs, List<Long> ids) {
 		int onlineLogCnt = onlineLogs.size();
 		int retCnt = ids.size() - onlineLogCnt;
 		List<Long> ret = new ArrayList<Long>(retCnt);
 		int idx = 0;
 
 		for (long id : ids) {
 			// online logs' ids are subset of requested ids.
 			if (idx < onlineLogCnt && onlineLogs.get(idx).getId() == id) {
 				++idx;
 				continue;
 			}
 
 			ret.add(id);
 		}
 		
 		if (ret.size() != retCnt) {
 			throw new IllegalStateException("log ids are wrong");
 		}
 		
 		return ret;
 	}
 	
 	@Override
 	public List<LogRecord> getLogRecords(String tableName, Date day, List<Long> ids) {
 		if (closed)
 			throw new IllegalStateException("already closed");
 
 		int tableId = tableRegistry.getTableId(tableName);
 
 		List<LogRecord> ret = new ArrayList<LogRecord>(ids.size());
 		List<Log> onlineLogs = getLogsFromOnlineWriter(tableName, tableId, day, ids);
 		List<Long> fileLogIds = getFileLogIds(onlineLogs, ids);
 		List<LogRecord> fileLogRecords = null;
 
 		try {
 			LogFileReader reader = getReader(tableName, tableId, day);
 			fileLogRecords = reader.find(fileLogIds);
 		} catch (IOException e) {
 		}
 		
 		// merge online log and file log
 		int i = 0;
 		int j = 0;
 		for (long id : ids) {
 			if (i < onlineLogs.size()) {
 				 Log l = onlineLogs.get(i);
 				 if (l.getId() == id) {
 					 ret.add(convert(l));
 					 ++i;
 				 }
 			} else if (fileLogRecords != null && j < fileLogRecords.size()) {
 				LogRecord r = fileLogRecords.get(j);
 				if (r.getId() == id) {
 					ret.add(r);
 					++j;
 				}
 			}
 		}
 
 		return ret;
 	}
 	
 	@Override
 	public Log getLog(String tableName, Date day, long id) throws IOException {
 		if (closed)
 			throw new IllegalStateException("already closed");
 
 		int tableId = tableRegistry.getTableId(tableName);
 
 		// check memory buffer (flush waiting)
 		Log bufferedLog = getLogFromOnlineWriter(tableName, tableId, day, id);
 		if (bufferedLog != null) 
 			return bufferedLog;
 
 		LogFileReader reader = getReader(tableName, tableId, day);
 		LogRecord log = reader.find(id);
 		if (log == null)
 			return null;
 
 		return LogMarshaler.convert(tableName, log);
 	}
 
 	@Override
 	public void close() {
 		if (closed)
 			return;
 
 		closed = true;
 
 		for (LogFileReader reader : cachedReaders.values()) {
 			reader.close();
 		}
 
 		cachedReaders.clear();
 	}
 }
