 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: AgentConfiguration.java,v 1.25 2008-04-22 00:23:15 veiming Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.common.configuration;
 
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.idm.AMIdentity;
 import com.sun.identity.idm.AMIdentityRepository;
 import com.sun.identity.idm.IdConstants;
 import com.sun.identity.idm.IdRepoException;
 import com.sun.identity.idm.IdType;
 import com.sun.identity.security.AdminTokenAction;
 import com.sun.identity.sm.AttributeSchema;
 import com.sun.identity.sm.SMSException;
 import com.sun.identity.sm.SchemaType;
 import com.sun.identity.sm.ServiceSchema;
 import com.sun.identity.sm.ServiceSchemaManager;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.security.AccessController;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 /**
  * This class provides agent configuration utilities.
  */
 public class AgentConfiguration {
 
     public final static String AGENT_TYPE_J2EE = "J2EEAgent";
     public final static String AGENT_TYPE_WEB = "WebAgent";
     public final static String AGENT_TYPE_2_DOT_2_AGENT = "2.2_Agent";
     public final static String ATTR_NAME_PWD = "userpassword";
     public final static String ATTR_NAME_FREE_FORM =
         "com.sun.identity.agents.config.freeformproperties";
     public final static String ATTR_CONFIG_REPO =
         "com.sun.identity.agents.config.repository.location";
     public final static String VAL_CONFIG_REPO_LOCAL = "local";
 
     private static final String AGENT_LOCAL_PROPERTIES = "agentlocaleprop";
     private static Map localAgentProperties;
 
     private static final Pattern patternArray =
         Pattern.compile("(.+?\\[.*?\\]\\s*?)=(.*)");
 
   
     static {
         localAgentProperties = new HashMap();
         ResourceBundle rb = ResourceBundle.getBundle(AGENT_LOCAL_PROPERTIES);
         for (Enumeration e = rb.getKeys(); e.hasMoreElements(); ) {
             String key = (String)e.nextElement();
             Set set = new HashSet();
             String value = rb.getString(key);
             StringTokenizer st = new StringTokenizer(value, ",");
             while (st.hasMoreTokens()) {
                 set.add(st.nextToken());
             }
             localAgentProperties.put(key, set);
         }
     }
     
     private AgentConfiguration() {
     }
 
     /**
      * Returns a set of supported agent types.
      *
      * @return a set of supported agent types.
      */
     public static Set getAgentTypes()
         throws SMSException, SSOException
     {
         Set agentTypes = new HashSet();
         ServiceSchema ss = getOrganizationSchema();
         if (ss != null) {
             Set names = ss.getSubSchemaNames();
             if ((names != null) && !names.isEmpty()) {
                 agentTypes.addAll(names);
             }
         }
         return agentTypes;
     }
 
     private static void validateAgentType(String type)
         throws ConfigurationException {
         validateAgentType(type, false);
     }
 
     private static void validateAgentType(String type, boolean isGroup)
         throws ConfigurationException {
         try {
             Set types = getAgentTypes();
             if (isGroup) {
                 types.remove(AGENT_TYPE_2_DOT_2_AGENT);
             }
 
             if (!types.contains(type)) {
                 Object[] param = {type};
                 throw new ConfigurationException(
                     "agent.invalid.type", param);
             }
         } catch (SMSException e) {
             throw new ConfigurationException(e.getMessage());
         } catch (SSOException e) {
             throw new ConfigurationException(e.getMessage());
         }
     }
 
     /**
      * Creates an agent group.
      *
      * @param ssoToken Single Sign On token that is to be used for creation.
      * @param realm Realm where group resides.
      * @param agentGroupName Name of agent group.
      * @param agentType Type of agent group.
      * @param values Map of attribute name to its values.
      * @throws IdRepoException if there are Id Repository related errors.
      * @throws SSOException if the Single Sign On token is invalid or has
      *         expired.
      * @throws SMSException if there are errors in service management layers.
      * @throws ConfigurationException if there are missing information in
      *         server or agent URL; or invalid agent type.
      */
     public static void createAgentGroup(
         SSOToken ssoToken,
         String realm,
         String agentGroupName,
         String agentType,
         Map attrValues
     ) throws IdRepoException, SSOException, SMSException,
         ConfigurationException {
         createAgentGroupEx(ssoToken, realm, agentGroupName, agentType, attrValues,
             null, null);
     }
 
     /**
      * Creates an agent group.
      *
      * @param ssoToken Single Sign On token that is to be used for creation.
      * @param realm Realm where group resides.
      * @param agentGroupName Name of agent group.
      * @param agentType Type of agent group.
      * @param values Map of attribute name to its values.
      * @param serverURL Server URL.
      * @param agentURL Agent URL.
      * @throws IdRepoException if there are Id Repository related errors.
      * @throws SSOException if the Single Sign On token is invalid or has
      *         expired.
      * @throws SMSException if there are errors in service management layers.
      * @throws MalformedURLException if server or agent URL is invalid.
      * @throws ConfigurationException if there are missing information in
      *         server or agent URL; or invalid agent type
      */
     public static void createAgentGroup(
         SSOToken ssoToken,
         String realm,
         String agentGroupName,
         String agentType,
         Map attrValues,
         String serverURL,
         String agentURL
     ) throws IdRepoException, SSOException, SMSException,
         MalformedURLException, ConfigurationException {
         if ((serverURL == null) || (serverURL.trim().length() == 0)) {
             throw new ConfigurationException(
                 "create.agent.invalid.server.url", null);
         }
 
         URL urlAgent = null;
         if ((agentURL != null) && (agentURL.trim().length() > 0)) {
             urlAgent = new URL(agentURL);
         }
         createAgentGroupEx(ssoToken, realm, agentGroupName, agentType, 
             attrValues, new URL(serverURL), urlAgent);
     }
 
     /**
      * Creates an agent group.
      *
      * @param ssoToken Single Sign On token that is to be used for creation.
      * @param realm Name of realm where agent group is going to reside.
      * @param agentGroupName Name of agent group.
      * @param agentType Type of agent group.
      * @param values Map of attribute name to its values.
      * @throws IdRepoException if there are Id Repository related errors.
      * @throws SSOException if the Single Sign On token is invalid or has
      *         expired.
      * @throws SMSException if there are errors in service management layers.
      * @throws ConfigurationException if there are missing information in
      *         server or agent URL; or invalid agent type.
      */
     private static void createAgentGroupEx(
         SSOToken ssoToken,
         String realm,
         String agentGroupName,
         String agentType,
         Map attrValues,
         URL serverURL,
         URL agentURL
     ) throws IdRepoException, SSOException, SMSException,
         ConfigurationException {
         validateAgentType(agentType, true);
         AMIdentityRepository amir = new AMIdentityRepository(
             ssoToken, realm);
         Map attributeValues = parseAttributeMap(agentType, attrValues);
         Set setAgentType = new HashSet(2);
         setAgentType.add(agentType);
         attributeValues.put(IdConstants.AGENT_TYPE, setAgentType);
         Map inheritedValues = getDefaultValues(agentType);
         //overwrite inherited values with what user has given
         inheritedValues.putAll(attributeValues);
             
          if ((serverURL != null) || (agentURL != null)) {
             tagswapAttributeValues(inheritedValues, agentType, serverURL,
                 agentURL);
          }
 
         amir.createIdentity(IdType.AGENTGROUP, agentGroupName, inheritedValues);
     }
     /**
      * Creates an agent.
      *
      * @param ssoToken Single Sign On token that is to be used for creation.
      * @param realm Realm where agent resides.
      * @param agentName Name of agent.
      * @param agentType Type of agent.
      * @param values Map of attribute name to its values.
      * @param serverURL Server URL.
      * @param agentURL Agent URL.
      * @throws IdRepoException if there are Id Repository related errors.
      * @throws SSOException if the Single Sign On token is invalid or has
      *         expired.
      * @throws SMSException if there are errors in service management layers.
      * @throws MalformedURLException if server or agent URL is invalid.
      * @throws ConfigurationException if there are missing information in
      *         server or agent URL; or invalid agent type.
      */
     public static void createAgent(
         SSOToken ssoToken,
         String realm,
         String agentName,
         String agentType,
         Map attrValues,
         String serverURL,
         String agentURL
     ) throws IdRepoException, SSOException, SMSException,
         MalformedURLException, ConfigurationException {
         if ((serverURL == null) || (serverURL.trim().length() == 0)) {
             throw new ConfigurationException(
                 "create.agent.invalid.server.url", null);
         }
         if ((agentURL == null) || (agentURL.trim().length() == 0)) {
             throw new ConfigurationException(
                 "create.agent.invalid.agent.url", null);
         }
         createAgentEx(ssoToken, realm, agentName, agentType, attrValues,
             new URL(serverURL), new URL(agentURL));
     }
 
     /**
      * Creates an agent.
      *
      * @param ssoToken Single Sign On token that is to be used for creation.
      * @param realm Realm where agent resides.
      * @param agentName Name of agent.
      * @param agentType Type of agent.
      * @param values Map of attribute name to its values.
      * @throws IdRepoException if there are Id Repository related errors.
      * @throws SSOException if the Single Sign On token is invalid or has
      *         expired.
      * @throws SMSException if there are errors in service management layers.
      * @throws ConfigurationException if there are missing information in
      *         server or agent URL; or invalid agent type.
      */
     public static void createAgent(
         SSOToken ssoToken,
         String realm,
         String agentName,
         String agentType,
         Map attrValues
     ) throws IdRepoException, SSOException, SMSException,
         ConfigurationException {
         createAgentEx(ssoToken, realm, agentName, agentType, attrValues, null,
             null);
     }
 
     /**
      * Creates an agent.
      *
      * @param ssoToken Single Sign On token that is to be used for creation.
      * @param realm Name of realm where agent is going to reside.
      * @param agentName Name of agent.
      * @param agentType Type of agent.
      * @param values Map of attribute name to its values.
      * @param serverURL Server URL.
      * @param agentURL Agent URL.
      * @throws IdRepoException if there are Id Repository related errors.
      * @throws SSOException if the Single Sign On token is invalid or has
      *         expired.
      * @throws SMSException if there are errors in service management layers.
      * @throws ConfigurationException if there are missing information in
      *         server or agent URL; or invalid agent type.
      */
     private static void createAgentEx(
         SSOToken ssoToken,
         String realm,
         String agentName,
         String agentType,
         Map attrValues,
         URL serverURL,
         URL agentURL
     ) throws IdRepoException, SSOException, SMSException,
         ConfigurationException {
         validateAgentType(agentType);
         AMIdentityRepository amir = new AMIdentityRepository(
             ssoToken, realm);
         Map attributeValues = parseAttributeMap(agentType, attrValues);
         Set setAgentType = new HashSet(2);
         setAgentType.add(agentType);
         attributeValues.put(IdConstants.AGENT_TYPE, setAgentType);
         Map inheritedValues = getDefaultValues(agentType);
         //overwrite inherited values with what user has given
         inheritedValues.putAll(attributeValues);
         
         if ((serverURL != null) || (agentURL != null)) {
             tagswapAttributeValues(inheritedValues, agentType, serverURL,
                 agentURL);
         }
         amir.createIdentity(IdType.AGENTONLY, agentName, inheritedValues);
     }
 
     private static void tagswapAttributeValues(
         Map attributeValues,
         String agentType,
         URL serverURL,
         URL agentURL
     ) throws ConfigurationException {
         Map map = new HashMap();
 
         if (serverURL != null) {
             String uri = getURI(serverURL);
             if (uri.length() == 0){
                 throw new ConfigurationException(
                     "create.agent.invalid.server.url.missing.uri", null);
             }
 
             String port = Integer.toString(serverURL.getPort());
             if (port.equals("-1")){
                 throw new ConfigurationException(
                     "create.agent.invalid.server.url.missing.port", null);
             }
 
             map.put("SERVER_PROTO", serverURL.getProtocol());
             map.put("SERVER_HOST", serverURL.getHost());
             map.put("SERVER_PORT", port);
             map.put("AM_SERVICES_DEPLOY_URI", uri);
         }
 
         if (agentURL != null) {
             String port = Integer.toString(agentURL.getPort());
             map.put("AGENT_PROTO", agentURL.getProtocol());
             map.put("AGENT_HOST", agentURL.getHost());
             map.put("AGENT_PORT", port);
 
             if (agentType.equals(AGENT_TYPE_J2EE)) {
                 String uri = getURI(agentURL);
                 if (uri.length() == 0) {
                     throw new ConfigurationException(
                         "create.agent.invalid.agent.url.missing.uri", null);
                 }
                 map.put("AGENT_APP_URI", uri);
             }
 
             if (agentType.equals(AGENT_TYPE_J2EE)) {
                 String logFileName = agentURL.getHost();
                 logFileName = "amAgent_" + logFileName.replaceAll("\\.", "_") +
                     "_" + port + ".log";
                 map.put("AUDIT_LOG_FILENAME", logFileName);
             }
         }
 
         for (Iterator i = attributeValues.keySet().iterator(); i.hasNext(); ) {
             String attrName = (String)i.next();
             Set values = (Set)attributeValues.get(attrName);
             Set newValues = new HashSet(values.size() *2);
 
             for (Iterator j = values.iterator(); j.hasNext(); ) {
                 String value = (String)j.next();
                 newValues.add(tagswap(map, value));
             }
             values.clear();
             values.addAll(newValues);
         }
     }
 
     private static String tagswap(Map map, String value) {
         for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
             String k = (String)i.next();
             value = value.replaceAll("@" + k + "@", (String)map.get(k));
         }
         return value;
     }
 
     private static String getURI(URL url) {
         String uri = url.getPath();
         int idx = uri.indexOf('/', 1);
         if (idx != -1) {
             uri = uri.substring(0, idx);
         }
         return uri;
     }
     
     /**
      * Updates agent attribute values.
      *
      * @param ssoToken Single Sign On token that is to be used for creation.
      * @param realm Name of realm where agent resides.
      * @param agentName Name of agent.
      * @param values Map of attribute name to its values.
      * @param bSet <code>true</code> to overwrite the values for the
      *        attribute.
      * @throws IdRepoException if there are Id Repository related errors.
      * @throws SSOException if the Single Sign On token is invalid or has
      *         expired.
      * @throws SMSException if there are errors in service management layers.
      */
     public static void updateAgent(
         SSOToken ssoToken,
         String realm,
         String agentName,
         Map attrValues,
         boolean bSet
     ) throws IdRepoException, SSOException, SMSException {
         AMIdentity amid = new AMIdentity(ssoToken, agentName, 
             IdType.AGENTONLY, realm, null); 
         String agentType = getAgentType(amid);
         Map attributeValues = parseAttributeMap(agentType, attrValues);
 
         if (!bSet) {
             Map origValues = amid.getAttributes(attributeValues.keySet());
             for (Iterator i = attributeValues.keySet().iterator();
                 i.hasNext(); 
             ) {
                 String attrName = (String)i.next();
                 attributeValues.put(attrName, updateAttrValues(
                     agentType, attrName,
                     (Set)origValues.get(attrName),
                     (Set)attributeValues.get(attrName)));
             }
         }
         amid.setAttributes(attributeValues);
         amid.store();
     }
 
     private static Set updateAttrValues(
         String agentType,
         String attrName,
         Set origValues,
         Set newValues
     ) throws SMSException, SSOException {
         AttributeSchema as = getAgentAttributeSchema(attrName, agentType);
         return (as.getType().equals(AttributeSchema.Type.LIST)) ?
             updateAttrValues(origValues, newValues) : newValues;
     }
     
     private static Set updateAttrValues(Set origValues, Set newValues) {
         if ((origValues == null) || origValues.isEmpty()) {
             return newValues;
         }
 
         Set set = new HashSet();
         set.addAll(origValues);
 
         for (Iterator i = newValues.iterator(); i.hasNext(); ) {
             String val = (String)i.next();
             if (val.startsWith("[")) {
                 int idx = val.indexOf(']');
                 if (idx != -1) {
                     String key = val.substring(0, idx +1);
                     removeEntryByKey(set, key);
                 }
             }
             set.add(val);
         }
         return set;
     }
 
     private static boolean removeEntryByKey(Set set, String key) {
         boolean bRemoved = false;
         String match = key + "=";
         for (Iterator i = set.iterator(); i.hasNext() && !bRemoved; ) {
             String val = (String)i.next();
             if (val.startsWith(match)) {
                 i.remove();
                 bRemoved = true;
             }
         }
         return bRemoved;
     }
 
     /**
      * Updates agent group attribute values.
      *
      * @param ssoToken Single Sign On token that is to be used for creation.
      * @param realm Realm where group resides.
      * @param agentName Name group of agent.
      * @param values Map of attribute name to its values.
      * @throws IdRepoException if there are Id Repository related errors.
      * @throws SSOException if the Single Sign On token is invalid or has
      *         expired.
      * @throws SMSException if there are errors in service management layers.
      */
     public static void updateAgentGroup(
         SSOToken ssoToken,
         String realm,
         String agentGroupName,
         Map attrValues
     ) throws IdRepoException, SSOException, SMSException {
         updateAgentGroup(ssoToken, realm, agentGroupName, attrValues, true);
     }
 
     
     /**
      * Updates agent group attribute values.
      *
      * @param ssoToken Single Sign On token that is to be used for creation.
      * @param realm Name of realm where agent resides.
      * @param agentGroupName Name of agent group.
      * @param values Map of attribute name to its values.
      * @param bSet <code>true</code> to overwrite the values for the
      *        attribute.
      * @throws IdRepoException if there are Id Repository related errors.
      * @throws SSOException if the Single Sign On token is invalid or has
      *         expired.
      * @throws SMSException if there are errors in service management layers.
      */
     public static void updateAgentGroup(
         SSOToken ssoToken,
         String realm,
         String agentGroupName,
         Map attrValues,
         boolean bSet
     ) throws IdRepoException, SSOException, SMSException {
         AMIdentity amid = new AMIdentity(ssoToken, agentGroupName, 
             IdType.AGENTGROUP, realm, null); 
         String agentType = getAgentType(amid);
         Map attributeValues = parseAttributeMap(agentType, attrValues);
         if (!bSet) {
             Map origValues = amid.getAttributes(attributeValues.keySet());
             for (Iterator i = attributeValues.keySet().iterator();
                 i.hasNext();
             ) {
                 String attrName = (String)i.next();
                 attributeValues.put(attrName, updateAttrValues(
                     agentType, attrName,
                     (Set)origValues.get(attrName),
                     (Set)attributeValues.get(attrName)));
             }
         }
 
         amid.setAttributes(attributeValues);
         amid.store();
     }
     
     /**
      * Returns a set of attribute schemas of a given agent type.
      *
      * @param agentTypeName Name of agent type.
      * @return a set of attribute schemas of a given agent type.
      * @throws SSOException if the Single Sign On token is invalid or has
      *         expired.
      * @throws SMSException if there are errors in service management layers.
      */
     public static Set getAgentAttributeSchemas(String agentTypeName)
         throws SMSException, SSOException {
         Set attrSchemas = new HashSet();
         ServiceSchema ss = getOrganizationSchema();
         if (ss != null) {
             ServiceSchema ssType = ss.getSubSchema(agentTypeName);
             Set attrs = ssType.getAttributeSchemas();
             if ((attrs != null) && !attrs.isEmpty()) {
                 attrSchemas.addAll(attrs);
             }
         }
         
         for (Iterator i = attrSchemas.iterator(); i.hasNext(); ) {
             AttributeSchema as = (AttributeSchema)i.next();
             if (as.getType().equals(AttributeSchema.Type.VALIDATOR)) {
                 i.remove();
             }
         }
         return attrSchemas;
     }
 
     private static Set getAgentAttributeSchemaNames(String agentTypeName)
         throws SMSException, SSOException {
         Set attrSchemas = getAgentAttributeSchemas(agentTypeName);
         Set names = new HashSet(attrSchemas.size() *2);
         
         for (Iterator i = attrSchemas.iterator(); i.hasNext(); ) {
             AttributeSchema as = (AttributeSchema)i.next();
             names.add(as.getName());
         }
         
         return names;
     }
 
     /**
      * Returns agent group's attribute values.
      *
      * @param ssoToken Single Sign On token that is to be used for query.
      * @param realm Name of realm where agent group resides.
      * @param agentGroupName Name of agent group.
      * @return agent group's attribute values.
      * @throws IdRepoException if there are Id Repository related errors.
      * @throws SSOException if the Single Sign On token is invalid or has
      *         expired.
      * @throws SMSException if there are errors in service management layers.
      */
     public static Map getAgentGroupAttributes(
         SSOToken ssoToken,
         String realm,
         String agentGroupName
     ) throws IdRepoException, SMSException, SSOException {
         AMIdentity amid = new AMIdentity(ssoToken, agentGroupName,
             IdType.AGENTGROUP, realm, null);
         Map values = amid.getAttributes();
         return unparseAttributeMap(getAgentType(amid), values);
     }
 
 
     /**
      * Returns agent's attribute values.
      *
      * @param ssoToken Single Sign On token that is to be used for query.
      * @param realm Realm where agent resides.
      * @param agentName Name of agent.
      * @return agent's attribute values.
      * @throws IdRepoException if there are Id Repository related errors.
      * @throws SSOException if the Single Sign On token is invalid or has
      *         expired.
      * @throws SMSException if there are errors in service management layers.
      */
     public static Map getAgentAttributes(
         SSOToken ssoToken, 
         String realm,
         String agentName
     ) 
         throws IdRepoException, SMSException, SSOException {
         AMIdentity amid = new AMIdentity(ssoToken, agentName,
             IdType.AGENTONLY, realm, null);
         return getAgentAttributes(amid, true);
     }
     
     /**
      * Returns agent's attribute values.
      *
      * @param ssoToken Single Sign On token that is to be used for query.
      * @param realm Realm where agent resides.
      * @param agentName Name of agent.
      * @param bInherit <code>true</code> to inherit from group.
      * @return agent's attribute values.
      * @throws IdRepoException if there are Id Repository related errors.
      * @throws SSOException if the Single Sign On token is invalid or has
      *         expired.
      * @throws SMSException if there are errors in service management layers.
      */
     public static Map getAgentAttributes(
         SSOToken ssoToken,
         String realm,
         String agentName,
         boolean bInherit
     ) throws IdRepoException, SMSException, SSOException {
         IdType type = (bInherit) ? IdType.AGENT : IdType.AGENTONLY;
         AMIdentity amid = new AMIdentity(ssoToken, agentName,
             type, realm, null);
         return getAgentAttributes(amid, true);
     }
     
     /**
      * Returns agent's attribute values.
      *
      * @param amid Identity object.
      * @param reformat <code>true</code> to reformat the values.
      * @return agent's attribute values.
      * @throws IdRepoException if there are Id Repository related errors.
      * @throws SSOException if the Single Sign On token is invalid or has
      *         expired.
      * @throws SMSException if there are errors in service management layers.
      */
     public static Map getAgentAttributes(AMIdentity amid, boolean reformat)
         throws IdRepoException, SMSException, SSOException {
         Map values = amid.getAttributes();
         String agentType = getAgentType(amid);
         if (supportLocalProperties(agentType) && 
             isPropertiesLocallyStored(amid)
         ) {
             Set localProp = getLocalPropertyNames(agentType);
             Map temp = new HashMap(localProp.size()*2);
             for (Iterator i = localProp.iterator(); i.hasNext(); ) {
                 String key = (String)i.next();
                 temp.put(key, values.get(key));
             }
             values = temp;
         }
         
         return (reformat) ? unparseAttributeMap(agentType, values) :
             correctAttributeNames(agentType, values);
     }
     
     private static boolean isPropertiesLocallyStored(AMIdentity amid)
         throws IdRepoException, SSOException {
         boolean isLocal = false;
         Set setRepo = (Set)amid.getAttribute(ATTR_CONFIG_REPO);
         if ((setRepo != null) && !setRepo.isEmpty()) {
             String repo = (String) setRepo.iterator().next();
             isLocal = (repo.equalsIgnoreCase(VAL_CONFIG_REPO_LOCAL));
         }
         return isLocal;
     }
     
     private static String getAgentType(AMIdentity amid)
         throws IdRepoException, SSOException {
         Set setType = amid.getAttribute(IdConstants.AGENT_TYPE);
         return ((setType != null) && !setType.isEmpty()) ?
             (String)setType.iterator().next() : "";
     }
     
     private static Map parseAttributeMap(String agentType, Map attrValues)
         throws SMSException, SSOException {
         Map dummy = new HashMap();
         dummy.putAll(attrValues);
         Map result = new HashMap();
         Set attributeSchemas = getAgentAttributeSchemas(agentType);
         
         if ((attributeSchemas != null) && !attributeSchemas.isEmpty()) {
             for (Iterator i = attributeSchemas.iterator(); i.hasNext(); ) {
                 AttributeSchema as = (AttributeSchema)i.next();
                 Set values = parseAttributeMap(as, dummy);
                 if (values != null) {
                     result.put(as.getName(), values);
                 }
             }
         }
         if (!dummy.isEmpty()) {
             Set freeForm = new HashSet();
             for (Iterator i = dummy.keySet().iterator(); i.hasNext(); ) {
                 String name = (String)i.next();
                 Set values = (Set)dummy.get(name);
                 for (Iterator j = values.iterator(); j.hasNext(); ) {
                     freeForm.add(name + "=" + ((String)j.next()));
                 }
             }
             result.put(ATTR_NAME_FREE_FORM, freeForm);
         }
         
         return result;
     }
 
     /**
      * E.g. abc[0]=x
      *      abc[1]=y
      *      where abc is the attribute schema name
      * this method will return {[0]=x, [1]=y}.
      */
     private static Set parseAttributeMap(AttributeSchema as, Map attrValues) {
         Set results = null;
         String attrName = as.getName();
 
         if (as.getType().equals(AttributeSchema.Type.LIST)) {
             results = new HashSet();
             int lenAttrName = attrName.length();
             
             for (Iterator i = attrValues.keySet().iterator(); i.hasNext(); ) {
                 String key = (String)i.next();
                 if (key.startsWith(attrName)) {
                     String sKey = key.substring(lenAttrName);
                     Set set = (Set)attrValues.get(key);
 
                     if (set != Collections.EMPTY_SET) {
                         if (sKey.startsWith("[") && sKey.endsWith("]")) {
                             results.add(sKey + "=" + set.iterator().next());
                         } else if (attrName.equals(key)) {
                             // this is for special case, where attribute can be
                             // list and non list type
                             results.add(set.iterator().next());
                         }
                     }
                     i.remove();
                 }
             }
             if (results.isEmpty()) {
                 results = null;
             }
         } else {
             results = (Set)attrValues.remove(attrName);
         }
         return results;
     }
     
 
     private static Map correctAttributeNames(String agentType, Map attrValues)
         throws SMSException, SSOException {
         Map result = new HashMap();
         Set asNames = getAgentAttributeSchemaNames(agentType);
 
         if ((asNames != null) && !asNames.isEmpty()) {
             for (Iterator i = asNames.iterator(); i.hasNext(); ) {
                 String name = (String)i.next();
                 Set values = (Set)attrValues.get(name);
                 if (values != null) {
                     result.put(name, values);
                 }
             }
         } else {
             result.putAll(attrValues);
         }
         
         return result;
     }
     
     private static Map unparseAttributeMap(String agentType, Map attrValues)
         throws SMSException, SSOException {
         Map result = new HashMap();
         Set asNames = getAgentAttributeSchemaNames(agentType);
         Set asListType = getAttributesSchemaNames(agentType,
             AttributeSchema.Type.LIST);
         Set asValidatorType = getAttributesSchemaNames(agentType,
             AttributeSchema.Type.VALIDATOR);
         
         if (asListType == null) {
             asListType = Collections.EMPTY_SET;
         }
         if (asValidatorType == null) {
             asValidatorType = Collections.EMPTY_SET;
         }
         
         if ((asNames != null) && !asNames.isEmpty()) {
             for (Iterator i = asNames.iterator(); i.hasNext(); ) {
                 String name = (String)i.next();
                 Set values = (Set)attrValues.get(name);
                 
                 if ((values != null) && !values.isEmpty()) {
                     if (name.equals(ATTR_NAME_FREE_FORM)) {
                         handleFreeFormAttrValues(values, result);
                     } else if (!asValidatorType.contains(name)) {
                         if (asListType.contains(name)) {
                             for (Iterator j = values.iterator(); j.hasNext();) {
                                 String val = (String) j.next();
                                 int idx = val.indexOf("]=");
 
                                 if (idx != -1) {
                                     Set set = new HashSet(2);
                                     set.add(val.substring(idx + 2));
                                     String indice = val.substring(0, idx + 1);
                                     indice = indice.replaceAll("=", "\\\\=");
                                     result.put(name + indice, set);
                                 } else {
                                     Set set = new HashSet(2);
                                     set.add(val);
                                     // this is for special case, where attribute
                                     // can be list and non list type
                                     result.put(name, set);
                                 }
                             }
                         } else {
                             result.put(name, values);
                         }
                     }
                 }
             }
         } else {
             result.putAll(attrValues);
         }
         
         return result;
     }
 
     private static void handleFreeFormAttrValues(Set values, Map result) {
         for (Iterator i = values.iterator(); i.hasNext();) {
             String val = (String)i.next();
             Matcher m = patternArray.matcher(val);
 
             // this is to handle the attribute value like
             // my.new.map.property[cn=user1,o=xyz]=value1
             if (m.find()) {
                 Set set = new HashSet(2);
                 set.add(m.group(2));
                 result.put(m.group(1), set);
             } else {
                 int idx = val.indexOf("=");
                 if (idx != -1) {
                     Set set = new HashSet(2);
                     set.add(val.substring(idx+1));
                     result.put(val.substring(0, idx), set);
                 }
             }
         }
     }
 
     /**
      * Returns a set of attribute schema names whose schema match a given 
      * syntax.
      *
      * @param amid Identity Object. Agent Type is to be gotten from it.
      * @param syntax Syntax.
      * @return a set of attribute schema names whose schema match a given 
      * syntax.
      * @throws IdRepoException if there are Id Repository related errors.
      * @throws SSOException if the Single Sign On token is invalid or has
      *         expired.
      * @throws SMSException if there are errors in service management layers.
      */
     public static Set getAttributesSchemaNames(
         AMIdentity amid, 
         AttributeSchema.Syntax syntax
     ) throws SMSException, SSOException, IdRepoException {
         Set results = new HashSet();
         Set attributeSchemas = getAgentAttributeSchemas(getAgentType(amid));
         
         if ((attributeSchemas != null) && !attributeSchemas.isEmpty()) {
             for (Iterator i = attributeSchemas.iterator(); i.hasNext(); ) {
                 AttributeSchema as = (AttributeSchema)i.next();
                 if (as.getSyntax().equals(syntax)){
                     results.add(as.getName());
                 }
             }
         }
         return results;
     }
     
     /**
      * Returns a set of attribute schema names whose schema match a given 
      * type.
      *
      * @param amid Identity Object. Agent Type is to be gotten from it.
      * @param type Type.
      * @return a set of attribute schema names whose schema match a given 
      * type.
      * @throws IdRepoException if there are Id Repository related errors.
      * @throws SSOException if the Single Sign On token is invalid or has
      *         expired.
      * @throws SMSException if there are errors in service management layers.
      */
     public static Set getAttributesSchemaNames(
         String agentType, 
         AttributeSchema.Type type
     ) throws SMSException, SSOException {
         Set results = new HashSet();
         Set attributeSchemas = getAgentAttributeSchemas(agentType);
         
         if ((attributeSchemas != null) && !attributeSchemas.isEmpty()) {
             for (Iterator i = attributeSchemas.iterator(); i.hasNext(); ) {
                 AttributeSchema as = (AttributeSchema)i.next();
                 if (as.getType().equals(type)){
                     results.add(as.getName());
                 }
             }
         }
         return results;
     }
 
     private static ServiceSchema getOrganizationSchema()
         throws SMSException, SSOException {
         ServiceSchema ss = null;
         SSOToken adminToken = (SSOToken) AccessController.doPrivileged(
             AdminTokenAction.getInstance());
         ServiceSchemaManager ssm = new ServiceSchemaManager(
             IdConstants.AGENT_SERVICE, adminToken);
         if (ssm != null) {
             ss = ssm.getSchema(SchemaType.ORGANIZATION);
         }
         return ss;
     }
     
     /**
      * Returns the default values of attribute schemas
      * of a given agent type.
      *
      * @param agentType Type of agent.
      * @throws SSOException if the Single Sign On token is invalid or has
      *         expired.
      * @throws SMSException if there are errors in service management layers.
      */
     public static Map getDefaultValues(String agentType) 
         throws SMSException, SSOException {
         Map mapDefault = new HashMap();
         Set attributeSchemas = getAgentAttributeSchemas(agentType);
         
         if ((attributeSchemas != null) && !attributeSchemas.isEmpty()) {
             for (Iterator i = attributeSchemas.iterator(); i.hasNext(); ) {
                 AttributeSchema as = (AttributeSchema)i.next();
                 mapDefault.put(as.getName(), as.getDefaultValues());
             }
         }
         return mapDefault;
     }
     
     /**
      * Returns choice values of an attribute schema.
      *
      * @param name Name of attribute schema.
      * @param agentType Type of agent.
      * @return choice values of an attribute schema.
      * @throws SSOException if the Single Sign On token is invalid or has
      *         expired.
      * @throws SMSException if there are errors in service management layers.
      */
     public static Map getChoiceValues(String name, String agentType) 
         throws SMSException, SSOException {
         Map choiceValues = new HashMap();
         AttributeSchema as = getAgentAttributeSchema(name, agentType);
         
         if (as != null) {
             String[] cValues = as.getChoiceValues();
             
             if (cValues != null) {
                 for (int i = 0; i < cValues.length; i++) {
                     String v = cValues[i];
                     choiceValues.put(as.getChoiceValueI18NKey(v), v);
                 }
             }
         }
         return choiceValues;
     }
 
     /**
      * Returns attribute schema of a given agent type.
      *
      * @param name Name of attribute schema.
      * @param agentTypeName Name of agent type.
      * @return attribute schema of a given agent type.
      * @throws SSOException if the Single Sign On token is invalid or has
      *         expired.
      * @throws SMSException if there are errors in service management layers.
      */
     public static AttributeSchema getAgentAttributeSchema(
         String name, 
         String agentTypeName
     ) throws SMSException, SSOException {
         AttributeSchema as = null;
         ServiceSchema ss = getOrganizationSchema();
         if (ss != null) {
             ServiceSchema ssType = ss.getSubSchema(agentTypeName);
             as = ssType.getAttributeSchema(name);
         }
         return as;
     }
 
     /**
      * Returns the inherited attribute names.
      *
      * @param amid Identity object of the agent.
      * @return the inherited attribute names.
      * @throws IdRepoException if attribute names cannot obtained.
      * @throws SSOException if single sign on token is expired or invalid.
      */
     public static Set getInheritedAttributeNames(AMIdentity amid)
         throws IdRepoException, SSOException, SMSException {
         String agentType = getAgentType(amid);
         Set attributeSchemaNames = getAgentAttributeSchemaNames(agentType);
         Map values = getAgentAttributes(amid, false);
         
         if ((values != null) && !values.isEmpty()) {
             attributeSchemaNames.removeAll(values.keySet());
         }
         return attributeSchemaNames;
     }
     
     /**
      * Updates the inherited attribute names.
      *
      * @param amid Identity object of the agent.
      * @param inherit Map of attribute name to either "1" or "0". "1" to 
      *        inherit and "0" not.
      * @throws IdRepoException if attribute names cannot obtained.
      * @throws SSOException if single sign on token is expired or invalid.
      */
     public static void updateInheritance(AMIdentity amid, Map inherit) 
         throws IdRepoException, SSOException, SMSException {
         
         Set toInherit = new HashSet();
         Set notToInherit = new HashSet();
         
         for (Iterator i = inherit.keySet().iterator(); i.hasNext(); ) {
             String attrName = (String)i.next();
             String flag = (String)inherit.get(attrName);
             if (flag.equals("1")) {
                 toInherit.add(attrName);
             } else {
                 notToInherit.add(attrName);
             }
         }
         Map origValues = getAgentAttributes(amid, false);
         Map values = amid.getAttributes(toInherit);
         amid.removeAttributes(values.keySet());
         
         String agentType = getAgentType(amid);
         Map attrSchemas = getAttributeSchemas(agentType, notToInherit);
         Map resetValues = new HashMap(notToInherit.size() *2);
         
         for (Iterator i = notToInherit.iterator(); i.hasNext(); ) {
             String attrName = (String)i.next();
             if (origValues.get(attrName) == null) {
                 AttributeSchema as = (AttributeSchema)attrSchemas.get(attrName);
                 Set defaultValues = as.getDefaultValues();
                 if ((defaultValues == null)) {
                     resetValues.put(attrName, Collections.EMPTY_SET);
                 } else {
                     resetValues.put(attrName, defaultValues);
                 }
             }
         }
         
         if (!resetValues.isEmpty()) {
             amid.setAttributes(resetValues);
             amid.store();
         }
     }
 
     /**
      * Returns attribute schema for a given set of attribute names.
      *
      * @param agentType Agent type.
      * @param names Set of attribute names.
      * @return localized names for a given set of attribute names.
      */
     public static Map getAttributeSchemas(
         String agentType, 
         Collection names
     ) throws SMSException, SSOException {
         Map map = new HashMap();
         Set attributeSchema = getAgentAttributeSchemas(agentType);
         for (Iterator i = attributeSchema.iterator(); i.hasNext(); ) {
             AttributeSchema as = (AttributeSchema)i.next();
             if (names.contains(as.getName())) {
                 map.put(as.getName(), as);
             }
         }
         return map;
     }
 
     public static ResourceBundle getServiceResourceBundle(Locale locale)
         throws SMSException, SSOException {
         SSOToken adminToken = (SSOToken) AccessController.doPrivileged(
             AdminTokenAction.getInstance());
         ServiceSchemaManager ssm = new ServiceSchemaManager(
             IdConstants.AGENT_SERVICE, adminToken);
 
         String rbName = ssm.getI18NFileName();
         return ResourceBundle.getBundle(rbName, locale);
     }
     
     
     /**
      * Adds an agent to a group.
      * 
      * @param group Agent Group.
      * @param agent Agent.
      * @throws SSOException if Single Sign on for accessing identity attribute
      *         values is invalid.
      * @throws IdRepoException if unable to access attribute values.
      * @throws ConfigurationException if agent's properties are locally stored.
      */
     public static void AddAgentToGroup(AMIdentity group, AMIdentity agent) 
         throws IdRepoException, SSOException, ConfigurationException {
         if (!group.isExists()) {
             String[] param = {group.getName()};
             throw new ConfigurationException(
                 "cannot.add.agent.to.group.group.does.not.exist", param);
         }
         if (!agent.isExists()) {
             String[] param = {agent.getName()};
             throw new ConfigurationException(
                 "cannot.add.agent.to.group.agent.does.not.exist", param);
         }
 
         String agentType = getAgentType(agent);
         String agentGroupType = getAgentType(group);
         if (supportLocalProperties(agentType) && 
             isPropertiesLocallyStored(agent)
         ) {
             String agentName = agent.getName();
             String[] param = {agentName};
             throw new ConfigurationException(
                 "cannot.add.agent.to.group.proeprties.locally.stored", param);
         }
 
         if (!agentType.equals(agentGroupType)) {
             String agentName = agent.getName();
             String groupName = group.getName();
             String[] param = {agentName, groupName};
             throw new ConfigurationException(
                 "cannot.add.agent.to.group.type.mismatched", param);
         }
 
         group.addMember(agent);
     }
     
     /**
      * Returns <code>true</code> if an agent type support local properties.
      * 
      * @param agentType Agent Type.
      * @return <code>true</code> if an agent type support local properties.
      */
     public static boolean supportLocalProperties(String agentType) {
         return localAgentProperties.containsKey(agentType);
     }
     
     /**
      * Returns a set of local property name if an agent type. Returns null
      * if agent type does not support local properties.
      * 
      * @param agentType Agent Type.
      * @return a set of local property name if an agent type.
      */
     public static Set getLocalPropertyNames(String agentType) {
         return (Set)localAgentProperties.get(agentType);
     }
 }
