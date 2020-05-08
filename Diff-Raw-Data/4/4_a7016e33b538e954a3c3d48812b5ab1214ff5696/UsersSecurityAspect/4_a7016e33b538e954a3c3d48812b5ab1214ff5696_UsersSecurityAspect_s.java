 /*
  * Copyright 2009 Luigi Dell'Aquila (luigi.dellaquila@assetdata.it)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.romaframework.module.security.users;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import org.romaframework.aspect.authentication.AuthenticationAspect;
 import org.romaframework.aspect.security.Secure;
 import org.romaframework.aspect.security.SecurityAspectAbstract;
 import org.romaframework.aspect.security.exception.SecurityException;
 import org.romaframework.aspect.security.feature.SecurityActionFeatures;
 import org.romaframework.aspect.security.feature.SecurityClassFeatures;
 import org.romaframework.aspect.security.feature.SecurityFieldFeatures;
 import org.romaframework.aspect.view.feature.ViewFieldFeatures;
 import org.romaframework.core.Roma;
 import org.romaframework.core.schema.SchemaAction;
 import org.romaframework.core.schema.SchemaClass;
 import org.romaframework.core.schema.SchemaClassElement;
 import org.romaframework.core.schema.SchemaEvent;
 import org.romaframework.core.schema.SchemaField;
 import org.romaframework.module.users.domain.AbstractAccount;
 import org.romaframework.module.users.domain.BaseAccount;
 import org.romaframework.module.users.domain.BaseGroup;
 
 public class UsersSecurityAspect extends SecurityAspectAbstract {
 
 	// protected String
 	@Override
 	public String aspectName() {
 		return ASPECT_NAME;
 	}
 
 	@Override
 	public void startup() {
 		super.startup();
 	}
 
 	public Object getUnderlyingComponent() {
 		return null;
 	}
 
 	public void configEvent(SchemaEvent event) {
 	}
 
 	private BaseAccount getAccount() {
 		BaseAccount account = (BaseAccount) Roma.aspect(AuthenticationAspect.class).getCurrentAccount();
 		return account;
 	}
 
 	public boolean canRead(Object obj, SchemaField iSchemaField) {
 		if (obj instanceof Secure && !((Secure) obj).canRead()) {
 			return false;
 		}
 		return canRead(obj, iSchemaField, getAccount());
 	}
 
 	public boolean canWrite(Object obj, SchemaField iSchemaField) {
 		if (obj instanceof Secure && !((Secure) obj).canWrite()) {
 			return false;
 		}
 		return canWrite(obj, iSchemaField, getAccount());
 	}
 
 	public boolean canExecute(Object obj, SchemaClassElement iSchemaElement) {
 		return canExecute(obj, iSchemaElement, getAccount());
 	}
 
 	public boolean canRead(Object obj, SchemaField iSchemaField, AbstractAccount account) {
 		String[] readRules = iSchemaField.getFeature(SecurityFieldFeatures.READ_ROLES);
 		if (readRules == null || readRules.equals("")) {
 			readRules = iSchemaField.getEntity().getFeature(SecurityClassFeatures.READ_ROLES);
 		}
 		return matchesRule(iSchemaField.toString(), account, readRules);
 	}
 
 	public boolean canWrite(Object obj, SchemaField iSchemaField, AbstractAccount account) {
 		String[] readRules = iSchemaField.getFeature(SecurityFieldFeatures.WRITE_ROLES);
 		if (readRules == null || readRules.equals("")) {
 			readRules = iSchemaField.getEntity().getFeature(SecurityClassFeatures.WRITE_ROLES);
 		}
 		return matchesRule(iSchemaField.toString(), account, readRules);
 	}
 
 	public boolean canExecute(Object obj, SchemaClassElement iSchemaAction, AbstractAccount account) {
 		String[] readRules = iSchemaAction.getFeature(SecurityActionFeatures.ROLES);
 		if (readRules == null || readRules.equals("")) {
 			readRules = iSchemaAction.getEntity().getFeature(SecurityClassFeatures.EXECUTE_ROLES);
 		}
 		return matchesRule(iSchemaAction.toString(), account, readRules);
 	}
 
 	public boolean matchesRule(String iResource, AbstractAccount account, String[] readRules) {
 		if (readRules == null || readRules.length == 0)
 			return true;// no rules exist on this element
 		if (account == null) {
 			throw new SecurityException("The resource requested '" + iResource + "' is protected and need an authenticated account to access in");
 		}
 
 		for (String readRule : readRules) {
 			readRule = readRule.trim();
 			if (readRule.isEmpty())
 				throw new IllegalArgumentException("Found an empty rule for the resource: " + iResource);
 
 			int split_idx = readRule.indexOf(':');
 			if (split_idx == -1)
 				throw new IllegalArgumentException("Found wrong rule: '" + readRule + "' for the resource: " + iResource);
 			String target = readRule.substring(0, split_idx);
 			String rule = readRule.substring(split_idx + 1);
 			if ("user".equalsIgnoreCase(target)) {
 				if (Pattern.matches(rule, account.getName()))
 					return true;
 			}
 			if (account instanceof BaseAccount) {
 				BaseAccount baseAccount = (BaseAccount) account;
 				if ("profile".equalsIgnoreCase(target)) {
 					if (baseAccount.getProfile() != null && baseAccount.getProfile().getName() != null) {
 						if (Pattern.matches(rule, baseAccount.getProfile().getName()))
 							return true;
 					}
 				}
 				if ("group".equalsIgnoreCase(target)) {
 					for (BaseGroup group : baseAccount.getGroups()) {
 						if (matchesRule(iResource, group, readRules))
 							return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	public Object decrypt(Object obj, String fieldName) {
 		// TODO implement this!!!
 		throw new UnsupportedOperationException();
 	}
 
 	public Object encrypt(Object obj, String fieldName) {
 		// TODO implement this!!!
 		throw new UnsupportedOperationException();
 	}
 
 	public Object onAfterFieldRead(Object iContent, SchemaField iField, Object iCurrentValue) {
 		if (iCurrentValue instanceof Collection<?>) {
 			Iterator<?> iter = ((Collection<?>) iCurrentValue).iterator();
 			while (iter.hasNext()) {
 				if (hasToRemoveValue(iter.next()))
 					iter.remove();
 			}
 		}
 
 		if (iCurrentValue instanceof Map<?, ?>) {
 			Map<?, ?> map = (Map<?, ?>) iCurrentValue;
 
 			Object key;
 
 			Iterator<?> iterator = map.entrySet().iterator();
 			while (iterator.hasNext()) {
 				Map.Entry<?, ?> iter = (Map.Entry<?, ?>) iterator.next();
 				key = iter.getKey();
 
 				// CHECK THE KEY
 				if (key != null && !allowClass(Roma.schema().getSchemaClass(key.getClass())))
 					iterator.remove();
 				// CHECK THE VALUE
 				else if (iter.getValue() != null && !allowClass(Roma.schema().getSchemaClass(iter.getValue().getClass())))
 					iterator.remove();
 			}
 		}
 
 		return iCurrentValue;
 	}
 
 	public Object onAfterFieldWrite(Object iContent, SchemaField iField, Object iCurrentValue) {
 		return iCurrentValue;
 	}
 
 	public void onAfterAction(Object iContent, SchemaAction iAction, Object returnedValue) {
 	}
 
 	public boolean onBeforeAction(Object iContent, SchemaAction iAction) {
 		if (canExecute(iContent, iAction)) {
 			return true;
 		}
 		throw new SecurityException("Current account can't execute the action '" + iAction + "' because has no privileges");
 	}
 
 	public void onExceptionAction(Object iContent, SchemaAction iAction, Exception exception) {
 
 	}
 
 	public Object onBeforeFieldRead(Object iContent, SchemaField iField, Object iCurrentValue) {
 		if (canRead(iContent, iField)) {
 			if (!canWrite(iContent, iField)) {
 				Boolean enabled = (Boolean) iField.getFeature(ViewFieldFeatures.ENABLED);
 				if (enabled == null || enabled) {
 					Roma.setFeature(iContent, iField.getName(), ViewFieldFeatures.ENABLED, false);
 				}
 			}
 			return IGNORED;
 		}
 		Boolean enabled = (Boolean) iField.getFeature(ViewFieldFeatures.ENABLED);
 		if (enabled == null || enabled) {
 			Roma.setFeature(iContent, iField.getName(), ViewFieldFeatures.ENABLED, false);
 		}
 		return null;
 	}
 
 	public Object onBeforeFieldWrite(Object iContent, SchemaField iField, Object iCurrentValue) {
 		if (canWrite(iContent, iField)) {
 			return iCurrentValue;
 		}
 		Object result = iField.getValue(iContent);
 		Boolean enabled = (Boolean) iField.getFeature(ViewFieldFeatures.ENABLED);
 		if (enabled == null || enabled) {
 			Roma.setFeature(iContent, iField.getName(), ViewFieldFeatures.ENABLED, false);
 		}
 		return result;
 	}
 
 	public boolean allowAction(SchemaAction iAction) {
 		if (iAction == null)
 			return true;
 
 		String[] rule = iAction.getFeature(SecurityActionFeatures.ROLES);
 		if (rule == null) {
 			rule = iAction.getEntity().getFeature(SecurityClassFeatures.EXECUTE_ROLES);
 		}
 		if (rule == null)
 			return true;
 		return matchesRule(iAction.toString(), getAccount(), rule);
 	}
 
 	public boolean allowClass(SchemaClass iClass) {
 		if (iClass == null)
 			return true;
 
 		String rule[] = iClass.getFeature(SecurityClassFeatures.READ_ROLES);
 		if (rule == null)
 			return true;
 		return matchesRule(iClass.toString(), getAccount(), rule);
 	}
 
 	public boolean allowEvent(SchemaEvent iEvent) {
 		if (iEvent == null)
 			return true;
 
		String[] rule = iEvent.getFeature(SecurityActionFeatures.ROLES);
 		if (rule == null) {
 			rule = iEvent.getEntity().getFeature(SecurityClassFeatures.EXECUTE_ROLES);
 		}
 		if (rule == null)
 			return true;
 		return matchesRule(iEvent.toString(), getAccount(), rule);
 	}
 
 	public boolean allowField(SchemaField iField) {
 		if (iField == null)
 			return true;
 
 		String[] rule = iField.getFeature(SecurityFieldFeatures.READ_ROLES);
 		if (rule == null) {
 			rule = iField.getEntity().getFeature(SecurityClassFeatures.READ_ROLES);
 		}
 		if (rule == null)
 			return true;
 		return matchesRule(iField.toString(), getAccount(), rule);
 	}
 
 	private boolean hasToRemoveValue(Object iValue) {
 		if (iValue instanceof Secure && !((Secure) iValue).canRead())
 			return true;
 
 		if (iValue != null && !allowClass(Roma.schema().getSchemaClassIfExist(iValue.getClass())))
 			return true;
 
 		return false;
 	}
 }
