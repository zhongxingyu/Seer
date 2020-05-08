 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.web.test;
 
 import static javax.servlet.http.HttpServletResponse.SC_OK;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.virgo.kernel.deployer.core.DeploymentIdentity;
 import org.eclipse.virgo.kernel.deployer.core.ApplicationDeployer.DeploymentOptions;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 /**
  */
@Ignore("[DMS-2496] comment this out until the failure can be debugged")
 public class ServiceBundleRedeploymentTests extends AbstractWebIntegrationTests {
 
     private static final DeploymentOptions DEPLOYMENT_OPTIONS = new DeploymentOptions(false, false, true);
 
     private final Map<String, List<String>> expectations = new HashMap<String, List<String>>();
 
     @Before 
     public void setUpExpectations() {
         expectations.put("", Arrays.asList("Choose an apprentice magician"));
         expectations.put("form.htm?id=1", Arrays.asList("Harry Potter", "Promising Wizard..."));
         expectations.put("form.htm?id=2", Arrays.asList("Ronald Weasly"));
         expectations.put("form.htm?id=3", Arrays.asList("Hermione Granger"));
     }
 
     @Test 
     public void redeployServiceBundleTest() throws Exception {
         DeploymentIdentity standardDeploymentIdentity = this.appDeployer.deploy(new File(
             "src/test/apps-static/service-bundle-redeploy-bundles/standard.jar").toURI(), DEPLOYMENT_OPTIONS);
         DeploymentIdentity formtagsServiceDeploymentIdentity = this.appDeployer.deploy(new File(
             "src/test/apps-static/service-bundle-redeploy-bundles/formtags.jar").toURI(), DEPLOYMENT_OPTIONS);
         DeploymentIdentity formtagsWebDeploymentIdentity = assertDeployBehavior("formtags", new File(
             "src/test/apps-static/service-bundle-redeploy-bundles/formtags.war"), this.expectations);
         
         formtagsServiceDeploymentIdentity = this.appDeployer.deploy(new File(
             "src/test/apps-static/service-bundle-redeploy-bundles/formtags.jar").toURI(), DEPLOYMENT_OPTIONS);
         
         // Refresh packages will stop and start any bundle that's wired to the bundle that's
         // being refreshed. In this case that means the Web bundle. Unfortunately refresh packages
         // doesn't tell anyone which bundle's it's messed around with so we can't track its start.
         // This means that the deploy call (which became a refresh) will return before the web
         // bundle has fully restarted. We sleep for a bit to give it time to complete its restart
         // before checking that it's still serving the expected content.
         Thread.sleep(5000);
         
         for (String resource : expectations.keySet()) {
             List<String> expectedContents = expectations.get(resource);
             assertGetRequest("formtags", resource, SC_OK, expectedContents);
         }
         
         this.appDeployer.undeploy(formtagsWebDeploymentIdentity);
         this.appDeployer.undeploy(formtagsServiceDeploymentIdentity);
         this.appDeployer.undeploy(standardDeploymentIdentity);
     }
 
 }
