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
 * $Id: PriviligeUtilsTest.java,v 1.2 2009-02-28 16:38:34 dillidorai Exp $
  */
 
 package com.sun.identity.policy;
 
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.authentication.internal.server.AuthSPrincipal;
 import com.sun.identity.entitlement.Entitlement;
 import com.sun.identity.entitlement.Privilige;
 import com.sun.identity.entitlement.SimulatedResult;
 import com.sun.identity.idm.AMIdentity;
 import com.sun.identity.idm.AMIdentityRepository;
 import com.sun.identity.idm.IdRepoException;
 import com.sun.identity.idm.IdType;
 import com.sun.identity.idm.IdUtils;
 import com.sun.identity.policy.interfaces.Condition;
 import com.sun.identity.policy.interfaces.Subject;
 import com.sun.identity.security.AdminTokenAction;
 import com.sun.identity.sm.ServiceManager;
 import com.sun.identity.unittest.UnittestLog;
 import java.security.AccessController;
 import java.security.Principal;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.Test;
 import org.testng.annotations.BeforeClass;
 
 /**
  *
  * @author dillidorai
  */
 public class PriviligeUtilsTest {
     private static String POLICY_NAME1 = "PriviligeUtilsTestP1";
 
     @BeforeClass
     public void setup() throws PolicyException, SSOException, IdRepoException {
         SSOToken adminToken = (SSOToken) AccessController.doPrivileged(
             AdminTokenAction.getInstance());
         PolicyManager pm = new PolicyManager(adminToken, "/");
         Policy policy = new Policy(POLICY_NAME1, "test1 - discard",
             false, true);
         policy.addRule(createRule("welcome"));
         policy.addRule(createRule("banner"));
         createUsers(adminToken, "user11", "user21", "user22");
         policy.addSubject("Users1", createUsersSubject(pm, "user11"));
         policy.addSubject("Users2", createUsersSubject(pm, "user21", "user22"));
         pm.addPolicy(policy);
     }
     
     @AfterClass
     public void cleanup() throws PolicyException, SSOException, IdRepoException{
         SSOToken adminToken = (SSOToken) AccessController.doPrivileged(
             AdminTokenAction.getInstance());
         PolicyManager pm = new PolicyManager(adminToken, "/");
         pm.removePolicy(POLICY_NAME1);
         deleteUsers(adminToken, "user11", "user21", "user22");
     }
     
     @Test
     public void testPriviligeEqualsPolicy() throws Exception {
         SSOToken adminToken = (SSOToken) AccessController.doPrivileged(
             AdminTokenAction.getInstance());
         PolicyManager pm = new PolicyManager(adminToken, "/");
         Policy policy = pm.getPolicy(POLICY_NAME1);
         UnittestLog.logMessage(
             "PriviligeUnitlsTest.testPriviligeEqualsPolicy():"
             + "policy=" + policy.toXML());
         Privilige privilige = PriviligeUtils.policyToPrivilige(policy);
         UnittestLog.logMessage(
             "PriviligeUnitlsTest.testPriviligeEqualsPolicy():"
             + "privilige=" + privilige);
         Policy policy1 = PriviligeUtils.priviligeToPolicy(privilige);
         UnittestLog.logMessage(
             "PriviligeUnitlsTest.testPriviligeEqualsPolicy():"
             + "policy1=" + policy1.toXML());
         assert(policy1.equals(policy));
     }
     
     private Rule createRule(String ruleName) throws PolicyException {
         Map<String, Set<String>> actionMap 
                 = new HashMap<String, Set<String>>();
         Set<String> getValues = new HashSet<String>();
         getValues.add("allow");
         actionMap.put("GET", getValues);
         Set<String> postValues = new HashSet<String>();
         postValues.add("deny");
         actionMap.put("POST", postValues);
         return new Rule(ruleName, 
             "iPlanetAMWebAgentService",
             "http://sample.com/" + ruleName,
             actionMap);
     }
     
     private Subject createUsersSubject(
             PolicyManager pm, 
            String subjectName,
             String ... userNames) throws PolicyException {
         SubjectTypeManager mgr = pm.getSubjectTypeManager();
         Subject subject = mgr.getSubject("AMIdentitySubject");
         Set<String> values = new HashSet<String>();
         for (String value : userNames) {
            String uuid = "id=name" + ",ou=user," + ServiceManager.getBaseDN();
             values.add(uuid);
         }
         subject.setValues(values);
         return subject;
     }
 
     private void createUsers(
             SSOToken adminToken, 
             String ... names)
         throws IdRepoException, SSOException {
         AMIdentityRepository amir = new AMIdentityRepository(
             adminToken, "/");
         for (String name : names) {
             Map attrMap = new HashMap();
 
             Set cnVals = new HashSet();
             cnVals.add(name);
             attrMap.put("cn", cnVals);
 
             Set snVals = new HashSet();
             snVals.add(name);
             attrMap.put("sn", snVals);
 
             Set nameVals = new HashSet();
             nameVals.add(name);
             attrMap.put("givenname", nameVals);
 
             Set passworVals = new HashSet();
             passworVals.add(name);
             attrMap.put("userpassword", passworVals);
 
             amir.createIdentity(IdType.USER, name, attrMap);
         }
     }
     
     private void deleteUsers(SSOToken adminToken, 
             String ... names)
             throws IdRepoException, SSOException {
         AMIdentityRepository amir = new AMIdentityRepository(
             adminToken, "/");
         Set identities = new HashSet();
         for (String name : names) {
             String uuid = "id=" + name + ",ou=user," + ServiceManager.getBaseDN();
             identities.add(IdUtils.getIdentity(adminToken, uuid));
         }
         amir.deleteIdentities(identities);
     }
 
 }
