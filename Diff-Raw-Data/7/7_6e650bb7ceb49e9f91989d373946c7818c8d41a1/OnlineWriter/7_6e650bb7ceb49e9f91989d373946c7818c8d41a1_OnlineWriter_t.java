 /*
  * Copyright 2010 NCHOVY
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
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.atomic.AtomicLong;
 
 import org.araqne.logstorage.CallbackSet;
 import org.araqne.logstorage.Log;
 import org.araqne.logstorage.LogFileService;
 import org.araqne.logstorage.TableConfig;
 import org.araqne.logstorage.TableSchema;
 import org.araqne.logstorage.file.DatapathUtil;
 import org.araqne.logstorage.file.LogFileWriter;
 import org.araqne.storage.api.FilePath;
 import org.araqne.storage.api.StorageManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class OnlineWriter {
 	private final Logger logger = LoggerFactory.getLogger(OnlineWriter.class.getName());
 
 	/**
 	 * table id
 	 */
 	private int tableId;
 
 	/**
 	 * is in closing state?
 	 */
 	private boolean closing;
 
 	/**
 	 * only yyyy-MM-dd (excluding hour, min, sec, milli)
 	 */
 	private Date day;
 
 	/**
 	 * maintain last write access time. idle writer should be evicted
 	 */
 	private Date lastAccess = new Date();
 	private AtomicLong nextId;
 
 	/**
 	 * binary log file writer
 	 */
 	private LogFileWriter writer;
 
 	private final LogFileService logFileService;
 
 	private volatile boolean closeReserved;
 
 	public OnlineWriter(StorageManager storageManager, LogFileService logFileService, TableSchema schema, Date day, 
 			CallbackSet callbackSet)
 			throws IOException {
 		this.logFileService = logFileService;
 		this.tableId = schema.getId();
 		this.day = day;
 		this.closeReserved = new Boolean(false);
 		
 		String basePathString = schema.getPrimaryStorage().getBasePath();
 		FilePath basePath = null;
 		if (basePathString != null)
 			basePath = storageManager.resolveFilePath(basePathString);
 
 		FilePath indexPath = DatapathUtil.getIndexFile(tableId, day, basePath);
 		FilePath dataPath = DatapathUtil.getDataFile(tableId, day, basePath);
 		FilePath keyPath = DatapathUtil.getKeyFile(tableId, day, basePath);
 
 		indexPath.getParentFilePath().mkdirs();
 		dataPath.getParentFilePath().mkdirs();
 
 		try {
 			// options including table metadata
 			Map<String, Object> writerOptions = new HashMap<String, Object>();
			writerOptions.put("storageConfig", schema.getPrimaryStorage());
 			writerOptions.putAll(schema.getMetadata());
 			writerOptions.put("tableName", schema.getName());
 			writerOptions.put("day", day);
 			writerOptions.put("indexPath", indexPath);
 			writerOptions.put("dataPath", dataPath);
 			writerOptions.put("keyPath", keyPath);
 			writerOptions.put("callbackSet", callbackSet);
 			
 			for (TableConfig c : schema.getPrimaryStorage().getConfigs()) {
 				writerOptions.put(c.getKey(), c.getValues().size() > 1 ? c.getValues() : c.getValue());
 			}
 
 			writer = this.logFileService.newWriter(writerOptions);
 		} catch (IllegalArgumentException e) {
 			throw e;
 		} catch (Throwable t) {
 			throw new IllegalStateException("araqne-logstorage: unexpected error", t);
 		}
 		nextId = new AtomicLong(writer.getLastKey());
 	}
 
 	public boolean isOpen() {
 		return (writer != null && !writer.isClosed()) && closing == false;
 	}
 
 	public boolean isClosed() {
 		return closing == true && (writer.isClosed());
 	}
 
 	public int getTableId() {
 		return tableId;
 	}
 
 	public Date getDay() {
 		return day;
 	}
 
 	private long nextId() {
 		// do NOT update last access here
 		return nextId.incrementAndGet();
 	}
 
 	public Date getLastAccess() {
 		return lastAccess;
 	}
 
 	public Date getLastFlush() {
 		return writer.getLastFlush();
 	}
 	
 	public void write(Log log) throws IOException {
 		synchronized (this) {
 			if (writer == null)
 				throw new IOException("file closed");
 
 			long nid = nextId();
 
 			log.setId(nid);
 
 			// prevent external id modification
 			writer.write(log.shallowCopy());
 			lastAccess = new Date();
 		}
 	}
 
 	public void write(List<Log> logs) throws IOException {
 		if (writer == null)
 			throw new IllegalStateException("file closed");
 
 		synchronized (this) {
 			ArrayList<Log> copy = new ArrayList<Log>(logs.size());
 			for (Log record : logs) {
 				record.setId(nextId());
 				copy.add(record.shallowCopy());
 			}
 
 			writer.write(copy);
 			lastAccess = new Date();
 		}
 	}
 
 	public List<Log> getBuffer() {
 		// all log file writer should have lock free implementation
 		return writer.getBuffer();
 		// synchronized (this) {
 		// // return new ArrayList<LogRecord>(writer.getBuffer());
 		// List<Log> buffers = writer.getBuffer();
 		// int bufSize = 0;
 		// for (List<Log> buffer : buffers) {
 		// bufSize += buffer.size();
 		// }
 		// List<Log> merged = new ArrayList<Log>(bufSize);
 		// for (List<Log> buffer : buffers) {
 		// merged.addAll(buffer);
 		// }
 		// return merged;
 		// }
 	}
 
 	public void flush() throws IOException {
 		if (logger.isTraceEnabled()) {
 			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 			logger.trace("araqne logstorage: flushing log table [{}], day [{}]", tableId, dateFormat.format(day));
 		}
 
 		synchronized (this) {
 			writer.flush(true);
 			notifyAll();
 		}
 	}
 
 	public void sync() throws IOException {
 		// intentional access without lock
 		writer.sync();
 	}
 
 	public void close() {
 		if (closing)
 			return;
 
 		try {
 			synchronized (this) {
 				closing = true;
 				writer.close();
 				notifyAll();
 				closeMonitor.countDown();
 			}
 
 		} catch (IOException e) {
 			logger.error("cannot close online log writer", e);
 		}
 	}
 
 	public String getFileServiceType() {
 		return logFileService.getType();
 	}
 
 	CountDownLatch closeMonitor = new CountDownLatch(1);
 
 	public CountDownLatch reserveClose() {
 		synchronized (this) {
 			closeReserved = true;
 			return closeMonitor;
 		}
 	}
 
 	public boolean isCloseReserved() {
 		synchronized (this) {
 			return closeReserved;
 		}
 	}
 }
