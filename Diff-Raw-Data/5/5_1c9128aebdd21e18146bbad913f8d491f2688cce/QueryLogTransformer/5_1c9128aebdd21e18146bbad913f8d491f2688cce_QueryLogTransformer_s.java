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
 package org.araqne.logdb.logapi;
 
 import java.util.List;
 import java.util.Map;
 
 import org.araqne.log.api.Log;
 import org.araqne.log.api.LogTransformer;
 import org.araqne.log.api.LogTransformerFactory;
 import org.araqne.log.api.SimpleLog;
 import org.araqne.logdb.QueryCommand;
 import org.araqne.logdb.QueryCommandPipe;
 import org.araqne.logdb.Row;
 
 /**
  * @since 1.7.8
  * @author xeraph
  * 
  */
 public class QueryLogTransformer extends QueryCommand implements LogTransformer {
 	private final LogTransformerFactory factory;
 	private QueryCommand first;
 	private Map<String, Object> last;
 
 	public QueryLogTransformer(LogTransformerFactory factory, List<QueryCommand> commands) {
 		this.factory = factory;
 
 		first = commands.get(0);
 		commands.add(this);
 
 		for (int i = commands.size() - 2; i >= 0; i--)
 			commands.get(i).setOutput(new QueryCommandPipe(commands.get(i + 1)));
 	}
 
 	@Override
 	public String getName() {
 		return "querylogtransformer";
 	}
 
 	@Override
 	public LogTransformerFactory getTransformerFactory() {
 		return factory;
 	}
 
 	@Override
 	public Log transform(Log log) {
		first.onPush(new Row(Row.clone(log.getParams())));
 		Map<String, Object> m = last;
 		last = null;
 		return new SimpleLog(log.getDate(), log.getLoggerName(), m);
 	}
 
 	@Override
 	public void onPush(Row m) {
 		last = m.map();
 	}
 }
