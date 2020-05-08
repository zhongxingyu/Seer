 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.wicket.security;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.security.actions.WaspAction;
 import org.apache.wicket.security.components.SecureComponentHelper;
 import org.apache.wicket.security.models.ISecureModel;
 import org.apache.wicket.security.strategies.ClassAuthorizationStrategy;
 import org.apache.wicket.security.strategies.LoginException;
 
final class TestStrategy extends ClassAuthorizationStrategy
 {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private boolean loggedin = false;
 
 	private Map authorized = new HashMap();
 
 	/**
 	 * 
 	 */
 	public TestStrategy()
 	{
 		super();
 	}
 
 	/**
 	 * @param secureClass
 	 */
 	public TestStrategy(Class secureClass)
 	{
 		super(secureClass);
 	}
 
 	public void destroy()
 	{
 		super.destroy();
 	}
 
 	public boolean isClassAuthenticated(Class clazz)
 	{
 		return loggedin;
 	}
 
 	public boolean isClassAuthorized(Class clazz, WaspAction action)
 	{
 		return isAuthorized(clazz, action);
 	}
 
 	/**
 	 * @param obj
 	 * @param action
 	 * @return
 	 */
 	private boolean isAuthorized(Object obj, WaspAction action)
 	{
 		WaspAction authorizedAction = (WaspAction)authorized.get(obj);
 		if (authorizedAction == null)
 			return false;
 		return authorizedAction.implies(action);
 	}
 
 	public boolean isComponentAuthenticated(Component component)
 	{
 		return loggedin;
 	}
 
 	public boolean isComponentAuthorized(Component component, WaspAction action)
 	{
 		if (!isAuthorized(component.getClass(), action))
 			return isAuthorized(SecureComponentHelper.alias(component), action);
 		return true;
 	}
 
 	public boolean isModelAuthenticated(IModel model, Component component)
 	{
 		return loggedin;
 	}
 
 	public boolean isModelAuthorized(ISecureModel model, Component component, WaspAction action)
 	{
 		return isAuthorized("model:" + component.getId(), action);
 	}
 
 	public void login(Object context) throws LoginException
 	{
 		if (context instanceof Map)
 		{
 			loggedin = true;
 			authorized.putAll((Map)context);
 		}
 		else
 			throw new LoginException(
 					"Specify a map containing all the classes/components and what actions are authorized");
 	}
 
 	public boolean logoff(Object context)
 	{
 		if (context instanceof Map)
 		{
 			Map map = (Map)context;
 			Iterator it = map.keySet().iterator();
 			while (it.hasNext())
 				authorized.remove(it.next());
 		}
 		else
 			authorized.clear();
 		loggedin = !authorized.isEmpty();
 		return true;
 	}
 }
