 package dk.statsbiblioteket.broadcasttranscoder;
 
 import dk.statsbiblioteket.broadcasttranscoder.cli.FetcherContext;
 import dk.statsbiblioteket.broadcasttranscoder.util.CentralWebserviceFactory;
 import dk.statsbiblioteket.doms.central.CentralWebservice;
 import dk.statsbiblioteket.doms.central.InvalidCredentialsException;
 import dk.statsbiblioteket.doms.central.MethodFailedException;
 import dk.statsbiblioteket.doms.central.RecordDescription;
 import org.apache.commons.io.FileUtils;
import org.junit.*;
 
 import java.io.File;
 import java.net.InetAddress;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.List;
 
 import static org.junit.Assert.assertTrue;
 
 /**
  * Created with IntelliJ IDEA.
  * User: abr
  * Date: 11/21/12
  * Time: 1:29 PM
  * To change this template use File | Settings | File Templates.
  */
 public class BtaDomsFetcherTest {
 
     private File foobar4;
 
     @Before
       public void setUp() throws Exception {
           foobar4 = new File("./foobar4");
           foobar4.mkdir();
           foobar4.deleteOnExit();
         try {
             InetAddress.getByName("alhena");
         } catch (UnknownHostException e) {
             Assume.assumeNoException(e);
         }
 
       }
 
       @After
       public void tearDown() throws Exception {
           FileUtils.deleteDirectory(foobar4);
 
 
       }
 
 
 
     @Test
     public void testMain() throws Exception {
 
         ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
         URL resource = contextClassLoader.getResource("bta.infrastructure.properties");
         final File infrastructureFile = new File(resource.toURI());
         URL resource1 = contextClassLoader.getResource("bta.fetcher.properties");
         final File behaviouralFile = new File(resource1.toURI());
         String hibernate = new File(contextClassLoader.getResource("hibernate-derby.xml").toURI()).getAbsolutePath();
 
         assertTrue(infrastructureFile.getAbsolutePath() + " should exist.", infrastructureFile.exists());
         assertTrue(behaviouralFile.getAbsolutePath() + " should exist.", behaviouralFile.exists());
         BtaDomsFetcher.main(
                 new String[]{
                         "-infrastructure_configfile",
                         infrastructureFile.getAbsolutePath(),
                         "-behavioural_configfile",
                         behaviouralFile.getAbsolutePath(),
                         "-hibernate_configfile",hibernate,
                         "-since", "0"
                 });
     }
 

     @Test
    @Ignore("to slow")
     public void testFetcher() throws InvalidCredentialsException, MethodFailedException {
         FetcherContext context = new FetcherContext();
         context.setBatchSize(100);
         context.setCollection("doms:RadioTV_Collection");
         context.setFedoraState("Published");
         context.setViewAngle("SummaVisible");
         context.setDomsPassword("fedoraAdminPass");
         context.setDomsUsername("fedoraAdmin");
         context.setDomsEndpoint("http://alhena:7480/centralWebservice-service/central/");
         CentralWebservice doms = CentralWebserviceFactory.getServiceInstance(context);
         List<RecordDescription> records = BtaDomsFetcher.requestInBatches(doms, context);
         for (RecordDescription record : records) {
             System.out.println(record.getPid()+":"+record.getDate());
         }
         assertTrue("No records found",records.size()>0);
     }
 }
