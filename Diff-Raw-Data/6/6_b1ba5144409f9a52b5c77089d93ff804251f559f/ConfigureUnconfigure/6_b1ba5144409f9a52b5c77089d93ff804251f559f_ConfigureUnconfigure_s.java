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
 * $Id: ConfigureUnconfigure.java,v 1.1 2007-08-17 17:16:00 rmisra Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.agents;
 
 import com.sun.identity.idm.IdType;
 import com.sun.identity.qatest.common.IdmCommon;
 import com.sun.identity.qatest.common.TestCommon;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import org.testng.annotations.AfterSuite;
 import org.testng.annotations.BeforeSuite;
 
 public class ConfigureUnconfigure extends TestCommon {
     
     private ResourceBundle rbg;
     private IdmCommon idmc;
     private String agentId;
     private String agentPassword;
     private String strGblRB = "agentsGlobal";
 
     public ConfigureUnconfigure() 
     throws Exception{
         super("ConfigureUnconfigure");
         rbg = ResourceBundle.getBundle(strGblRB);
         agentId = rbg.getString(strGblRB + ".agentId");
         agentPassword = rbg.getString(strGblRB + ".agentPassword");
         idmc = new IdmCommon();
     }
     
     @BeforeSuite(groups={"ds_ds, ds_ds_sec, ff_ds, ff_ds_sec"})
     public void createAgentProfile() 
     throws Exception {
         entering("createAgentProfile", null);
         try {
             Map map = new HashMap();
             Set set = new HashSet();
             set.add(agentPassword);
             map.put("userpassword", set);
             set = new HashSet();
             set.add("Active");
             map.put("sunIdentityServerDeviceStatus", set);
             admintoken = getToken(adminUser, adminPassword, basedn);
             idmc.createIdentity(admintoken, realm, IdType.AGENT, agentId,  map);
         } catch (Exception e) {
             log(Level.SEVERE, "createAgentProfile", e.getMessage());
             e.printStackTrace();
             throw e;
         } finally {
             destroyToken(admintoken);
         }
         exiting("createAgentProfile");
     }
 
     @AfterSuite(groups={"ds_ds, ds_ds_sec, ff_ds, ff_ds_sec"})
     public void deleteAgentProfile()
     throws Exception {
         entering("deleteAgentProfile", null);
         try {
             admintoken = getToken(adminUser, adminPassword, basedn);
             idmc.deleteIdentity(admintoken, realm, IdType.AGENT, agentId);
         } catch (Exception e) {
             log(Level.SEVERE, "deleteAgentProfile", e.getMessage());
             e.printStackTrace();
             throw e;
         } finally {
             destroyToken(admintoken);
         }
         exiting("deleteAgentProfile");
     }
 }
