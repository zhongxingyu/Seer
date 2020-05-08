 package com.atlassian.sdk.accept;
 
 import static com.atlassian.sdk.accept.SdkHelper.runSdkScript;
 
 import java.io.File;
 import java.io.IOException;
 
 public class TestCreateAndVerifyPlugin extends SdkTestBase
 {
     public void testJIRA() throws IOException, InterruptedException
     {
         createAndVerify("jira");
     }
 
     public void testRefapp() throws IOException, InterruptedException
     {
         createAndVerify("refapp");
     }
 
     public void testConfluence() throws IOException, InterruptedException
     {
         createAndVerify("confluence");
     }
 
     public void testFeCru() throws IOException, InterruptedException
     {
         createAndVerify("fecru");
     }
 
     public void testCrowd() throws IOException, InterruptedException
     {
         createAndVerify("crowd");
     }
 
    public void testBamboo() throws IOException, InterruptedException
     {
         createAndVerify("bamboo");
     }
 
     private void createAndVerify(String productId)
             throws IOException, InterruptedException
     {
         final String prefix = "create-and-verify";
         File appDir = SdkHelper.createPlugin(productId, baseDir, sdkHome, prefix);
 
         runSdkScript(sdkHome, appDir, "atlas-integration-test",
                 "-p", System.getProperty("http.port", "9482"));
 
         File pluginJar = new File(new File(appDir, "target"), prefix + "-" + productId + "-plugin-1.0-SNAPSHOT.jar");
         assertTrue(pluginJar.exists());
     }
 }
