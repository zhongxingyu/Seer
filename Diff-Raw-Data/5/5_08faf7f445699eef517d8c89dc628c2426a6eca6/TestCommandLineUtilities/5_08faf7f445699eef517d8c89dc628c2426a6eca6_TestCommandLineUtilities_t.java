 package fedora.test.integration;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 
 import javax.xml.rpc.ServiceException;
 
 import junit.extensions.TestSetup;
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.custommonkey.xmlunit.SimpleXpathEngine;
 
 import fedora.client.batch.AutoModify;
 import fedora.test.FedoraServerTestCase;
 import fedora.test.FedoraServerTestSetup;
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
         TestSuite suite = new TestSuite(TestCommandLineUtilities.class);
         TestSetup wrapper = new FedoraServerTestSetup(suite) 
         {
             public void setUp() throws Exception 
             {
                 TestIngestDemoObjects.ingestDemoObjects();
                 sbOut = new ByteArrayOutputStream();
                 sbErr = new ByteArrayOutputStream();
             }
             
             public void tearDown() throws Exception 
             {
                 //  I tried  TestIngestDemoObjects.purgeDemoObjects();  but it seemed to not work 
                 sbOut = null;
                 sbErr = null;
                 TestCommandLineUtilities.purgeDemoObjects();
             }
         };
         return new FedoraServerTestSetup(wrapper);
                 
     }
     
     private static void purgeDemoObjects()
     {
         String demoObjs[] = {"demo:14", "demo:21", "demo:26", "demo:SmileyBeerGlass", "demo:SmileyBucket",
                 "demo:SmileyDinnerware", "demo:SmileyEarring", "demo:SmileyKeychain", "demo:SmileyNightlight", 
                 "demo:SmileyPens", "demo:SmileyShortRoundCup", "demo:SmileyStuff", "demo:SmileyTallRoundCup", 
                 "demo:SmileyToiletBrush", "demo:SmileyWastebasket", "demo:29", "demo:18", "demo:31", "demo:5", 
                 "demo:17", "demo:30", "demo:6", "demo:7", "demo:10", "demo:11" }; 
         String demoBMechs[] = { "demo:13", "demo:20", "demo:25", "demo:DualResImageCollection", 
                 "demo:DualResImageImpl", "demo:28", "demo:2", "demo:16", "demo:3", "demo:4", "demo:9"}; 
         String demoBDefs[] = { "demo:12", "demo:19", "demo:22", "demo:Collection", 
                 "demo:DualResImage", "demo:27", "demo:1", "demo:15", "demo:8"}; 
         // first purge the objects
         System.out.println("Purging demo objects");
         purgeAll(demoObjs);
         // then then BMechs
         System.out.println("Purging demo BMechs");
         purgeAll(demoBMechs);
         //then the BDefs
         System.out.println("Purging demo BDefs");
         purgeAll(demoBDefs); 
     }
     
     public void testFedoraPurgeAndIngest() 
     {
         System.out.println("Purging object demo:5");
         purge("demo:5");
         assertEquals(sbOut.size(), 0);
         assertEquals(sbErr.size(), 0);
         System.out.println("Re-ingesting object demo:5");
         ingestFoxmlFile(new File("src/demo-objects/foxml/local-server-demos/simple-image-demo/obj_demo_5.xml"));
         String out = sbOut.toString();
         String err = sbErr.toString();
         assertEquals(out.indexOf("Ingested PID: demo:5")!= -1, true );
         System.out.println("Purge and ingest test succeeded");
     }
     
     public void testBatchBuildAndBatchIngestAndPurge()
     {
         System.out.println("Building batch objects");
         batchBuild(new File("dist/client/demo/batch-demo/foxml-template.xml"),
                    new File("dist/client/demo/batch-demo/object-specifics"),
                    new File("dist/client/demo/batch-demo/objects"),
                    new File("dist/client/logs/build.log"));
         String out = sbOut.toString();
         String err = sbErr.toString();
         assertEquals(err.indexOf("10 Fedora null XML documents successfully created")!= -1, true );
         System.out.println("Ingesting batch objects");
         batchIngest(new File("dist/client/demo/batch-demo/objects"), 
                     new File("dist/client/logs/ingest.log"));
         out = sbOut.toString();
         err = sbErr.toString();
         assertEquals(err.indexOf("10 objects successfully ingested into Fedora")!= -1, true ); 
         String batchObjs[] = { "demo:3010", "demo:3011", "demo:3012", "demo:3013", "demo:3014",
                                "demo:3015", "demo:3016", "demo:3017", "demo:3018", "demo:3019"};
         System.out.println("Purging batch objects");
         purgeAll(batchObjs);
         System.out.println("Build and ingest test succeeded");
     }
 
     public void testBatchBuildIngestAndPurge()
     {
         System.out.println("Building and Ingesting batch objects");
         batchBuildIngest(new File("dist/client/demo/batch-demo/foxml-template.xml"),
                    new File("dist/client/demo/batch-demo/object-specifics"),
                    new File("dist/client/demo/batch-demo/objects"),
                    new File("dist/client/logs/buildingest.log"));
         String out = sbOut.toString();
         String err = sbErr.toString();
         assertEquals(err.indexOf("10 Fedora null XML documents successfully created")!= -1, true );
         assertEquals(err.indexOf("10 objects successfully ingested into Fedora")!= -1, true );
         String batchObjs[] = { "demo:3010", "demo:3011", "demo:3012", "demo:3013", "demo:3014",
                                "demo:3015", "demo:3016", "demo:3017", "demo:3018", "demo:3019"};
         System.out.println("Purging batch objects");
         purgeAll(batchObjs);
         System.out.println("Build/ingest test succeeded");
     }    
     
     public void testBatchModify()
     {
         System.out.println("Running batch modify of objects");
         batchModify(new File("dist/client/demo/batch-demo/modify-batch-directives.xml"),
                     new File("dist/client/logs/modify.log"));
         String out = sbOut.toString();
         String err = sbErr.toString();
         assertEquals(out.indexOf("24 modify directives successfully processed.")!= -1, true );
         assertEquals(out.indexOf("0 modify directives failed.")!= -1, true );
         System.out.println("Purging batch modify object");
         purge("demo:32");
         System.out.println("Batch modify test succeeded");
     }
     
     public void testExport()
     {
         System.out.println("Testing fedora-export");
         File outFile = new File("dist/client/demo/batch-demo/demo_5.xml");
         String absPath = outFile.getAbsolutePath();
         if (outFile.exists())
         {
             outFile.delete();
         }
         System.out.println("Exporting object demo:5");
         exportObj("demo:5", new File("dist/client/demo/batch-demo"));
         String out = sbOut.toString();
         String err = sbErr.toString();
         assertEquals(out.indexOf("Exported demo:5")!= -1, true );
         File outFile2 = new File("dist/client/demo/batch-demo/demo_5.xml");
         String absPath2 = outFile2.getAbsolutePath();
         assertEquals(outFile2.exists(), true );
         System.out.println("Deleting exported file");
         if (outFile2.exists())
         {
             outFile2.delete();
         }
         System.out.println("Export test succeeded");
     }
     
     
     private static void purgeAll(String items[])
     {
         for (int i = 0; i < items.length; i++)
         {
             purge(items[i]);
         }
     }
 
     private void ingestFoxmlDirectory(File dir) 
     {
         //fedora-ingest f obj1.xml foxml1.0 myrepo.com:8443 jane jpw https
         execute("/client/bin/fedora-ingest d " + dir.getAbsolutePath() + 
                 " foxml1.0 DMO " + getHost() + ":" + getPort() + " " + getUsername() + 
                 " " + getPassword() + " " + getProtocol() + " \"junit ingest\"");
     }
     
     private void ingestFoxmlFile(File f) 
     {
         //fedora-ingest f obj1.xml foxml1.0 myrepo.com:8443 jane jpw https
         execute("/client/bin/fedora-ingest f " + f.getAbsolutePath() + 
                 " foxml1.0 " + getHost() + ":" + getPort() + " " + getUsername() + 
                 " " + getPassword() + " " + getProtocol() + " junit-ingest");
     }
     
     private static void purge(String pid) 
     {
         execute("/client/bin/fedora-purge " + getHost() + ":" + getPort() +
                 " " + getUsername() + " " + getPassword() + " " + pid + " " + 
                 getProtocol() + " junit-purge");
     }
     
     private void batchBuild(File objectTemplateFile, File objectSpecificDir, File objectDir, File logFile)
     {
         execute("/client/bin/fedora-batch-build " + objectTemplateFile.getAbsolutePath() + " " + 
                 objectSpecificDir.getAbsolutePath() + " " + objectDir.getAbsolutePath() + " " + 
                 logFile.getAbsolutePath() + " text");
     }
     
     private void batchIngest(File objectDir, File logFile)
     {
         execute("/client/bin/fedora-batch-ingest " + objectDir.getAbsolutePath() + " " + 
                 logFile.getAbsolutePath() + " text foxml1.0 " + getHost() + ":" + getPort() +
                 " " + getUsername() + " " + getPassword() + " " + getProtocol() );
     }
     
     private void batchBuildIngest(File objectTemplateFile, File objectSpecificDir, File objectDir, File logFile)
     {
         execute("/client/bin/fedora-batch-buildingest " + objectTemplateFile.getAbsolutePath() + " " + 
                 objectSpecificDir.getAbsolutePath() + " " + objectDir.getAbsolutePath() + " " + 
                 logFile.getAbsolutePath() + " text " + getHost() + ":" + getPort() +
                     " " + getUsername() + " " + getPassword() + " " + getProtocol() );
     }
     
     private void batchModify(File batchDirectives, File logFile)
     {
         execute("/client/bin/fedora-modify " + getHost() + ":" + getPort() + " " + 
                 getUsername() + " " + getPassword() + " " + batchDirectives.getAbsolutePath() + " " + 
                logFile.getAbsolutePath() + " " + getProtocol() );
     }
 
     private void exportObj(String pid, File dir)
     {
         execute("/client/bin/fedora-export " + getHost() + ":" + getPort() + " " + 
                 getUsername() + " " + getPassword() + " " + pid + " foxml1.0 public " + 
                dir.getAbsolutePath() + " " + getProtocol() );
     }
     
     public static void execute(String cmd) 
     {
         if (sbOut != null && sbErr != null)
         {
             sbOut.reset();
             sbErr.reset();
             ExecUtility.execCommandLineUtility(FEDORA_HOME + cmd, sbOut, sbErr);
         }
         else
         {
             ExecUtility.execCommandLineUtility(FEDORA_HOME + cmd);
         }
     }
     
     public static void main(String[] args) 
     {
         junit.textui.TestRunner.run(TestCommandLineUtilities.class);
     }
 
 }
