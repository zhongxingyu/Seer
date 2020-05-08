 /*
     jbilling - The Enterprise Open Source Billing System
     Copyright (C) 2003-2007 Sapienter Billing Software Corp. and Emiliano Conde
 
     This file is part of jbilling.
 
     jbilling is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     jbilling is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 /*
  * Created on Apr 15, 2003
  *
  */
 package com.sapienter.jbilling.server.pluggableTask;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Properties;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 import org.drools.FactHandle;
 import org.drools.RuleBase;
 import org.drools.StatefulSession;
 import org.drools.agent.RuleAgent;
 
 import com.sapienter.jbilling.common.Util;
 import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskDTO;
 import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
 import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskParameterDTO;
 
 
 public abstract class PluggableTask {
     protected HashMap<String, Object> parameters = null;
     private Integer entityId = null;
     private PluggableTaskDTO task = null;
     protected Hashtable<Object,FactHandle> handlers = null;
     protected StatefulSession session = null;
     private static final Logger LOG = Logger.getLogger(PluggableTask.class);
     private static HashMap<Integer, RuleBase> rulesCache = new HashMap<Integer, RuleBase>();
 
     protected Integer getEntityId() {
         return entityId;
     }
     
     public void initializeParamters(PluggableTaskDTO task) 
             throws PluggableTaskException {
         Collection <PluggableTaskParameterDTO>DBparameters = task.getParameters();
         parameters = new HashMap<String, Object>();
         entityId = task.getEntityId();
         this.task = task;
         if (DBparameters.size() < 
                 task.getType().getMinParameters().intValue()) {
             throw new PluggableTaskException("Type [" + task.getType().getTitle(
                     new Integer(1)) + "] requires at least " + 
                     task.getType().getMinParameters() + " parameters." +
                     DBparameters.size() + " found.");
         }
         
         if (DBparameters.isEmpty()) {
             return;
         }
         
         for(PluggableTaskParameterDTO parameter: DBparameters) {
             Object value = parameter.getIntValue();
             if (value == null) {
                 value = parameter.getStrValue();
                 if (value == null) {
                     value = parameter.getFloatValue();
                 }
             }
             
             parameters.put(parameter.getName(), value);
         }
     }
     
     /**
      * Any pluggable task can get a rule base that takes the task's 
      * parameters as the configuration for the rule agent.
      * TODO: RuleBase is thread safe. There should be only one per pluggable task id.
      * So they can be cached in a simple hashmap, but you need to invalidate an entry
      * in the cache if the pluggable task is updated or deleted.
      * @return
      * @throws IOException
      * @throws Exception
      */
     protected RuleBase readRule() throws IOException, Exception {
         if (rulesCache.get(task.getId()) != null) {
             return rulesCache.get(task.getId());
         }
         Properties rulesProperties = new Properties();
         
         for (String key: parameters.keySet()) {
             rulesProperties.setProperty(key, (String) parameters.get(key));
             LOG.debug("adding parameter " + key + " value " + 
                     rulesProperties.getProperty(key));
         }
         if (parameters.size() == 0) {
             String defaultDir = Util.getSysProp("base_dir") + "rules";
             rulesProperties.setProperty("dir", defaultDir);
             LOG.debug("No task parameters, using directory default:" + defaultDir);
         }
         RuleAgent agent = RuleAgent.newRuleAgent(rulesProperties);
         RuleBase retValue = agent.getRuleBase();
         // update the cache, if configured that way
         if (Util.getSysPropBooleanTrue("cache_rules")) {
             rulesCache.put(task.getId(), retValue);
         }
         return retValue; 
     }
     
     public static void invalidateRuleCache(Integer taskId) {
         rulesCache.remove(taskId);
     }
     
     protected void executeStatefulRules(StatefulSession session, Vector context) {
         handlers = new Hashtable<Object,FactHandle>();
         for (Object o: context) {
             handlers.put(o, session.insert(o));
         }
 
         session.fireAllRules();
     }
 
     protected void removeObject(Object o) {
         FactHandle h = handlers.get(o);
         if (h != null) {
            session.retract(h);
         }
     }
     
     public void updateObject(Object old, Object newO)
             throws TaskException {
         if (old != null) { 
             removeObject(old);
         }
        handlers.put(newO, session.insert(newO));
        session.fireAllRules(); // could it lead to infinite recurring loop?
     }
 
 }
