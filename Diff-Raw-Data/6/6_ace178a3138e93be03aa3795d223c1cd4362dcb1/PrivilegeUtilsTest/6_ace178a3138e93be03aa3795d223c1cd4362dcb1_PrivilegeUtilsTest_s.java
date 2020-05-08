 /**
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2009 Sun Microsystems Inc. All Rights Reserved
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
 * $Id: PrivilegeUtilsTest.java,v 1.1 2009-05-06 22:36:43 dillidorai Exp $
  */
 package com.sun.identity.entitlement.xacml3;
 
 import com.sun.identity.entitlement.AndCondition;
 import com.sun.identity.entitlement.Entitlement;
 import com.sun.identity.entitlement.EntitlementCondition;
 import com.sun.identity.entitlement.EntitlementSubject;
 import com.sun.identity.entitlement.IPCondition;
 import com.sun.identity.entitlement.IdRepoUserSubject;
 import com.sun.identity.entitlement.OrCondition;
 import com.sun.identity.entitlement.OrSubject;
 import com.sun.identity.entitlement.Privilege;
 import com.sun.identity.entitlement.ResourceAttributes;
 import com.sun.identity.entitlement.StaticAttributes;
 import com.sun.identity.entitlement.UserAttributes;
 import com.sun.identity.entitlement.UserSubject;
 import com.sun.identity.entitlement.opensso.OpenSSOPrivilege;
 import com.sun.identity.sm.ServiceManager;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import org.testng.annotations.Test;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 
 /**
  *
  * @author dorai
  */
 public class PrivilegeUtilsTest {
 
     private static String SERVICE_NAME = "iPlanetAMWebAgentService";
     private static String PRIVILEGE_NAME = "TestPrivilege";
 
     @BeforeClass
     public void setup() {
     }
 
     @AfterClass
     public void cleanup() {
     }
 
     @Test
     public void testPrivilegeToXACMLPolicy() throws Exception {
         Map<String, Boolean> actionValues = new HashMap<String, Boolean>();
         actionValues.put("GET", Boolean.TRUE);
         actionValues.put("POST", Boolean.FALSE);
         // The port is required for passing equals  test
         // opensso policy would add default port if port not specified
         String resourceName = "http://www.sun.com:80";
         Entitlement entitlement = new Entitlement(); //SERVICE_NAME,
                 //resourceName); //, actionValues);
         entitlement.setName("ent1");
 
         String user11 = "id=user11,ou=user," + ServiceManager.getBaseDN();
         String user12 = "id=user12,ou=user," + ServiceManager.getBaseDN();
         UserSubject ua1 = new IdRepoUserSubject();
         ua1.setID(user11);
         UserSubject ua2 = new IdRepoUserSubject();
         ua2.setID(user12);
         Set<EntitlementSubject> subjects = new HashSet<EntitlementSubject>();
         subjects.add(ua1);
         subjects.add(ua2);
         OrSubject os = new OrSubject(subjects);
 
         Set<String> excludedResourceNames = new HashSet<String>();
         entitlement.setExcludedResourceNames(excludedResourceNames);
         
         Set<EntitlementCondition> conditions = new HashSet<EntitlementCondition>();
         String startIp = "100.100.100.100";
         String endIp = "200.200.200.200";
         IPCondition ipc = new IPCondition(startIp, endIp);
         ipc.setPConditionName("ipc");
         conditions.add(ipc);
         OrCondition oc = new OrCondition(conditions);
         AndCondition ac = new AndCondition(conditions);
 
         StaticAttributes sa = new StaticAttributes();
         Map<String, Set<String>> attrValues = new HashMap<String, Set<String>>();
         Set<String> aValues = new HashSet<String>();
         aValues.add("a10");
         aValues.add("a20");
         Set<String> bValues = new HashSet<String>();
         bValues.add("b10");
         bValues.add("b20");
         attrValues.put("a", aValues);
         attrValues.put("b", bValues);
         sa.setProperties(attrValues);
         sa.setPResponseProviderName("sa");
 
         UserAttributes ua = new UserAttributes();
         attrValues = new HashMap<String, Set<String>>();
         Set<String> mailAliases = new HashSet<String>();
         mailAliases.add("email1");
         mailAliases.add("email2");
         attrValues.put("mail", mailAliases);
         attrValues.put("uid", null);
         ua.setProperties(attrValues);
         ua.setPResponseProviderName("ua");
 
         Set<ResourceAttributes> ra = new HashSet<ResourceAttributes>();
         ra.add(sa);
         ra.add(ua);
 
         Privilege privilege = new OpenSSOPrivilege(PRIVILEGE_NAME, entitlement, os,
                 ipc, ra);
        String xacmlString = PrivilegeUtils.toXACML(privilege);
 
     }
 
     public static void main(String[] args) throws Exception {
         System.out.println("PrivilegeUtilsTest");
         PrivilegeUtilsTest put = new PrivilegeUtilsTest();
         put.testPrivilegeToXACMLPolicy();
     }
 
 }
