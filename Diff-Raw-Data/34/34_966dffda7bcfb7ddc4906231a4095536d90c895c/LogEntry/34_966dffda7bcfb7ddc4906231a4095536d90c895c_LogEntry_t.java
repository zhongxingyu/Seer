 /**
  * Copyright (C) 2013
  *   Michael Mosmann <michael@mosmann.de>
  *
  * with contributions from
  * 	${lic.developers}
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package de.flapdoodle.logparser;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import de.flapdoodle.logparser.stacktrace.StackTrace;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 public class LogEntry {
 
 	private final ImmutableList<String> _source;
 	private final ImmutableMap<String, String> _attributes;
 	private final Optional<StackTrace> _stackTrace;
 	private final ImmutableList<String> _messages;
 
 	public LogEntry(List<String> source, Map<String, String> attributes, Optional<StackTrace> stackTrace,
 			List<String> messages) {
 		_source = ImmutableList.copyOf(source);
 		_attributes = ImmutableMap.copyOf(attributes);
 		_stackTrace = stackTrace;
 		_messages = ImmutableList.copyOf(messages);
 	}
 
 	public ImmutableMap<String, String> attributes() {
 		return _attributes;
 	}
 
 	public Optional<StackTrace> stackTrace() {
 		return _stackTrace;
 	}
 
 	public ImmutableList<String> messages() {
 		return _messages;
 	}
 
 	public ImmutableList<String> source() {
 		return _source;
 	}
 
 	public static Map<String, String> join(Collection<? extends Map<String, String>> maps) {
 		Map<String, String> ret = Maps.newHashMap();
 		for (Map<String, String> map : maps) {
 			ret = join(ret, map);
 		}
 		return ret;
 	}
 
 	public static Map<String, String> join(Map<String, String>... maps) {
 		Map<String, String> ret = Maps.newHashMap();
 		for (Map<String, String> map : maps) {
 			ret = join(ret, map);
 		}
 		return ret;
 	}
 
 	private static <K, V> Map<K, V> join(Map<K, V> a, Map<K, V> b) {
 		Map<K, V> ret = Maps.newHashMap(a);
 		ret.putAll(b);
 		if (ret.size() != a.size() + b.size()) {
 			throw new RuntimeException("Map contains same keys: " + Sets.intersection(a.keySet(), b.keySet()));
 		}
		return ret;
 	}
 
 }
