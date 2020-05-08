 package fedora.test.integration;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.custommonkey.xmlunit.SimpleXpathEngine;
 
 import fedora.client.FedoraClient;
 import fedora.server.management.FedoraAPIM;
 import fedora.test.DemoObjectTestSetup;
 import fedora.test.FedoraServerTestCase;
 import fedora.utilities.ExecUtility;
 
 /**
  * @author Edwin Shin
  *
  */
 public class TestCommandLineUtilities extends FedoraServerTestCase 
 {
     static ByteArrayOutputStream sbOut = null;
     static ByteArrayOutputStream sbErr = null;
     static TestCommandLineUtilities curTest = null;
     public static Test suite() 
     {
         TestSuite suite = new TestSuite("Command Line Utilities TestSuite");
         suite.addTestSuite(TestCommandLineUtilities.class);
         return new DemoObjectTestSetup(suite);
     }
     
     public void testFedoraPurgeAndIngest() 
     {
         System.out.println("Purging object demo:5");
         System.out.println("FEDORA-HOME = " + FEDORA_HOME);
         purgeUsingScript("demo:5");
         assertEquals(0, sbErr.size());
         System.out.println("Re-ingesting object demo:5");
         ingestFoxmlFile(new File(FEDORA_HOME + "/client/demo/foxml/local-server-demos/simple-image-demo/obj_demo_5.xml"));
         String out = sbOut.toString();
         String err = sbErr.toString();
         assertEquals(out.indexOf("Ingested PID: demo:5")!= -1, true );        
         System.out.println("Purge and ingest test succeeded");
     }
     
     public void testBatchBuildAndBatchIngestAndPurge() throws Exception
     {
         System.out.println("Building batch objects");
         batchBuild(new File(FEDORA_HOME + "/client/demo/batch-demo/foxml-template.xml"),
                    new File(FEDORA_HOME + "/client/demo/batch-demo/object-specifics"),
                    new File(FEDORA_HOME + "/client/demo/batch-demo/objects"),
                    new File(FEDORA_HOME + "/client/logs/build.log"));
         String out = sbOut.toString();
         String err = sbErr.toString();
         assertEquals(err, true, err.indexOf("10 Fedora FOXML XML documents successfully created")!= -1);
         System.out.println("Ingesting batch objects");
         batchIngest(new File(FEDORA_HOME + "/client/demo/batch-demo/objects"), 
                     new File(FEDORA_HOME + "/server/logs/junit_ingest.log"));
         out = sbOut.toString();
         err = sbErr.toString();
         assertEquals(err.indexOf("10 objects successfully ingested into Fedora")!= -1, true ); 
         String batchObjs[] = { "demo:3010", "demo:3011", "demo:3012", "demo:3013", "demo:3014",
                                "demo:3015", "demo:3016", "demo:3017", "demo:3018", "demo:3019"};
         System.out.println("Purging batch objects");
         purgeFast(batchObjs);
         System.out.println("Build and ingest test succeeded");
     }
 
     public void testBatchBuildIngestAndPurge() throws Exception
     {
         System.out.println("Building and Ingesting batch objects");
         batchBuildIngest(new File(FEDORA_HOME + "/client/demo/batch-demo/foxml-template.xml"),
                    new File(FEDORA_HOME + "/client/demo/batch-demo/object-specifics"),
                    new File(FEDORA_HOME + "/client/demo/batch-demo/objects"),
                    new File(FEDORA_HOME + "/server/logs/junit_buildingest.log"));
         String out = sbOut.toString();
         String err = sbErr.toString();
         assertEquals("Response did not contain expected string re: FOXML XML documents: <reponse>" + err + "</response>", err.indexOf("10 Fedora FOXML XML documents successfully created")!= -1, true );
         assertEquals("Response did not contain expected string re: objects successfully ingested: <reponse>" + err + "</reponse", err.indexOf("10 objects successfully ingested into Fedora")!= -1, true );
         String batchObjs[] = { "demo:3010", "demo:3011", "demo:3012", "demo:3013", "demo:3014",
                                "demo:3015", "demo:3016", "demo:3017", "demo:3018", "demo:3019"};
         System.out.println("Purging batch objects");
         purgeFast(batchObjs);
         System.out.println("Build/ingest test succeeded");
     }    
     
     public void testBatchModify() throws Exception 
     {
         System.out.println("Running batch modify of objects");
         batchModify(new File(FEDORA_HOME + "/client/demo/batch-demo/modify-batch-directives.xml"),
                     new File(FEDORA_HOME + "/server/logs/junit_modify.log"));
         String out = sbOut.toString();
         String err = sbErr.toString();
        if (out.indexOf("24 modify directives successfully processed.")== -1)
         {
             System.out.println(" out = " + out);
             System.out.println(" err = " + err);
         }
        assertEquals(false, out.indexOf("24 modify directives successfully processed.")== -1);
         assertEquals(false, out.indexOf("0 modify directives failed.")== -1);
         System.out.println("Purging batch modify object");
         purgeFast("demo:32");
         System.out.println("Batch modify test succeeded");
     }
    
     public void testExport()
     {
         System.out.println("Testing fedora-export");
         File outFile = new File(FEDORA_HOME + "/client/demo/batch-demo/demo_5.xml");
         String absPath = outFile.getAbsolutePath();
         if (outFile.exists())
         {
             outFile.delete();
         }
         System.out.println("Exporting object demo:5");
         exportObj("demo:5", new File(FEDORA_HOME + "/client/demo/batch-demo"));
         String out = sbOut.toString();
         String err = sbErr.toString();
         assertEquals(out.indexOf("Exported demo:5")!= -1, true );
         File outFile2 = new File(FEDORA_HOME + "/client/demo/batch-demo/demo_5.xml");
         String absPath2 = outFile2.getAbsolutePath();
         assertEquals(outFile2.exists(), true );
         System.out.println("Deleting exported file");
         if (outFile2.exists())
         {
             outFile2.delete();
         }
         System.out.println("Export test succeeded");
     }
     
     public void testValidatePolicy()
     {
         System.out.println("Testing Validate Policies");
         File validDir = new File("src/test/junit/XACMLTestPolicies/valid-policies");
         traverseAndValidate(validDir, true);
         
         File invalidDir = new File("src/test/junit/XACMLTestPolicies/invalid-policies");
         traverseAndValidate(invalidDir, false);
         
         System.out.println("Validate Policies test succeeded");
     }
     
     private void traverseAndValidate(File testDir, boolean expectValid)
     {
   //      assertEquals(testDir.isDirectory(), true);
         File testFiles[] = testDir.listFiles(new java.io.FilenameFilter()
             {
                 public boolean accept(File dir, String name)
                 {
                     if ((name.toLowerCase().startsWith("permit") ||
                          name.toLowerCase().startsWith("deny")) &&
                          name.endsWith(".xml"))
                     {
                         return(true);
                     }
                     return(false);
                 }
             }
         );
         for ( int i = 0; i < testFiles.length; i++)
         {
             System.out.println("Checking "+(expectValid ? "valid" : "invalid") +" policy: "+
                                testFiles[i].getName());
             execute(FEDORA_HOME+ "/server/bin/validate-policy", testFiles[i].getAbsolutePath());
             String out = sbOut.toString();
             String err = sbErr.toString();
             
             if (expectValid)
             {
                 assertEquals(err.length() == 0, true);
             }
             else
             {
                 assertEquals(err.length() == 0, false);
             }                 
         }        
     }
     
     private void ingestFoxmlDirectory(File dir) 
     {
         //fedora-ingest f obj1.xml foxml1.0 myrepo.com:8443 jane jpw https
         execute(FEDORA_HOME + "/client/bin/fedora-ingest", "d " + dir.getAbsolutePath() + 
                 " foxml1.0 DMO " + getHost() + ":" + getPort() + " " + getUsername() + 
                 " " + getPassword() + " " + getProtocol() + " \"junit ingest\"");
     }
     
     private void ingestFoxmlFile(File f) 
     {
         //fedora-ingest f obj1.xml foxml1.0 myrepo.com:8443 jane jpw https
         
     //    File exe = new File("client/bin/fedora-ingest");
         execute(FEDORA_HOME + "/client/bin/fedora-ingest", "f " + f.getAbsolutePath() + 
                 " foxml1.0 " + getHost() + ":" + getPort() + " " + getUsername() + 
                 " " + getPassword() + " " + getProtocol() + " junit-ingest");
     }
     
     private static void purgeUsingScript(String pid) 
     {
     //    File exe = new File("client/bin/fedora-purge");
         execute(FEDORA_HOME + "/client/bin/fedora-purge", getHost() + ":" + getPort() +
                 " " + getUsername() + " " + getPassword() + " " + pid + " " + 
                 getProtocol() + " junit-purge");
     }
 
     private static void purgeFast(String pid) throws Exception {
         getAPIM().purgeObject(pid, "because", false);
     }
 
     private static void purgeFast(String[] pids) throws Exception {
         FedoraAPIM apim = getAPIM();
         for (int i = 0; i < pids.length; i++) {
             apim.purgeObject(pids[i], "because", false);
         }
     }
     
     private static FedoraAPIM getAPIM() throws Exception {
         String baseURL = getProtocol() + "://" 
                        + getHost() + ":" 
                        + getPort() + "/fedora";
         FedoraClient client = new FedoraClient(baseURL,
                                                getUsername(),
                                                getPassword());
         return client.getAPIM();
     }
 
     private void batchBuild(File objectTemplateFile, File objectSpecificDir, File objectDir, File logFile)
     {
         execute(FEDORA_HOME + "/client/bin/fedora-batch-build", objectTemplateFile.getAbsolutePath() + " " + 
                 objectSpecificDir.getAbsolutePath() + " " + objectDir.getAbsolutePath() + " " + 
                 logFile.getAbsolutePath() + " text");
     }
     
     private void batchIngest(File objectDir, File logFile)
     {
         execute(FEDORA_HOME + "/client/bin/fedora-batch-ingest", objectDir.getAbsolutePath() + " " + 
                 logFile.getAbsolutePath() + " text foxml1.0 " + getHost() + ":" + getPort() +
                 " " + getUsername() + " " + getPassword() + " " + getProtocol() );
     }
     
     private void batchBuildIngest(File objectTemplateFile, File objectSpecificDir, File objectDir, File logFile)
     {
         execute(FEDORA_HOME + "/client/bin/fedora-batch-buildingest", objectTemplateFile.getAbsolutePath() + " " + 
                 objectSpecificDir.getAbsolutePath() + " " + objectDir.getAbsolutePath() + " " + 
                 logFile.getAbsolutePath() + " text " + getHost() + ":" + getPort() +
                     " " + getUsername() + " " + getPassword() + " " + getProtocol() );
     }
     
     private void batchModify(File batchDirectives, File logFile)
     {
         execute(FEDORA_HOME + "/client/bin/fedora-modify", getHost() + ":" + getPort() + " " + 
                 getUsername() + " " + getPassword() + " " + batchDirectives.getAbsolutePath() + " " + 
                 logFile.getAbsolutePath() + " " + getProtocol() );
     }
 
     private void exportObj(String pid, File dir)
     {
         execute(FEDORA_HOME + "/client/bin/fedora-export", getHost() + ":" + getPort() + " " + 
                 getUsername() + " " + getPassword() + " " + pid + " foxml1.0 public " + 
                 dir.getAbsolutePath() + " " + getProtocol() );
     }
     
     public static void execute(String cmd, String args) 
     {
  //       System.out.println("Executing cmd:" + cmd + " " + args);
         if (sbOut != null && sbErr != null)
         {
             sbOut.reset();
             sbErr.reset();
             ExecUtility.execCommandLineUtility(cmd + " " + args, sbOut, sbErr);
         }
         else
         {
             ExecUtility.execCommandLineUtility(cmd + " " + args);
         }
  //       System.out.println("Executed cmd:" + cmd );
    }
 
     public void setUp() throws Exception {
         
         SimpleXpathEngine.registerNamespace("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
         SimpleXpathEngine.registerNamespace("uvalibadmin", "http://dl.lib.virginia.edu/bin/admin/admin.dtd/");
         sbOut = new ByteArrayOutputStream();
         sbErr = new ByteArrayOutputStream();
     }
     
     public void tearDown() 
     {
         SimpleXpathEngine.clearNamespaces();
     }
 
     
     public static void main(String[] args) 
     {
         junit.textui.TestRunner.run(TestCommandLineUtilities.class);
     }
 
 }
