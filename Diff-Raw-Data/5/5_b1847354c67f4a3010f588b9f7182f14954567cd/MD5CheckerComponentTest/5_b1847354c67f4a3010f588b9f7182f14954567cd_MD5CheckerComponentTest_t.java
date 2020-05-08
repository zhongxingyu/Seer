 package dk.statsbiblioteket.newspaper.md5checker;
 
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import dk.statsbiblioteket.medieplatform.autonomous.Batch;
 import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
 
 /** Test MD5 checker */
 public class MD5CheckerComponentTest {
 
     @Test
     /**
      * Test checking checksums on two batches
      */
     public void testDoWorkOnBatch() throws Exception {
         MD5CheckerComponent md5CheckerComponent = new MockupIteratorSuper(System.getProperties());
 
         // Run on first batch with one wrong checksum
         ResultCollector result = new ResultCollector(
                 md5CheckerComponent.getComponentName(),
                 md5CheckerComponent.getComponentVersion());
         Batch batch = new Batch("400022028241");
         batch.setRoundTripNumber(1);
         md5CheckerComponent.doWorkOnBatch(batch, result);
 
         // Assert one failure with the expected report
         Assert.assertFalse(result.isSuccess(), result.toReport() + "\n");
         String expected = "<result tool=\"" + md5CheckerComponent.getComponentName() + "\" xmlns=\"http://schemas.statsbiblioteket.dk/result/\">\n"
                           +
                           "    <outcome>Failure</outcome>\n"
                           +
                           "    <date>[date]</date>\n"
                           +
                           "    <failures>\n"
                           +
                           "        <failure>\n"
                           +
                           "            <filereference>B400022028241-RT1/400022028241-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01-0006-brik.jp2/contents</filereference>\n"
                           +
                           "            <type>checksum</type>\n"
                           +
                          "            <component>MockupIteratorSuper</component>\n"
                           +
                          "            <description>2F-O1: Expected checksum d41d8cd98f00b204e9800998ecf8427f, but was d41d8cd98f00b204e9800998ecf8427e</description>\n"
                           +
                           "        </failure>\n"
                           +
                           "    </failures>\n"
                           +
                           "</result>";
         Assert.assertEquals(
                 result.toReport()
                       .replaceAll("<date>[^<]*</date>", "<date>[date]</date>"), expected);
 
         // Run on second batch with no wrong checksums
         result = new ResultCollector(md5CheckerComponent.getComponentName(), md5CheckerComponent.getComponentVersion());
         batch = new Batch("400022028241");
         batch.setRoundTripNumber(2);
         md5CheckerComponent.doWorkOnBatch(batch, result);
 
         // Assert no errors
         Assert.assertTrue(result.isSuccess(), result.toReport() + "\n");
     }
 }
