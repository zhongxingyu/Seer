 /*
  * Licensed to the Sakai Foundation (SF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The SF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 package org.sakaiproject.nakamura.api.lite.authorizable;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 import org.apache.commons.lang.StringUtils;
 import org.sakaiproject.nakamura.api.lite.RemoveProperty;
 import org.sakaiproject.nakamura.api.lite.StorageClientException;
 import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
 import org.sakaiproject.nakamura.api.lite.util.Iterables;
 import org.sakaiproject.nakamura.api.lite.util.PreemptiveIterator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Base Authorizable object.
  */
 public class Authorizable {
 
     public static final String PASSWORD_FIELD = "pwd";
 
     /**
      * List of principals that this Authorizable has.
      */
     public static final String PRINCIPALS_FIELD = "principals";
 
     /**
      * List of members that are members of this authorizable.
      */
     public static final String MEMBERS_FIELD = "members";
 
     /**
      * The ID of the authorizable.
      */
     public static final String ID_FIELD = "id";
 
     /**
      * The name of the authorizable.
      */
     public static final String NAME_FIELD = "name";
 
     /**
      * The type of the authorizable, either g or u (Group or User)
      */
     public static final String AUTHORIZABLE_TYPE_FIELD = "type";
 
     /**
      * The type value indicating a group.
      */
     public static final String GROUP_VALUE = "g";
     /**
      * The type value indicating a user.
      */
     public static final Object USER_VALUE = "u";
 
     /**
      * The name of the administrators group, members of which are granted access
      * to certain functions.
      */
     public static final String ADMINISTRATORS_GROUP = "administrators";
 
     /**
      * The time (epoch long) the authroizable was modified.
      */
     public static final String LASTMODIFIED_FIELD = "lastModified";
     /**
      * The ID of the authorizable that last modified this authorizable.
      */
     public static final String LASTMODIFIED_BY_FIELD = "lastModifiedBy";
     /**
      * The time (epoch long) when the authorizable was created.
      */
     public static final String CREATED_FIELD = "created";
     /**
      * The ID of the authorizable that created this authorizable.
      */
     public static final String CREATED_BY_FIELD = "createdBy";
 
     /**
      * A set of properties to filter out when sending out and setting.
      */
     private static final Set<String> FILTER_PROPERTIES = ImmutableSet.of(PASSWORD_FIELD, ID_FIELD);
 
     /**
      * A set of properties that are not visiable.
      */
     private static final Set<String> PRIVATE_PROPERTIES = ImmutableSet.of(PASSWORD_FIELD);
 
     /**
      * no password value.
      */
     public static final String NO_PASSWORD = "--none--";
 
     protected static final Logger LOGGER = LoggerFactory.getLogger(Authorizable.class);
 
     private static final Set<String> IMMUTABLE_AUTH_IDS = ImmutableSet.of(Group.EVERYONE);
 
     /**
      * A read only copy of the map, protected by an Immutable Wrapper
      */
     protected ImmutableMap<String, Object> authorizableMap;
     /**
      * A set of principals that this Authorizable has.
      */
     protected Set<String> principals;
 
     /**
      * The ID of this authorizable.
      */
     protected String id;
 
     /**
      * Modifications to the map.
      */
     protected Map<String, Object> modifiedMap;
     /**
      * true if the principals have been modified.
      */
     protected boolean principalsModified;
 
     /**
      * true if the objec is new.
      */
     private boolean isObjectNew = true;
 
     /**
      * true if the object is read only.
      */
     protected boolean readOnly;
 
     private boolean immutable;
 
     public Authorizable(Map<String, Object> autorizableMap) {
         principalsModified = false;
         modifiedMap = Maps.newHashMap();
         init(autorizableMap);
     }
 
     private void init(Map<String, Object> newMap) {
         this.authorizableMap = ImmutableMap.copyOf(newMap);
         Object principalsB = authorizableMap.get(PRINCIPALS_FIELD);
         if (principalsB == null) {
             this.principals = Sets.newLinkedHashSet();
         } else {
             this.principals = Sets.newLinkedHashSet(Iterables.of(StringUtils.split(
                     (String) principalsB, ';')));
         }
         this.id = (String) authorizableMap.get(ID_FIELD);
         if (!User.ANON_USER.equals(this.id)) {
             this.principals.add(Group.EVERYONE);
         }
     }
 
     /**
      * @param newMap
      *            the new map to reset the authorizable to.
      */
     public void reset(Map<String, Object> newMap) {
         if (!readOnly) {
             principalsModified = false;
             modifiedMap.clear();
             init(newMap);
 
             LOGGER.debug("After Update to Authorizable {} ", authorizableMap);
         }
     }
 
     /**
      * @return an array of principals that the authorizable has, indicating the
      *         groups that the authorizable is a member of and any other
      *         principals that have been granted to this authorizable.
      *         Principals are generally use in access control list and are not
      *         limited to group ids.
      */
     public String[] getPrincipals() {
         return principals.toArray(new String[principals.size()]);
     }
 
     /**
      * @return the ID of this authorizable (immutable)
      */
     public String getId() {
         return id;
     }
 
     // TODO: Unit test
     /**
      * @return get the current set of safe properties that can be updated, laking into account any modifications.
      */
     public Map<String, Object> getSafeProperties() {
         if (!readOnly && principalsModified) {
             modifiedMap.put(PRINCIPALS_FIELD, StringUtils.join(principals, ';'));
         }
         return StorageClientUtils.getFilterMap(authorizableMap, modifiedMap, null,
                 FILTER_PROPERTIES, false);
     }
 
     /**
      * @return true if this authorizable is a group.
      */
     public boolean isGroup() {
         return false;
     }
 
     /**
      * @return get the orriginal properties of this authorizable ignoring any unsaved properties.
      */
     public Map<String, Object> getOriginalProperties() {
         return StorageClientUtils.getFilterMap(authorizableMap, null, null, FILTER_PROPERTIES, false);
     }
 
     /**
      * Set a property. The property will only be set if writable. If the property or this athorizable is read only, nothing will happen.
      * @param name the name of the property
      * @param value the value of the property.
      */
     public void setProperty(String name, Object value) {
         if (!readOnly && !FILTER_PROPERTIES.contains(name)) {
             Object cv = authorizableMap.get(name);
            if (!value.equals(cv)) {
                 modifiedMap.put(name, value);
             } else if (modifiedMap.containsKey(name) && !value.equals(modifiedMap.get(name))) {
                 modifiedMap.put(name, value);
             }
 
         }
     }
 
     /**
      * @param name
      * @return the instance of the property. Note that if the property is an array or object it will be mutable.
      */
     public Object getProperty(String name) {
         if (!PRIVATE_PROPERTIES.contains(name)) {
             if (modifiedMap.containsKey(name)) {
                 Object o = modifiedMap.get(name);
                 if (o instanceof RemoveProperty) {
                     return null;
                 } else {
                     return o;
                 }
             }
             return authorizableMap.get(name);
         }
         return null;
     }
 
     /**
      * remove the property.
      * @param name 
      */
     public void removeProperty(String key) {
         if (!readOnly && (authorizableMap.containsKey(key) || modifiedMap.containsKey(key))) {
             modifiedMap.put(key, new RemoveProperty());
         }
     }
 
     /**
      * add a principal to this authorizable.
      * @param principal 
      */
     public void addPrincipal(String principal) {
         if (!readOnly && !principals.contains(principal)) {
             principals.add(principal);
             principalsModified = true;
         }
     }
 
     /**
      * remove a principal from this authorizable.
      * @param principal
      */
     public void removePrincipal(String principal) {
         if (!readOnly && principals.contains(principal)) {
             principals.remove(principal);
             principalsModified = true;
         }
     }
 
     /**
      * @return a Map or properties that should be saved to storage. This merges the original properties and unsaved changed.
      */
     public Map<String, Object> getPropertiesForUpdate() {
         if (!readOnly && principalsModified) {
             principals.remove(Group.EVERYONE);
             modifiedMap.put(PRINCIPALS_FIELD, StringUtils.join(principals, ';'));
             principals.add(Group.EVERYONE);
         }
         return StorageClientUtils.getFilterMap(authorizableMap, modifiedMap, null,
                 FILTER_PROPERTIES, true);
     }
 
     /**
      * @return true if the authorizable is modified.
      */
     public boolean isModified() {
         return !readOnly && (principalsModified || (modifiedMap.size() > 0));
     }
 
     /**
      * @param name
      * @return true if the property is set in the unsaved version of the authorizable.
      */
     public boolean hasProperty(String name) {
         Object modifiedValue = modifiedMap.get(name);
         if (modifiedValue instanceof RemoveProperty) {
             return false;
         }
         if (modifiedValue != null) {
             return true;
         }
         return authorizableMap.containsKey(name);
     }
 
     /**
      * @param authorizableManager
      * @return an Iterator containing Groups this authorizable is a direct or
      *         indirect member of.
      */
     public Iterator<Group> memberOf(final AuthorizableManager authorizableManager) {
         final List<String> memberIds = new ArrayList<String>();
         Collections.addAll(memberIds, getPrincipals());
         return new PreemptiveIterator<Group>() {
 
             private int p;
             private Group group;
 
             @Override
             protected boolean internalHasNext() {
                 while (p < memberIds.size()) {
                     String id = memberIds.get(p);
                     p++;
                     try {
                         Authorizable a = authorizableManager.findAuthorizable(id);
                         if (a instanceof Group) {
                             group = (Group) a;
                             for (String pid : a.getPrincipals()) {
                                 if (!memberIds.contains(pid)) {
                                     memberIds.add(pid);
                                 }
                             }
                             return true;
                         }
                     } catch (AccessDeniedException e) {
                         LOGGER.debug(e.getMessage(), e);
                     } catch (StorageClientException e) {
                         LOGGER.debug(e.getMessage(), e);
                     }
                 }
                 return false;
             }
 
             @Override
             protected Group internalNext() {
                 return group;
             }
 
         };
     }
 
     /**
      * @param isObjectNew mark the object as new.
      */
     protected void setObjectNew(boolean isObjectNew) {
         this.isObjectNew = isObjectNew;
     }
 
     /**
      * @return true if the object is new.
      */
     public boolean isNew() {
         return isObjectNew;
     }
 
     /**
      * @param readOnly mark the object read only.
      */
     protected void setReadOnly(boolean readOnly) {
         if (!this.readOnly) {
             this.readOnly = readOnly;
         }
     }
     public boolean isReadOnly() {
         return readOnly;
     }
 
     @Override
     public int hashCode() {
         return id.hashCode();
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj instanceof Authorizable) {
             Authorizable a = (Authorizable) obj;
             return id.equals(a.getId());
         }
         return super.equals(obj);
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
         return id;
     }
 
     public boolean isImmutable() {
         return immutable || IMMUTABLE_AUTH_IDS.contains(id);
     }
 }
