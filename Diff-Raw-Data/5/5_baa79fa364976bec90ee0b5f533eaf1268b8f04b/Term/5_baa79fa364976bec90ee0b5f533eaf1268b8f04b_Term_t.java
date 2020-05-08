 /**
  * Copyright 2011 Molindo GmbH
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
 
 package at.molindo.elastic.term;
 
 import org.elasticsearch.index.query.xcontent.QueryBuilders;
 import org.elasticsearch.index.query.xcontent.XContentQueryBuilder;
 
 public abstract class Term {
 
 	private String _name;
 	private Object _value;
 
 	public static StringTerm string(String name, String value) {
 		return new StringTerm(name, value);
 	}
 	
 	public Term(String name, Object value) {
 		_name = name;
 		_value = value;
 	}
 
 	public String getName() {
 		return _name;
 	}
 
 	public Object getValue() {
 		return _value;
 	}
 
 	@Override
 	public String toString() {
 		return _name + ": " + _value;
 	}
 
	public abstract XContentQueryBuilder buildQuery();
 
 	private static class StringTerm extends Term {
 
 		public StringTerm(String name, String value) {
 			super(name, value);
 		}
 
 		@Override
		public XContentQueryBuilder buildQuery() {
 			return QueryBuilders.termQuery(getName(), getValue());
 		}
 
 	}
 }
