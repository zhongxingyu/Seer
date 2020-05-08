 /**
  * 
  */
 package org.opf_labs.arc_cd.cue;
 
 import static org.junit.Assert.assertTrue;
 
import java.io.File;
 import java.io.FileNotFoundException;
import java.net.URISyntaxException;
 
 import org.junit.Test;
 import org.opf_labs.arc_cd.AllArcCdCliTests;
import org.opf_labs.arc_cd.collection.CdTrack;
 import org.opf_labs.arc_cd.collection.CdItemRecord;
 import org.opf_labs.audio.CueSheet;
 import org.opf_labs.audio.FileData;
 import org.opf_labs.audio.TrackData;
 
 /**
  * @author carl
  *
  */
 public class CueSheetTest {
 
 	@Test
 	public void testSetPerformer() throws FileNotFoundException {
 		CdItemRecord itemRecord = CdItemRecord.fromInfoFile(AllArcCdCliTests.INFO_00022);
 		CueSheet testInstance = new CueSheet();
 		testInstance.setPerformer(itemRecord.getAlbumArtist());
 		assertTrue(testInstance.getPerformer().equals(itemRecord.getAlbumArtist()));
 	}
 
 	@Test
 	public void testSetTitle() throws FileNotFoundException {
 		CdItemRecord itemRecord = CdItemRecord.fromInfoFile(AllArcCdCliTests.INFO_00022);
 		CueSheet testInstance = new CueSheet();
 		testInstance.setTitle(itemRecord.getTitle());
 		assertTrue(testInstance.getTitle().equals(itemRecord.getTitle()));
 	}
 
 	@Test
 	public void testAddFile() {
 		CueSheet testInstance = new CueSheet();
 		FileData fileData = new FileData(testInstance);
 		fileData.setFile("./00022.bin");
 		fileData.setFileType("BINARY");
 		testInstance.getFileData().add(fileData);
 		assertTrue(testInstance.getFileData().size() == 1);
 		assertTrue(testInstance.getFileData().get(0).getFile().equals("./00022.bin"));
 		assertTrue(testInstance.getFileData().get(0).getFileType().equals("BINARY"));
 	}
 
 	@Test
 	public void testAddTrack() throws FileNotFoundException {
 		CdItemRecord itemRecord = CdItemRecord.fromInfoFile(AllArcCdCliTests.INFO_00022);
 		CueSheet testInstance = new CueSheet();
 		FileData fileData = new FileData(testInstance);
 		fileData.setFile("./00022.bin");
 		fileData.setFileType("BINARY");
 		int trackNumber = 1;
 		for (CdTrack track : itemRecord.getTracks()) {
 			TrackData td = new TrackData(fileData, trackNumber, "AUDIO");
 			td.setTitle(track.getTitle());
 			
 		}
 	}
 }
