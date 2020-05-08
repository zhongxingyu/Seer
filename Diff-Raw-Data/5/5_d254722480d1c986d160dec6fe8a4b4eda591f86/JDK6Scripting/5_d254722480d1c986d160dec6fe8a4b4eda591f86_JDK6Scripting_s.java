 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.core;
 
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.scripting.FxScriptBinding;
 import com.flexive.shared.scripting.FxScriptResult;
 
 import javax.script.ScriptEngineFactory;
 import javax.script.ScriptEngine;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Java 6 Scripting extensions for [fleXive]
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev
  */
 public class JDK6Scripting {
 
     /**
      * Run a script.
      * <b>This method is internal and intended to be exclusively called by the ScriptingEngine!</b>
      * If its a groovy script (determined by extension "gy" or "groovy"), the native groovy shell is used, else
      * a scripting engine registered with the JDK is probed.
      *
      * @param name    name of the script - needed to determine language
      * @param binding bindings
      * @param code    code to execute
      * @return FxScriptResult
      * @throws FxApplicationException on errors
      */
     public static FxScriptResult runScript(String name, FxScriptBinding binding, String code) throws FxApplicationException {
         if (name == null)
             name = "unknown";
         if (binding != null) {
             if (!binding.getProperties().containsKey("ticket"))
                 binding.setVariable("ticket", FxContext.get().getTicket());
             if (!binding.getProperties().containsKey("environment"))
                 binding.setVariable("environment", CacheAdmin.getEnvironment());
             binding.setVariable("scriptname", name);
         }
         try {
            Class.forName("javax.script.ScriptEngineManager"); //provoke exception if no JDK >= 6 installed
             String ext = name.substring(name.lastIndexOf('.') + 1);
             javax.script.ScriptEngineManager manager = new javax.script.ScriptEngineManager();
            javax.script.ScriptEngine engine = manager.getEngineByName(ext);
             if (engine == null)
                 throw new FxInvalidParameterException(name, "ex.general.scripting.noEngine", name).asRuntimeException();
             javax.script.Bindings b = engine.createBindings();
             if (binding != null)
                 b.putAll(binding.getProperties());
             b.put(ScriptEngine.FILENAME, name);
             engine.setBindings(b, javax.script.ScriptContext.ENGINE_SCOPE);
             Object result = engine.eval(code);
             if (binding != null) {
                 binding.getProperties().clear();
                 Object o;
                 for (String key : engine.getBindings(javax.script.ScriptContext.ENGINE_SCOPE).keySet()) {
                     o = engine.getBindings(javax.script.ScriptContext.ENGINE_SCOPE).get(key);
                     if (o instanceof Serializable)
                         binding.getProperties().put(key, (Serializable) o);
                 }
             }
             return new FxScriptResult(binding, result);
         } catch (ClassNotFoundException cnfe) {
             throw new FxInvalidParameterException(name, "ex.general.scripting.needJDK6", name).asRuntimeException();
         } catch (javax.script.ScriptException e) {
             throw new FxInvalidParameterException(name, "ex.general.scripting.exception", name, e.getMessage()).asRuntimeException();
         }
     }
 
     /**
      * Get a list of all available scripting engines
      *
      * @return list of all available scripting engines, [0]=ext, [1]=description
      */
     public static List<String[]> getAvailableScriptEngines() {
         List<String[]> res = new ArrayList<String[]>(5);
         for (ScriptEngineFactory f : new javax.script.ScriptEngineManager().getEngineFactories()) {
             for (String ext : f.getExtensions())
                 res.add(new String[]{ext, ext + ": " + f.getLanguageName() + " v" + f.getLanguageVersion() +
                         " (" + f.getEngineName() + " v" + f.getEngineVersion() + ")"});
         }
         return res;
     }
 
 }
