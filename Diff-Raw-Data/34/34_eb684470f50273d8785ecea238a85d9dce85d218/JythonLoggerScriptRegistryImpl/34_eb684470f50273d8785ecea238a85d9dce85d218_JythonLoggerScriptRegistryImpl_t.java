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
 package org.araqne.logdb.jython.impl;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Invalidate;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.Validate;
 import org.araqne.confdb.Config;
 import org.araqne.confdb.ConfigDatabase;
 import org.araqne.confdb.ConfigIterator;
 import org.araqne.confdb.ConfigService;
 import org.araqne.confdb.Predicate;
 import org.araqne.confdb.Predicates;
 import org.araqne.jython.JythonService;
 import org.araqne.log.api.AbstractLoggerFactory;
 import org.araqne.log.api.Logger;
 import org.araqne.log.api.LoggerConfigOption;
 import org.araqne.log.api.LoggerSpecification;
 import org.araqne.log.api.StringConfigType;
 import org.araqne.logdb.jython.JythonActiveLogger;
 import org.araqne.logdb.jython.JythonLoggerScriptRegistry;
 import org.python.core.PyObject;
 import org.python.util.PythonInterpreter;
 
 @Component(name = "jython-logger-script-registry")
 @Provides
 public class JythonLoggerScriptRegistryImpl extends AbstractLoggerFactory implements JythonLoggerScriptRegistry {
 	private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JythonLoggerScriptRegistryImpl.class);
 	private ConcurrentMap<String, PyObject> scripts = new ConcurrentHashMap<String, PyObject>();
 
 	@Requires
 	private JythonService jython;
 
 	@Requires
 	private ConfigService conf;
 
 	@Validate
 	public void start() {
 		// load scripts
 		ConfigDatabase db = conf.ensureDatabase("araqne-logdb-jython");
 		ConfigIterator it = db.find(ScriptConfig.class, Predicates.field("type", "logger"));
 		try {
 			for (ScriptConfig sc : it.getDocuments(ScriptConfig.class)) {
 				try {
 					PyObject o = eval(sc.getName(), sc.getScript());
 					scripts.put(sc.getName(), o);
 				} catch (Throwable t) {
 					logger.error("araqne logdb jython: cannot load logger script [" + sc.getName() + "]", t);
 				}
 			}
 		} finally {
 			it.close();
 		}
 
 	}
 
 	@Invalidate
 	public void stop() {
 	}
 
 	//
 	// implement logger factory
 	//
 
 	@Override
 	public String getName() {
 		return "jython";
 	}
 
 	@Override
 	public String getDisplayName(Locale locale) {
		if (locale != null && locale.equals(Locale.CHINESE))
			return "Jython数据采集器";
		if (locale != null && locale.equals(Locale.KOREAN))
			return "자이썬 수집기";
		return "Jython Logger";
 	}

 	@Override
 	public Collection<Locale> getDisplayNameLocales() {
 		return Arrays.asList(Locale.ENGLISH, Locale.CHINESE);
 	}
 
 	@Override
 	public String getDescription(Locale locale) {
 		if (locale != null && locale.equals(Locale.CHINESE))
			return "Jython数据采集器脚本";
		if (locale != null && locale.equals(Locale.KOREAN))
			return "자이썬 수집 스크립트";
		return "Jython Logger Script";
 	}

 	@Override
 	public Collection<Locale> getDescriptionLocales() {
 		return Arrays.asList(Locale.ENGLISH, Locale.CHINESE);
 	}
 
 	@Override
 	public Collection<LoggerConfigOption> getConfigOptions() {
		LoggerConfigOption scriptName = new StringConfigType("logger_script", map("logger script name", "스크립트 이름", "数据采集器脚本名称"),
				map("jython logger script class name", "스크립트 클래스 이름", "数据采集器脚本类名称"), true);
 		return Arrays.asList(scriptName);
 	}
 
	private Map<Locale, String> map(String value, String ko, String cn) {
 		Map<Locale, String> m = new HashMap<Locale, String>();
 		m.put(Locale.ENGLISH, value);
		m.put(Locale.KOREAN, ko);
 		m.put(Locale.CHINESE, cn);
 		return m;
 	}
 
 	@Override
 	protected Logger createLogger(LoggerSpecification spec) {
 		String scriptName = spec.getConfig().get("logger_script");
 		PyObject factory = scripts.get(scriptName);
 		if (factory == null)
 			throw new IllegalStateException("jython logger script not found: " + scriptName);
 
 		PyObject instance = factory.__call__();
 		JythonActiveLogger logger = (JythonActiveLogger) instance.__tojava__(JythonActiveLogger.class);
 		logger.preInit(this, spec);
 		logger.init(spec);
 		return logger;
 	}
 
 	//
 	// implement script registry
 	//
 
 	@Override
 	public Set<String> getScriptNames() {
 		return scripts.keySet();
 	}
 
 	@Override
 	public String getScriptCode(String name) {
 		ConfigDatabase db = conf.ensureDatabase("araqne-logdb-jython");
 		Config c = db.findOne(ScriptConfig.class, getPredicate(name));
 		if (c == null)
 			return null;
 
 		ScriptConfig sc = c.getDocument(ScriptConfig.class);
 		return sc.getScript();
 	}
 
 	private Predicate getPredicate(String name) {
 		Map<String, Object> pred = new HashMap<String, Object>();
 		pred.put("name", name);
 		pred.put("type", "logger");
 		return Predicates.field(pred);
 	}
 
 	@Override
 	public void loadScript(String name, String script) {
 		PyObject factory = eval(name, script);
 		scripts.put(name, factory);
 
 		ScriptConfig sc = new ScriptConfig(name, "logger", script);
 		ConfigDatabase db = conf.ensureDatabase("araqne-logdb-jython");
 		Config c = db.findOne(ScriptConfig.class, getPredicate(name));
 		if (c == null) {
 			db.add(sc);
 		} else {
 			db.update(c, sc);
 		}
 	}
 
 	@Override
 	public void unloadScript(String name) {
 		ConfigDatabase db = conf.ensureDatabase("araqne-logdb-jython");
 		Config c = db.findOne(ScriptConfig.class, Predicates.field("name", name));
 		if (c == null)
 			throw new IllegalStateException("script not found: " + name);
 
 		scripts.remove(name);
 	}
 
 	private PyObject eval(String name, String script) {
 		PythonInterpreter interpreter = jython.newInterpreter();
 		interpreter.exec("from org.araqne.logdb.jython import JythonActiveLogger");
 		interpreter.exec("from org.araqne.log.api import SimpleLog");
 		interpreter.exec("from java.util import Date");
 		interpreter.exec(script);
 		PyObject clazz = interpreter.get(name);
 		if (clazz == null)
 			throw new IllegalStateException("cannot eval jython logger script " + name);
 
 		return clazz;
 	}
 }
