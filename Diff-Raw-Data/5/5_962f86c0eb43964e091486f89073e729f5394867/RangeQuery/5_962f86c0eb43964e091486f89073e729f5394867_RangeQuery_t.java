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
 
 package at.molindo.elastic.query;
 
 import org.elasticsearch.index.query.xcontent.QueryBuilders;
 import org.elasticsearch.index.query.xcontent.RangeQueryBuilder;
 import org.elasticsearch.index.query.xcontent.XContentQueryBuilder;
 
 public class RangeQuery extends Query {
 
 	private RangeQueryBuilder _builder;
 
 	public RangeQuery(String property) {
 		_builder = QueryBuilders.rangeQuery(property);
 	}
 
 	public RangeQuery setFrom(Term from) {
		_builder.from(from == null ? null : from.getValue());
 		return this;
 	}
 
 	public RangeQuery setTo(Term to) {
		_builder.to(to == null ? null : to.getValue());
 		return this;
 	}
 
 	public RangeQuery setIncludeLower(boolean includeLower) {
 		_builder.includeLower(includeLower);
 		return this;
 	}
 
 	public RangeQuery setIncludeUpper(boolean includeUpper) {
 		_builder.includeUpper(includeUpper);
 		return this;
 	}
 
 	public RangeQuery setIncludeBoth(boolean includeBoth) {
 		_builder.includeUpper(includeBoth).includeLower(includeBoth);
 		return this;
 	}
 	
 	@Override
 	public XContentQueryBuilder getBuilder() {
 		return _builder;
 	}
 
 }
