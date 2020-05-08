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
 package org.araqne.log.api;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.araqne.api.FieldOption;
 import org.araqne.confdb.CollectionName;
import org.araqne.msgbus.Marshalable;
 
 @CollectionName("transformer_profiles")
public class LogTransformerProfile implements Comparable<LogTransformerProfile>, Marshalable {
 	private String name;
 	private String factoryName;
 	private Map<String, String> configs = new HashMap<String, String>();
 
 	@FieldOption(skip = true)
 	private boolean ready;
 
 	@FieldOption(skip = true)
 	private Throwable cause;
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getFactoryName() {
 		return factoryName;
 	}
 
 	public void setFactoryName(String factoryName) {
 		this.factoryName = factoryName;
 	}
 
 	public Map<String, String> getConfigs() {
 		return configs;
 	}
 
 	public void setConfigs(Map<String, String> configs) {
 		this.configs = configs;
 	}
 
 	public boolean isReady() {
 		return ready;
 	}
 
 	public void setReady(boolean ready) {
 		this.ready = ready;
 	}
 
 	public Throwable getCause() {
 		return cause;
 	}
 
 	public void setCause(Throwable cause) {
 		this.cause = cause;
 	}
 
 	@Override
 	public String toString() {
 		return "name=" + name + ", factory=" + factoryName + ", configs=" + configs + ", ready=" + ready + ", error="
 				+ (cause == null ? "N/A" : cause.getMessage());
 	}
 
 	@Override
 	public int compareTo(LogTransformerProfile o) {
 		if (o == null)
 			return -1;
 		return name.compareTo(o.name);
 	}
 
 	/**
 	 * @since 3.4.6
 	 */
	@Override
 	public Map<String, Object> marshal() {
 		Map<String, Object> m = new HashMap<String, Object>();
 		m.put("name", name);
 		m.put("factory_name", factoryName);
 		m.put("configs", configs);
 		m.put("ready", ready);
 		m.put("cause", cause != null ? cause.toString() : null);
 		return m;
 	}
 }
