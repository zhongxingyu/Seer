 package my.triviagame.xmcd;
 
 import junit.framework.Assert;
 import my.triviagame.dal.AlbumRow;
 import my.triviagame.dal.TrackRow;
 import org.apache.commons.io.FileUtils;
 import org.junit.Test;
 
 /**
  * Tests XmcdDisc.
  */
 public class XmcdDiscTest {
 
     /**
      * Tests that generating a disc from an empty xmcd file fails.
      */
     @Test
     public void testConstructionFromEmptyFileFails() throws Throwable {
         try {
             XmcdDisc.fromXmcdFile("", FreedbGenre.BLUES);
             // Expecting an exception
             Assert.fail();
         } catch (XmcdException e) {
         }
     }
 
     /**
      * Tests that generating a disc from a badly formatted xmcd file fails.
      */
     @Test
     public void testConstructionFromBadFileFails() throws Throwable {
         try {
             XmcdDisc.fromXmcdFile("blah!\nSome more text\n\n\nmore blah!", FreedbGenre.CLASSICAL);
             // Expecting an exception
             Assert.fail();
         } catch (XmcdException e) {
         }
     }
 
     /**
      * Tests that generating a disc from a sample good xmcd file works.
      */
     @Test
     public void testConstructionFromSampleFile1() throws Throwable {
         String xmcd0a0d7d14 = FileUtils.readFileToString(FileUtils.toFile(
                 getClass().getResource("resources/xmcd samples/0a0d7d14")));
         XmcdDisc disc = XmcdDisc.fromXmcdFile(xmcd0a0d7d14, FreedbGenre.REGGAE);
         
         // Set expectations for the album part
         AlbumRow expectedAlbumRow = new AlbumRow();
         expectedAlbumRow.freedbGenre = (byte)FreedbGenre.REGGAE.ordinal();
         expectedAlbumRow.revision = 0;
         expectedAlbumRow.freedbId = 0x0a0d7d14;
         expectedAlbumRow.artistName = "Kinks, The";
        expectedAlbumRow.title = "Face To Face: Deluxe Edition";
         expectedAlbumRow.year = 1966;
         expectedAlbumRow.freeTextGenre = "Pop";
         Assert.assertEquals(expectedAlbumRow, disc.albumRow);
 
         // Set expectations for some of the tracks
         TrackRow expectedTrackRow0 = new TrackRow();
         expectedTrackRow0.trackNum = 0;
        expectedTrackRow0.title = "Party Line";
         expectedTrackRow0.artistName = "Kinks, The";
         expectedTrackRow0.lenInSec = 2 * 60 + 38;
         Assert.assertEquals(expectedTrackRow0, disc.trackRows.get(0));
         TrackRow expectedTrackRow19 = new TrackRow();
         expectedTrackRow19.trackNum = 19;
        expectedTrackRow19.title = "Dead End Street";
         expectedTrackRow19.artistName = "Kinks, The";
         expectedTrackRow19.lenInSec = 2 * 60 + 56;
         Assert.assertEquals(expectedTrackRow19, disc.trackRows.get(19));
     }
     
     /**
      * Tests another sample xmcd file.
      * This test doesn't bother verifying fields, it just checks that nothing blows up.
      */
     @Test
     public void testConstructionFromSampleFile2() throws Throwable {
         String xmcda10be40d = FileUtils.readFileToString(FileUtils.toFile(
                 getClass().getResource("resources/xmcd samples/a10be40d")));
         XmcdDisc disc = XmcdDisc.fromXmcdFile(xmcda10be40d, FreedbGenre.NEWAGE);
     }
 }
