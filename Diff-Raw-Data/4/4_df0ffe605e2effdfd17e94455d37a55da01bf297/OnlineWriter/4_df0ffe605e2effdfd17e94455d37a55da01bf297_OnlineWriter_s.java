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
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 import java.util.concurrent.atomic.AtomicLong;
 import java.util.concurrent.atomic.AtomicStampedReference;
 
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
 	private volatile boolean closing = false;
 
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
 
 	private final TableSchema schema;
 
 	private volatile boolean loadWriter = false;
 	private volatile boolean closeReserved;
 
 	private CountDownLatch writerMonitor = new CountDownLatch(1);
 
 	public OnlineWriter(LogFileService logFileService, TableSchema schema, Date day) {
 		this.logFileService = logFileService;
 		this.schema = schema;
 		this.tableId = schema.getId();
 		this.day = day;
 		this.closeReserved = false;
 	}
 
 	public synchronized void prepareWriter(
 			StorageManager storageManager, CallbackSet callbackSet, FilePath logDir, AtomicLong lastKey)
 			throws IOException {
 		try {
 			if (closing)
 				return;
 
 			loadWriter = true;
 
 			String basePathString = schema.getPrimaryStorage().getBasePath();
 			FilePath basePath = logDir;
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
 				writerOptions.put("basePath", basePath);
 				writerOptions.put("indexPath", indexPath);
 				writerOptions.put("dataPath", dataPath);
 				writerOptions.put("keyPath", keyPath);
 				writerOptions.put("callbackSet", callbackSet);
 				writerOptions.put("lastKey", lastKey);
 
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
 		} finally {
 			writerMonitor.countDown();
 		}
 	}
 
 	public void awaitWriterPreparation() throws TimeoutException, InterruptedException, WriterPreparationException {
 		for (int i = 0; i < 3; ++i) {
 			if (writerMonitor.await(10, TimeUnit.SECONDS)) {
 				if (!isReady())
 					throw new WriterPreparationException("araqne logstorage: log writer preparation failed");
 				return;
 			}
 			if (logger.isDebugEnabled())
 				logger.debug("araqne logstorage: awaiting for log writer preparation {}", tableId);
 		}
 
 		logger.info("araqne logstorage: awaiting for log writer preparation {} timeout", tableId);
 		throw new TimeoutException("araqne logstorage: awaiting for log writer preparation " + tableId + " timeout");
 	}
 	
 	public boolean isReady() {
 		return writer != null;
 	}
 
 	public boolean isClosed() {
 		return closing == true;
 	}
 
 	// checking order is important
 	public boolean isCloseCompleted() {
 		return writer.isClosed() && closing == true;
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
 
 	/**
 	 * @since 2.7.0
 	 */
 	public LogFileWriter getWriter() {
 		return writer;
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
 			debugCounter.addAndGet(1);
 
 			writer.write(log.shallowCopy());
 			lastAccess = new Date();
 		}
 	}
 	
 	private AtomicLong debugCounter = new AtomicLong();
 
 	public void write(List<Log> logs) throws IOException {
 		if (isClosed())
 			throw new IllegalStateException("file closed");
 		if (writer == null)
 			throw new IllegalStateException("not ready");
 
 		synchronized (this) {
 			ArrayList<Log> copy = new ArrayList<Log>(logs.size());
 			for (Log record : logs) {
 				record.setId(nextId());
 				copy.add(record.shallowCopy());
 			}
 			
 			debugCounter.addAndGet(copy.size());
 
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
 				try {
 					closing = true;
 					// XXX: assume there is NO prepareWriter() hang
 					if (loadWriter) {
 						awaitWriterPreparation();
 						writer.close();
 					}
 				} catch (WriterPreparationException ex) {
 					// ignore
 				} finally {
 					notifyAll();
 					closeMonitor.countDown();
 				}
 			}
 		} catch (InterruptedException e) {
 			logger.error("cannot close online log writer", e);
 		} catch (TimeoutException e) {
 			logger.error("cannot close online log writer", e);
 		} catch (IOException e) {
 			logger.error("cannot close online log writer", e);
 		}
 	}
 
 	@Override
 	public String toString() {
 		return String.format("OnlineWriter [tableId=%s, day=%s]", tableId, day);
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
