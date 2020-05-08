 /**
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2008 Sun Microsystems Inc. All Rights Reserved
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
  * at opensso/legal/CDDLv1.0.txt
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: PrivilegeUtils.java,v 1.5 2009-04-09 23:40:16 dillidorai Exp $
  */
 package com.sun.identity.entitlement.opensso;
 
 import com.iplanet.sso.SSOToken;
 import com.iplanet.sso.SSOException;
 import com.sun.identity.entitlement.Entitlement;
 import com.sun.identity.entitlement.EntitlementCondition;
 import com.sun.identity.entitlement.EntitlementSubject;
 import com.sun.identity.entitlement.UserSubject;
 import com.sun.identity.entitlement.GroupSubject;
 import com.sun.identity.entitlement.IPCondition;
 import com.sun.identity.entitlement.RoleSubject;
 import com.sun.identity.entitlement.OrSubject;
 import com.sun.identity.entitlement.NotSubject;
 import com.sun.identity.entitlement.OrCondition;
 import com.sun.identity.entitlement.AndCondition;
 import com.sun.identity.entitlement.DNSNameCondition;
 import com.sun.identity.entitlement.Privilege;
 import com.sun.identity.entitlement.EntitlementException;
 import com.sun.identity.entitlement.ResourceAttributes;
 import com.sun.identity.entitlement.StaticAttributes;
 import com.sun.identity.entitlement.TimeCondition;
 import com.sun.identity.entitlement.UserAttributes;
 import com.sun.identity.idm.AMIdentity;
 import com.sun.identity.idm.IdType;
 import com.sun.identity.idm.IdRepoException;
 import com.sun.identity.idm.IdUtils;
 import com.sun.identity.policy.ActionSchema;
 import com.sun.identity.policy.NameNotFoundException;
 import com.sun.identity.policy.Policy;
 import com.sun.identity.policy.PolicyConfig;
 import com.sun.identity.policy.PolicyESubject;
 import com.sun.identity.policy.PolicyException;
 import com.sun.identity.policy.Rule;
 import com.sun.identity.policy.ServiceType;
 import com.sun.identity.policy.ServiceTypeManager;
 import com.sun.identity.policy.interfaces.Condition;
 import com.sun.identity.policy.interfaces.ResponseProvider;
 import com.sun.identity.policy.interfaces.Subject;
 import com.sun.identity.policy.plugins.IDRepoResponseProvider;
 import com.sun.identity.security.AdminTokenAction;
 import com.sun.identity.shared.debug.Debug;
 import java.security.AccessController;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 
 /**
  * Class with utility methods to map from
  * <code>com.sun.identity.entity.Privilege</code>
  * to
  * </code>com.sun.identity.policy.Policy</code>
  */
 public class PrivilegeUtils {
 
     private static Random random = new Random();
     private static ServiceTypeManager svcTypeManager;
 
     static {
         try {
             SSOToken adminToken = (SSOToken) AccessController.doPrivileged(
                 AdminTokenAction.getInstance());
             svcTypeManager = new ServiceTypeManager(adminToken);
         } catch (SSOException ex) {
             //TOFIX
         }
     }
 
     /**
      * Constructs PrivilegeUtils
      */
     private PrivilegeUtils() {
     }
 
     /**
      * Maps a OpenSSO Policy to entitlement Privilege
      * @param policy OpenSSO Policy object
      * @return entitlement Privilege object
      * @throws com.sun.identity.policy.PolicyException if the mapping fails
      */
     public static Privilege policyToPrivilege(Policy policy)
             throws PolicyException, EntitlementException {
         //TODO: split a policy to multiple prrivileges if the rules have
         // different acation values
         if (policy == null) {
             return null;
         }
 
         String policyName = policy.getName();
         Set ruleNames = policy.getRuleNames();
         Set<Rule> rules = new HashSet<Rule>();
         for (Object ruleNameObj : ruleNames) {
             String ruleName = (String) ruleNameObj;
             Rule rule = policy.getRule(ruleName);
             rules.add(rule);
         }
         Entitlement entitlement = null;
         try {
             entitlement = rulesToEntitlement(rules);
         } catch (SSOException e) {
             //TODO: record, wrap and propogate the exception
         }
 
         Set subjectNames = policy.getSubjectNames();
         Set nqSubjects = new HashSet();
         for (Object subjectNameObj : subjectNames) {
             String subjectName = (String) subjectNameObj;
             Subject subject = policy.getSubject(subjectName);
             boolean exclusive = policy.isSubjectExclusive(subjectName);
             Object[] nqSubject = new Object[3];
             nqSubject[0] = subjectName;
             nqSubject[1] = subject;
             nqSubject[2] = exclusive;
             nqSubjects.add(nqSubject);
         }
         EntitlementSubject eSubject = nqSubjectsToESubject(nqSubjects);
 
         Set conditionNames = policy.getConditionNames();
         Set nConditions = new HashSet();
         for (Object conditionNameObj : conditionNames) {
             String conditionName = (String) conditionNameObj;
             Condition condition = policy.getCondition(conditionName);
             Object[] nCondition = new Object[2];
             nCondition[0] = conditionName;
             nCondition[1] = condition;
             nConditions.add(nCondition);
         }
         EntitlementCondition eCondition = nConditionsToECondition(nConditions);
 
         Set rpNames = policy.getResponseProviderNames();
         Set nrps = new HashSet();
         for (Object rpNameObj : rpNames) {
             String rpName = (String) rpNameObj;
             ResponseProvider rp = policy.getResponseProvider(rpName);
             Object[] nrp = new Object[2];
             nrp[0] = rpName;
             nrp[1] = rp;
             nrps.add(nrp);
         }
         Set<ResourceAttributes> resourceAttributesSet =
             nrpsToResourceAttributes(nrps);
 
         Privilege privilege = new OpenSSOPrivilege(policyName, entitlement,
             eSubject, eCondition, resourceAttributesSet);
         return privilege;
     }
 
     private static Entitlement rulesToEntitlement(Set<Rule> rules)
             throws PolicyException, SSOException {
         if (rules == null || rules.isEmpty()) {
             return null;
         }
         Set<String> resourceNames = new HashSet<String>();
         Set<String> excludedResourceNames = new HashSet<String>();
         Rule lrule = null;
         //TODO: split a policy to multiple prrivileges if the rules have different
         // acation values
         for (Rule rule : rules) {
             lrule = rule;
             String resourceName = rule.getResourceName();
             Set excludedResourceNames1 = rule.getExcludedResourceNames();
             resourceNames.add(resourceName);
             if (excludedResourceNames1 != null) {
                 excludedResourceNames.addAll(excludedResourceNames1);
             }
 
         }
         String serviceName = lrule.getServiceTypeName();
         Map<String, Boolean> actionMap = pavToPrav(lrule.getActionValues(), 
                 serviceName);
         String entitlementName = lrule.getName();
         int dashi = entitlementName.indexOf("---");
         if (dashi != -1) {
             entitlementName = entitlementName.substring(0, dashi);
         }
 
         Entitlement entitlement = new Entitlement(serviceName, resourceNames,
                 actionMap);
         entitlement.setName(entitlementName);
         entitlement.setExcludedResourceNames(excludedResourceNames);
         return entitlement;
     }
 
     private static EntitlementSubject nqSubjectsToESubject(Set nqSubjects) {
         Set esSet = new HashSet();
         EntitlementSubject es = null;
         for (Object nqSubjectObj : nqSubjects) {
             Object[] nqSubject = (Object[]) nqSubjectObj;
             Subject subject = (Subject) nqSubject[1];
             if (subject instanceof com.sun.identity.policy.plugins.AMIdentitySubject) {
                 es = mapAMIdentitySubjectToESubject(nqSubject);
             } else { // mapt to PolicyESubject
             }
             esSet.add(es);
         }
         if (esSet.size() == 1) {
             es = (EntitlementSubject) esSet.iterator().next();
         } else if (esSet.size() > 1) {
             es = new OrSubject(esSet);
         }
         return es;
     }
 
     private static EntitlementSubject mapAMIdentitySubjectToESubject(
         Object[] nqSubject) {
         String subjectName = (String) nqSubject[0];
         Subject subject = (Subject) nqSubject[1];
         Set<String> values = subject.getValues();
 
         if (values == null || values.isEmpty()) {
             EntitlementSubject es = new UserSubject(null, subjectName);
             Boolean exclusive = (Boolean) nqSubject[2];
             return (exclusive) ? new NotSubject(es, subjectName) : es;
         }
 
         Set<EntitlementSubject> esSet = getEntitlementSubjects(
             subjectName, values);
         EntitlementSubject es = null;
         
         if (esSet.size() == 1) {
             es = (EntitlementSubject) esSet.iterator().next();
         } else if (esSet.size() > 1) {
             es = new OrSubject(esSet, subjectName);
         } else {
             es = new UserSubject(null, subjectName);
         }
 
         Boolean exclusive = (Boolean) nqSubject[2];
         return (exclusive) ? new NotSubject(es, subjectName) : es;
     }
 
     private static Set<EntitlementSubject> getEntitlementSubjects(
         String subjectName,
         Set<String> values
     ) {
         Set<EntitlementSubject> esSet = new HashSet<EntitlementSubject>();
         SSOToken adminToken = (SSOToken) AccessController.doPrivileged(
             AdminTokenAction.getInstance());
 
         for (String value : values) {
             IdType idType = null;
             try {
                 AMIdentity amIdentity = IdUtils.getIdentity(adminToken, value);
                 if (amIdentity != null) {
                     idType = amIdentity.getType();
                     EntitlementSubject es = null;
                     if (IdType.USER.equals(idType)) {
                         es = new UserSubject(value, subjectName);
                     } else if (IdType.GROUP.equals(idType)) {
                         es = new GroupSubject(value, subjectName);
                     } else if (IdType.ROLE.equals(idType)) {
                         es = new RoleSubject(value, subjectName);
                     } else {
                         Debug debug = Debug.getInstance("Entitlement");
                         debug.error(
                             "PrivilegeUtils.getEntitlementSubjects: " +
                                 "unsupported IDType = " + idType);
                     }
                     if (es != null) {
                         esSet.add(es);
                     }
                 }
             } catch (IdRepoException e) {
                 //TOFIX
             }
         }
         return esSet;
     }
 
     private static EntitlementCondition nConditionsToECondition(
             Set nConditons) {
         Set ecSet = new HashSet();
         EntitlementCondition ec = null;
         for (Object nConditionObj : nConditons) {
             Object[] nCondition = (Object[]) nConditionObj;
             Condition condition = (Condition) nCondition[1];
             if (condition instanceof com.sun.identity.policy.plugins.IPCondition) {
                 ec = mapIPPConditionToIPECondition(nCondition);
             } else if (condition instanceof com.sun.identity.policy.plugins.SimpleTimeCondition) {
                 ec = mapSimpleTimeConditionToTimeCondition(nCondition);
             } else { //TODO: map to generic eCondition
             }
             ecSet.add(ec);
         }
         if (ecSet.size() == 1) {
             ec = (EntitlementCondition) ecSet.iterator().next();
         } else if (ecSet.size() > 1) {
             Map cnEntcMap = new HashMap();
             for (Object entcObj : ecSet) {
                 EntitlementCondition entc = (EntitlementCondition) entcObj;
                 Set values = (Set) cnEntcMap.get(entc.getClass().getName());
                 if (values == null) {
                     values = new HashSet();
                 }
                 values.add(entc);
                 cnEntcMap.put(entc.getClass().getName(), values);
             }
             Set keySet = cnEntcMap.keySet();
             if (keySet.size() == 1) {
                 Set values = (Set) cnEntcMap.get(keySet.iterator().next());
                 if (values.size() == 1) {
                     ec = (EntitlementCondition) values.iterator().next();
                 } else if (values.size() > 1) {
                     ec = new OrCondition(values);
                 }
             } else if (keySet.size() > 1) {
                 Set andSet = new HashSet();
                 for (Object keyObj : keySet) {
                     String key = (String) keyObj;
                     Set values = (Set) cnEntcMap.get(key);
                     if (values.size() == 1) {
                         andSet.add(values.iterator().next());
                     } else if (values.size() > 1) {
                         andSet.add(new OrCondition(values));
                     }
                 }
                 ec = new AndCondition(andSet);
             }
         }
         return ec;
     }
 
     private static EntitlementCondition mapIPPConditionToIPECondition(
             Object[] nCondition) {
         String pConditionName = (String) nCondition[0];
         com.sun.identity.policy.plugins.IPCondition pipc =
                 (com.sun.identity.policy.plugins.IPCondition) nCondition[1];
         Map props = pipc.getProperties();
         String dnsName = getCpValue(props,
                 com.sun.identity.policy.plugins.IPCondition.DNS_NAME);
         String startIP = getCpValue(props,
                 com.sun.identity.policy.plugins.IPCondition.START_IP);
         String endIP = getCpValue(props,
             com.sun.identity.policy.plugins.IPCondition.END_IP);
 
         IPCondition ipCondition = ((startIP != null) || (endIP != null)) ?
             new IPCondition(startIP, endIP) : null;
         DNSNameCondition dnsCondition = (dnsName != null) ?
             new DNSNameCondition(dnsName) : null;
 
         Set<EntitlementCondition> conditions = new
             HashSet<EntitlementCondition>();
         if (ipCondition != null) {
             ipCondition.setPConditionName(pConditionName);
             conditions.add(ipCondition);
         }
         if (dnsCondition != null) {
             dnsCondition.setPConditionName(pConditionName);
             conditions.add(dnsCondition);
         }
 
         if (conditions.size() > 1) {
             AndCondition andC = new AndCondition();
             andC.setEConditions(conditions);
             andC.setPConditionName(pConditionName);
             return andC;
         } else {
             return conditions.iterator().next();
         }
     }
 
     private static TimeCondition mapSimpleTimeConditionToTimeCondition(
             Object[] nCondition) {
         String pConditionName = (String) nCondition[0];
         com.sun.identity.policy.plugins.SimpleTimeCondition stc =
             (com.sun.identity.policy.plugins.SimpleTimeCondition) nCondition[1];
         Map props = stc.getProperties();
         TimeCondition tc = new TimeCondition(
             getCpValue(props,
                 com.sun.identity.policy.plugins.SimpleTimeCondition.START_TIME),
             getCpValue(props,
                 com.sun.identity.policy.plugins.SimpleTimeCondition.END_TIME),
             getCpValue(props,
                 com.sun.identity.policy.plugins.SimpleTimeCondition.START_DAY),
             getCpValue(props,
                 com.sun.identity.policy.plugins.SimpleTimeCondition.END_DAY));
         tc.setStartDate(getCpValue(props,
             com.sun.identity.policy.plugins.SimpleTimeCondition.START_DATE));
         tc.setEndDate(getCpValue(props,
             com.sun.identity.policy.plugins.SimpleTimeCondition.END_DATE));
         tc.setEnforcementTimeZone(getCpValue(props,
             com.sun.identity.policy.plugins.SimpleTimeCondition.ENFORCEMENT_TIME_ZONE));
         tc.setPConditionName(pConditionName);
         return tc;
     }
 
     private static String getCpValue(Map props, String name) {
         if (props == null || name == null) {
             return null;
         }
         Object valueObj = props.get(name);
         if (valueObj == null) {
             return null;
         }
         return valueObj.toString();
     }
 
     private static Set<ResourceAttributes> nrpsToEResourceAttributesSet(Set nrps) {
         Set raSet = new HashSet();
         for (Object nrpaObj : nrps) {
             Object[] nrpa = (Object[]) nrpaObj;
             String raName = (String) nrpa[0];
             ResponseProvider rp = (ResponseProvider) nrpa[1];
             if (rp instanceof com.sun.identity.policy.plugins.IDRepoResponseProvider) {
             }
             raSet.add(rp);
         }
         return raSet;
     }
 
     public static Policy privilegeToPolicy(Privilege privilege)
             throws PolicyException, SSOException {
         Policy policy = null;
         policy = new Policy(privilege.getName());
         if (privilege.getEntitlement() != null) {
             Entitlement entitlement = privilege.getEntitlement();
             Set<Rule> rules = entitlementToRules(entitlement);
             for (Rule rule : rules) {
                 policy.addRule(rule);
             }
         }
         if (privilege.getSubject() != null) {
             List pSubjects = eSubjectToPSubjects(privilege.getSubject());
             for (Object obj : pSubjects) {
                 Object[] arr = (Object[]) obj;
                 String pSubjectName = (String) arr[0];
                 Subject subject = (Subject) arr[1];
                 Subject s = null;
                 try {
                     s = policy.getSubject(pSubjectName);
                 } catch (NameNotFoundException nnfe) {
                 }
                 if (s != null) {
                     Set values = s.getValues();
                     if (values == null) {
                         values = new HashSet();
                     }
                     values.addAll(subject.getValues());
                     s.setValues(values);
                 } else {
                     Boolean exclusive = (Boolean) arr[2];
                     policy.addSubject(pSubjectName, subject, exclusive);
                 }
             }
         }
         if (privilege.getCondition() != null) {
             List pConditions = eConditionToPConditions(privilege.getCondition());
             for (Object obj : pConditions) {
                 Object[] arr = (Object[]) obj;
                 String pConditionName = (String) arr[0];
                 Condition condition = (Condition) arr[1];
                 policy.addCondition(pConditionName, condition);
             }
         }
         if (privilege.getResourceAttributes() != null) {
             List nrps = resourceAttributesToResponseProviders(
                     privilege.getResourceAttributes());
             for (Object obj : nrps) {
                 Object[] arr = (Object[]) obj;
                 String pResponseProviderName = (String) arr[0];
                 ResponseProvider responseProvider = (ResponseProvider) arr[1];
                 policy.addResponseProvider(pResponseProviderName, responseProvider);
             }
         }
         return policy;
     }
 
     private static Set<Rule> entitlementToRules(Entitlement entitlement)
             throws PolicyException, SSOException {
         Set<Rule> rules = new HashSet<Rule>();
         String entName = entitlement.getName();
         String serviceName = entitlement.getApplicationName();
         Set<String> resourceNames = entitlement.getResourceNames();
         Map<String, Boolean> actionValues = entitlement.getActionValues();
         Map av = pravToPav(actionValues, serviceName);
         if (resourceNames != null) {
             int rc = 0;
             for (String resourceName : resourceNames) {
                 rc += 1;
                 Rule rule = new Rule(entName + "---" + rc, serviceName,
                         resourceName, av);
                 rule.setExcludedResourceNames(
                         entitlement.getExcludedResourceNames());
                 rules.add(rule);
             }
         }
         return rules;
     }
 
     private static List eSubjectToPSubjects(EntitlementSubject es)
             throws PolicyException, SSOException {
         List subjects = new ArrayList();
         if (es instanceof UserSubject) {
             subjects.add(userESubjectToPSubject((UserSubject) es));
         } else if (es instanceof GroupSubject) {
             subjects.add(groupESubjectToPSubject((GroupSubject) es));
         } else if (es instanceof RoleSubject) {
             subjects.add(roleESubjectToPSubject((RoleSubject) es));
         } else if (es instanceof PolicyESubject) {
             subjects.add(policyESubjectToPSubject((PolicyESubject) es));
         } else if (es instanceof NotSubject) {
             List list = notESubjectToPSubject((NotSubject) es);
             for (Object obj : list) {
                 subjects.add(obj);
             }
         } else if (es instanceof OrSubject) {
             List list = orESubjectToPSubject((OrSubject) es);
             for (Object obj : list) {
                 subjects.add(obj);
             }
         } else { // map to EntitlementSubject
 
             subjects.add(eSubjectToEntitlementSubject(es));
         }
         return subjects;
     }
 
     private static Object[] userESubjectToPSubject(UserSubject us)
             throws PolicyException, SSOException {
         Subject subject = new com.sun.identity.policy.plugins.AMIdentitySubject();
         Set<String> values = new HashSet<String>();
         values.add(us.getUser());
         subject.setValues(values);
         String pSubjectName = us.getPSubjectName();
         if (pSubjectName == null) {
             pSubjectName = "UserSubject" + randomName();
         }
         Object[] arr = new Object[3];
         arr[0] = pSubjectName;
         arr[1] = subject;
         arr[2] = false;
         return arr;
     }
 
     private static Object[] groupESubjectToPSubject(GroupSubject gs)
             throws PolicyException, SSOException {
         Subject subject = new com.sun.identity.policy.plugins.AMIdentitySubject();
         Set<String> values = new HashSet<String>();
         values.add(gs.getGroup());
         subject.setValues(values);
         String pSubjectName = gs.getPSubjectName();
         if (pSubjectName == null) {
             pSubjectName = "GroupSubject" + randomName();
         }
         Object[] arr = new Object[3];
         arr[0] = pSubjectName;
         arr[1] = subject;
         arr[2] = false;
         return arr;
     }
 
     private static Object[] roleESubjectToPSubject(RoleSubject rs)
             throws PolicyException, SSOException {
         Subject subject = new com.sun.identity.policy.plugins.AMIdentitySubject();
         Set<String> values = new HashSet<String>();
         values.add(rs.getRole());
         subject.setValues(values);
         String pSubjectName = rs.getPSubjectName();
         if (pSubjectName == null) {
             pSubjectName = "RoleSubject" + randomName();
         }
         Object[] arr = new Object[3];
         arr[0] = pSubjectName;
         arr[1] = subject;
         arr[2] = false;
         return arr;
     }
 
     private static Object[] policyESubjectToPSubject(PolicyESubject ps)
             throws PolicyException, SSOException {
         Subject subject = null; //stm.getSubject("AMIdentitySubject");
         /*
         Set<String> values = new HashSet<String>();
         values.add(rs.getRole());
         subject.setValues(values);
         String pSubjectName = rs.getPSubjectName();
         if (pSubjectName == null) {
         pSubjectName = randomName();
         }
          * */
         Object[] arr = new Object[3];
         //arr[0] = pSubjectName;
         arr[1] = subject;
         arr[2] = false;
         return arr;
     }
 
     private static List notESubjectToPSubject(NotSubject nos)
             throws PolicyException, SSOException {
         List list = new ArrayList();
         EntitlementSubject ns = nos.getESubject();
         if (ns instanceof OrSubject) {
             OrSubject ores = (OrSubject) ns;
             Set<EntitlementSubject> nested2Subjects = ores.getESubjects();
             if (nested2Subjects != null) {
                 for (EntitlementSubject es : nested2Subjects) {
                     if (es instanceof UserSubject) {
                         Object[] arr = userESubjectToPSubject(
                                 (UserSubject) es);
                         arr[2] = Boolean.TRUE;
                         list.add(arr);
                     } else if (es instanceof GroupSubject) {
                         Object[] arr = groupESubjectToPSubject(
                                 (GroupSubject) es);
                         arr[2] = Boolean.TRUE;
                         list.add(arr);
                     } else if (es instanceof RoleSubject) {
                         Object[] arr = roleESubjectToPSubject(
                                 (RoleSubject) es);
                         arr[2] = Boolean.TRUE;
                         list.add(arr);
                     } else { // map to EntitlementSubject
                         Object[] arr = eSubjectToEntitlementSubject(es);
                         arr[2] = Boolean.TRUE;
                         list.add(arr);
                     }
                 }
             }
         } else if (ns instanceof UserSubject) {
 
             Object[] arr = userESubjectToPSubject((UserSubject) ns);
             arr[2] = Boolean.TRUE;
             list.add(arr);
         } else if (ns instanceof GroupSubject) {
             Object[] arr = groupESubjectToPSubject((GroupSubject) ns);
             arr[2] = Boolean.TRUE;
             list.add(arr);
         } else if (ns instanceof RoleSubject) {
             Object[] arr = roleESubjectToPSubject((RoleSubject) ns);
             arr[2] = Boolean.TRUE;
             list.add(arr);
         } else { // map to EntitlementSubejct
             Object[] arr = eSubjectToEntitlementSubject(ns);
             arr[2] = Boolean.TRUE;
             list.add(arr);
         }
         return list;
     }
 
     private static List orESubjectToPSubject(
             OrSubject os) throws PolicyException, SSOException {
         List list = new ArrayList();
         Set nestedSubjects = os.getESubjects();
         if (nestedSubjects != null) {
             for (Object ns : nestedSubjects) {
                 if (ns instanceof UserSubject) {
                     list.add(userESubjectToPSubject((UserSubject) ns));
                 } else if (ns instanceof GroupSubject) {
                     list.add(groupESubjectToPSubject((GroupSubject) ns));
                 } else if (ns instanceof RoleSubject) {
                     list.add(roleESubjectToPSubject((RoleSubject) ns));
                 } else if (ns instanceof OrSubject) {
                     List list1 = orESubjectToPSubject((OrSubject) ns);
                     for (Object obj : list1) {
                         list.add(obj);
                     }
                 } else if (ns instanceof NotSubject) {
                     List list1 = notESubjectToPSubject((NotSubject) ns);
                     for (Object obj : list1) {
                         Object[] arr = (Object[]) obj;
                         arr[2] = Boolean.TRUE;
                         list.add(arr);
                     }
                 } else { // map to EntitlementSubejct
                     list.add(eSubjectToEntitlementSubject((EntitlementSubject) ns));
                 }
             }
         }
         return list;
     }
 
     private static Object[] eSubjectToEntitlementSubject(EntitlementSubject es)
             throws PolicyException, SSOException {
         return null;
     }
 
     private static List eConditionToPConditions(EntitlementCondition ec)
             throws PolicyException, SSOException {
         List conditions = new ArrayList();
         if (ec instanceof IPCondition) {
             Object[] ncondition = new Object[2];
             ncondition[0] = ((IPCondition) ec).getPConditionName();
             ncondition[1] = ipConditionToPCondition((IPCondition) ec);
             conditions.add(ncondition);
         } else if (ec instanceof DNSNameCondition) {
             Object[] ncondition = new Object[2];
             ncondition[0] = ((DNSNameCondition) ec).getPConditionName();
             ncondition[1] = dnsNameConditionToPCondition((DNSNameCondition) ec);
             conditions.add(ncondition);
         } else if (ec instanceof TimeCondition) {
             Object[] ncondition = new Object[2];
             ncondition[0] = ((TimeCondition) ec).getPConditionName();
             ncondition[1] = timeConditionToPCondition((TimeCondition) ec);
             conditions.add(ncondition);
         } else if (ec instanceof OrCondition) {
             List list = orConditionToPCondition((OrCondition) ec);
             for (Object obj : list) {
                 conditions.add(obj);
             }
         } else if (ec instanceof AndCondition) {
             List list = andConditionToPCondition((AndCondition) ec);
             for (Object obj : list) {
                 conditions.add(obj);
             }
         } else { // map to EPCondition
 
             conditions.add(eConditionToEPCondition(ec));
         }
         return conditions;
     }
 
     private static Condition ipConditionToPCondition(IPCondition ipc)
             throws PolicyException, SSOException {
         com.sun.identity.policy.plugins.IPCondition ipCondition =
             new com.sun.identity.policy.plugins.IPCondition();
         Map props = new HashMap();
         if (ipc.getStartIp() != null) {
             props.put(com.sun.identity.policy.plugins.IPCondition.START_IP,
                 toSet(ipc.getStartIp()));
         }
         if (ipc.getEndIp() != null) {
             props.put(com.sun.identity.policy.plugins.IPCondition.END_IP,
                 toSet(ipc.getEndIp()));
         }
         ipCondition.setProperties(props);
         return ipCondition;
     }
 
     private static Condition dnsNameConditionToPCondition(DNSNameCondition dnsc)
             throws PolicyException, SSOException {
         com.sun.identity.policy.plugins.IPCondition ipCondition =
             new com.sun.identity.policy.plugins.IPCondition();
         Map props = new HashMap();
         if (dnsc.getDomainNameMask() != null) {
             props.put(com.sun.identity.policy.plugins.IPCondition.DNS_NAME,
                 toSet(dnsc.getDomainNameMask()));
         }
         ipCondition.setProperties(props);
         return ipCondition;
     }
 
     private static Condition timeConditionToPCondition(TimeCondition tc)
             throws PolicyException, SSOException {
         com.sun.identity.policy.plugins.SimpleTimeCondition stc =
             new com.sun.identity.policy.plugins.SimpleTimeCondition();
         Map props = new HashMap();
         if (tc.getStartTime() != null) {
             props.put(
                 com.sun.identity.policy.plugins.SimpleTimeCondition.START_TIME,
                 toSet(tc.getStartTime()));
         }
         if (tc.getEndTime() != null) {
             props.put(
                 com.sun.identity.policy.plugins.SimpleTimeCondition.END_TIME,
                 toSet(tc.getEndTime()));
         }
         if (tc.getStartDay() != null) {
             props.put(
                 com.sun.identity.policy.plugins.SimpleTimeCondition.START_DAY,
                 toSet(tc.getStartDay()));
         }
         if (tc.getEndDay() != null) {
             props.put(
                 com.sun.identity.policy.plugins.SimpleTimeCondition.END_DAY,
                 toSet(tc.getEndDay()));
         }
         if (tc.getStartDate() != null) {
             props.put(
                 com.sun.identity.policy.plugins.SimpleTimeCondition.START_DATE,
                 toSet(tc.getStartDate()));
         }
         if (tc.getEndDate() != null) {
             props.put(
                 com.sun.identity.policy.plugins.SimpleTimeCondition.START_DATE,
                 toSet(tc.getEndDate()));
         }
         if (tc.getEnforcementTimeZone() != null) {
             props.put(
                 com.sun.identity.policy.plugins.SimpleTimeCondition.ENFORCEMENT_TIME_ZONE,
                 toSet(tc.getEnforcementTimeZone()));
         }
         stc.setProperties(props);
         return stc;
     }
 
     private static List orConditionToPCondition(OrCondition oc)
             throws PolicyException, SSOException {
         List list = new ArrayList();
         Set nestedConditions = oc.getEConditions();
         if (nestedConditions != null) {
             for (Object nc : nestedConditions) {
                 if (nc instanceof IPCondition) {
                     IPCondition ipc = (IPCondition) nc;
                     Object[] arr = new Object[2];
                     arr[0] = ipc.getPConditionName();
                     arr[1] = ipConditionToPCondition(ipc);
                     list.add(arr);
                 } else if (nc instanceof DNSNameCondition) {
                     DNSNameCondition dnsc = (DNSNameCondition) nc;
                     Object[] arr = new Object[2];
                     arr[0] = dnsc.getPConditionName();
                     arr[1] = dnsNameConditionToPCondition(dnsc);
                     list.add(arr);
                 } else if (nc instanceof TimeCondition) {
                     TimeCondition tc = (TimeCondition) nc;
                     Object[] arr = new Object[2];
                     arr[0] = tc.getPConditionName();
                     arr[1] = timeConditionToPCondition(tc);
                     list.add(arr);
                 } else if (nc instanceof OrCondition) {
                     List list1 = orConditionToPCondition((OrCondition) nc);
                     for (Object obj : list1) {
                         list.add(obj);
                     }
                 } else if (nc instanceof TimeCondition) { //NotCondition) {
                     List list1 = orConditionToPCondition((OrCondition) nc);
                     for (Object obj : list1) {
                         Object[] arr = (Object[]) obj;
                         arr[2] = Boolean.TRUE;
                         list.add(arr);
                     }
                 } else { // map to EPCondition
                     //list.add(eConditionToEPCondition((EntitlementCondiiton) nc));
                 }
             }
         }
         return list;
     }
 
     private static List andConditionToPCondition(AndCondition ac)
             throws PolicyException, SSOException {
         List list = new ArrayList();
         Set nestedConditions = ac.getEConditions();
         if (nestedConditions != null) {
             for (Object nc : nestedConditions) {
                 if (nc instanceof IPCondition) {
                     IPCondition ipc = (IPCondition) nc;
                     Object[] arr = new Object[2];
                     arr[0] = ipc.getPConditionName();
                     arr[1] = ipConditionToPCondition(ipc);
                     list.add(arr);
                 } else if (nc instanceof DNSNameCondition) {
                     DNSNameCondition dnsc = (DNSNameCondition) nc;
                     Object[] arr = new Object[2];
                     arr[0] = dnsc.getPConditionName();
                     arr[1] = dnsNameConditionToPCondition(dnsc);
                     list.add(arr);
                 } else if (nc instanceof TimeCondition) {
                     TimeCondition tc = (TimeCondition) nc;
                     Object[] arr = new Object[2];
                     arr[0] = tc.getPConditionName();
                     arr[1] = timeConditionToPCondition(tc);
                     list.add(arr);
                 } else if (nc instanceof OrCondition) {
                     List list1 = orConditionToPCondition((OrCondition) nc);
                     for (Object obj : list1) {
                         list.add(obj);
                     }
                 } else if (nc instanceof TimeCondition) { //NotCondition) {
                     List list1 = orConditionToPCondition((OrCondition) nc);
                     for (Object obj : list1) {
                         Object[] arr = (Object[]) obj;
                         arr[2] = Boolean.TRUE;
                         list.add(arr);
                     }
                 } else { // map to EPCondiiton
                     //list.add(eConditionToEPCondition((EntitlementCondiiton) nc));
                 }
             }
         }
         return list;
     }
 
     private static Condition eConditionToEPCondition(EntitlementCondition ec)
             throws PolicyException, SSOException {
         return null;
     }
 
     private static Condition eConditionToEPCondition(Condition tc)
             throws PolicyException, SSOException {
         return null;
     }
 
     private static Set<ResourceAttributes> nrpsToResourceAttributes(
             Set nrps) throws EntitlementException {
         Set<ResourceAttributes> resourceAttributesSet = new HashSet();
         if (nrps != null && !nrps.isEmpty()) {
             for (Object nrpObj : nrps) {
                 Object[] nrpa = (Object[]) nrpObj;
                 String nrpName = (String) nrpa[0];
                 ResponseProvider rp = (ResponseProvider) nrpa[1];
                 if (rp instanceof IDRepoResponseProvider) {
                     IDRepoResponseProvider irp = (IDRepoResponseProvider) rp;
                     Map props = irp.getProperties();
                     if (props != null) {
                         Set sas = (Set) props.get(irp.STATIC_ATTRIBUTE);
                         if (sas != null && !sas.isEmpty()) {
                             StaticAttributes sa = new StaticAttributes();
                             Map saprops = new HashMap();
                             for (Object obj : sas) {
                                 String sat = (String) obj;
                                 int i = sat.indexOf("=");
                                 String name = null;
                                 String value = null;
                                 if (i > 0) {
                                     name = sat.substring(i);
                                     value = sat.substring(i, sat.length());
                                 } else {
                                     name = sat;
                                     value = null;
                                 }
                                 Set values = (Set) saprops.get(name);
                                 if (values == null) {
                                     values = new HashSet();
                                     saprops.put(name, values);
                                 }
                                 values.add(value);
                             }
                             sa.setProperties(saprops);
                             sa.setPResponseProviderName(nrpName);
                             resourceAttributesSet.add(sa);
                         }
                         Set uas = (Set) props.get(irp.DYNAMIC_ATTRIBUTE);
                         if (uas != null && !uas.isEmpty()) {
                             UserAttributes ua = new UserAttributes();
                             Map uaprops = new HashMap();
                             for (Object obj : uas) {
                                 String uat = (String) obj;
                                 int i = uat.indexOf("=");
                                 String name = null;
                                 String value = null;
                                 if (i > 0) {
                                     name = uat.substring(i);
                                     value = uat.substring(i, uat.length());
                                 } else {
                                     name = uat;
                                     value = null;
                                 }
                                 Set values = (Set) uaprops.get(name);
                                 if (values == null) {
                                     values = new HashSet();
                                     uaprops.put(name, values);
                                 }
                                 values.add(value);
                             }
                             ua.setProperties(uaprops);
                             ua.setPResponseProviderName(nrpName);
                             resourceAttributesSet.add(ua);
                         }
                     }
                 }
 
             }
         }
         return resourceAttributesSet;
     }
 
     private static List resourceAttributesToResponseProviders(
             Set<ResourceAttributes> resourceAttributes) throws PolicyException {
         List nrps = new ArrayList();
         if (resourceAttributes != null) {
             for (ResourceAttributes ra : resourceAttributes) {
                 if (ra instanceof StaticAttributes) {
                     StaticAttributes sa = (StaticAttributes) ra;
                     Object[] arr = new Object[2];
                     arr[0] = sa.getPResponseProviderName();
                     IDRepoResponseProvider rp = new IDRepoResponseProvider();
                     Map props = sa.getProperties();
                     if (props != null) {
                         Set newValues = new HashSet();
                         Set entrySet = props.entrySet();
                         for (Object entryObj : entrySet) {
                             Map.Entry entry = (Map.Entry) entryObj;
                             String name = (String) entry.getKey();
                             Set values = (Set) entry.getValue();
                             if (values != null && !values.isEmpty()) {
                                 for (Object valueObj : values) {
                                     String value = (String) valueObj;
                                     newValues.add(name + "=" + value);
                                 }
 
                             }
                             if (!newValues.isEmpty()) {
                                 Map newProps = new HashMap();
                                 newProps.put(rp.STATIC_ATTRIBUTE, newValues);
                                 rp.setProperties(newProps);
                             }
 
                         }
                         arr[1] = rp;
                         nrps.add(arr);
                     }
 
                 } else if (ra instanceof UserAttributes) {
                     UserAttributes ua = (UserAttributes) ra;
                     Object[] arr = new Object[2];
                     arr[0] = ua.getPResponseProviderName();
                     Map props = ua.getProperties();
                     IDRepoResponseProvider rp = new IDRepoResponseProvider();
                     if (props != null) {
                         Set newValues = new HashSet();
                         Set entrySet = props.entrySet();
                         for (Object entryObj : entrySet) {
                             Map.Entry entry = (Map.Entry) entryObj;
                             String name = (String) entry.getKey();
                             Set values = (Set) entry.getValue();
                             String value = null;
                             if (values != null && !values.isEmpty()) {
                                 value = (String) values.iterator().next();
                             }
 
                             String newValue = name;
                             if (value != null) {
                                 newValue = name + "=" + value;
 
                             }
 
                             newValues.add(newValue);
                             if (!newValues.isEmpty()) {
                                 Map newProps = new HashMap();
                                 newProps.put(rp.DYNAMIC_ATTRIBUTE, newValues);
                                 Map configParams = new HashMap();
                                 configParams.put(
                                         PolicyConfig.SELECTED_DYNAMIC_ATTRIBUTES,
                                         newValues);
                                 rp.initialize(configParams);
                                 rp.setProperties(newProps);
                             }
 
                         }
                     }
                     arr[1] = rp;
                     nrps.add(arr);
                 }
 
             }
         }
         return nrps;
     }
 
     private static String randomName() {
         return "" + random.nextInt(10000);
     }
 
     private static Set toSet(Object obj) {
         if (obj == null) {
             return null;
         }
 
         Set set = new HashSet();
         set.add(obj);
         return set;
     }
 
     static Map pravToPav(Map<String, Boolean> actionValues,
             String serviceName) throws PolicyException, SSOException  {
         if (actionValues == null) {
             return null;
         }
         ServiceType st = svcTypeManager.getServiceType(serviceName);
         Map av = new HashMap();
         Set<String> keySet = actionValues.keySet();
         for (String action : keySet) {
             ActionSchema as = st.getActionSchema(action);
             String trueValue = as.getTrueValue();
             String falseValue = as.getFalseValue();
             Boolean value = actionValues.get(action);
             if (value.equals(Boolean.TRUE)) {
                 Set values = new HashSet();
                 values.add(trueValue);
                 av.put(action, values);
             } else {
                 Set values = new HashSet();
                 values.add(falseValue);
                av.put(action, values);
             }
 
         }
         return av;
     }
 
     static Map<String, Boolean> pavToPrav(Map actionValues,
             String serviceName) throws PolicyException, SSOException {
         if (actionValues == null) {
             return null;
         }
         ServiceType st = svcTypeManager.getServiceType(serviceName);
         Map av = new HashMap();
         Set keySet = (Set) actionValues.keySet();
         for (Object actionObj : keySet) {
             String action = (String) actionObj;
             ActionSchema as = st.getActionSchema(action);
             String trueValue = as.getTrueValue();
             Set values = (Set) actionValues.get(action);
             if ((values != null) && (values.contains(trueValue))) {
                 av.put(action, Boolean.TRUE);
             } else {
                 av.put(action, Boolean.FALSE);
             }
 
         }
         return av;
     }
 }
