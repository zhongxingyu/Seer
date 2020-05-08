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
 * $Id: PolicyCommon.java,v 1.6 2007-09-13 16:39:39 arunav Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.common;
 
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.iplanet.sso.SSOToken;
 import com.iplanet.sso.SSOException;
 import com.sun.identity.policy.PolicyDecision;
 import com.sun.identity.policy.client.PolicyEvaluator;
 import com.sun.identity.qatest.common.TestCommon;
 import com.sun.identity.qatest.common.TestConstants;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.ResourceBundle;
 import java.util.StringTokenizer;
 
 
 /**
  * This class has common methods related to policy management and is
  * consumed by policy tests
  */
 public class PolicyCommon extends TestCommon {
     
     private String loginURL;
     private String logoutURL;
     private String fmadmURL;
     private String baseDir;
     private String strGlobalRB;
     private String strLocalRB;
     private FederationManager fmadm;
     private WebClient webClient;
     private SSOToken userToken;
     private Map identityMap;
     
     /**
      * Class constructor. Sets class variables.
      */
     public PolicyCommon()
     throws Exception{
         super("PolicyCommon");
         loginURL = protocol + ":" + "//" + host + ":" + port + uri +
                 "/UI/Login";
         logoutURL = protocol + ":" + "//" + host + ":" + port + uri +
                 "/UI/Logout";
         fmadmURL = protocol + ":" + "//" + host + ":" + port + uri;
         ResourceBundle rbAMConfig = ResourceBundle.
                 getBundle(TestConstants.TEST_PROPERTY_AMCONFIG);
         identityMap = new HashMap();
         try {
             baseDir = getTestBase();
             fmadm = new FederationManager(fmadmURL);
         } catch (Exception e) {
             log(Level.SEVERE, "PolicyCommon", e.getMessage(), null);
             e.printStackTrace();
             throw e;
         }
     }
     
     /**
      * Creates policy xml file.
      */
     public void createPolicyXML(String strGblRB, String strLocRB,
             int policyIdx, String policyFile, String subRealm)
     throws Exception {
         String strResource;
         strGlobalRB = strGblRB;
         strLocalRB = strLocRB;
         ResourceBundle rbg = ResourceBundle.getBundle(strGlobalRB);
         ResourceBundle rbp = ResourceBundle.getBundle(strLocalRB);
         String glbPolIdx = strLocalRB + policyIdx + ".";
         String locPolIdx = glbPolIdx + "policy";
         String strSubRealm = subRealm;
         
         int noOfPolicies = new Integer(rbp.getString(glbPolIdx +
                 "noOfPolicies")).intValue();
         
         FileWriter fstream = new FileWriter(baseDir + policyFile);
         BufferedWriter out = new BufferedWriter(fstream);
         
         writeHeader(out);
         out.write("<Policies>");
         out.write(newline);
         
         for (int i = 0; i < noOfPolicies; i++) {
             
             out.write("<Policy name=\"" + rbp.getString(locPolIdx + i +
                     ".name") + "\" referralPolicy=\"" +
                     rbp.getString(locPolIdx + i + ".referral") +
                     "\" active=\"" + rbp.getString(locPolIdx + i +
                     ".active") + "\">");
             out.write(newline);
             
             int noOfRules = new Integer(rbp.getString(locPolIdx + i +
                     ".noOfRules")).intValue();
             if (noOfRules != 0) {
                 for (int j = 0; j < noOfRules; j++) {
                     out.write("<Rule name=\"" + rbp.getString(locPolIdx + i +
                             ".rule" + j + ".name") + "\">" );
                     out.write(newline);
                     out.write("<ServiceName name=\"" +
                             rbp.getString(locPolIdx + i + ".rule" + j +
                             ".serviceName") + "\"/>" );
                     out.write(newline);
                     strResource = rbp.getString(locPolIdx + i + ".rule" + j +
                             ".resource");
                     out.write("<ResourceName name=\"" +
                             rbg.getString(strResource) + "\"/>" );
                     int noOfActions = new Integer(rbp.getString(locPolIdx + i +
                             ".rule" + j + ".noOfActions")).intValue();
                     if (noOfActions != 0) {
                         List list = new ArrayList();
                         for (int k = 0; k < noOfActions; k++) {
                             list.add(rbp.getString(locPolIdx + i + ".rule" + j +
                                     ".action" + k));
                         }
                         createSubXML(rbg, rbp, out, null, null, null, list);
                     }
                     out.write("</Rule>");
                     out.write(newline);
                 }
             }
             
             int noOfSubjects = new Integer(rbp.getString(locPolIdx + i +
                     ".noOfSubjects")).intValue();
             if (noOfSubjects != 0) {
                 out.write("<Subjects name=\"" + "subjects" +
                         "\" description=\"" + "description\">");
                 out.write(newline);
                 String name;
                 String type;
                 String incType;
                 for (int j = 0; j < noOfSubjects; j++) {
                     name = rbp.getString(locPolIdx + i + ".subject" + j +
                             ".name");
                     type = rbp.getString(locPolIdx + i + ".subject" + j +
                             ".type");
                     incType = rbp.getString(locPolIdx + i + ".subject" + j +
                             ".includeType");
                     int noOfAttributes = new Integer(rbp.getString(locPolIdx +
                             i + ".subject" + j + ".noOfAttributes")).intValue();
                     if (noOfAttributes != 0) {
                         List list = new ArrayList();
                         for (int k = 0; k < noOfAttributes; k++) {
                             list.add(rbp.getString(locPolIdx + i + ".subject" +
                                     j + ".att" + k));
                         }
                         createSubXML(rbg, rbp, out, "Subject", name, type,
                                 incType, list, strSubRealm);
                     } else
                         createSubXML(rbg, rbp, out, "Subject", name, type,
                                 incType, null, strSubRealm);
                 }
                 out.write("</Subjects>");
                 out.write(newline);
             }
             
             int noOfConditions = new Integer(rbp.getString(locPolIdx + i +
                     ".noOfConditions")).intValue();
             if (noOfConditions != 0) {
                 out.write("<Conditions name=\"" + "conditions" +
                         "\" description=\"" + "desc\">");
                 out.write(newline);
                 String name;
                 String type;
                 for (int j = 0; j < noOfConditions; j++) {
                     name = rbp.getString(locPolIdx + i + ".condition" + j +
                             ".name");
                     type = rbp.getString(locPolIdx + i + ".condition" + j +
                             ".type");
                     int noOfAttributes = new Integer(rbp.getString(locPolIdx +
                             i + ".condition" + j +
                             ".noOfAttributes")).intValue();
                     if (noOfAttributes != 0) {
                         List list = new ArrayList();
                         for (int k = 0; k < noOfAttributes; k++) {
                             list.add(rbp.getString(locPolIdx + i +
                                     ".condition" + j + ".att" + k));
                         }
                         createSubXML(rbg, rbp, out, "Condition", name, type,
                                 list);
                     }
                 }
                 out.write("</Conditions>");
                 out.write(newline);
             }
             
             int noOfResponseProviders = new Integer(rbp.getString(locPolIdx +
                     i + ".noOfResponseProviders")).intValue();
             if (noOfResponseProviders != 0) {
                 Map map = new HashMap();
                 out.write("<ResponseProviders name=\"" + "responseproviders" +
                         "\" description=\"" + "desc\">");
                 out.write(newline);
                 String name;
                 String type;
                 for (int j = 0; j < noOfResponseProviders; j++) {
                     name = rbp.getString(locPolIdx + i + ".responseprovider" +
                             j + ".name");
                     type = rbp.getString(locPolIdx + i + ".responseprovider" +
                             j + ".type");
                     int noOfStatic = new Integer(rbp.getString(locPolIdx + i +
                             ".responseprovider" + j +
                             ".noOfStatic")).intValue();
                     if (noOfStatic != 0) {
                         List list = new ArrayList();
                         String strStaticAttribute = rbp.getString(locPolIdx +
                                 i + ".responseprovider" + j +
                                 ".staticAttributeName");
                         for (int k = 0; k < noOfStatic; k++) {
                             list.add(rbp.getString(locPolIdx + i +
                                     ".responseprovider" + j + ".static" + k));
                         }
                         map.put(strStaticAttribute, list);
                     }
                     int noOfDynamic = new Integer(rbp.getString(locPolIdx + i +
                             ".responseprovider" + j +
                             ".noOfDynamic")).intValue();
                     if (noOfDynamic != 0) {
                         List list = new ArrayList();
                         List dynSetlist = new ArrayList();
                         String strDynamicAttribute = rbp.getString(locPolIdx +
                                 i + ".responseprovider" + j +
                                 ".dynamicAttributeName");
                         for (int k = 0; k < noOfDynamic; k++) {
                             String attrValue = rbp.getString(locPolIdx + i +
                                     ".responseprovider" + j + ".dynamic" + k);
                             list.add(attrValue);
                             dynSetlist.add("sun-am-policy-dynamic-response-" +
                                     "attributes" +  "=" + attrValue);
                         }
                         map.put(strDynamicAttribute, list);
                         modifyDynamicRespAttribute(strSubRealm, dynSetlist,
                                 "add");
                         Thread.sleep(50000);
                         createRPXML(out, "ResponseProvider", name, type, map);
                     }
                 }
                 out.write("</ResponseProviders>");
                 out.write(newline);
             }
             out.write("</Policy>");
             out.write(newline);
         }
         out.write("</Policies>");
         out.write(newline);
         out.close();
     }
     
     /**
      * Creates part of policy xml related to atttributes. Does not take
      * Subject includeType as part of input parameters.
      */
     public void createSubXML(ResourceBundle rbg, ResourceBundle rbp,
             BufferedWriter out, String nameType, String name, String type,
             List list)
     throws Exception {
         createSubXML(rbg, rbp, out, nameType, name, type, null, list, null);
     }
     
     /**
      * Creates part of policy xml related to atttributes.
      */
     public void createSubXML(ResourceBundle rbg, ResourceBundle rbp,
             BufferedWriter out, String nameType, String name, String type,
             String includeType, List list, String subRealm)
     throws Exception {
         
         if (nameType != null) {
             if (includeType != null)
                 out.write("<" + nameType + " name=\"" + name + "\"" +
                         " type=\"" + type + "\" includeType=\"" +
                         includeType + "\">");
             else
                 out.write("<" + nameType + " name=\"" + name + "\"" +
                         " type=\"" + type + "\">");
         }
         out.write(newline);
         if (list != null) {
             if (list.size() > 0) {
                 int iIdx;
                 String key;
                 String value;
                 String subType;
                 String subName;
                 String idPrefix = null;
                 String idSuffix = null;
                 String subRealmName;
                 String subRealmPrefix = null;
                 String subRealmSuffix = null;
                 String uuid = null;
                 for (int i = 0; i < list.size(); i++) {
                     out.write("<AttributeValuePair>");
                     out.write(newline);
                     iIdx = ((String)list.get(i)).indexOf("=");
                     key = ((String)list.get(i)).substring(0, iIdx);
                     value = ((String)list.get(i)).substring(iIdx + 1,
                             ((String)list.get(i)).length());
                     
                     if (includeType != null) {
                         subType = rbp.getString(value + ".type");
                         subName = rbp.getString(value + ".name");
                         idPrefix = rbg.getString(strGlobalRB + ".uuid.prefix."
                                 + type + "." + subType);
                         idSuffix = rbg.getString(strGlobalRB + ".uuid.suffix."
                                 + type + "." + subType);
                         subRealmPrefix = rbg.getString(strGlobalRB +
                                 ".uuid.prefix." + "subRealm");
                         subRealmSuffix = rbg.getString(strGlobalRB +
                                 ".uuid.suffix." + "subRealm");
                         subRealmName = subRealm;
                         if (subRealmName.equals(TestCommon.realm)) {
                             if (idSuffix.equals(null) || idSuffix.equals("")) {
                                 uuid = idPrefix + "=" + subName + "," + basedn;
                             } else {
                                 uuid = idPrefix + "=" + subName + ","
                                         + idSuffix + "," + basedn;
                             }
                             if ((idPrefix.equals(null) || idPrefix.equals(""))
                             && (idSuffix.equals(null) ||
                                     idSuffix.equals(""))) {
                                 if (subType.equals("Organization")){
                                     uuid = basedn;
                                 }
                             }
                         } else {
                             if (type.equals("AMIdentitySubject")){
                                 if (idSuffix.equals(null) ||
                                         idSuffix.equals("")) {
                                     uuid = idPrefix + "=" + subName
                                             + "," + basedn;
                                 } else {
                                     uuid = idPrefix + "=" + subName + "," +
                                             idSuffix + "," + subRealmPrefix +
                                             "=" + subRealmName + "," +
                                             subRealmSuffix + "," + basedn;
                                 }
                             } else {
                                 if (idSuffix.equals(null) ||
                                         idSuffix.equals("")) {
                                     uuid = idPrefix + "=" + subName
                                             + "," + basedn;
                                 } else {
                                     uuid = idPrefix + "=" + subName + ","
                                             + idSuffix + "," + basedn;
                                 }
                             }
                         }
                         out.write("<Attribute name=\"" + key + "\"/><Value>" +
                                 uuid + "</Value>");
                     } else
                         out.write("<Attribute name=\"" + key + "\"/><Value>" +
                                 value + "</Value>");
                     out.write(newline);
                     out.write("</AttributeValuePair>");
                     out.write(newline);
                 }
             }
         }
         if (nameType != null) {
             out.write("</" + nameType + ">");
             out.write(newline);
         }
     }
     
     /**
      * Creates part of policy xml related to atttributes for ResponseProviders.
      */
     public void createRPXML(BufferedWriter out, String nameType, String name,
             String type, Map map)
     throws Exception {
         if (nameType != null) {
             out.write("<" + nameType + " name=\"" + name + "\"" + " type=\"" +
                     type + "\">");
             out.write(newline);
         }
         if (map != null) {
             if (map.size() > 0) {
                 int iIdx;
                 String key;
                 List value;
                 Set s = map.keySet();
                 Iterator it = s.iterator();
                 while (it.hasNext()) {
                     key = (String)it.next();
                     value = (List)map.get(key);
                     out.write("<AttributeValuePair>");
                     out.write(newline);
                     out.write("<Attribute name=\"" + key + "\"/>");
                     out.write(newline);
                     for (int i = 0; i < value.size(); i++) {
                         out.write("<Value>" + (String)value.get(i) +
                                 "</Value>");
                         out.write(newline);
                     }
                     out.write("</AttributeValuePair>");
                     out.write(newline);
                 }
             }
         }
         if (nameType != null) {
             out.write("</" + nameType + ">");
             out.write(newline);
         }
     }
     
     /**
      * Creates identities (users, roles and groups) required by the policy
      * definition and policy evaluation.
      */
     public void createIdentities(String strLocRB, int glbPolIdx,
             String strLocRealm)
     throws Exception {
         try {
             ResourceBundle rb = ResourceBundle.getBundle(strLocRB);
             String strPolIdx = strLocRB + glbPolIdx;
             String realm = strLocRealm;
             int noOfIdentities = new Integer(rb.getString(strPolIdx +
                     ".noOfIdentities")).intValue();
             for (int i = 0; i < noOfIdentities; i++) {
                 String name = rb.getString(strPolIdx + ".identity" + i +
                         ".name");
                 String type = rb.getString(strPolIdx + ".identity" + i +
                         ".type");
                 if (type.equals("Organization"))
                     continue;
                 String strAttList = rb.getString(strPolIdx + ".identity" + i +
                         ".attributes");
                 List list = null;
                 
                 if (!(strAttList.equals("")))
                     list = getAttributeList(strAttList, ",");
                 webClient = new WebClient();
                 consoleLogin(webClient, loginURL, adminUser, adminPassword);
                 if (list != null){
                     HtmlPage IdentityCheckPage ;
                     IdentityCheckPage = fmadm.createIdentity(webClient, realm,
                             name, type, list);
                     log(Level.FINER, "createIdentity", newline +
                             IdentityCheckPage.asXml(), null);
                 } else{
                     HtmlPage IdentityCheckPage ;
                     IdentityCheckPage =fmadm.createIdentity(webClient, realm,
                             name, type, null);
                     log(Level.FINER, "createIdentity", newline +
                             IdentityCheckPage.asXml(), null);
                 }
                 String isMemberOf = rb.getString(strPolIdx + ".identity" + i +
                         ".isMemberOf");
                 if (isMemberOf.equals("yes")) {
                     String memberList = rb.getString(strPolIdx + ".identity" +
                             i + ".memberOf");
                     List lstMembers = getAttributeList(memberList, ",");
                     String strIDIdx;
                     String memberType;
                     String memberName;
                     for (int j = 0; j < lstMembers.size(); j++) {
                         strIDIdx = (String)lstMembers.get(j);
                         memberType = rb.getString(strIDIdx + ".type");
                         memberName = rb.getString(strIDIdx + ".name");
                         fmadm.addMember(webClient, realm, name, type,
                                 memberName, memberType);
                     }
                 }
             }
         } catch(Exception e) {
             log(Level.SEVERE, "createIdentities", e.getMessage(), null);
             e.printStackTrace();
             throw e;
         } finally {
             consoleLogout(webClient, logoutURL);
         }
     }
     
     /**
      * Create actual policy in the system.
      */
     public void createPolicy(String fileName, String strLocRealm)
     throws Exception {
         try{
             webClient = new WebClient();
             consoleLogin(webClient, loginURL, adminUser, adminPassword);
             String absFileName = baseDir + fileName;
             String realm = strLocRealm;
             String policyXML;
             if (fileName != null) {
                 StringBuffer contents = new StringBuffer();
                 BufferedReader input = new BufferedReader
                         (new FileReader(absFileName));
                 String line = null;
                 while ((line = input.readLine()) != null) {
                     contents.append(line + "\n");
                 }
                 if (input != null)
                     input.close();
                 policyXML = contents.toString();
                 log(logLevel, "createPolicy", newline + policyXML);
                 HtmlPage policyCheckPage ;
                 policyCheckPage = fmadm.createPolicies(webClient, realm,
                         policyXML);
                 log(Level.FINER, "createPolicy", newline +
                         policyCheckPage.asXml(), null);
             }
         } catch(Exception e) {
             log(Level.SEVERE, "createPolicy", e.getMessage(), null);
             e.printStackTrace();
             throw e;
         } finally {
             consoleLogout(webClient, logoutURL);
         }
     }
     
     /**
      * Deletes all the identities.
      */
     public void deleteIdentities(String strLocRB, int gPolIdx,
             String strLocRealm)
     throws Exception {
         try {
             ResourceBundle rb = ResourceBundle.getBundle(strLocRB);
             String glbPolIdx = strLocRB + gPolIdx;
             String realm = strLocRealm;
             List list = new ArrayList();
             webClient = new WebClient();
             consoleLogin(webClient, loginURL, adminUser, adminPassword);
             String type;
             String name;
             int noOfIdentities = new Integer(rb.getString(glbPolIdx +
                     ".noOfIdentities")).intValue();
             for (int i = 0; i < noOfIdentities; i++) {
                 type = rb.getString(glbPolIdx + ".identity" + i + ".type");
                 name = rb.getString(glbPolIdx + ".identity" + i + ".name");
                 list.clear();
                 list.add(name);
                 fmadm.deleteIdentities(webClient, realm, list, type);
             }
         } catch(Exception e) {
             log(Level.SEVERE, "deleteIdentities", e.getMessage(), null);
             e.printStackTrace();
             throw e;
         } finally {
             consoleLogout(webClient, logoutURL);
         }
     }
     
     /**
      * Deletes the policies.
      */
     public void deletePolicies(String strLocRB, int gPolIdx, String strLocRealm)
     throws Exception {
         try {
             ResourceBundle rb = ResourceBundle.getBundle(strLocRB);
             String glbPolIdx = strLocRB + gPolIdx;
             String locPolIdx = glbPolIdx + ".policy";
             String realm = strLocRealm;
             List list = new ArrayList();
             webClient = new WebClient();
             consoleLogin(webClient, loginURL, adminUser, adminPassword);
             String name;
             int noOfPolicies = new Integer(rb.getString(glbPolIdx +
                     ".noOfPolicies")).intValue();
             for (int i = 0; i < noOfPolicies; i++) {
                 name = rb.getString(locPolIdx + i + ".name");
                 list.add(name);
             }
             fmadm.deletePolicies(webClient, realm, list);
         } catch(Exception e) {
             log(Level.SEVERE, "deletePolicies", e.getMessage(), null);
             e.printStackTrace();
             throw e;
         } finally {
             consoleLogout(webClient, logoutURL);
         }
     }
     
     /**
      * Deletes the referral policies.
      */
     public void deleteReferralPolicies(String strLocRB, String strRefRB,
             int gPolIdx)
     throws Exception {
         try {
             ResourceBundle rb = ResourceBundle.getBundle(strRefRB);
             String glbPolIdx = strLocRB + gPolIdx;
             String locPolIdx = glbPolIdx + ".refpolicy";
             List list = new ArrayList();
             webClient = new WebClient();
             consoleLogin(webClient, loginURL, adminUser, adminPassword);
             String name;
             int noOfRefPolicies = new Integer(rb.getString(glbPolIdx +
                     ".noOfRefPolicies")).intValue();
             String strReferringOrg = rb.getString(strLocRB + gPolIdx +
                     ".referringOrg");
             for (int i = 0; i < noOfRefPolicies; i++) {
                 name = rb.getString(locPolIdx + i + ".name");
                 list.add(name);
             }
             HtmlPage refPolicyCheckPage ;
             refPolicyCheckPage = fmadm.deletePolicies(webClient,
                     strReferringOrg, list);
             log(Level.FINER, "deleteRefPolicy", newline +
                     refPolicyCheckPage.asXml(), null);
         } catch(Exception e) {
             log(Level.SEVERE, "deleteRefPolicies", e.getMessage(), null);
             e.printStackTrace();
             throw e;
         } finally {
             consoleLogout(webClient, logoutURL);
         }
     }
     
     
     /**
      * Creates policy header xml
      */
     public void writeHeader(BufferedWriter out)
     throws Exception {
         out.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
         out.write(newline);
         out.write("<!DOCTYPE Policies");
         out.write(newline);
         out.write("PUBLIC \"-//Sun Java System Access Manager 7.1 2006Q3" +
                 " Admin CLI DTD//EN\"");
         out.write(newline);
         out.write("\"jar://com/sun/identity/policy/policyAdmin.dtd\"");
         out.write(newline);
         out.write(">");
         out.write(newline);
     }
     
     
     /**
      * Evaluates policy using api
      */
     public void evaluatePolicyThroughAPI(String resource, SSOToken userToken,
             String action, Map envMap, String expResult, int idIdx)
     throws Exception {
         try {
             PolicyEvaluator pe =
                     new PolicyEvaluator("iPlanetAMWebAgentService");
             
             Set actions = new HashSet();
             actions.add(action);
             
             boolean pResult = pe.isAllowed(userToken, resource, action, envMap);
             log(logLevel, "evaluatePolicyThroughAPI", "Policy Decision: " +
                     pResult);
             
             PolicyDecision pd = pe.getPolicyDecision(userToken, resource,
                     actions, envMap);
             
             Map respAttrMap = new HashMap();
             respAttrMap = pd.getResponseAttributes();
             log(logLevel, "evaluatePolicyThroughAPI", "Policy Decision XML: " +
                     pd.toXML());
             log(logLevel, "evaluatePolicyThroughAPI",
                     "Policy Response Attributes: " +  respAttrMap);
             boolean expectedResult = new Boolean(expResult).booleanValue();
             assert (pResult == expectedResult);
         } catch (Exception e) {
             log(Level.SEVERE, "evaluatePolicyThroughAPI", e.getMessage(), null);
             e.printStackTrace();
             throw e;
         } finally {
             destroyToken(userToken);
         }
     }
     
     /**
      * Returns the enviornment map used for policy evaluation when evaluation
      * is through api.
      *
      */
     public Map getPolicyEnvParamMap(String strLocRB, int polIdx, int evalIdx)
     throws Exception {
         Map map = null;
         try {
             ResourceBundle rbp = ResourceBundle.getBundle(strLocRB);
             String glbPolIdx = strLocRB + polIdx;
             String locEvalIdx = glbPolIdx + ".evaluation";
             int noOfEnvParams = new Integer(rbp.getString(locEvalIdx +
                     evalIdx + ".noOfEnvParamSet")).intValue();
             if (noOfEnvParams != 0) {
                 map = new HashMap();
                 String type;
                 int noOfVal;
                 for (int i = 0; i < noOfEnvParams; i++) {
                     type =  rbp.getString(locEvalIdx + evalIdx +
                             ".envParamSet" + i + ".type");
                     noOfVal =  new Integer(rbp.getString(locEvalIdx + evalIdx +
                             ".envParamSet" + i + ".noOfValues")).intValue();
                     Set set = null;
                     if (noOfVal != 0) {
                         set = new HashSet();
                         String strVal;
                         for (int j = 0; j < noOfVal; j++) {
                             strVal =  rbp.getString(locEvalIdx + evalIdx +
                                     ".envParamSet" + i + ".val" + j);
                             set.add(strVal);
                         }
                     }
                     map.put(type, set);
                 }
             }
         } catch (Exception e) {
             log(Level.SEVERE, "getPolicyEnvParamMap", e.getMessage(), null);
             e.printStackTrace();
             throw e;
         }
         return (map);
     }
     
     /**
      * sets the requested properties in the sso token
      *
      */
     public void setProperty(String strLocRB, SSOToken userToken, int polIdx,
             int idIdx)
     throws Exception {
         ResourceBundle rbp = ResourceBundle.getBundle(strLocRB);
         String glbPolIdx = strLocRB + polIdx;
         String locEvalIdx = glbPolIdx + ".identity" + idIdx;
         boolean hasSessionAttr = new Boolean(rbp.getString(locEvalIdx +
                 ".hasSessionAttributes")).booleanValue();
         if (hasSessionAttr) {
             int noOfSessionAttr = new Integer(rbp.getString(locEvalIdx +
                     ".noOfSessionAttributes")).intValue();
             String strVal;
             String propName;
             String propValue;
             int j;
             for (int i = 0; i < noOfSessionAttr; i++) {
                 strVal = rbp.getString(locEvalIdx + ".sessionAttribute" + i);
                 j = strVal.indexOf("=");
                 propName = strVal.substring(0, j);
                 propValue = strVal.substring(j + 1, strVal.length());
                 userToken.setProperty(propName, propValue);
             }
         }
     }
     
     /**
      * Gets the index for identity from the identity resource bundle string
      * identifier
      */
     public int getIdentityIndex(String strIdx)
     throws Exception {
         List list = getAttributeList(strIdx, ".");
         int iLen = ((String)list.get(1)).length();
         int idx = new Integer(((String)list.get(1)).
                 substring(iLen - 1, iLen)).intValue();
         return (idx);
     }
     
     /**
      * Create referral policy xml file.
      */
     public void createReferralPolicyXML(String strGlbRB, String strRefRB,
             String strLocRB, int policyIdx, String policyFile)
     throws Exception {
         String strResource;
         String strReferralRB = strRefRB;
         strLocalRB = strLocRB;
         String subRealmPrefix = null;
         String subRealmSuffix = null;
         
         ResourceBundle rbr = ResourceBundle.getBundle(strRefRB);
         ResourceBundle rbg = ResourceBundle.getBundle(strGlbRB);
         String locPolIdx = strLocRB + policyIdx + "." + "refpolicy";
         String refPolIdx = strLocRB + policyIdx + ".";
         
         int noOfRefPolicies = new Integer(rbr.getString(refPolIdx +
                 "noOfRefPolicies")).intValue();
         subRealmPrefix = rbg.getString(strGlbRB + ".uuid.prefix."
                 + "subRealm");
         subRealmSuffix = rbg.getString(strGlbRB + ".uuid.suffix."
                 + "subRealm");
         FileWriter fstream = new FileWriter(baseDir + policyFile);
         BufferedWriter out = new BufferedWriter(fstream);
         
         writeHeader(out);
         out.write("<Policies>");
         out.write(newline);
         
         for (int i = 0; i < noOfRefPolicies; i++) {
             
             out.write("<Policy name=\"" + rbr.getString(locPolIdx + i +
                     ".name") + "\" referralPolicy=\"" +
                     rbr.getString(locPolIdx + i + ".referral") +
                     "\" active=\"" + rbr.getString(locPolIdx + i +
                     ".active") + "\">");
             out.write(newline);
             
             int noOfRules = new Integer(rbr.getString(locPolIdx + i +
                     ".noOfRules")).intValue();
             if (noOfRules != 0) {
                 for (int j = 0; j < noOfRules; j++) {
                     out.write("<Rule name=\"" + rbr.getString(locPolIdx + i +
                             ".rule" + j + ".name") + "\">" );
                     out.write(newline);
                     out.write("<ServiceName name=\"" +
                             rbr.getString(locPolIdx + i + ".rule" + j +
                             ".serviceName") + "\"/>" );
                     out.write(newline);
                     strResource = rbr.getString(locPolIdx + i + ".rule" + j +
                             ".resource");
                     out.write("<ResourceName name=\"" +
                             rbg.getString(strResource) + "\"/>" );
                     out.write("</Rule>");
                     out.write(newline);
                 }
                 
             }
             
             int noOfReferrals = new Integer(rbr.getString(locPolIdx + i +
                     ".noOfReferrals")).intValue();
             if (noOfReferrals != 0) {
                 out.write("<Referrals name=\"" + "Referral" +
                         "\" description=\"" + "desc\">");
                 out.write(newline);
                 String type;
                 String referredOrg;
                 
                 for (int j = 0; j < noOfReferrals; j++) {
                     type = rbr.getString(locPolIdx + i + ".referral" + j +
                             ".type");
                     referredOrg = rbr.getString(locPolIdx + i + ".referral" +
                             j + ".referredOrg");
                     out.write("<Referral name=\"" + rbr.getString(locPolIdx +
                             i + ".referral" + j + ".name") +  "\"" + "  type="
                             + "\"" + type + "\"" + ">" );
                     out.write(newline);
                     out.write("<AttributeValuePair>");
                     out.write(newline);
                     out.write("<Attribute name=" + "\"" + "Values" + "\"" +
                             "/>" );
                     out.write("<Value>");
                     out.write(subRealmPrefix + "=" + referredOrg + "," +
                             subRealmSuffix + "," + basedn);
                     out.write("</Value>");
                     out.write(newline);
                     out.write("</AttributeValuePair>");
                     out.write(newline);
                     out.write("</Referral>");
                     out.write(newline);
                 }
                 out.write("</Referrals>");
                 out.write(newline);
             }
             
             out.write("</Policy>");
             out.write(newline);
         }
         out.write("</Policies>");
         out.write(newline);
         out.close();
     }
     
     /**
      * Creates the realm
      *
      */
     public void createRealm(String realmName)
     throws Exception {
         
         WebClient webClient = new WebClient();
         HtmlPage realmCheckPage;
         List realmStatus = new ArrayList();
         try {
             realmStatus.add("sunOrganizationStatus=Active");
             consoleLogin(webClient, loginURL, adminUser, adminPassword);
             realmCheckPage = fmadm.createRealm(webClient, realmName);
             fmadm.setRealmAttributes(webClient,
                     realmName, "sunIdentityRepositoryService", realmStatus);
             log(logLevel, "createRealm", "Realm:" + realmName);
             log(Level.FINER, "createRealm", newline +
                     realmCheckPage.asXml(), null);
         } catch (Exception e) {
             log(Level.SEVERE, "createRealm", e.getMessage(), null);
             e.printStackTrace();
             throw e;
         } finally {
             consoleLogout(webClient, logoutURL);
         }
     }
     
     /**
      * Deletes the realm
      *
      */
     public void deleteRealm(String realmName)
     throws Exception {
         boolean recursive = false ;
         WebClient webClient = new WebClient();
         try {
             consoleLogin(webClient, loginURL, adminUser, adminPassword);
             fmadm.deleteRealm(webClient, realmName, recursive);
             log(logLevel, "deleteRealms", "Realm:" + realmName);
         } catch (Exception e) {
             log(Level.SEVERE, "deleteRealm", e.getMessage(), null);
             e.printStackTrace();
             throw e;
         } finally {
             consoleLogout(webClient, logoutURL);
         }
     }
     
     /**
      * Enables the dynamic referral to true at the top level org
      *
      */
     public void setDynamicReferral(String value)
     throws Exception {
         try {
             WebClient webClient = new WebClient();
             consoleLogin(webClient, loginURL, adminUser, adminPassword);
             List dnsAliasEnable = new ArrayList();
             dnsAliasEnable.add("sun-am-policy-config-org-alias-" +
                     "mapped-resources-enabled=" + value);
             HtmlPage orgServiceAtt = fmadm.setAttributeDefaults(webClient,
                     "iPlanetAMPolicyConfigService", "global", null,
                     dnsAliasEnable);
             log(logLevel, "setDynamicReferral", "enabled the org parameter" +
                     orgServiceAtt.getWebResponse().getContentAsString());
         } catch (Exception e) {
             log(Level.SEVERE, "setDynamicReferral", e.getMessage(), null);
             e.printStackTrace();
             throw e;
         } finally {
             consoleLogout(webClient, logoutURL);
         }
     }
     
     /**
      * Creates the dynamic referral
      *
      */
     public void createDynamicReferral(String strGlbRB,String strRefRB,
             String strLocRB, int polIdx, String strPeAtOrg)
     throws Exception {
         try {
             String strResource ;
             String dynamicRefResource ;
             List dnsAlias = new ArrayList();
             String locPolIdx = strLocRB + polIdx + "." + "refpolicy";
             String refPolIdx = strLocRB + polIdx + ".";
             ResourceBundle rbr = ResourceBundle.getBundle(strRefRB);
             ResourceBundle rbg = ResourceBundle.getBundle(strGlbRB);
             WebClient webClient = new WebClient();
             consoleLogin(webClient, loginURL, adminUser, adminPassword);
             
             int noOfRefPolicies = new Integer(rbr.getString(refPolIdx +
                     "noOfRefPolicies")).intValue();
             
             for (int i = 0; i < noOfRefPolicies; i++) {
                 
                 int noOfRules = new Integer(rbr.getString(locPolIdx + i +
                         ".noOfRules")).intValue();
                 if (noOfRules != 0) {
                     for (int j = 0; j < noOfRules; j++) {
                         strResource = rbg.getString(rbr.getString(locPolIdx + i
                                 + ".rule" + j + ".resource"));
                         URL dynamicRefResourceUrl = new URL(strResource);
                         String host = dynamicRefResourceUrl.getHost();
                         dynamicRefResource = host ;
                         dnsAlias.add("sunOrganizationAliases=" +
                                 dynamicRefResource);
                     }
                 }
                 HtmlPage realmAtt = fmadm.setRealmAttributes(webClient,
                         strPeAtOrg, "sunIdentityRepositoryService", dnsAlias);
                 log(logLevel, "createDynamicReferral",
                         realmAtt.getWebResponse().getContentAsString());
             }
         } catch (Exception e) {
             log(Level.SEVERE, "setDynamicReferral", e.getMessage(), null);
             e.printStackTrace();
             throw e;
         } finally {
             consoleLogout(webClient, logoutURL);
         }
     }
     
     /**
      * Enables the dynamic resp attributes at the org specified
      *
      */
     public void modifyDynamicRespAttribute(String strRealm, List dynRespList,
             String serviceAction)
     throws Exception {
         try {
             WebClient webClient = new WebClient();
             consoleLogin(webClient, loginURL, adminUser, adminPassword);
             
             String action = serviceAction;
             if (action.equals("add")) {
                 
                 HtmlPage policyServiceAtt = fmadm.addServiceAttributes
                         (webClient, strRealm, "iPlanetAMPolicyConfigService",
                         dynRespList);
                 log(logLevel, "", "setDynamicRespAttribute" +
                         policyServiceAtt.getWebResponse().getContentAsString());
                 
             } else if(action.equals("set")) {
                 
                 HtmlPage policyServiceAtt = fmadm.setServiceAttributes
                         (webClient, strRealm, "iPlanetAMPolicyConfigService",
                         dynRespList);
                 log(logLevel, "modifyDynamicRespAttribute",
                         "setDynamicRespAttribute" +
                         policyServiceAtt.getWebResponse().getContentAsString());
                 
             } else if (action.equals("remove")) {
                 
                 HtmlPage policyServiceAtt = fmadm.removeServiceAttributes
                         (webClient, strRealm, "iPlanetAMPolicyConfigService",
                         dynRespList);
                 log(logLevel, "modifyDynamicRespAttribute",
                         "setDynamicRespAttribute" +
                         policyServiceAtt.getWebResponse().getContentAsString());
             }
         } catch (Exception e) {
             log(Level.SEVERE, "setDynamicRespAttribute", e.getMessage(), null);
             e.printStackTrace();
             throw e;
         } finally {
             consoleLogout(webClient, logoutURL);
         }
     }
     
     /**
      * delete dynamic attributes from the service.
      *
      */
     public void deleteDynamicAttr(String strLocRB, int policyIdx,
             String subRealm)
     throws Exception {
         strLocalRB = strLocRB;
         ResourceBundle rbp = ResourceBundle.getBundle(strLocalRB);
         String glbPolIdx = strLocalRB + policyIdx + ".";
         String locPolIdx = glbPolIdx + "policy";
         String strSubRealm = subRealm;
         
         int noOfPolicies = new Integer(rbp.getString(glbPolIdx +
                 "noOfPolicies")).intValue();
         
         for (int i = 0; i < noOfPolicies; i++) {
             
             int noOfResponseProviders = new Integer(rbp.getString(locPolIdx +
                     i + ".noOfResponseProviders")).intValue();
             if (noOfResponseProviders != 0) {
                 Map map = new HashMap();
                 String name;
                 String type;
                 for (int j = 0; j < noOfResponseProviders; j++) {
                     name = rbp.getString(locPolIdx + i + ".responseprovider" +
                             j + ".name");
                     type = rbp.getString(locPolIdx + i + ".responseprovider" +
                             j + ".type");
                     int noOfDynamic = new Integer(rbp.getString(locPolIdx + i +
                             ".responseprovider" + j +
                             ".noOfDynamic")).intValue();
                     if (noOfDynamic != 0) {
 //                        List list = new ArrayList();
                         List dynSetlist = new ArrayList();
                         String strDynamicAttribute = rbp.getString(locPolIdx +
                                 i + ".responseprovider" + j +
                                 ".dynamicAttributeName");
                         for (int k = 0; k < noOfDynamic; k++) {
                             String attrValue = rbp.getString(locPolIdx + i +
                                     ".responseprovider" + j + ".dynamic" + k);
                             //                          list.add(attrValue);
                             dynSetlist.add("sun-am-policy-dynamic-response-" +
                                     "attributes" +  "=" + attrValue);
                         }
                         modifyDynamicRespAttribute(strSubRealm, dynSetlist,
                                 "remove");
                     }
                 }
             }
         }
     }
     
     /**
      * Create multiple Identites from the map
      *
      */
     public void createIds( Map testIdentityMap)
     throws Exception  {
         log(logLevel, "createIds" ,"Starting to create Identities");
         String logoutUrl = protocol + ":" + "//" + host + ":" + port
                     + uri + "/UI/Logout";
         WebClient webClient = new WebClient();
         try {
             int i ;
             int j = 0 ;
             identityMap = testIdentityMap;
             Integer testCount = (Integer)identityMap.get("testcount");
             String url = protocol + ":" + "//" + host + ":" + port +
                     uri ;
             //String logoutUrl = protocol + ":" + "//" + host + ":" + port
                   //  + uri + "/UI/Logout";
             //WebClient webClient = new WebClient();
             consoleLogin(webClient, url, adminUser, adminPassword);
             HtmlPage userCheckPage;
             log(logLevel, "createIds", "IdentityMap" + identityMap);
             log(logLevel, "createIds", "test count" + testCount);
             
            /*
             * get the password/userid/realm keys (no need to check for avai
             * lability)
             * 1) Loop thru the attr and add to the list
             * 2) Now check for the getidentity and verfiy for the user
             *    availability
             * 3) Create the user using the above params
             * 4) Now check for containsKey(MemberofRole). if key present,
             *    getIdentity (role)
             * 5) If not present, create the role and add this
                  member to the role
             * 6) Repeat the same for group
             * 7) Flrole to be added
             */
             
             for (i = 0; i < testCount; i++){
                 String uName = (String)identityMap.get("test" + i +
                         ".Identity." + "username");
                 String psWord = (String)identityMap.get("test" + i +
                         ".Identity." + "password");
                 String type = (String)identityMap.get("test" + i +
                         ".Identity.type");
                 Integer attributeCount = new Integer((String)identityMap.get
                         ("test" + i + ".Identity.attributecount"));
                 String rlmName = (String)identityMap.get("test" + i +
                         ".Identity." +  "realmname");
                 List attrList = new ArrayList();
                 attrList.add("userpassword=" + psWord);
                 log(logLevel, "createIds" , "attr count" + attributeCount);
                 for (j = 0; j < attributeCount; j++){
                     String attrName = (String)identityMap.get("test" + i +
                             ".Identity" + "." + "attribute" + j +".name");
                     String attrValue = (String)identityMap.get("test" + i +
                             ".Identity" + "." + "attribute" + j + ".value");
                     attrList.add(attrName + "=" + attrValue);
                 }
                 
                 //now verify the user and add the user if not present
                 userCheckPage = fmadm.listIdentities(webClient, rlmName, uName,
                         type);
                 String xmlString = userCheckPage.asXml();
                 if (xmlString.contains(uName)) {
                     log(logLevel, "createIds", "User already exists:"
                             + uName);
                 } else {
                     log(logLevel, "createIds", "User is not exists:"
                             + uName);
                     userCheckPage = fmadm.createIdentity(webClient, rlmName,
                             uName, type, attrList);
                     userCheckPage.cleanUp();
                     userCheckPage = fmadm.listIdentities(webClient, rlmName,
                             uName, type);
                     xmlString = userCheckPage.asXml();
                     if (xmlString.contains(uName)) {
                         log(logLevel, "createIds", "User " +
                                 "is created successfully:" + uName);
                     } else {
                         log(logLevel, "createIds", "User " +
                                 "is not created successfully:" + uName);
                         assert false;
                     }
                 }
                 
                /*
                 * now verify if the user is memberof group and create group and
                 * add the user
                 */
                 if (identityMap.containsKey("test" + i + ".Identity" +
                         ".memberOfGroup")) {
                     String grpName = (String)identityMap.get("test" + i +
                             ".Identity" + "." + "memberOfGroup");
                     HtmlPage groupCheckPage = fmadm.listIdentities(webClient,
                             rlmName, grpName, "Group");
                     if (groupCheckPage.asXml().contains(grpName)) {
                         log(logLevel, "createIds", "group exists:"
                                 + "add the member" + grpName);
                         fmadm.addMember(webClient, rlmName, uName,
                                 type, grpName, "GROUP");
                     } else {
                         List grpAttrList = new ArrayList();
                         log(logLevel, "createIds", "group is not found:"
                                 + "creating the group" + grpName);
                         groupCheckPage = fmadm.createIdentity(webClient,
                                 rlmName, grpName, "Group", grpAttrList);
                         groupCheckPage = fmadm.listIdentities(webClient,
                                 rlmName, grpName, "Group");
                         if (groupCheckPage.asXml().contains(grpName)) {
                             log(logLevel, "createIds", "group is " +
                                     "created successfully." +
                                     "Now add the member" + grpName);
                             fmadm.addMember(webClient, rlmName, uName,
                                     type, grpName, "GROUP");
                             log(logLevel, "createIds", uName +
                                     "member is  added successfully" + grpName);
                         }
                     }
                 }
                 
                 /*
                  * now verify if the user is memberof role and create role and
                  * add the user
                  */
                 if (identityMap.containsKey("test" + i + ".Identity." +
                         "memberOfRole")){
                     String roleName = (String)identityMap.get("test" + i +
                             ".Identity." + "memberOfRole");
                     HtmlPage roleCheckPage = fmadm.listIdentities(webClient,
                             rlmName, roleName, "Role");
                     if (roleCheckPage.asXml().contains(roleName)) {
                         log(logLevel, "createIds", "Role already exists"
                                 + roleName);
                         fmadm.addMember(webClient, rlmName, uName, type,
                                 roleName, "Role");
                         log(logLevel, "createIds", "added member to " +
                                 "the role" + roleName);
                     } else {
                         List roleAttrList = new ArrayList();
                         log(logLevel,"createIds","Role does not" +
                                 " exists. :Creating the role"  + roleName);
                         roleCheckPage = fmadm.createIdentity(webClient, rlmName,
                                 roleName, "Role", roleAttrList);
                         roleCheckPage = fmadm.listIdentities(webClient,
                                 rlmName, roleName, "Role");
                         if (roleCheckPage.asXml().contains(roleName)) {
                             log(logLevel, "createIds",
                                     "Role Created successfully." +
                                     "Now adding the member" + roleName);
                             fmadm.addMember(webClient, rlmName, uName,
                                     type, roleName, "Role");
                             log(logLevel, "createIds", uName +
                                     "member is  added successfully" + roleName);
                         }
                     }
                 }
             }
         } catch(Exception e) {
             log(logLevel, "createIds", e.getMessage());
             e.printStackTrace();
         } finally {
             consoleLogout(webClient, logoutUrl);
         }
     }
     
     /**
      * Delete multiple Identites from the map
      *
      */
     public void deleteIds(Map testIdentityMap)
     throws Exception{
         identityMap = testIdentityMap;
         String logoutUrl = protocol + ":" + "//" + host + ":" + port
                     + uri + "/UI/Logout";
         WebClient webClient = new WebClient();
         log(logLevel, "deleteIds" ,"Starting deleteIds");
         try{
             int i ;
             int j = 0;
             Integer testCount = (Integer)identityMap.get("testcount");
             String url = protocol + ":" + "//" + host + ":" +
                     port + uri ;
             consoleLogin(webClient, url, adminUser, adminPassword);
             HtmlPage htmlpage;
             List idList = new ArrayList();
             
            /*
             * Loop thru the map and delete the users and their associated
             * groups
             */
             for (i = 0; i < testCount; i++) {
                 String uName = (String)identityMap.get("test" + i +
                         ".Identity.username");
                 String type = (String)identityMap.get("test" + i
                         + ".Identity.type");
                 String rlmName = (String)identityMap.get("test" + i +
                         ".Identity." + "realmname");
                 idList.add(uName);
                 for (Iterator itr = idList.iterator(); itr.hasNext();) {
                     log(logLevel, "deleteIds", (String) itr.next());
                 }
                 
                 // now verify the user and delete the user if already present
                 fmadm.deleteIdentities(webClient, rlmName, idList, type);
                 htmlpage = fmadm.listIdentities(webClient,
                         rlmName, uName, "USER");
                 String xmlString = htmlpage.asXml();
                 if (xmlString.contains(uName)) {
                     log(logLevel, "deleteIds", "User is not deleted:" +
                             uName);
                     assert false;
                 } else{
                     log(logLevel, "deleteIds",
                             "User is deleted properly:" + uName);
                 }
                 idList.clear();
                 
                 // verify the user and delete the group if already present
                 if (identityMap.containsKey("test" + i + ".Identity" +
                         ".memberOfGroup")){
                     String grpName = (String)identityMap.get("test" + i +
                             ".Identity" + "." + "memberOfGroup");
                     HtmlPage groupCheckPage = fmadm.listIdentities(webClient,
                             rlmName, grpName, "Group");
                     log(logLevel,"deleteIds" , groupCheckPage.asXml());
                     if (groupCheckPage.asXml().contains(grpName)) {
                         log(logLevel, "deleteIds", "Group Needs to " +
                                 "be deleted" + ":" + grpName);
                         idList.add(grpName);
                         fmadm.deleteIdentities(webClient, rlmName, idList,
                                 "Group");
                         groupCheckPage = fmadm.listIdentities(webClient,
                                 rlmName, grpName,"Group");
                         if (groupCheckPage.asXml().contains(grpName)) {
                             log(logLevel, "deleteIds", "Group delete:"
                                     + "is not success" + grpName);
                             assert false;
                         } else {
                             log(logLevel, "deleteIds", "Group is already" +
                                     "deleted:" + grpName);
                         }
                     }
                 }
                 idList.clear();
                 if (identityMap.containsKey("test" + i + ".Identity" + "." +
                         "memberOfRole")){
                     String roleName = (String)identityMap.get("test" + i +
                             ".Identity" + "." + "memberOfRole");
                     HtmlPage roleCheckPage = fmadm.listIdentities(webClient,
                             rlmName, roleName, "Role");
                     if (roleCheckPage.asXml().contains(roleName)) {
                         log(logLevel, "deleteIds", "Role need to be" +
                                 " deleted:" + roleName);
                         idList.add(roleName);
                         fmadm.deleteIdentities(webClient, rlmName, idList,
                                 "Role");
                         roleCheckPage = fmadm.listIdentities(webClient,
                                 rlmName, roleName, "Role");
                         if (roleCheckPage.asXml().contains(roleName)) {
                             log(logLevel, "deleteIds", "Role is "+
                                     "not deleted:" + roleName);
                             assert false;
                         }
                     } else {
                         log(logLevel, "deleteIds", "Role" +
                                 " is already deleted:" + roleName);
                     }
                 }
                idList.clear();
             }
         } catch(Exception e){
             e.getMessage();
             e.printStackTrace();
         } finally {
             consoleLogout(webClient, logoutUrl);
         }
     }
     
     /**
      * creates policy from a given policy name (with out xml extension)
      *
      */
     public void createPolicy(String sceName)
     throws Exception {
         String scenarioname = sceName;
         String logoutUrl = protocol + ":" + "//" + host + ":" + port
                     + uri + "/UI/Logout";
         WebClient webClient = new WebClient(); 
         try{
             String url = protocol + ":" + "//" + host + ":"
                     + port + uri ;
             log(logLevel, "createPolicy - URL", url);
             String fileSeparator = System.getProperty("file.separator");
             consoleLogin(webClient, url, adminUser, adminPassword);
             String createPolicyXMLFile = scenarioname + ".xml";
             log(logLevel, "createPolicy", createPolicyXMLFile);
             String absFileName = getBaseDir() + fileSeparator + "xml" +
                     fileSeparator + "policy" + fileSeparator +
                     createPolicyXMLFile;
             log(logLevel, "createPolicy", absFileName);
             String policyXML = null;
             if (absFileName != null) {
                 StringBuffer contents = new StringBuffer();
                 BufferedReader input = new BufferedReader
                         (new FileReader(absFileName));
                 String line = null;
                 while ((line = input.readLine()) != null) {
                     contents.append(line + "\n");
                 }
                 if (input != null)
                     input.close();
                 policyXML = contents.toString();
             }
             log(logLevel, "createPolicy", absFileName);
             HtmlPage policyCheckPage = fmadm.createPolicies(webClient,
                     realm, policyXML);
             String xmlString = policyCheckPage.asXml();
             if (xmlString.contains("Policies are created")) {
                 log(logLevel, "createPolicy", "create policy is" +
                         "success" + absFileName);
                 log(logLevel, "createPolicy", xmlString);
             } else {
                 log(logLevel, "createPolicy",
                         "not success" + absFileName);
                 log(logLevel, "createPolicy", xmlString);
                 assert false;
             }
         } catch (Exception e) {
             log(Level.SEVERE, "createPolicy", e.getMessage(), null);
             e.printStackTrace();
             throw e;
         } finally {
             consoleLogout(webClient, logoutUrl);
         }
     }
     
     /**
      * deletes policies from a given policy name and policyCount
      *
      */
     public void deletePolicies(String poliName, Integer poliCount)
     throws Exception {
         String policyName = poliName;
         Integer pCount = poliCount;
         String logoutUrl = protocol + ":" + "//" + host + ":" + port
                     + uri + "/UI/Logout";
         WebClient webClient = new WebClient();
         try{
             List pList = new ArrayList();
             for (int p = 0; p < pCount; p++){
                 String pName = policyName + p;
                 pList.add(pName);
             }
             String url = protocol + ":" + "//" + host + ":"
                     + port + uri ;
             consoleLogin(webClient, url, adminUser, adminPassword);
             HtmlPage policyCheckPage  = fmadm.deletePolicies(webClient,
                     realm, pList);
             String xmlString = policyCheckPage.asXml();
             if (!xmlString.contains("Policies are deleted")) {
                 log(logLevel, "deletePolicy", "Delete Policy" +
                         "is not success" + policyName );
                 log(logLevel, "deletePolicies", xmlString);
                 assert false;
             } else {
                 log(logLevel, "deletePolicies", "is success" +
                         policyName);
                 log(logLevel, "deletePolicies", xmlString);
             }
             log(logLevel, "deletePolicies", "cleaned up the policies");
         } catch (Exception e) {
             log(Level.SEVERE, "deletePolicies", e.getMessage(), null);
             e.printStackTrace();
             throw e;
         } finally {
             consoleLogout(webClient, logoutUrl);
         }
     }
     
     /**
      * sets the requested properties in the sso token
      *
      */
     public void setProperty(SSOToken userToken, Map testIdentityMap, int i)
     throws Exception {
         int j ;
         try {
             Integer spCount = new Integer((String)testIdentityMap.get
                     ("test" + i + ".Identity.spcount"));
             for (j = 0; j < spCount; j++){
                 String spName = (String)testIdentityMap.get("test" + i +
                         ".Identity" + "." + "sp" + j +".name");
                 String spValue = (String)testIdentityMap.get("test" + i +
                         ".Identity" + "." + "sp" + j + ".value");
                 String userName =  (String)testIdentityMap.get("test" + i +
                         ".Identity"  + ".username");
                 userToken.setProperty(spName, spValue);
                 String spGetValue = userToken.getProperty(spName);
                 if (spGetValue == spValue) {
                     log(logLevel, "setProperty", "Session Property is set"
                             + "Correctly:" + spGetValue);
                 } else {
                     log(logLevel, "setProperty", "Session Property is not"
                             + " set Correctly:" + spGetValue);
                 }
             }
         } catch (SSOException ex) {
             ex.printStackTrace();
         }
     }
 }
 
