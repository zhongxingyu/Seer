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
 * $Id: SAML2Test.java,v 1.1 2006-10-30 23:18:10 qcheng Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.federation.cli;
 
 import com.sun.identity.cli.CLIConstants;
 import com.sun.identity.cli.CLIException;
 import com.sun.identity.cli.CLIRequest;
 import com.sun.identity.cli.CommandManager;
 import com.sun.identity.cli.DevNullOutputWriter;
 import com.sun.identity.cot.CircleOfTrustManager;
 import com.sun.identity.cot.CircleOfTrustDescriptor;
 import com.sun.identity.cot.COTException;
 import com.sun.identity.saml2.meta.SAML2MetaException;
 import com.sun.identity.test.common.TestBase;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import org.testng.annotations.BeforeTest;
 import org.testng.annotations.Test;
 
 public class SAML2Test extends TestBase {
     private CommandManager cmdManager;
     private static DevNullOutputWriter outputWriter = new DevNullOutputWriter();
 
     public SAML2Test() {
         super("FederationCLI");
     }
     
     /**
      * Create the CLIManager.
      */
     @BeforeTest(groups = {"cli"})
     public void suiteSetup()
         throws CLIException
     {
         Map<String, Object> env = new HashMap<String, Object>();
         env.put(CLIConstants.SYS_PROPERTY_COMMAND_NAME, "fmadm");
         env.put(CLIConstants.SYS_PROPERTY_DEFINITION_FILES,
             "com.sun.identity.federation.cli.FederationManager");
         env.put(CLIConstants.SYS_PROPERTY_OUTPUT_WRITER, outputWriter);
         cmdManager = new CommandManager(env);
     }
 
     @Test(groups = {"samlv2"})
     public void createCircleOfTrust()
         throws CLIException, COTException, SAML2MetaException {
         entering("createCircleOfTrust", null);
         String[] args = {"create-circle-of-trust",
             CLIConstants.PREFIX_ARGUMENT_LONG +
                 CreateCircleOfTrust.ARGUMENT_COT,
             "clitest"
         };
         CLIRequest req = new CLIRequest(null, args, getAdminSSOToken());
         cmdManager.addToRequestQueue(req);
 
         try {
             cmdManager.serviceRequestQueue();
             CircleOfTrustManager cotManager = new CircleOfTrustManager();
             CircleOfTrustDescriptor objCircleOfTrust = 
                 cotManager.getCircleOfTrust("/", "clitest");
             assert(objCircleOfTrust != null);
         } finally {
             exiting("createCircleOfTrust");
         }
     }
 
     @Test(groups = {"samlv2"}, dependsOnMethods = {"createCircleOfTrust"},
        expectedExceptions = {SAML2MetaException.class})
     public void deleteCircleOfTrust()
         throws CLIException, COTException, SAML2MetaException {
         entering("deleteCircleOfTrust", null);
         String[] args = {"delete-circle-of-trust",
             CLIConstants.PREFIX_ARGUMENT_LONG +
                 CreateCircleOfTrust.ARGUMENT_COT,
             "clitest"
         };
         CLIRequest req = new CLIRequest(null, args, getAdminSSOToken());
         cmdManager.addToRequestQueue(req);
 
         try {
             cmdManager.serviceRequestQueue();
             CircleOfTrustManager cotManager = new CircleOfTrustManager();
             CircleOfTrustDescriptor objCircleOfTrust = 
                 cotManager.getCircleOfTrust("/", "clitest");
         } finally {
             exiting("deleteCircleOfTrust");
         }
     }
 }
