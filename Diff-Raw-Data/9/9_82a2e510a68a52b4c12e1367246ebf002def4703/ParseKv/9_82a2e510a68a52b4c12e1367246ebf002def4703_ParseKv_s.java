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
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.araqne.logdb.QueryCommand;
 import org.araqne.logdb.Row;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ParseKv extends QueryCommand {
 	private final Logger logger = LoggerFactory.getLogger(ParseKv.class);
 	private final String field;
 	private final boolean overlay;
 	private final String pairDelim;
 	private final String kvDelim;
 
 	public ParseKv(String field, boolean overlay, String pairDelim, String kvDelim) {
 		this.field = field;
 		this.overlay = overlay;
 		this.pairDelim = pairDelim;
 		this.kvDelim = kvDelim;
 	}
 
 	@Override
 	public String getName() {
 		return "parsekv";
 	}
 
 	public String getField() {
 		return field;
 	}
 
 	public boolean isOverlay() {
 		return overlay;
 	}
 
 	public String getPairDelim() {
 		return pairDelim;
 	}
 
 	public String getKvDelim() {
 		return kvDelim;
 	}
 
 	@Override
 	public void onPush(Row m) {
 		Object target = m.get(field);
 		if (target == null) {
 			if (overlay)
 				pushPipe(m);
 			return;
 		}
 
 		try {
 			String line = target.toString();
 
 			Map<String, Object> kv = new HashMap<String, Object>();
 			int lastPairFrom = 0;
 
 			while (true) {
 				int p = line.indexOf(pairDelim, lastPairFrom);
 				if (p < 0)
 					break;
 
 				String pairToken = line.substring(lastPairFrom, p).trim();
 				lastPairFrom = p + pairDelim.length();
 				if (pairToken.isEmpty())
 					continue;
 
 				int p2 = pairToken.indexOf(kvDelim);
 				if (p2 < 0) {
 					kv.put(pairToken, null);
 				} else {
 					String k = pairToken.substring(0, p2);
 					String v = pairToken.substring(p2 + kvDelim.length());
 					kv.put(k, v);
 				}
 			}
 
 			// add last pair
 			String pairToken = line.substring(lastPairFrom).trim();
 			if (!pairToken.isEmpty()) {
 				int p2 = pairToken.indexOf(kvDelim);
 				if (p2 < 0) {
 					kv.put(pairToken, null);
 				} else {
 					String k = pairToken.substring(0, p2);
 					String v = pairToken.substring(p2 + kvDelim.length());
 					kv.put(k, v);
 				}
 			}
 
 			Object table = m.get("_table");
 			Object time = m.get("_time");
 
 			if (m.get("_id") != null && !kv.containsKey("_id"))
 				kv.put("_id", m.get("_id"));
 			if (time != null && !kv.containsKey("_time"))
 				kv.put("_time", m.get("_time"));
 			if (table != null && !kv.containsKey("_table"))
 				kv.put("_table", m.get("_table"));
 
 			if (overlay) {
 				m.map().putAll(kv);
 				pushPipe(m);
 			} else {
 				pushPipe(new Row(kv));
 			}
 		} catch (Throwable t) {
 			// parsing fail handling
 			if (overlay)
 				pushPipe(m);
 
 			if (logger.isDebugEnabled())
 				logger.debug("araqne logdb: [" + toString() + "] failed, log data [{}]", m.map());
 		}
 	}
 
 	@Override
 	public String toString() {
		return "parseKv field=" + field + " overlay=" + overlay + " pairdelim=\"" + pairDelim + "\" kvdelim=\"" + kvDelim + "\"";
 	}
 }
