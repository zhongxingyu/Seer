 package no.finntech.tika.harderner;
 
 import no.finntech.io.utils.BitFlipperInputStream;
 import no.finntech.parser.DocumentParser;
 import no.finntech.parser.DocumentParserException;
 import no.finntech.parser.TikaForkedProcessDocumentParser;
 import no.finntech.parser.TikaInProcessDocumentParser;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import static junit.framework.Assert.assertEquals;
 
 /**
  * A set of test that flips bits in various files to detect unexpected issues in the various document parsers.
  */
 public class TikaIndexerHardenerTest {
 
     private static final boolean DEBUG = false;
 
     private DocumentParser tika;
     private DocumentParser linearTika;
     private DocumentParser forkedTika;
 
     @Before
     public void setUp() throws Exception {
         linearTika = new TikaInProcessDocumentParser();
 
         forkedTika = new TikaForkedProcessDocumentParser();
 
         useLinearTika();
     }
 
     @After
     public void tearDown() throws Exception {
         forkedTika.close();
     }
 
     public void useLinearTika() {
         tika = linearTika;
     }
 
     public void useForkedTika() {
         tika = forkedTika;
     }
 
     @Test
     public void originalFileIndexesProperly() throws Exception {
         URL url = getFileUrl("testing.doc");
         assertEquals(IndexResult.OK, flipBitAndIndexContent(url, -1));
         assertEquals(IndexResult.OK, indexContent(url));
     }
 
     @Test
    /** https://issues.apache.org/bugzilla/show_bug.cgi?id=52372 */
     public void invalidPoiSectionSizeShouldntCauseUnhandledExceptions() throws Exception {
         URL url = getFileUrl("testing.doc");
         assertEquals(IndexResult.HANDLED, flipBitAndIndexContent(url, 2295 * 8 + 2));
     }
 
     @Test
    /** failure not yet reported **/
     public void invalidPoiSummaryPropertiesSizeShouldntCauseUnhandledExceptions() throws Exception {
         URL url = getFileUrl("testing.doc");
         assertEquals(IndexResult.HANDLED, flipBitAndIndexContent(url, 18138));
     }
 
     @Test
     /**
      * Errors are swallowed by tika. This test is only valid if the POI is still broken
      * https://issues.apache.org/jira/browse/TIKA-815
      */
     public void outOfMemoryErrorInPoiIsFixedAndPorperlyReportedInForkMode() throws Exception {
         URL url = getFileUrl("testing.doc");
         assertEquals(IndexResult.UNHANDLED, flipBitAndIndexContent(url, 2295 * 8 + 2));
         
         useForkedTika();
         assertEquals(IndexResult.HANDLED, flipBitAndIndexContent(url, 2295 * 8 + 2));
     }
 
     // these tests take longer time but allows to find various kind of problems
     @Test
     public void flipOneBitAtATimeSimpleDoc() throws Exception {
         flipOneBitAtATime("testing.doc");
     }
 
     @Test
     public void flipOneBitAtATimeSimpleDocx() throws Exception {
         flipOneBitAtATime("testing.docx");
     }
 
     @Test
     public void flipOneBitAtATimeSimplePdf() throws Exception {
         flipOneBitAtATime("testing.pdf");
     }
 
     private void flipOneBitAtATime(String fileName) throws IOException {
         URL url = getFileUrl(fileName);
         File f = new File(url.getFile());
         long nbBits = f.length() * 8;
         System.out.println("Testing... " + url + " with " + nbBits + " bits to flip");
         long before = System.nanoTime();
         List<Integer> unhandled = new ArrayList<Integer>();
         for (int indexOfBitToFlip = 0; indexOfBitToFlip < nbBits; indexOfBitToFlip++) {
             IndexResult handlingResult = flipBitAndIndexContent(url, indexOfBitToFlip);
             if (handlingResult == IndexResult.UNHANDLED)
                 unhandled.add(indexOfBitToFlip);
         }
         long after = System.nanoTime();
         System.out.println("Total indexing took: " + (after - before) / 1000000 + " ms.");
         assertEquals("The following flipped bit indexes caused unhandled exceptions in file: " + fileName + " : " + unhandled, 0, unhandled.size());
     }
 
     static enum IndexResult {
         OK, HANDLED, UNHANDLED
     }
 
     private IndexResult flipBitAndIndexContent(URL url, long indexOfBitToFlip) throws IOException {
         InputStream inputStream = url.openStream();
         inputStream = new BitFlipperInputStream(inputStream, indexOfBitToFlip);
         IndexResult result = parseContent(inputStream);
         if (result == IndexResult.UNHANDLED)
             System.err.println("[" + result + "] bit #" + indexOfBitToFlip + " caused unexpected exception.");
         return result;
     }
 
     private IndexResult indexContent(URL url) throws IOException {
         InputStream inputStream = url.openStream();
         IndexResult result = parseContent(inputStream);
         if (result == IndexResult.UNHANDLED)
             System.err.println("[" + result + "] caused unexpected exception.");
         return result;
     }
 
     private IndexResult parseContent(InputStream inputStream) throws IOException {
         IndexResult result = IndexResult.OK;
         try {
             String s = tika.parseToString(inputStream);
             if (DEBUG)
                 System.out.println("PARSED: " + s);
         } catch (DocumentParserException ignored) {
             result = IndexResult.HANDLED;
             if (DEBUG)
                 ignored.printStackTrace(System.err);
         } catch (IOException ignored) {
             result = IndexResult.HANDLED;
             if (DEBUG)
                 ignored.printStackTrace(System.err);
         } catch (Throwable unhandled) {
             result = IndexResult.UNHANDLED;
             unhandled.printStackTrace(System.err);
         } finally {
             inputStream.close();
         }
         return result;
     }
 
     private URL getFileUrl(String fileName) throws MalformedURLException {
         URL resource = this.getClass().getResource("/" + fileName);
         if (resource == null)
             throw new IllegalStateException("Couldn@t find resource for file " + fileName);
         return new URL(resource.toString());
     }
 }
