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
 import zcu.xutil.Objutil;
 import zcu.xutil.sql.ResultHandler;
 import zcu.xutil.sql.SQLType;
 import zcu.xutil.utils.Accessor;
 
 /**
  * 
  * @author <a href="mailto:zxiao@yeepay.com">xiao zaichu</a>
  */
 public final class BeanRow<T> extends ResultHandler<T> {
 	private final Class<? extends T> clazz;
 
 	public BeanRow(Class<? extends T> cls) {
 		this.clazz = cls;
 	}
 
 	@Override
 	public T handleRow(ResultSet rs) throws SQLException {
 		T result = Objutil.newInstance(clazz);
 		ResultSetMetaData rsmd = rs.getMetaData();
 		final int len = rsmd.getColumnCount();
 		String[] columns = new String[len + 1];// from len to 1
 		for (int i = len; i > 0; i--)
 			columns[i] = rsmd.getColumnLabel(i);
 		for (Accessor ac : getAllAccessor(clazz).values()) {
 			String s1, s2 = ac.getName();
 			int slen = s2.length();
 			outer: for (int i = len; i > 0; i--) { // from len to 1
 				if ((s1 = columns[i]) == null || slen != s1.length())
 					continue;
 				int diff, j = slen;
 				while (--j >= 0) {
 					char c = s1.charAt(j);
 					if ((diff = c ^ s2.charAt(j)) != 0) {
						if (diff == 32 && (diff |= c) >= 'a' && diff <= 'z')
 							continue;
 						continue outer;
 					}
 				}
 				columns[i] = null;
 				ac.setValue(result, SQLType.getValue(rs, i, ac.getType()));
 				break;
 			}
 		}
 		return result;
 	}
 }
