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
 package org.araqne.logdb.query.command;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.araqne.log.api.LogParser;
 import org.araqne.log.api.LogParserInput;
 import org.araqne.log.api.LogParserOutput;
 import org.araqne.logdb.QueryCommand;
 import org.araqne.logdb.Row;
 import org.araqne.logdb.RowBatch;
 import org.araqne.logdb.ThreadSafe;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @since 1.6.6
  * @author xeraph
  * 
  */
 public class Parse extends QueryCommand implements ThreadSafe {
 	private final Logger logger = LoggerFactory.getLogger(Parse.class);
 	private final int parserVersion;
	private final LogParserInput input = new LogParserInput();
 	private final String parserName;
 	private final LogParser parser;
 	private final boolean overlay;
 
 	public Parse(String parserName, LogParser parser, boolean overlay) {
 		this.parserName = parserName;
 		this.parser = parser;
 		this.parserVersion = parser.getVersion();
 		this.overlay = overlay;
 	}
 
 	@Override
 	public String getName() {
 		return "parse";
 	}
 
 	@Override
 	public void onPush(RowBatch rowBatch) {
 		// TODO: boost v2 performance
 		if (parserVersion == 2) {
 			if (rowBatch.selectedInUse) {
 				for (int i = 0; i < rowBatch.size; i++) {
 					Row row = rowBatch.rows[rowBatch.selected[i]];
 					onPush(row);
 				}
 			} else {
 				for (Row row : rowBatch.rows)
 					onPush(row);
 			}
 
 			return;
 		}
 
 		int n = 0;
 		if (rowBatch.selectedInUse) {
 			for (int i = 0; i < rowBatch.size; i++) {
 				int p = rowBatch.selected[i];
 				Row row = rowBatch.rows[p];
 				Row parsed = parseV1(row);
 				if (parsed != null) {
 					rowBatch.selected[n] = p;
 					rowBatch.rows[p] = parsed;
 					n++;
 				}
 			}
 		} else {
 			rowBatch.selected = new int[rowBatch.size];
 			for (int i = 0; i < rowBatch.size; i++) {
 				Row row = rowBatch.rows[i];
 				Row parsed = parseV1(row);
 				if (parsed != null) {
 					rowBatch.selected[n] = i;
 					rowBatch.rows[i] = parsed;
 					n++;
 				}
 			}
 		}
 
 		if (!rowBatch.selectedInUse && rowBatch.size != n)
 			rowBatch.selectedInUse = true;
 
 		rowBatch.size = n;
 
 		pushPipe(rowBatch);
 	}
 
 	@Override
 	public void onPush(Row m) {
 		try {
 			if (parserVersion == 2) {
 				Object table = m.get("_table");
 				Object time = m.get("_time");
 
 				if (time != null && time instanceof Date)
 					input.setDate((Date) time);
 				else
 					input.setDate(null);
 
 				if (table != null && table instanceof String)
 					input.setSource((String) table);
 				else
 					input.setSource(null);
 
 				input.setData(m.map());
 
 				LogParserOutput output = parser.parse(input);
 				if (output != null) {
 					for (Map<String, Object> row : output.getRows()) {
 						if (m.get("_id") != null && !row.containsKey("_id"))
 							row.put("_id", m.get("_id"));
 						if (time != null && !row.containsKey("_time"))
 							row.put("_time", m.get("_time"));
 						if (table != null && !row.containsKey("_table"))
 							row.put("_table", m.get("_table"));
 
 						if (overlay) {
 							Map<String, Object> source = new HashMap<String, Object>(m.map());
 							source.putAll(row);
 							pushPipe(new Row(source));
 						} else {
 							pushPipe(new Row(row));
 						}
 					}
 				}
 			} else {
 				Row parsed = parseV1(m);
 				if (parsed != null)
 					pushPipe(parsed);
 			}
 		} catch (Throwable t) {
 			if (logger.isDebugEnabled())
 				logger.debug("araqne logdb: cannot parse " + m.map() + ", query - " + toString(), t);
 		}
 	}
 
 	private Row parseV1(Row m) {
 		Map<String, Object> row = parser.parse(m.map());
 		if (row == null)
 			return null;
 
 		if (!row.containsKey("_id"))
 			row.put("_id", m.get("_id"));
 		if (!row.containsKey("_time"))
 			row.put("_time", m.get("_time"));
 		if (!row.containsKey("_table"))
 			row.put("_table", m.get("_table"));
 
 		if (overlay) {
 			Map<String, Object> source = new HashMap<String, Object>(m.map());
 			source.putAll(row);
 			return new Row(source);
 		} else
 			return new Row(row);
 	}
 
 	@Override
 	public String toString() {
 		return "parse " + parserName;
 	}
 }
