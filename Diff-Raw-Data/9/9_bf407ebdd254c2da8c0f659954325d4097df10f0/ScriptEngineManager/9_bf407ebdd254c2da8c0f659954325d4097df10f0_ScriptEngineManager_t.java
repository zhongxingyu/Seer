 /**
  *
  * Copyright 2006 - 2007 (C) The original author or authors
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package javax.script;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 
 /**
  * @version $Revision$ $Date$
  */
 public class ScriptEngineManager
 {
     private final Set<ScriptEngineFactory> engineSpis = new HashSet<ScriptEngineFactory>();
     private final Map<String, ScriptEngineFactory> byName = new HashMap<String, ScriptEngineFactory>();
     private final Map<String, ScriptEngineFactory> registeredByName = new HashMap<String, ScriptEngineFactory>();
     private final Map<String, ScriptEngineFactory> byExtension = new HashMap<String, ScriptEngineFactory>();
     private final Map<String, ScriptEngineFactory> registeredByExtension = new HashMap<String, ScriptEngineFactory>();
     private final Map<String, ScriptEngineFactory> byMimeType = new HashMap<String, ScriptEngineFactory>();
     private final Map<String, ScriptEngineFactory> registeredByMimeType = new HashMap<String, ScriptEngineFactory>();
     private Bindings globalScope;
 
     public ScriptEngineManager()
     {
         this(Thread.currentThread().getContextClassLoader());
     }
 
     public ScriptEngineManager(ClassLoader classLoader)
     {
         try
         {
             Enumeration factoryResources = classLoader.getResources("META-INF/services/javax.script.ScriptEngineFactory");
             while (factoryResources.hasMoreElements())
             {
                 URL url = (URL) factoryResources.nextElement();
                 for (String className : getClassNames(url))
                 {
                     try
                     {
                         Class factoryClass = classLoader.loadClass(className);
                         Object object = factoryClass.newInstance();
 
                         if (object instanceof ScriptEngineFactory)
                         {
                             ScriptEngineFactory factory = (ScriptEngineFactory) object;
 
                             byName.put(factory.getEngineName(), factory);
 
                             for (String extension : factory.getExtensions()) byExtension.put(extension, factory);
 
                             for (String mimeType : factory.getMimeTypes()) byMimeType.put(mimeType, factory);
 
                             engineSpis.add(factory);
                         }
                     }
                     catch (ClassNotFoundException doNothing)
                     {
                     }
                     catch (IllegalAccessException doNothing)
                     {
                     }
                     catch (InstantiationException doNothing)
                     {
                     }
                 }
 
             }
         }
         catch (IOException doNothing)
         {
         }
     }
 
     public Bindings getGlobalScope()
     {
         return globalScope;
     }
 
     public void setGlobalScope(Bindings globalScope)
     {
         this.globalScope = globalScope;
     }
 
     public void put(String key, Object value)
     {
         if (globalScope != null) globalScope.put(key, value);
     }
 
     public Object get(String key)
     {
         if (globalScope != null)
         {
             return globalScope.get(key);
         }
         else return null;
     }
 
     public ScriptEngine getEngineByName(String shortName)
     {
         ScriptEngineFactory factory = registeredByName.get(shortName);
 
         if (factory == null) factory = byName.get(shortName);
         if (factory == null) return null;
 
         ScriptEngine engine = factory.getScriptEngine();
 
         engine.setBindings(globalScope, ScriptContext.GLOBAL_SCOPE);
 
         return engine;
     }
 
     public ScriptEngine getEngineByExtension(String extension)
     {
         ScriptEngineFactory factory = registeredByExtension.get(extension);
 
         if (factory == null) factory = byExtension.get(extension);
         if (factory == null) return null;
 
         ScriptEngine engine = factory.getScriptEngine();
 
         engine.setBindings(globalScope, ScriptContext.GLOBAL_SCOPE);
 
         return engine;
     }
 
     public ScriptEngine getEngineByMimeType(String mimeType)
     {
         ScriptEngineFactory factory = registeredByMimeType.get(mimeType);
 
         if (factory == null) factory = byMimeType.get(mimeType);
         if (factory == null) return null;
 
         ScriptEngine engine = factory.getScriptEngine();
 
         engine.setBindings(globalScope, ScriptContext.GLOBAL_SCOPE);
 
         return engine;
     }
 
    public List<ScriptEngineFactory> getEngineFactories()
     {
        return new ArrayList<ScriptEngineFactory>(engineSpis);
     }
 
     public void registerEngineName(String name, ScriptEngineFactory factory)
     {
         registeredByName.put(name, factory);
     }
 
     public void registerEngineMimeType(String type, ScriptEngineFactory factory)
     {
         registeredByMimeType.put(type, factory);
     }
 
     public void registerEngineExtension(String extension, ScriptEngineFactory factory)
     {
         registeredByExtension.put(extension, factory);
     }
 
     private Iterable<String> getClassNames(URL url)
     {
         Stack<String> stack = new Stack<String>();
 
         try
         {
             BufferedReader r = new BufferedReader(new InputStreamReader(url.openStream()));
 
             String line;
             while ((line = r.readLine()) != null)
             {
                 int comment = line.indexOf('#');
                 if (comment != -1) line = line.substring(0, comment);
 
                 stack.push(line.trim());
             }
         }
         catch (IOException doNothing)
         {
         }
 
         return stack;
     }
 }
