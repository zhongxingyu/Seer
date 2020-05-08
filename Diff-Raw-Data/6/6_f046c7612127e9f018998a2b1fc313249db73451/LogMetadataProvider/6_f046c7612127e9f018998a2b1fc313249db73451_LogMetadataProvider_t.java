 /**
  * Copyright 2014 Eediom Inc.
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
 package org.araqne.logdb.metadata;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicLong;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Invalidate;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.Validate;
 import org.araqne.log.api.Log;
 import org.araqne.log.api.LogPipe;
 import org.araqne.log.api.Logger;
 import org.araqne.log.api.MultilineLogExtractor;
 import org.araqne.logdb.AccountService;
 import org.araqne.logdb.MetadataCallback;
 import org.araqne.logdb.MetadataProvider;
 import org.araqne.logdb.MetadataService;
 import org.araqne.logdb.QueryContext;
 import org.araqne.logdb.QueryParseException;
 import org.araqne.logdb.Row;
 
 @Component(name = "logdb-log-metadata")
 public class LogMetadataProvider implements MetadataProvider {
 	@Requires
 	private AccountService accountService;
 
 	@Requires
 	private MetadataService metadataService;
 
 	@Validate
 	public void start() {
 		metadataService.addProvider(this);
 	}
 
 	@Invalidate
 	public void stop() {
 		if (metadataService != null)
 			metadataService.removeProvider(this);
 	}
 
 	@Override
 	public String getType() {
 		return "logs";
 	}
 
 	@Override
 	public void verify(QueryContext context, String queryString) {
 		if (!context.getSession().isAdmin())
 			throw new QueryParseException("no-read-permission", -1);
 	}
 
 	@Override
 	public void query(QueryContext context, String queryString, MetadataCallback callback) {
 		File dir = new File(System.getProperty("araqne.log.dir"));
 
 		List<File> files = new ArrayList<File>();
 		for (File f : dir.listFiles()) {
 			if (f.getName().startsWith("araqne.log") && !f.getName().equals("araqne.log"))
 				files.add(f);
 		}
 
 		Collections.sort(files);
 
 		RowPipe pipe = new RowPipe(callback);
 
 		// rolled files
 		for (File f : files) {
 			loadFile(pipe, f);
 		}
 
 		// today log file
 		loadFile(pipe, new File(dir, "araqne.log"));
 	}
 
 	private void loadFile(RowPipe pipe, File f) {
 		FileInputStream is = null;
 		try {
 			is = new FileInputStream(f);
 			MultilineLogExtractor extractor = new MultilineLogExtractor(null, pipe);
 			extractor.setBeginMatcher(Pattern.compile("^\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3}\\]").matcher(""));
			extractor.setDateMatcher(Pattern.compile("\\[(.*)\\]").matcher(""));
			extractor.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS"));
 			extractor.setCharset(Charset.defaultCharset().name());
 			extractor.extract(is, new AtomicLong());
 		} catch (IOException e) {
 		} finally {
 			if (is != null) {
 				try {
 					is.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 
 	private static class RowPipe implements LogPipe {
 		private Matcher matcher;
 		private MetadataCallback output;
 
 		public RowPipe(MetadataCallback output) {
 			this.output = output;
 
 			matcher = Pattern.compile("(?sm)\\[.*?\\]\\s+(\\S+) \\((\\S+)\\) - (.*)").matcher("");
 		}
 
 		@Override
 		public void onLog(Logger logger, Log log) {
 			Row row = buildRow(log);
 			output.onPush(row);
 		}
 
 		@Override
 		public void onLogBatch(Logger logger, Log[] logs) {
 			for (Log log : logs) {
 				if (log == null)
 					continue;
 
 				Row row = buildRow(log);
 				output.onPush(row);
 			}
 		}
 
 		private Row buildRow(Log log) {
 			String line = (String) log.getParams().get("line");
 			Row row = new Row();
 			row.put("_time", log.getDate());
			System.out.println(log.getDate() + " " + line);
 
 			if (line != null) {
 				matcher.reset(line);
 				if (matcher.matches()) {
 					row.put("level", matcher.group(1));
 					row.put("class", matcher.group(2));
 					row.put("msg", matcher.group(3));
 				} else {
 					row.put("line", line);
 				}
 			}
 
 			return row;
 		}
 	}
 }
