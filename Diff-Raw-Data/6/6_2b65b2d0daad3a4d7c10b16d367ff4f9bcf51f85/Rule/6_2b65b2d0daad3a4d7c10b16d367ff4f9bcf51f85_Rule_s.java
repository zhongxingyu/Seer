 /**
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2006 Sun Microsystems Inc. All Rights Reserved
  *
  * The contents of this file are subject to the terms
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
 * $Id: Rule.java,v 1.1 2009-03-16 22:09:12 dillidorai Exp $
  *
  */
 package com.sun.identity.policy;
 
 import java.util.*;
 
 import org.w3c.dom.*;
 
 import com.sun.identity.shared.xml.XMLUtils;
 import com.iplanet.sso.SSOException;
 import com.sun.identity.policy.interfaces.*;
 
 /**
  * The class <code>Rule</code> provides interfaces to manage
  * a rule that can be added to a policy.
  * A rule constains the rule name, service type,
  * a resource and a map containing action names and action values.
  *
  * @supported.api
  */
 public class Rule extends Object implements Cloneable {
 
     public static final String EMPTY_RESOURCE_NAME = "";
     public static final String EXCLUDED_RESOURCE_NAMES = "EXCLUDED_resource_NAMES";
 
     // Name of the rule
     private String ruleName;
 
     // Service type
     private String serviceTypeName;
     private ServiceType serviceType;
 
     // Resource for which the rule applies
     String resourceName = EMPTY_RESOURCE_NAME;
     Set<String> excludedResourceNames;
 
     // Actions allowed on the resource
     private Map actions;
 
     /**
      * Contruct a <code>Rule</code>
      */
     protected Rule() {
         // do nothing
     }
 
     /**
      * Constructor to create a rule object with the
      * service name, resource name and actions. The actions
      * provided as a <code>Map</code> must have the action
      * name as key and a <code>Set</code> of <code>String</code>s
      * as its value. The action names and action values must
      * conform to the schema specified for the service.
      * Otherwise, <code>InvalidNameException
      * </code> is thrown. The parameters <code>ruleName</code>
      * and <code>resourceName</code> can be <code>null</code>.
      *
      * @param serviceName name of the service type as defined by
      * the service schema
      * @param resourceName name of the resource for the service type
      * @param actions map of action and action values for the resource
      *
      * @exception NameNotFoundException the service name provided does
      * not exist
      * @exception InvalidNameException the resource name, action name, 
      * or values is not valid
      * @supported.api
      */
     public Rule(String serviceName, String resourceName, Map actions) throws
             NameNotFoundException, InvalidNameException {
         this(null, serviceName, resourceName, actions);
     }
 
     /**
      * Constructor to create a rule object with the
      * service name and actions. This is useful for
      * services (and possibly action names) that do not have
      * resource names. The actions
      * provided as a <code>Map</code> must have the action
      * name as it key and a <code>Set</code> of <code>String</code>s
      * as its value. The action names and action values must
      * conform to the schema specified for the service.
      * Otherwise, <code>InvalidNameException
      * </code> is thrown. The parameters <code>ruleName</code>
      * and <code>resourceName</code> can be <code>null</code>.
      *
      * @param serviceName name of the service type as defined by
      * the service schema
      * @param actions map of action and action values for the resource
      *
      * @exception NameNotFoundException the service name provided does
      * not exist
      * @exception InvalidNameException the resource name, action name, 
      * or values is not valid
      * @supported.api
      */
     public Rule(String serviceName, Map actions) throws
             NameNotFoundException, InvalidNameException {
         this(null, serviceName, null, actions);
     }
 
     /**
      * Constructor to create a rule object with rule name,
      * service name, resource name and actions. The actions
      * provided as a <code>Map</code> must have the action
      * name as it key and a <code>Set</code> of <code>String</code>s
      * as its value. The action names and action values must
      * conform to the service schema.
      * Otherwise, <code>InvalidNameException
      * </code> is thrown. The parameters <code>ruleName</code>
      * and <code>resourceName</code> can be <code>null</code>.
      *
      * @param ruleName name of the rule
      * @param serviceName name of the service type as defined by
      *        the service schema
      * @param resourceName name of the resource for the service type
      * @param actions map of action and action values for the resource
      *
      * @exception NameNotFoundException the service name provided does
      * not exist
      * @exception InvalidNameException the resource name, action name, 
      * or values is not valid
      * @supported.api
      */
     public Rule(String ruleName, String serviceName,
             String resourceName, Map actions) throws
             NameNotFoundException, InvalidNameException {
         // Rule and resource name can be null
         this.ruleName = (ruleName != null) ? ruleName : ("rule" + ServiceTypeManager.generateRandomName());
         if ((resourceName == null) || (resourceName == "")) {
             resourceName = EMPTY_RESOURCE_NAME;
         }
         this.resourceName = resourceName.trim();
 
         // Check the service type name
         checkAndSetServiceType(serviceName);
         this.serviceTypeName = serviceName;
 
         // Verify the action names
         serviceType.validateActionValues(actions);
         this.actions = new HashMap(actions);
         try {
             this.resourceName = serviceType.canonicalize(resourceName);
         } catch (PolicyException pe) {
             throw new InvalidNameException(pe, resourceName, 2);
         }
     }
 
     /**
      * Constructor to create a <code>Rule</code> object from a XML Node
      * @param ruleNode XML node representation of <code>Rule</code>
      */
     protected Rule(Node ruleNode) throws InvalidFormatException,
             InvalidNameException, NameNotFoundException {
         //TODO: account for excludedResourceNames
         // Make sure the node name is rule
         if (!ruleNode.getNodeName().equalsIgnoreCase(
                 PolicyManager.POLICY_RULE_NODE)) {
             if (PolicyManager.debug.warningEnabled()) {
                 PolicyManager.debug.warning(
                         "invalid rule xml blob given to constructor");
             }
             throw (new InvalidFormatException(ResBundleUtils.rbName,
                     "invalid_xml_rule_node", null, "", PolicyException.RULE));
         }
 
         // Get rule name, can be null
         if ((ruleName = XMLUtils.getNodeAttributeValue(ruleNode,
                 PolicyManager.NAME_ATTRIBUTE)) == null) {
             ruleName = "rule" + ServiceTypeManager.generateRandomName();
         }
 
         // Get the service type name, cannot be null
         Node serviceNode = XMLUtils.getChildNode(ruleNode,
                 PolicyManager.POLICY_RULE_SERVICE_NODE);
         if ((serviceNode == null) || ((serviceTypeName =
                 XMLUtils.getNodeAttributeValue(serviceNode,
                 PolicyManager.NAME_ATTRIBUTE)) == null)) {
             if (PolicyManager.debug.warningEnabled()) {
                 PolicyManager.debug.warning(
                         "invalid service name in rule xml blob in constructor");
             }
             String objs[] = {((serviceTypeName == null) ? "null" : serviceTypeName)};
             throw (new InvalidFormatException(ResBundleUtils.rbName,
                     "invalid_xml_rule_service_name", objs,
                     ruleName, PolicyException.RULE));
         }
         checkAndSetServiceType(serviceTypeName);
 
         // Get resource node, can be null
         Node resourceNode = XMLUtils.getChildNode(ruleNode,
                 PolicyManager.POLICY_RULE_RESOURCE_NODE);
         if ((resourceNode != null) && ((resourceName =
                 XMLUtils.getNodeAttributeValue(resourceNode,
                 PolicyManager.NAME_ATTRIBUTE)) == null)) {
             if (PolicyManager.debug.warningEnabled()) {
                 PolicyManager.debug.warning(
                         "invalid resource name in rule xml blob in constructor");
             }
             String objs[] = {((resourceName == null) ? "null" : resourceName)};
             throw (new InvalidFormatException(ResBundleUtils.rbName,
                     "invalid_xml_rule_resource_name", objs,
                     ruleName, PolicyException.RULE));
         }
         if (resourceName != null) {
             resourceName = resourceName.trim();
             try {
                 resourceName = serviceType.canonicalize(resourceName);
             } catch (PolicyException pe) {
                 throw new InvalidNameException(pe, resourceName, 2);
             }
         }
 
         // Get the actions and action values, cannot be null
         Set actionNodes = XMLUtils.getChildNodes(ruleNode,
                 PolicyManager.ATTR_VALUE_PAIR_NODE);
         actions = new HashMap();
         if (actionNodes != null) {
             Iterator items = actionNodes.iterator();
             while (items.hasNext()) {
                 // Get action name & values
                 String actionName = null;
                 Set actionValues = null;
                 Node node = (Node) items.next();
                 Node attrNode = XMLUtils.getChildNode(node, PolicyManager.ATTR_NODE);
                 if ((attrNode == null) || ((actionName = XMLUtils.getNodeAttributeValue(attrNode,
                         PolicyManager.NAME_ATTRIBUTE)) == null) || ((actionValues =
                         XMLUtils.getAttributeValuePair(node)) == null)) {
                     String objs[] = {((actionName == null) ? "null" : actionName)};
                     throw (new InvalidFormatException(
                             ResBundleUtils.rbName,
                             "invalid_xml_rule_action_name", objs,
                             ruleName, PolicyException.RULE));
                 }
                 actions.put(actionName, actionValues);
 
             }
             excludedResourceNames = (Set<String>) actions.get(
                     EXCLUDED_RESOURCE_NAMES);
             actions.remove(EXCLUDED_RESOURCE_NAMES);
             // Validate the action values
             serviceType.validateActionValues(actions);
         }
     }
 
     /**
      * Sets the service type name of this object
      * @param serviceTypeName service type name for this object
      * @exception NameNotFoundException the service type name provided does
      * not exist
      */
     private void checkAndSetServiceType(String serviceTypeName)
             throws NameNotFoundException {
         // Check the service type name
         ServiceTypeManager stm = null;
         try {
             stm = ServiceTypeManager.getServiceTypeManager();
             serviceType = stm.getServiceType(serviceTypeName);
         } catch (SSOException ssoe) {
             PolicyManager.debug.error("Unable to get admin SSO token" + ssoe);
             throw (new NameNotFoundException(ssoe,
                     serviceTypeName, PolicyException.SERVICE));
         }
     }
 
     /**
      * Returns the name assigned to the rule. It could be <code>null</code>
      * if it was not constructed with a name.
      *
      * @return rule name
      * @supported.api
      */
     public String getName() {
         return (ruleName);
     }
 
     /**
      * Sets the name for the rule. If a name has already been
      * assigned, it will be replaced with the given name.
      *
      * @param ruleName rule name.
      * @throws InvalidNameException if rule name is invalid.
      * @supported.api
      */
     public void setName(String ruleName) throws InvalidNameException {
         if (ruleName != null) {
             this.ruleName = ruleName;
         } else {
             this.ruleName = "rule" + ServiceTypeManager.generateRandomName();
         }
     }
 
     /**
      * Returns the service name for which the rule has been created.
      * The service name of the rule cannot be changed once the rule is 
      * created.
      *
      * @return service name
      * @supported.api
      */
     public String getServiceTypeName() {
         return (serviceTypeName);
     }
 
     /**
      * Returns the resource name for which the rule has been created.
      * If the service does not support resource names, the method
      * will return <code>null</code>. The resource name of
      * the rule cannot be changed once the rule is created.
      *
      * @return resource name
      * @supported.api
      */
     public String getResourceName() {
         return (resourceName);
     }
 
     /**
      * Returns the excluded resource names for which the rule should not apply.
      * If the service does not support resource names, the method
      * will return <code>null</code>.
      * @return excluded resource names
      * @supported.api
      */
     public Set<String> getExcludedResourceNames() {
         return excludedResourceNames;
     }
 
     /**
      * Sets the excluded resource names for which the rule should not apply.
      * @param excludedResourceNames excluded resource names
      * @supported.api
      */
     public void setExcludedResourceNames(
             Set<String> exclduedResourceNames) {
         this.excludedResourceNames = excludedResourceNames;
     }
 
     /**
      * Returns the action names that have been set for the rule.
      * The action names returned could be the same as the service's
      * action names or a subset of it.
      *
      * @return action names defined in this rule for the service
      * @supported.api
      */
     public Set getActionNames() {
         return (new HashSet(actions.keySet()));
     }
 
     /**
      * Returns a set of action values that have been set for the
      * specified action name.
      *
      * @param actionName action name for which to compute values.
      * @return action names defined in this rule for the service
      * @throws NameNotFoundException if actions name is not
      *         found in the rule
      * @supported.api
      */
     public Set getActionValues(String actionName)
             throws NameNotFoundException {
         Set answer = (Set) actions.get(actionName);
         if (answer != null) {
             answer = (Set) ((HashSet) answer).clone();
         }
         return (answer);
     }
 
     /**
      * Returns a <code>Map</code> of all action names and their
      * corresponding action values that have been set in the rule.
      * The "key" of the <code>Map</code> will be the action name
      * as a string, and its "value" will be a <code>Set</code>
      * which contains the action values as strings.
      *
      * @return all action names and corresponding action values
      * @supported.api
      */
     public Map getActionValues() {
         return (new HashMap(actions));
     }
 
     /**
      * Sets the action names and their corresponding actions values
      * (or permissions) for the resource or the service. 
      *
      * @param actionValues action names and their corresponding values
      * @throws InvalidNameException if action name is invalid.
      * @supported.api
      */
     public void setActionValues(Map actionValues)
             throws InvalidNameException {
         serviceType.validateActionValues(actionValues);
         actions = new HashMap(actionValues);
     }
 
     /**
      * Checks if two rule objects are identical. Two rules are
      * identical only if the service name, resource name,
      * action name and values match.
      *
      * @param o object againt which this rule object
      * will be checked for equality
      *
      * @return <code>true</code> if the service type, resource, actions
      * and action values match, <code>false</code> otherwise.
      */
     public boolean equals(Object obj) {
         boolean matched = true;
         if (obj == null || !(obj instanceof Rule)) {
             return false;
         }
         Rule rule = (Rule) obj;
         if (excludedResourceNames == null) {
             if (rule.getExcludedResourceNames() != null) {
                 return false;
             }
        }
        if (!excludedResourceNames.equals(rule.getExcludedResourceNames())) {
             return false;
         }
         if (!isResourceMatch(rule.serviceTypeName,
                 rule.resourceName).equals(ResourceMatch.EXACT_MATCH)) {
             return false;
 
         }
         if (!actions.equals(rule.actions)) {
             return false;
         }
         return matched;
     }
 
     /**
      * Compares the given service and resource names with the
      * service and resource name specified in this rule.
      * The method returns a <code>ResourceMatch</code> object which
      * specifies if the resources match exactly, do not match, or one
      * of them is a subordinate resource of the other. If the
      * service name does not match, the method returns <code>
      * NO_MATCH</code>.
      *
      * @param serviceName name of the service
      * @param resourceName name of the resource
      *
      * @return returns <code>ResourceMatch</code> that
      * specifies if the service name and resource name are exact match, or
      * otherwise.
      */
     public ResourceMatch isResourceMatch(
             String serviceName,
             String resourceName) {
         //TODO: account for excludedResourceNames
         ResourceMatch rm = null;
         if (!serviceName.equalsIgnoreCase(serviceTypeName)) {
             rm = ResourceMatch.NO_MATCH;
         } else {
             //rm = serviceType.compare(this.resourceName, resourceName);
             rm = serviceType.compare(resourceName, this.resourceName);
         }
 
         return rm;
     }
 
     /**
      * Returns an XML string representing the rule.
      *
      * @return an XML string representing the rule.
      * @supported.api
      */
     public String toXML() {
         //TODO: account for excludedResourceNames
         StringBuffer answer = new StringBuffer(100);
         answer.append("\n").append("<Rule");
         if (ruleName != null) {
             answer.append(" name=\"");
             answer.append(XMLUtils.escapeSpecialCharacters(ruleName));
             answer.append("\">");
         } else {
             answer.append(">");
         }
 
         answer.append("\n").append("<ServiceName name=\"");
         answer.append(XMLUtils.escapeSpecialCharacters(serviceTypeName));
         answer.append("\" />");
         if (resourceName != null) {
             answer.append("\n").append("<ResourceName name=\"");
             answer.append(
                     XMLUtils.escapeSpecialCharacters(resourceName));
             answer.append("\" />");
         }
 
         Set actionNames = new HashSet();
         actionNames.addAll(actions.keySet());
         if (excludedResourceNames != null && !excludedResourceNames.isEmpty()) {
             actionNames.add(EXCLUDED_RESOURCE_NAMES);
         }
 
         Iterator actionNamesIter = actionNames.iterator();
         while (actionNamesIter.hasNext()) {
             String actionName = (String) actionNamesIter.next();
             answer.append("\n").append("<AttributeValuePair>");
             answer.append("\n").append("<Attribute name=\"");
             answer.append(XMLUtils.escapeSpecialCharacters(actionName));
             answer.append("\" />");
             Set values = (Set) actions.get(actionName);
             if (actionName.equals(EXCLUDED_RESOURCE_NAMES)) {
                 values = excludedResourceNames;
             }
 
             if (values.size() > 0) {
                 Iterator items = values.iterator();
                 while (items.hasNext()) {
                     answer.append("\n").append("<Value>");
                     answer.append(
                             XMLUtils.escapeSpecialCharacters(
                             (String) items.next()));
                     answer.append("</Value>");
                 }
 
             }
             answer.append("\n").append("</AttributeValuePair>");
         }
 
         answer.append("\n").append("</Rule>");
         return (answer.toString());
     }
 
     /**
      * Returns service type of this rules
      * @return service type of this rule
      */
     protected ServiceType getServiceType() {
         return (serviceType);
     }
 
     /**
      * Returns an XML respresentation of the rule with policy name to
      * use in resource index tree
      * @param policyName policy name to use while creating xml representation
      * @return an XML respresentation of the rule with policy name to
      * use in resource index tree
      */
     protected String toResourcesXml(String policyName) {
         StringBuffer beginning = new StringBuffer(100);
         // "<PolicyCrossReferences name=\"" + serviceTypeName +
         // "\" type=\"Resources\">"
         beginning.append("<").append(PolicyManager.POLICY_INDEX_ROOT_NODE).append(" ").append(PolicyManager.POLICY_INDEX_ROOT_NODE_NAME_ATTR).append("=\"").append(serviceTypeName).append("\" ").append(PolicyManager.POLICY_INDEX_ROOT_NODE_TYPE_ATTR).append("=\"").append(
                 PolicyManager.POLICY_INDEX_ROOT_NODE_TYPE_ATTR_RESOURCES_VALUE).append("\">");
 
         String normalizedResName = resourceName;
         if (resourceName == null || resourceName.length() == 0) {
             normalizedResName = ResourceManager.EMPTY_RESOURCE_NAME;
         }
 
         String[] resources = serviceType.split(normalizedResName);
         int n = resources.length;
 
         StringBuffer middle = new StringBuffer(100);
         // "<Reference name=\"" + resources[n-1]) +
         // "\"><PolicyName name=\"" + policyName +
         // "\"/></Reference>"
         middle.append("<").append(PolicyManager.POLICY_INDEX_REFERENCE_NODE).append(" ").append(PolicyManager.POLICY_INDEX_REFERENCE_NODE_NAME_ATTR).append("=\"").append(resources[n - 1]).append("\"><").append(PolicyManager.POLICY_INDEX_POLICYNAME_NODE).append(" ").append(PolicyManager.POLICY_INDEX_POLICYNAME_NODE_NAME_ATTR).append("=\"").append(policyName).append("\"/></").append(PolicyManager.POLICY_INDEX_REFERENCE_NODE).append(">");
         String tmp = middle.toString();
         for (int i = n - 2; i >=
                 0; i--) {
             //tmp = "<Reference name=\"" + resources[i] +"\">" +
             //    tmp + "</Reference>";
             tmp = "<" + PolicyManager.POLICY_INDEX_REFERENCE_NODE +
                     " " + PolicyManager.POLICY_INDEX_REFERENCE_NODE_NAME_ATTR +
                     "=\"" + resources[i] + "\">" + tmp + "</" +
                     PolicyManager.POLICY_INDEX_REFERENCE_NODE + ">";
         }
 
         return (beginning + tmp + "</" +
                 PolicyManager.POLICY_INDEX_ROOT_NODE + ">");
     }
 
     /**
      * Returns xml string representation of the rule.
      *
      * @return xml string representation of the rule
      */
     public String toString() {
         return (toXML());
     }
 
     /**
      * Creates and returns a copy of this object. The returned
      * <code>Rule</code> object will have the same rule
      * name, resource, service name, and actions
      * such that <code>x.clone().equals(x)</code> will be
      * <code>true</code>. However <code>x.clone()</code>
      * will not be the same as <code>x</code>, i.e.,
      * <code>x.clone() != x</code>.
      *
      * @return a copy of this object
      */
     public Object clone() {
         //TODO: clone excludedResourceNames, implement hashCode
         Rule answer = null;
         try {
             answer = (Rule) super.clone();
         } catch (CloneNotSupportedException se) {
             answer = new Rule();
         }
 
         answer.ruleName = ruleName;
         answer.serviceTypeName = serviceTypeName;
         answer.serviceType = serviceType;
         answer.resourceName = resourceName;
 
         // Copy the actions
         answer.actions = new HashMap();
         Iterator items = actions.keySet().iterator();
         while (items.hasNext()) {
             Object o = items.next();
             Set set = (Set) actions.get(o);
             HashSet aset = new HashSet();
             aset.addAll(set);
             answer.actions.put(o, aset);
         }
 
         return (answer);
     }
 
     /**
      * Returns action values given resource type, resource name and a set of
      * action names  by matching the arguments to those of the rule object
      *
      * @param resourceType resource type
      * @param resourceName resource name
      * @param actionNames a set of action names for which to compute values.
      * Each element of the set should be a <code>String</code>
      * valued action name
      * @return a map of action values for actions
      *         Each key of the map is a String valued action name
      *         Each value of the map is a set of String values
      * @throws NameNotFoundException if any name in <code>actionNames</code> is
      *         not found in the rule.
      */
     Map getActionValues(String resourceType, String resourceName,
             Set actionNames) throws NameNotFoundException {
         Map actionValues = null;
         String serviceTypeName = getServiceTypeName();
         if ((serviceTypeName.equalsIgnoreCase(resourceType)) && (actionNames != null)) {
             ResourceMatch rm = isResourceMatch(resourceType, resourceName);
             if (ResourceMatch.EXACT_MATCH.equals(rm) || ResourceMatch.WILDCARD_MATCH.equals(rm)) {
                 //if (ResourceMatch.EXACT_MATCH.equals(rm) ) {
                 actionValues = new HashMap();
                 Iterator actionIter = actionNames.iterator();
                 while (actionIter.hasNext()) {
                     String actionName = (String) actionIter.next();
                     Set values = getActionValues(actionName);
                     if (values != null) {
                         actionValues.put(actionName, values);
                     }
 
                 }
             }
         }
         return (actionValues);
     }
 }
