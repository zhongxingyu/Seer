 package edu.cis350.mosstalkwords.test;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import android.os.Bundle;
 import android.os.Parcel;
 
 import edu.cis350.mosstalkwords.ImageStatistics;
 import junit.framework.TestCase;
 
 public class ImageStatisticsTest extends TestCase {
 
 	private ImageStatistics imgStat;
 	private long calendarTime = Calendar.getInstance().getTimeInMillis();
 			
 	public void setUp() {
 		Calendar c = Calendar.getInstance();
 		c.setTime(new Date(calendarTime));
 		imgStat = new ImageStatistics("imgName", "url", "category", true, 2, 3, 4, true, true, c);		
 	}
 	
 	
 	public void testWriteToParcel() {
 		// Write to Parcel 
 		Bundle b = new Bundle();
 		b.putParcelable("tagForImageStatistics", imgStat);
 		Parcel p = Parcel.obtain();
 		b.writeToParcel(p, 0);
 		// Read from Parcel 
 		p.setDataPosition(0);
 		Bundle b2 = p.readBundle();
 		b2.setClassLoader(ImageStatistics.class.getClassLoader());
 		ImageStatistics imgStat2 = b2.getParcelable("tagForImageStatistics");
 					    
 		// Compare imgStat and imgStat2
 		assertTrue(imgStat2.getImageName().equals("imgName"));
 		assertTrue(imgStat2.getUrl().equals("url"));
 		assertTrue(imgStat2.getCategory().equals("category"));
 		assertTrue(imgStat2.getIsFavorite());
 		assertEquals(imgStat2.getWordHints(),2);
 		assertEquals(imgStat2.getSoundHints(),3);
 		assertEquals(imgStat2.getAttempts(),4);
 		assertTrue(imgStat2.isSolved());
 		assertTrue(imgStat2.isSeenToday());
 		assertEquals(imgStat2.getLastSeen().getTimeInMillis(), calendarTime);	
 	}
 
 	public void testResetImageStatistics() {
 		imgStat.resetImageStatistics();
 		assertEquals(imgStat.getWordHints(),0);
 		assertEquals(imgStat.getSoundHints(),0);
 		assertEquals(imgStat.getAttempts(),0);
 		assertFalse(imgStat.isSolved());
 		assertTrue(imgStat.isSeenToday());
 	}
 
 	public void testGetImageName() {
 		assertTrue(imgStat.getImageName().equals("imgName"));
 	}
 
 	public void testSetImageName() {
 		imgStat.setImageName("imgName2");
 		assertTrue(imgStat.getImageName().equals("imgName2"));
 	}
 
 	public void testGetUrl() {
 		assertTrue(imgStat.getUrl().equals("url"));
 	}
 
 	public void testSetUrl() {
 		imgStat.setUrl("url2");
 		assertTrue(imgStat.getUrl().equals("url2"));
 	}
 
 	public void testGetCategory() {
 		assertTrue(imgStat.getCategory().equals("category"));
 	}
 
 	public void testSetCategory() {
 		imgStat.setCategory("category2");
 		assertTrue(imgStat.getCategory().equals("category2"));
 	}
 
 	public void testGetIsFavorite() {
 		assertTrue(imgStat.getIsFavorite());
 	}
 
 	public void testSetIsFavorite() {
 		imgStat.setIsFavorite(false);
 		assertFalse(imgStat.getIsFavorite());
 	}
 
 	public void testGetWordHints() {
 		assertEquals(imgStat.getWordHints(),2);
 	}
 
 	public void testSetWordHints() {
 		imgStat.setWordHints(3);
 		assertEquals(imgStat.getWordHints(),3);
 	}
 
 	public void testGetSoundHints() {
 		assertEquals(imgStat.getSoundHints(),3);
 	}
 
 	public void testSetSoundHints() {
 		imgStat.setSoundHints(4);
 		assertEquals(imgStat.getSoundHints(),4);
 	}
 
 	public void testGetAttempts() {
 		assertEquals(imgStat.getAttempts(), 4);
 	}
 
 	public void testSetAttempts() {
 		imgStat.setAttempts(5);
 		assertEquals(imgStat.getAttempts(), 5);
 	}
 
 	public void testIsSolved() {
 		assertTrue(imgStat.isSolved());
 	}
 
 	public void testSetSolved() {
 		imgStat.setSolved(false);
 		assertFalse(imgStat.isSolved());
 	}
 
 	public void testIsSeenToday() {
 		assertTrue(imgStat.isSeenToday());
 	}
 
 	public void testSetSeenToday() {
 		imgStat.setSeenToday(false);
 		assertFalse(imgStat.isSeenToday());
 	}
 
 	public void testGetLastSeen() {
 		assertEquals(imgStat.getLastSeen().getTimeInMillis(), calendarTime);
 	}
 
 	public void testSetLastSeen() {
 		Calendar c = Calendar.getInstance();
 		imgStat.setLastSeen(c);
 		assertEquals(imgStat.getLastSeen(), c);
 	}
 
 	public void testEqualsObject() {
 		Calendar c = Calendar.getInstance();
 		c.setTime(new Date(calendarTime));
 		ImageStatistics imgStat2 = new ImageStatistics("imgName", "url", "category", true, 2, 3, 4, true, true, c);	
 		assertTrue(imgStat.equals(imgStat2));
 	}
 
 }
