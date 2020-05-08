 /*
  * Copyright 2009 zaichu xiao
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package zcu.xutil.sql.handl;
 
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import zcu.xutil.sql.ResultHandler;
 import zcu.xutil.sql.SQLType;
 
 /**
  *
  * @author <a href="mailto:zxiao@yeepay.com">xiao zaichu</a>
  */
 public final class MapRow extends ResultHandler<Map<String, Object>> {
 	private final Map<String, Class<?>> typeMap;
 
 	public MapRow(Map<String, Class<?>> returnTypes) {
 		this.typeMap = returnTypes == null || returnTypes.isEmpty() ? null : returnTypes;
 	}
 
 	@Override
 	public Map<String, Object> handleRow(ResultSet rs) throws SQLException {
 		Map<String, Object> ret = new HashMap<String, Object>();
 		ResultSetMetaData rsmd = rs.getMetaData();
 		final int len = rsmd.getColumnCount();
 		if (typeMap == null) {
 			for (int i = len; i > 0; i--)
 				ret.put(rsmd.getColumnLabel(i), rs.getObject(i));
 			return ret;
 		}
 		String[] columns = new String[len + 1];// from len to 1
 		for (int i = len; i > 0; i--)
 			columns[i] = rsmd.getColumnLabel(i);
 		for (Entry<String, Class<?>> entry : typeMap.entrySet()) {
 			String s1, s2 = entry.getKey();
 			int slen = s2.length();
 			outer: for (int i = len; i > 0; i--) { // from len to 1
 				if ((s1 = columns[i]) == null || slen != s1.length())
 					continue;
 				int diff, j = slen;
 				while (--j >= 0) {
 					char c = s1.charAt(j);
 					if ((diff = c ^ s2.charAt(j)) != 0) {
						if (diff == 32 && (32 | c) >= 'a' && (32 | c) <= 'z')
 							continue;
 						continue outer;
 					}
 				}
 				columns[i] = null;
 				ret.put(s2, SQLType.getValue(rs, i, entry.getValue()));
 				break;
 			}
 		}
 		return ret;
 	}
 }
