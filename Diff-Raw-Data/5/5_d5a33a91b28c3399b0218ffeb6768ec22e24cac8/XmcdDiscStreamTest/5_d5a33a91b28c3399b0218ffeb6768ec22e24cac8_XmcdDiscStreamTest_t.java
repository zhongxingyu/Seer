 package my.triviagame.xmcd;
 
 import org.junit.Ignore;
 import org.junit.Test;
 
 /**
  * Tests {@link XmcdDiscStream}.
  */
 public class XmcdDiscStreamTest {
     
     @Test
     public void testFullExtractionFromSampleArchive() throws Throwable {
         XmcdDiscStream xmcdArchive = new XmcdDiscStream(TestUtilities.getUpdate_20120401_20120501(), false);
         int recordsCount = 0;
         while (xmcdArchive.hasNext()) {
             xmcdArchive.next();
             recordsCount++;
         }
         xmcdArchive.close();
         System.out.println(recordsCount + " records total in update");
     }
     
     @Test
     public void testFullExtractionFromSampleArchiveOnlyAscii() throws Throwable {
         XmcdDiscStream xmcdArchive = new XmcdDiscStream(TestUtilities.getUpdate_20120401_20120501(), true);
         int recordsCount = 0;
         while (xmcdArchive.hasNext()) {
             xmcdArchive.next();
             recordsCount++;
         }
         xmcdArchive.close();
         System.out.println(recordsCount + " ASCII records in update");
     }
     
     /**
      * Tests extraction & parsing of a complete FreeDB archive.
      * References a file that exists outside the source control and should exist in the local source tree.
      * Takes a long time to complete, hence the @ignore annotation.
      */
     @Ignore
     @Test
     public void testCompleteArchiveExtraction() throws Throwable {
         XmcdDiscStream xmcdArchive = new XmcdDiscStream(TestUtilities.getComplete_20120601(), true);
         int recordsCount = 0;
         while (xmcdArchive.hasNext()) {
             xmcdArchive.next();
             recordsCount++;
             if (recordsCount % 10000 == 0) {
                System.out.println(recordsCount + " ASCII records parsed so far");
             }
         }
        System.out.println(recordsCount + " ASCII records total in complete archive");
     }
 }
