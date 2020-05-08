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
 
 import org.araqne.log.api.FieldDefinition;
 import org.araqne.log.api.LogParser;
 import org.araqne.log.api.LogParserInput;
 import org.araqne.log.api.LogParserOutput;
 import org.araqne.logdb.QueryCommand;
 import org.araqne.logdb.QueryCommandPipe;
 import org.araqne.logdb.Row;
 
 public class QueryLogParser extends QueryCommand implements LogParser {
 	private String queryString;
 	private QueryCommand first;
 	private Map<String, Object> last;
 
 	public QueryLogParser(String queryString, List<QueryCommand> commands) {
 		this.queryString = queryString;
 		first = commands.get(0);
 		commands.add(this);
 
 		for (int i = commands.size() - 2; i >= 0; i--)
 			commands.get(i).setOutput(new QueryCommandPipe(commands.get(i + 1)));
 	}
 
 	@Override
 	public String getName() {
 		return "querylogparser";
 	}
 
 	@Override
 	public int getVersion() {
 		return 1;
 	}
 
 	@Override
 	public LogParserOutput parse(LogParserInput input) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Map<String, Object> parse(Map<String, Object> params) {
		first.onPush(new Row(params));
 		Map<String, Object> m = last;
 		last = null;
 		return m;
 	}
 
 	@Override
 	public void onPush(Row m) {
 		last = m.map();
 	}
 
 	@Override
 	public List<FieldDefinition> getFieldDefinitions() {
 		return null;
 	}
 
 	@Override
 	public String toString() {
 		return "query log parser: " + queryString;
 	}
 }
