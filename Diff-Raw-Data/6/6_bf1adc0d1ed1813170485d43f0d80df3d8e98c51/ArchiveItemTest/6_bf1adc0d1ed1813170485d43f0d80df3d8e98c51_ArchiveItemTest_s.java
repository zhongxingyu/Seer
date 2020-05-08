 /**
  * 
  */
 package org.opf_labs.arc_cd.collection;
 
import static org.junit.Assert.*;
 
import org.junit.AfterClass;
import org.junit.BeforeClass;
 import org.junit.Test;
 import org.opf_labs.arc_cd.AllArcCdCliTests;
 import org.opf_labs.arc_cd.cdrdao.toc.TocItemRecord;
 import org.opf_labs.audio.CueSheet;
 
 /**
  * @author carl
  *
  */
 @SuppressWarnings("static-method")
 public class ArchiveItemTest {
 
 	/**
 	 * Test method for {@link org.opf_labs.arc_cd.collection.ArchiveItem#getId()}.
 	 */
 	@Test
 	public final void testGetId() {
 		assertTrue(AllArcCdCliTests.ITEM_00022.getId().equals(Integer.valueOf(22)));
 		assertTrue(AllArcCdCliTests.ITEM_00103.getId().equals(Integer.valueOf(103)));
 		assertTrue(AllArcCdCliTests.ITEM_00212.getId().equals(Integer.valueOf(212)));
 		assertTrue(AllArcCdCliTests.ITEM_00304.getId().equals(Integer.valueOf(304)));
 		assertTrue(AllArcCdCliTests.ITEM_00390.getId().equals(Integer.valueOf(390)));
 		assertTrue(AllArcCdCliTests.ITEM_00429.getId().equals(Integer.valueOf(429)));
 	}
 
 	/**
 	 * Test method for {@link org.opf_labs.arc_cd.collection.ArchiveItem#getItem()}.
 	 */
 	@Test
 	public final void testGetItem() {
 		ArchiveItem testItem = AllArcCdCliTests.ITEM_00022;
 		CdItemRecord testInfo = testItem.getItem().getCdDetails();
 		assertFalse(testInfo.equals(CdItemRecord.DEFAULT_ITEM));
 		CdItemRecord testEquals = CdItemRecord.fromInfoFile(AllArcCdCliTests.INFO_00022);
 		assertFalse(testInfo == testEquals);
 		assertTrue(testInfo.equals(testEquals));
 	}
 
 	/**
 	 * Test method for {@link org.opf_labs.arc_cd.collection.ArchiveItem#getInfo()}.
 	 */
 	@Test
 	public final void testGetInfo() {
 		ArchiveItem testItem = AllArcCdCliTests.ITEM_00304;
 		CdItemRecord testInfo = testItem.getInfo();
 		assertFalse(testInfo.equals(CdItemRecord.DEFAULT_ITEM));
 		CdItemRecord testEquals = CdItemRecord.fromInfoFile(AllArcCdCliTests.INFO_00304);
 		assertFalse(testInfo == testEquals);
 		assertTrue(testInfo.equals(testEquals));
 	}
 
 	/**
 	 * Test method for {@link org.opf_labs.arc_cd.collection.ArchiveItem#getToc()}.
 	 */
 	@Test
 	public final void testGetToc() {
 		ArchiveItem testItem = AllArcCdCliTests.ITEM_00212;
 		TocItemRecord testToc = testItem.getToc();
 		assertFalse(testToc.equals(TocItemRecord.defaultInstance()));
 		TocItemRecord testEquals = TocItemRecord.fromTocFile(testItem.getTocFile());
 		assertFalse(testToc == testEquals);
 		assertTrue(testToc.equals(testEquals));
 	}
 
 	/**
 	 * Test method for {@link org.opf_labs.arc_cd.collection.ArchiveItem#getCue()}.
 	 */
 	@Test
 	public final void testGetCue() {
 		ArchiveItem testItem = AllArcCdCliTests.ITEM_00103;
 		CueSheet testCue = testItem.getCue();
 		assertFalse(testCue.equals(ArchiveItem.DEFAULT_CUE));
 	}
 
 	/**
 	 * Test method for {@link org.opf_labs.arc_cd.collection.ArchiveItem#getRecordedManifest()}.
 	 */
 	@Test
 	public final void testGetRecordedManifest() {
 		ArchiveItem testItem = AllArcCdCliTests.ITEM_00429;
 		ItemManifest testManifest = testItem.getRecordedManifest();
 		ItemManifest test = ItemManifest.fromManifestFile(testItem.getManifestFile());
 		assertFalse(testManifest.equals(ItemManifest.defaultInstance()));
 		assertTrue(testManifest.equals(test));
 	}
 
 	/**
 	 * Test method for {@link org.opf_labs.arc_cd.collection.ArchiveItem#checkManifest()}.
 	 */
 	@Test
 	public final void testCheckManifest() {
 		assertTrue(AllArcCdCliTests.ITEM_00022.checkManifest().hasPassed());
 		assertFalse(AllArcCdCliTests.ITEM_00429.checkManifest().hasPassed());
 	}
 
 	/**
 	 * Test method for {@link org.opf_labs.arc_cd.collection.ArchiveItem#createCue()}.
 	 */
 	@Test
 	public final void testCreateCue() {
 		ArchiveItem testItem = AllArcCdCliTests.ITEM_00212;
 		if (testItem.hasCue())
 			assertTrue( "Couldn't delete cue file.", testItem.getCueFile().delete());
 		assertTrue(testItem.createCue());
 		assertTrue(testItem.hasCue());
 		assertTrue(testItem.getCue().getAllTrackData().size() == testItem.getInfo().getTracks().size());
 		testItem.getCueFile().delete();
 		assertFalse(testItem.hasCue());
 	}
 
 }
