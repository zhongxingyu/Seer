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
 package org.araqne.logdb.msgbus;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.zip.Deflater;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.araqne.codec.Base64;
 import org.araqne.codec.FastEncodingRule;
 import org.araqne.logdb.AccountService;
 import org.araqne.logdb.LogQuery;
 import org.araqne.logdb.LogQueryCallback;
 import org.araqne.logdb.LogQueryContext;
 import org.araqne.logdb.LogQueryService;
 import org.araqne.logdb.LogTimelineCallback;
 import org.araqne.logdb.RunMode;
 import org.araqne.logdb.impl.LogQueryHelper;
 import org.araqne.logstorage.Log;
 import org.araqne.logstorage.LogStorage;
 import org.araqne.logstorage.LogTableRegistry;
 import org.araqne.msgbus.MsgbusException;
 import org.araqne.msgbus.PushApi;
 import org.araqne.msgbus.Request;
 import org.araqne.msgbus.Response;
 import org.araqne.msgbus.Session;
 import org.araqne.msgbus.handler.CallbackType;
 import org.araqne.msgbus.handler.MsgbusMethod;
 import org.araqne.msgbus.handler.MsgbusPlugin;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Component(name = "logdb-logquery-msgbus")
 @MsgbusPlugin
 public class LogQueryPlugin {
 	private final Logger logger = LoggerFactory.getLogger(LogQueryPlugin.class.getName());
 
 	@Requires
 	private LogQueryService service;
 
 	@Requires
 	private LogTableRegistry tableRegistry;
 
 	@Requires
 	private LogStorage storage;
 
 	@Requires
 	private PushApi pushApi;
 
 	@Requires
 	private AccountService accountService;
 
 	@MsgbusMethod
 	public void logs(Request req, Response resp) {
 		String tableName = req.getString("table");
 		int limit = req.getInteger("limit");
 		int offset = 0;
 		if (req.has("offset"))
 			offset = req.getInteger("offset");
 
 		if (!tableRegistry.exists(tableName))
 			throw new MsgbusException("logdb", "table-not-exists");
 
 		Collection<Log> logs = storage.getLogs(tableName, null, null, offset, limit);
 		List<Object> serialized = new ArrayList<Object>(limit);
 		for (Log log : logs)
 			serialized.add(serialize(log));
 
 		resp.put("logs", serialized);
 	}
 
 	private Map<String, Object> serialize(Log log) {
 		Map<String, Object> m = new HashMap<String, Object>();
 		m.put("table", log.getTableName());
 		m.put("id", log.getId());
 		m.put("date", log.getDate());
 		m.put("data", log.getData());
 		return m;
 	}
 
 	@MsgbusMethod
 	public void queries(Request req, Response resp) {
 		org.araqne.logdb.Session dbSession = getDbSession(req);
 		List<Object> result = LogQueryHelper.getQueries(dbSession, service);
 		resp.put("queries", result);
 	}
 
 	@MsgbusMethod
 	public void createQuery(Request req, Response resp) {
 		try {
 			org.araqne.logdb.Session dbSession = getDbSession(req);
 			LogQuery query = service.createQuery(dbSession, req.getString("query"));
 			resp.put("id", query.getId());
 		} catch (Exception e) {
 			logger.error("araqne logdb: cannot create query", e);
 			throw new MsgbusException("logdb", e.getMessage());
 		}
 	}
 
 	@MsgbusMethod
 	public void removeQuery(Request req, Response resp) {
 		int id = req.getInteger("id", true);
 		org.araqne.logdb.Session dbSession = getDbSession(req);
 		service.removeQuery(dbSession, id);
 	}
 
 	private org.araqne.logdb.Session getDbSession(Request req) {
 		return getDbSession(req.getSession());
 	}
 
 	private org.araqne.logdb.Session getDbSession(Session session) {
 		return (org.araqne.logdb.Session) session.get("araqne_logdb_session");
 	}
 
 	@MsgbusMethod
 	public void startQuery(Request req, Response resp) {
 		String orgDomain = req.getOrgDomain();
 		int id = req.getInteger("id");
 		int offset = req.getInteger("offset");
 		int limit = req.getInteger("limit");
 		Integer timelineLimit = req.getInteger("timeline_limit");
 
 		LogQuery query = service.getQuery(id);
 
 		// validation check
 		if (query == null) {
 			Map<String, Object> params = new HashMap<String, Object>();
 			params.put("query_id", id);
 			throw new MsgbusException("logdb", "query not found", params);
 		}
 
 		if (!query.isEnd())
 			throw new MsgbusException("logdb", "already running");
 
 		// set query and timeline callback
 		LogQueryCallback qc = new MsgbusLogQueryCallback(orgDomain, query, offset, limit);
 		query.registerQueryCallback(qc);
 
 		if (timelineLimit != null) {
 			int size = timelineLimit.intValue();
 			LogTimelineCallback tc = new MsgbusTimelineCallback(orgDomain, query, size);
 			query.registerTimelineCallback(tc);
 		}
 
 		// start query
 		service.startQuery(query.getId());
 	}
 
 	@MsgbusMethod
 	public void stopQuery(Request req, Response resp) {
 		int id = req.getInteger("id", true);
 		LogQuery query = service.getQuery(id);
 		if (query != null)
 			query.cancel();
 		else {
 			Map<String, Object> params = new HashMap<String, Object>();
 			params.put("query_id", id);
 			throw new MsgbusException("logdb", "query-not-found", params);
 		}
 	}
 
 	@MsgbusMethod
 	public void getResult(Request req, Response resp) throws IOException {
 		int id = req.getInteger("id", true);
 		int offset = req.getInteger("offset", true);
 		int limit = req.getInteger("limit", true);
 		Boolean binaryEncode = req.getBoolean("binary_encode");
 
 		Map<String, Object> m = LogQueryHelper.getResultData(service, id, offset, limit);
 		if (m == null)
 			return;
 
 		FastEncodingRule enc = new FastEncodingRule();
 		if (binaryEncode != null && binaryEncode) {
 			ByteBuffer binary = enc.encode(m);
 			int uncompressedSize = binary.array().length;
 			byte[] b = compress(binary.array());
 			resp.put("binary", new String(Base64.encode(b)));
 			resp.put("uncompressed_size", uncompressedSize);
 		} else
 			resp.putAll(m);
 	}
 
 	private byte[] compress(byte[] b) throws IOException {
 		Deflater c = new Deflater();
 		try {
 			c.reset();
 			c.setInput(b);
 			c.finish();
 			ByteBuffer compressed = ByteBuffer.allocate(b.length);
 			int compressedSize = c.deflate(compressed.array());
 			return Arrays.copyOf(compressed.array(), compressedSize);
 		} finally {
 			c.end();
 		}
 	}
 
 	/**
 	 * @since 0.17.0
 	 */
 	@MsgbusMethod
 	public void setRunMode(Request req, Response resp) {
 		int id = req.getInteger("id", true);
 		boolean background = req.getBoolean("background", true);
 
 		LogQuery query = service.getQuery(id);
 		if (query == null)
 			throw new MsgbusException("logdb", "query-not-found");
 
 		org.araqne.logdb.Session dbSession = getDbSession(req);
 
 		if (!query.isAccessible(dbSession))
 			throw new MsgbusException("logdb", "no-permission");
 
 		query.setRunMode(background ? RunMode.BACKGROUND : RunMode.FOREGROUND, new LogQueryContext(dbSession));
 	}
 
 	@MsgbusMethod(type = CallbackType.SessionClosed)
 	public void sessionClosed(Session session) {
		accountService.logout(getDbSession(session));
 	}
 
 	private class MsgbusLogQueryCallback implements LogQueryCallback {
 		private String orgDomain;
 		private LogQuery query;
 		private int offset;
 		private int limit;
 
 		private MsgbusLogQueryCallback(String orgDomain, LogQuery query, int offset, int limit) {
 			this.orgDomain = orgDomain;
 			this.query = query;
 			this.offset = offset;
 			this.limit = limit;
 		}
 
 		@Override
 		public int offset() {
 			return offset;
 		}
 
 		@Override
 		public int limit() {
 			return limit;
 		}
 
 		@Override
 		public void onQueryStatusChange() {
 			try {
 				String status = null;
 				if (query.getLastStarted() == null)
 					status = "Waiting";
 				else
 					status = query.isEnd() ? "End" : "Running";
 
 				Map<String, Object> m = new HashMap<String, Object>();
 				m.put("id", query.getId());
 				m.put("type", "status_change");
 				m.put("status", status);
 				m.put("count", query.getResultCount());
 				pushApi.push(orgDomain, "logdb-query-" + query.getId(), m);
 				pushApi.push(orgDomain, "logstorage-query-" + query.getId(), m); // deprecated
 			} catch (IOException e) {
 				logger.error("araqne logdb: msgbus push fail", e);
 			}
 		}
 
 		@Override
 		public void onPageLoaded() {
 			try {
 				Map<String, Object> m = LogQueryHelper.getResultData(service, query.getId(), offset, limit);
 				m.put("id", query.getId());
 				m.put("type", "page_loaded");
 				pushApi.push(orgDomain, "logdb-query-" + query.getId(), m);
 				pushApi.push(orgDomain, "logstorage-query-" + query.getId(), m); // deprecated
 			} catch (IOException e) {
 				logger.error("araqne logdb: msgbus push fail", e);
 			}
 		}
 
 		@Override
 		public void onEof() {
 			try {
 				Map<String, Object> m = new HashMap<String, Object>();
 				m.put("id", query.getId());
 				m.put("type", "eof");
 				m.put("total_count", query.getResultCount());
 				pushApi.push(orgDomain, "logdb-query-" + query.getId(), m);
 				pushApi.push(orgDomain, "logstorage-query-" + query.getId(), m); // deprecated
 				query.unregisterQueryCallback(this);
 			} catch (IOException e) {
 				logger.error("araqne logdb: msgbus push fail", e);
 			}
 		}
 	}
 
 	private class MsgbusTimelineCallback extends LogTimelineCallback {
 		private Logger logger = LoggerFactory.getLogger(MsgbusTimelineCallback.class);
 		private String orgDomain;
 		private LogQuery query;
 		private int size;
 
 		private MsgbusTimelineCallback(String orgDomain, LogQuery query) {
 			this(orgDomain, query, 10);
 		}
 
 		private MsgbusTimelineCallback(String orgDomain, LogQuery query, int size) {
 			this.orgDomain = orgDomain;
 			this.query = query;
 			this.size = size;
 		}
 
 		@Override
 		public int getSize() {
 			return size;
 		}
 
 		@Override
 		protected void callback(Date beginTime, SpanValue spanValue, int[] values, boolean isEnd) {
 			try {
 				Map<String, Object> m = new HashMap<String, Object>();
 				m.put("id", query.getId());
 				m.put("type", isEnd ? "eof" : "periodic");
 				m.put("span_field", spanValue.getFieldName());
 				m.put("span_amount", spanValue.getAmount());
 				m.put("begin", beginTime);
 				m.put("values", values);
 				pushApi.push(orgDomain, "logdb-query-timeline-" + query.getId(), m);
 
 				m.put("count", query.getResultCount());
 				pushApi.push(orgDomain, "logstorage-query-timeline-" + query.getId(), m); // deprecated
 
 				Object[] trace = new Object[] { query.getId(), spanValue.getFieldName(), spanValue.getAmount(), beginTime,
 						Arrays.toString(values), query.getResultCount() };
 				logger.trace("araqne logdb: timeline callback => "
 						+ "{id={}, span_field={}, span_amount={}, begin={}, values={}, count={}}", trace);
 			} catch (IOException e) {
 				logger.error("araqne logdb: msgbus push fail", e);
 			}
 		}
 	}
 }
