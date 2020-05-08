 package com.github.mnicky.bible4j.storage;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.testng.Assert;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import com.github.mnicky.bible4j.data.BibleBook;
 import com.github.mnicky.bible4j.data.BibleVersion;
 import com.github.mnicky.bible4j.data.Bookmark;
 import com.github.mnicky.bible4j.data.Note;
 import com.github.mnicky.bible4j.data.Position;
 import com.github.mnicky.bible4j.data.Verse;
 import com.github.mnicky.bible4j.data.Note.NoteType;
 
 import static com.github.mnicky.bible4j.storage.H2DbNaming.*;
 
 /**
  * Unit tests for H2DbBibleStorage class.
  */
 public final class H2DbBibleStorage_Test {
 
     private Connection conn;
     private H2DbBibleStorage bible;
 
     @BeforeMethod
     public void setUpTest() {
 	try {
 	    conn = DriverManager.getConnection("jdbc:h2:mem:test", "test", "");
 
 	    // for debugging purposes:
 	    // conn = DriverManager.getConnection("jdbc:h2:tcp://localhost/mem:test", "test", "");
 
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	bible = new H2DbBibleStorage(conn);
 
     }
 
     @AfterMethod
     public void tearDownTest() {
 	try {
 	    conn.close();
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
     }
 
     @Test
    public void shouldCreateBibleStorage() {
 
 	// expected numbers of column updates
 	// see Statement.executeBatch() javadoc for more info
	int[] exp = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
 
 	int[] columns = null;
 
 	try {
 	    columns = bible.initializeStorage();
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	Assert.assertTrue(Arrays.equals(columns, exp));
 
     }
 
     @Test
     public void shouldCloseBibleStorage() {
 	try {
 	    bible.close();
 	    Assert.assertTrue(conn.isClosed());
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
     }
 
     @Test
     public void shouldInsertBibleBook() {
 
 	Object[] exp = { "baruch", true };
 	Object[] actual = new Object[2];
 
 	try {
 	    bible.initializeStorage();
 	    bible.insertBibleBook(BibleBook.BARUCH);
 
 	    Statement st = conn.createStatement();
 	    ResultSet rs = st
 		    .executeQuery("SELECT " + BOOK_NAME_F + ", " + BOOK_DEUT_F + " FROM " + BOOKS + " WHERE " + BOOK_NAME_F + " = 'baruch' LIMIT 1");
 
 	    int i = 0;
 	    while (rs.next()) {
 		actual[i++] = rs.getString(1);
 		actual[i++] = rs.getBoolean(2);
 	    }
 	    if (rs != null)
 		rs.close();
 	    if (st != null)
 		st.close();
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 
 	Assert.assertTrue(Arrays.deepEquals(actual, exp));
 
     }
 
     @Test
     public void shouldInsertPosition() {
 
 	Object[] exp = { "john", 3, 16 };
 	Object[] actual = new Object[3];
 
 	try {
 	    bible.initializeStorage();
 	    bible.insertBibleBook(BibleBook.JOHN);
 	    bible.insertPosition(new Position(BibleBook.JOHN, 3, 16));
 
 	    Statement st = conn.createStatement();
 	    ResultSet rs = st
 		    .executeQuery("SELECT " + BOOK_NAME_F + ", " + COORD_CHAPT_F + ", " + COORD_VERSE_F
 			    + " FROM " + COORDS + ", " + BOOKS + " WHERE " + BOOK_ID_F + " = " + COORD_BOOK_F+ " LIMIT 1");
 
 	    int i = 0;
 	    while (rs.next()) {
 		actual[i++] = rs.getString(1);
 		actual[i++] = rs.getInt(2);
 		actual[i++] = rs.getInt(3);
 	    }
 	    if (rs != null)
 		rs.close();
 	    if (st != null)
 		st.close();
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 
 	Assert.assertTrue(Arrays.deepEquals(actual, exp));
 
     }
 
     @Test
     public void shouldInsertBibleVersion() {
 
 	Object[] exp = {"Douay-Rheims", "D-R", "en" };
 	Object[] actual = new Object[3];
 
 	try {
 	    bible.initializeStorage();
 	    bible.insertBibleVersion(new BibleVersion("Douay-Rheims", "D-R", "en"));
 
 	    Statement st = conn.createStatement();
 	    ResultSet rs = st
 		    .executeQuery("SELECT " + VERSION_NAME_F + ", " + VERSION_ABBR_F + ", " + VERSION_LANG_F + " FROM " + VERSIONS
 			    + " WHERE " + VERSION_ABBR_F + " = 'D-R' LIMIT 1");
 
 	    int i = 0;
 	    while (rs.next()) {
 		actual[i++] = rs.getString(1);
 		actual[i++] = rs.getString(2);
 		actual[i++] = rs.getString(3);
 	    }
 	    if (rs != null)
 		rs.close();
 	    if (st != null)
 		st.close();
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 
 	Assert.assertTrue(Arrays.deepEquals(actual, exp));
 
     }
 
     @Test
     public void insertVerseShouldInsertVerse() {
 
 	Object[] exp = { "There was a man sent from God, whose name was John.", "john", 1, 6,
 		"English Standard Version", };
 	Object[] actual = new Object[5];
 
 	try {
 	    bible.initializeStorage();
 	    bible.insertBibleVersion(new BibleVersion("English Standard Version", "esv", "en"));
 	    bible.insertBibleBook(BibleBook.JOHN);
 	    bible.insertPosition(new Position(BibleBook.JOHN, 1, 6));
 	    bible.insertVerse(new Verse("There was a man sent from God, whose name was John.",
 					new Position(BibleBook.JOHN, 1, 6),
 					new BibleVersion("English Standard Version", "esv", "en")));
 
 	    Statement st = conn.createStatement();
 	    ResultSet rs = st
 		    .executeQuery("SELECT " + VERSE_TEXT_F + ", " + BOOK_NAME_F + ", " + COORD_CHAPT_F + ", "
 		                  	+ COORD_VERSE_F + ", " + VERSION_NAME_F + " FROM " + VERSIONS
 			    + " INNER JOIN " + VERSES + " ON " + VERSION_ID_F + " = " + VERSE_VERSION_F
 			    + " INNER JOIN " + COORDS + " ON " + VERSE_COORD_F + " = " + COORD_ID_F
 			    + " INNER JOIN " + BOOKS + " ON " + COORD_BOOK_F + " = " + BOOK_ID_F
 			    + " WHERE " + VERSE_TEXT_F + " = 'There was a man sent from God, whose name was John.' LIMIT 1");
 
 	    int i = 0;
 	    while (rs.next()) {
 		actual[i++] = rs.getString(1);
 		actual[i++] = rs.getString(2);
 		actual[i++] = rs.getInt(3);
 		actual[i++] = rs.getInt(4);
 		actual[i++] = rs.getString(5);
 	    }
 	    if (rs != null)
 		rs.close();
 	    if (st != null)
 		st.close();
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	Assert.assertTrue(Arrays.deepEquals(actual, exp));
     }
     
     @Test
     public void getBibleVersionShoulReturnBibleVersionSpecifiedByAbbr() {
 	BibleVersion exp = new BibleVersion("King's James Version", "KJV", "en");
 	BibleVersion retrieved = null;
 
 	try {
 	    //given
 	    bible.initializeStorage();
 	    bible.insertBibleVersion(new BibleVersion("Revised Standard Version", "RSV", "en"));
 	    bible.insertBibleVersion(new BibleVersion("King's James Version", "KJV", "en"));
 	    bible.insertBibleVersion(new BibleVersion("New International Version", "NIV", "en"));
 	    //when
 	    retrieved = bible.getBibleVersion("KJV");
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	//then
 	Assert.assertEquals(retrieved, exp);
     }
 
     @Test
     public void getAllBibleVersionsShoulReturnListofAllBibleVersions() {
 	List<BibleVersion> exp = new ArrayList<BibleVersion>();
 	exp.add(new BibleVersion("English Standard Version", "ESV", "en"));
 	exp.add(new BibleVersion("King's James Version", "KJV", "en"));
 	exp.add(new BibleVersion("New International Version", "NIV", "en"));
 
 	List<BibleVersion> retrieved = null;
 
 	try {
 	    //given
 	    bible.initializeStorage();
 	    bible.insertBibleVersion(new BibleVersion("King's James Version", "KJV", "en"));
 	    bible.insertBibleVersion(new BibleVersion("New International Version", "NIV", "en"));
 	    bible.insertBibleVersion(new BibleVersion("English Standard Version", "ESV", "en"));
 
 	    //when
 	    retrieved = bible.getAllBibleVersions();
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	//then
 	Assert.assertEquals(retrieved, exp);
     }
 
     @Test
     public void getVerseShouldReturnOneVerse() {
         Verse exp = new Verse("some little testing sample", new Position(BibleBook.ACTS, 11, 2), new BibleVersion("King's James Version", "KJV", "en"));
         Verse retrieved = null;
     
         try {
             bible.initializeStorage();
             bible.insertBibleVersion(new BibleVersion("King's James Version", "KJV", "en"));
             bible.insertBibleBook(BibleBook.ACTS);
             bible.insertPosition(new Position(BibleBook.ACTS, 11, 2));
             bible.insertVerse(new Verse("some little testing sample", new Position(BibleBook.ACTS, 11, 2),
         				new BibleVersion("King's James Version", "KJV", "en")));
     
             retrieved = bible.getVerse(new Position(BibleBook.ACTS, 11, 2), new BibleVersion("King's James Version", "KJV", "en"));
     
         } catch (Exception e) {
             e.printStackTrace();
             Assert.fail();
         }
         Assert.assertEquals(retrieved, exp);
     }
 
     @Test
     public void getVersesShouldRetrieveListOfAllRequestedVerses() {
 
 	List<Verse> exp = new ArrayList<Verse>();
 	exp.add(new Verse("test text1", new Position(BibleBook.ACTS, 1, 2), new BibleVersion("King's James Version", "KJV", "en")));
 	exp.add(new Verse("test text2", new Position(BibleBook.ACTS, 1, 3), new BibleVersion("King's James Version", "KJV", "en")));
 	exp.add(new Verse("test text3", new Position(BibleBook.ACTS, 1, 4), new BibleVersion("King's James Version", "KJV", "en")));
 
 	List<Verse> retrieved = null;
 
 	try {
 	    bible.initializeStorage();
 	    bible.insertBibleVersion(new BibleVersion("King's James Version", "KJV", "en"));
 	    bible.insertBibleBook(BibleBook.ACTS);
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 2));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 3));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 4));
 	    bible.insertVerse(new Verse("test text1", new Position(BibleBook.ACTS, 1, 2),
 					new BibleVersion("King's James Version", "KJV", "en")));
 	    bible.insertVerse(new Verse("test text2", new Position(BibleBook.ACTS, 1, 3),
 					new BibleVersion("King's James Version", "KJV", "en")));
 	    bible.insertVerse(new Verse("test text3", new Position(BibleBook.ACTS, 1, 4),
 					new BibleVersion("King's James Version", "KJV", "en")));
 
 	    List<Position> positions = new ArrayList<Position>();
 	    positions.add(new Position(BibleBook.ACTS, 1, 2));
 	    positions.add(new Position(BibleBook.ACTS, 1, 3));
 	    positions.add(new Position(BibleBook.ACTS, 1, 4));
 
 	    retrieved = bible.getVerses(positions, new BibleVersion("KJV", "en"));
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	Assert.assertEquals(retrieved, exp);
     }
     
     @Test
     public void getChapterShouldReturnAllVersesFromSpecifiedChapter() {
 
 	List<Verse> exp = new ArrayList<Verse>();
 	exp.add(new Verse("test text1", new Position(BibleBook.ACTS, 1, 2), new BibleVersion("King's James Version", "KJV", "en")));
 	exp.add(new Verse("test text2", new Position(BibleBook.ACTS, 1, 3), new BibleVersion("King's James Version", "KJV", "en")));
 	exp.add(new Verse("test text3", new Position(BibleBook.ACTS, 1, 4), new BibleVersion("King's James Version", "KJV", "en")));
 
 	List<Verse> retrieved = null;
 
 	try {
 	    //given
 	    bible.initializeStorage();
 	    bible.insertBibleVersion(new BibleVersion("King's James Version", "KJV", "en"));
 	    bible.insertBibleBook(BibleBook.ACTS);
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 2));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 3));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 4));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 2, 2));
 	    bible.insertVerse(new Verse("test text1", new Position(BibleBook.ACTS, 1, 2), new BibleVersion("King's James Version", "KJV", "en")));
 	    bible.insertVerse(new Verse("test text2", new Position(BibleBook.ACTS, 1, 3), new BibleVersion("King's James Version", "KJV", "en")));
 	    bible.insertVerse(new Verse("test text3", new Position(BibleBook.ACTS, 1, 4), new BibleVersion("King's James Version", "KJV", "en")));
 	    bible.insertVerse(new Verse("test text4", new Position(BibleBook.ACTS, 2, 2), new BibleVersion("King's James Version", "KJV", "en")));
 
 	    Position chapter = new Position(BibleBook.ACTS, 1, 0);
 
 	    //when
 	    retrieved = bible.getChapter(chapter, new BibleVersion("King's James Version", "KJV", "en"));
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	//then
 	Assert.assertEquals(retrieved, exp);
     }
     
     @Test
     public void getChapterListShouldReturnAllChaptersInSpecifiedBibleVersion() {
 	List<Position> exp = new ArrayList<Position>();
 	exp.add(new Position(BibleBook.LUKE, 4, 0));
 	exp.add(new Position(BibleBook.ACTS, 1, 0));
 	exp.add(new Position(BibleBook.ACTS, 2, 0));
 	exp.add(new Position(BibleBook.ACTS, 4, 0));
 
 	List<Position> retrieved = null;
 
 	try {
 	    //given
 	    bible.initializeStorage();
 	    bible.insertBibleVersion(new BibleVersion("King's James Version", "KJV", "en"));
 	    bible.insertBibleVersion(new BibleVersion("New International Version", "NIV", "en"));
 	    bible.insertBibleBook(BibleBook.ACTS);
 	    bible.insertBibleBook(BibleBook.JOB);
 	    bible.insertBibleBook(BibleBook.LUKE);
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 1));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 2, 2));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 3, 3));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 4, 4));
 	    bible.insertPosition(new Position(BibleBook.JOB, 4, 4));
 	    bible.insertPosition(new Position(BibleBook.LUKE, 4, 2));
 	    bible.insertPosition(new Position(BibleBook.LUKE, 4, 4));
 	    bible.insertVerse(new Verse("test text1", new Position(BibleBook.ACTS, 1, 1), new BibleVersion("King's James Version", "KJV", "en")));
 	    bible.insertVerse(new Verse("test text2", new Position(BibleBook.ACTS, 2, 2), new BibleVersion("King's James Version", "KJV", "en")));
 	    bible.insertVerse(new Verse("test text3", new Position(BibleBook.ACTS, 3, 3), new BibleVersion("New International Version", "NIV", "en")));
 	    bible.insertVerse(new Verse("test text4", new Position(BibleBook.ACTS, 4, 4), new BibleVersion("King's James Version", "KJV", "en")));
 	    bible.insertVerse(new Verse("test text5", new Position(BibleBook.LUKE, 4, 2), new BibleVersion("King's James Version", "KJV", "en")));
 	    bible.insertVerse(new Verse("test text6", new Position(BibleBook.LUKE, 4, 4), new BibleVersion("King's James Version", "KJV", "en")));
 
 	    //when
 	    retrieved = bible.getChapterList(new BibleVersion("KJV", "en"));
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	//then
 	Assert.assertEquals(retrieved, exp);
     }
     
     @Test
     public void compareVersesForOnePositionShouldRetrieveListOfAllRequestedVerses() {
 
 	int numOfVersionsToTest = 9;
 
 	List<Verse> exp = new ArrayList<Verse>();
 
 	for (int i = 0; i < numOfVersionsToTest; i++)
 	    exp.add(new Verse("x2test text" + (i + 1), new Position(BibleBook.ACTS, 1, 2),
 			      new BibleVersion("King's James Version", "KJV" + (i + 1), "en")));
 
 	List<Verse> retrieved = null;
 
 	try {
 	    bible.initializeStorage();
 
 	    for (int i = 0; i < numOfVersionsToTest; i++)
 		bible.insertBibleVersion(new BibleVersion("King's James Version", "KJV" + (i + 1), "en"));
 
 	    bible.insertBibleBook(BibleBook.ACTS);
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 2));
 
 	    for (int i = 0; i < numOfVersionsToTest; i++)
 		bible.insertVerse(new Verse("x2test text" + (i + 1), new Position(BibleBook.ACTS, 1, 2),
 					    new BibleVersion("King's James Version", "KJV" + (i + 1), "en")));
 
 	    List<BibleVersion> versions = new ArrayList<BibleVersion>();
 
 	    for (int i = 0; i < numOfVersionsToTest; i++)
 		versions.add(new BibleVersion("King's James Version", "KJV" + (i + 1), "en"));
 
 	    retrieved = bible.compareVerses(new Position(BibleBook.ACTS, 1, 2), versions);
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	Assert.assertEquals(retrieved, exp);
     }
 
     @Test
     public void compareVersesForMorePositionsShouldRetrieveListOfAllRequestedVerses() {
 
 	List<Verse> exp = new ArrayList<Verse>();
 	exp.add(new Verse("test text1", new Position(BibleBook.ACTS, 1, 2), new BibleVersion("King's James Version1", "KJV1", "en")));
 	exp.add(new Verse("test text1", new Position(BibleBook.ACTS, 1, 3), new BibleVersion("King's James Version1", "KJV1", "en")));
 	exp.add(new Verse("test text1", new Position(BibleBook.ACTS, 1, 4), new BibleVersion("King's James Version1", "KJV1", "en")));
 
 	exp.add(new Verse("test text2", new Position(BibleBook.ACTS, 1, 2), new BibleVersion("King's James Version2", "KJV2", "en")));
 	exp.add(new Verse("test text2", new Position(BibleBook.ACTS, 1, 3), new BibleVersion("King's James Version2", "KJV2", "en")));
 	exp.add(new Verse("test text2", new Position(BibleBook.ACTS, 1, 4), new BibleVersion("King's James Version2", "KJV2", "en")));
 
 	exp.add(new Verse("test text3", new Position(BibleBook.ACTS, 1, 2), new BibleVersion("King's James Version3", "KJV3", "en")));
 	exp.add(new Verse("test text3", new Position(BibleBook.ACTS, 1, 3), new BibleVersion("King's James Version3", "KJV3", "en")));
 	exp.add(new Verse("test text3", new Position(BibleBook.ACTS, 1, 4), new BibleVersion("King's James Version3", "KJV3", "en")));
 
 	List<Verse> retrieved = null;
 
 	try {
 	    bible.initializeStorage();
 	    bible.insertBibleVersion(new BibleVersion("King's James Version1", "KJV1", "en"));
 	    bible.insertBibleVersion(new BibleVersion("King's James Version2", "KJV2", "en"));
 	    bible.insertBibleVersion(new BibleVersion("King's James Version3", "KJV3", "en"));
 	    bible.insertBibleBook(BibleBook.ACTS);
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 2));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 3));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 4));
 	    bible.insertVerse(new Verse("test text1", new Position(BibleBook.ACTS, 1, 2),
 					new BibleVersion("King's James Version1", "KJV1", "en")));
 	    bible.insertVerse(new Verse("test text1", new Position(BibleBook.ACTS, 1, 3),
 					new BibleVersion("King's James Version1", "KJV1", "en")));
 	    bible.insertVerse(new Verse("test text1", new Position(BibleBook.ACTS, 1, 4),
 					new BibleVersion("King's James Version1", "KJV1", "en")));
 
 	    bible.insertVerse(new Verse("test text2", new Position(BibleBook.ACTS, 1, 2),
 					new BibleVersion("King's James Version2", "KJV2", "en")));
 	    bible.insertVerse(new Verse("test text2", new Position(BibleBook.ACTS, 1, 3),
 					new BibleVersion("King's James Version2", "KJV2", "en")));
 	    bible.insertVerse(new Verse("test text2", new Position(BibleBook.ACTS, 1, 4),
 					new BibleVersion("King's James Version2", "KJV2", "en")));
 
 	    bible.insertVerse(new Verse("test text3", new Position(BibleBook.ACTS, 1, 2),
 					new BibleVersion("King's James Version3", "KJV3", "en")));
 	    bible.insertVerse(new Verse("test text3", new Position(BibleBook.ACTS, 1, 3),
 					new BibleVersion("King's James Version3", "KJV3", "en")));
 	    bible.insertVerse(new Verse("test text3", new Position(BibleBook.ACTS, 1, 4),
 					new BibleVersion("King's James Version3", "KJV3", "en")));
 
 	    List<BibleVersion> versions = new ArrayList<BibleVersion>();
 	    versions.add(new BibleVersion("King's James Version1", "KJV1", "en"));
 	    versions.add(new BibleVersion("King's James Version2", "KJV2", "en"));
 	    versions.add(new BibleVersion("King's James Version3", "KJV3", "en"));
 	    List<Position> positions = new ArrayList<Position>();
 	    positions.add(new Position(BibleBook.ACTS, 1, 2));
 	    positions.add(new Position(BibleBook.ACTS, 1, 3));
 	    positions.add(new Position(BibleBook.ACTS, 1, 4));
 
 	    retrieved = bible.compareVerses(positions, versions);
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	Assert.assertEquals(retrieved, exp);
     }
 
     @Test
     public void insertBookmarkShouldInsertBookmark() {
 
 	Object[] exp = { "joel", "But this is that which was spoken by the prophet Joel;" };
 	Object[] actual = new Object[2];
 
 	try {
 	    // given
 	    bible.initializeStorage();
 	    bible.insertBibleVersion(new BibleVersion("King's James Version", "KJV", "en"));
 	    bible.insertBibleBook(BibleBook.ACTS);
 	    bible.insertPosition(new Position(BibleBook.ACTS, 2, 16));
 	    bible.insertVerse(new Verse("But this is that which was spoken by the prophet Joel;",
 					new Position(
 						     BibleBook.ACTS, 2, 16),
 					new BibleVersion("King's James Version", "KJV", "en")));
 
 	    // when
 	    bible.insertBookmark(new Bookmark(
 					      "joel",
 					      new Verse(
 							"But this is that which was spoken by the prophet Joel;",
 							new Position(
 								     BibleBook.ACTS, 2, 16),
 							new BibleVersion("King's James Version", "KJV", "en"))));
 
 	    Statement st = conn.createStatement();
 	    ResultSet rs = st
 		    .executeQuery("SELECT " + BKMARK_NAME_F + ", " + VERSE_TEXT_F + " FROM " + BKMARKS
 			    + " INNER JOIN " + VERSES + " ON " + VERSE_ID_F + " = " + BKMARK_VERSE_F
 			    + " WHERE " + BKMARK_NAME_F + " = 'joel' LIMIT 1");
 
 	    int i = 0;
 	    while (rs.next()) {
 		actual[i++] = rs.getString(1);
 		actual[i++] = rs.getString(2);
 	    }
 	    if (rs != null)
 		rs.close();
 	    if (st != null)
 		st.close();
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 
 	// then
 	Assert.assertTrue(Arrays.deepEquals(actual, exp));
 
     }
 
     @Test
     public void getBookmarksShouldRetrieveAllBookmarks() {
 	List<Bookmark> exp = new ArrayList<Bookmark>();
 	exp.add(new Bookmark("bkmark1", new Verse("test text1", new Position(BibleBook.ACTS, 1, 2),
 						  new BibleVersion("King's James Version", "KJV", "en"))));
 	exp.add(new Bookmark("bkmark2", new Verse("test text2", new Position(BibleBook.ACTS, 1, 3),
 						  new BibleVersion("King's James Version", "KJV", "en"))));
 	exp.add(new Bookmark("bkmark3", new Verse("test text3", new Position(BibleBook.ACTS, 1, 4),
 						  new BibleVersion("King's James Version", "KJV", "en"))));
 
 	List<Bookmark> retrieved = null;
 
 	try {
 	    // given
 	    bible.initializeStorage();
 	    bible.insertBibleVersion(new BibleVersion("King's James Version", "KJV", "en"));
 	    bible.insertBibleBook(BibleBook.ACTS);
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 2));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 3));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 4));
 
 	    bible.insertVerse(new Verse("test text1", new Position(BibleBook.ACTS, 1, 2),
 					new BibleVersion("King's James Version", "KJV", "en")));
 	    bible.insertVerse(new Verse("test text2", new Position(BibleBook.ACTS, 1, 3),
 					new BibleVersion("King's James Version", "KJV", "en")));
 	    bible.insertVerse(new Verse("test text3", new Position(BibleBook.ACTS, 1, 4),
 					new BibleVersion("King's James Version", "KJV", "en")));
 
 	    bible.insertBookmark(new Bookmark("bkmark1", new Verse("test text1", new Position(BibleBook.ACTS, 1, 2),
 								   new BibleVersion("King's James Version", "KJV", "en"))));
 	    bible.insertBookmark(new Bookmark("bkmark2", new Verse("test text2", new Position(BibleBook.ACTS, 1, 3),
 								   new BibleVersion("King's James Version", "KJV", "en"))));
 	    bible.insertBookmark(new Bookmark("bkmark3", new Verse("test text3", new Position(BibleBook.ACTS, 1, 4),
 								   new BibleVersion("King's James Version", "KJV", "en"))));
 
 	    // when
 	    retrieved = bible.getBookmarks();
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	// then
 	Assert.assertEquals(retrieved, exp);
     }
 
     @Test
     public void getBookmarksShouldRetrieveAllBookmarksForSpecifiedBibleVersion() {
 	List<Bookmark> exp = new ArrayList<Bookmark>();
 	exp.add(new Bookmark("bkmark1", new Verse("x1test text1", new Position(BibleBook.ACTS, 1, 2), new BibleVersion("King's James Version", "KJV", "en"))));
 	exp.add(new Bookmark("bkmark3", new Verse("x1test text3", new Position(BibleBook.ACTS, 1, 4), new BibleVersion("King's James Version", "KJV", "en"))));
 
 	List<Bookmark> retrieved = null;
 
 	try {
 	    // given
 	    bible.initializeStorage();
 	    bible.insertBibleVersion(new BibleVersion("King's James Version", "KJV", "en"));
 	    bible.insertBibleVersion(new BibleVersion("Rohacek", "ROH", "sk"));
 	    bible.insertBibleVersion(new BibleVersion("New International Version", "NIV", "en"));
 	    bible.insertBibleVersion(new BibleVersion("Ecav SR", "ECAV", "sk"));
 	    bible.insertBibleBook(BibleBook.ACTS);
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 2));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 3));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 4));
 
 	    bible.insertVerse(new Verse("x1test text1", new Position(BibleBook.ACTS, 1, 2), new BibleVersion("King's James Version", "KJV", "en")));
 	    bible.insertVerse(new Verse("x1test text2", new Position(BibleBook.ACTS, 1, 3), new BibleVersion("New International Version", "NIV", "en")));
 	    bible.insertVerse(new Verse("x1test text3", new Position(BibleBook.ACTS, 1, 4), new BibleVersion("King's James Version", "KJV", "en")));
 	    bible.insertVerse(new Verse("x1test text4", new Position(BibleBook.ACTS, 1, 4), new BibleVersion("Rohacek", "ROH", "sk")));
 	    bible.insertVerse(new Verse("x1test text5", new Position(BibleBook.ACTS, 1, 4), new BibleVersion("Ecav SR", "ECAV", "sk")));
 
 	    bible.insertBookmark(new Bookmark("bkmark1", new Verse("x1test text1", new Position(BibleBook.ACTS, 1, 2), new BibleVersion("King's James Version", "KJV", "en"))));
 	    bible.insertBookmark(new Bookmark("bkmark2", new Verse("x1test text2", new Position(BibleBook.ACTS, 1, 3), new BibleVersion("New International Version", "NIV", "en"))));
 	    bible.insertBookmark(new Bookmark("bkmark3", new Verse("x1test text3", new Position(BibleBook.ACTS, 1, 4), new BibleVersion("King's James Version", "KJV", "en"))));
 	    bible.insertBookmark(new Bookmark("bkmark4", new Verse("x1test text4", new Position(BibleBook.ACTS, 1, 4), new BibleVersion("Rohacek", "ROH", "sk"))));
 	    bible.insertBookmark(new Bookmark("bkmark5", new Verse("x1test text5", new Position(BibleBook.ACTS, 1, 4), new BibleVersion("Ecav SR", "ECAV", "sk"))));
 
 	    // when
 	    retrieved = bible.getBookmarks(new BibleVersion("King's James Version", "KJV", "en"));
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	// then
 	Assert.assertEquals(retrieved, exp);
     }
 
     @Test
     public void insertNoteShouldInsertNote() {
 	Object[] exp = { "note text", "acts", 2, 16, "u" };
 	Object[] actual = new Object[5];
 
 	try {
 	    // given
 	    bible.initializeStorage();
 	    bible.insertBibleVersion(new BibleVersion("King's James Version", "KJV", "en"));
 	    bible.insertBibleBook(BibleBook.ACTS);
 	    bible.insertPosition(new Position(BibleBook.ACTS, 2, 16));
 
 	    // when
 	    bible.insertNote(new Note("note text", new Position(BibleBook.ACTS, 2, 16), NoteType.USER_NOTE));
 
 	    Statement st = conn.createStatement();
 	    ResultSet rs = st
 		    .executeQuery("SELECT " + NOTE_TEXT_F + ", " + BOOK_NAME_F + ", " + COORD_CHAPT_F + ", " + COORD_VERSE_F + ", " + NOTE_TYPE_F
 			    + " FROM " + NOTES
 			    + " INNER JOIN " + COORDS + " ON " + COORD_ID_F + " = " + NOTE_COORD_F
 			    + " INNER JOIN " + BOOKS + " ON " + BOOK_ID_F + " = " + COORD_BOOK_F
 			    + " WHERE " + NOTE_TEXT_F + " = 'note text' LIMIT 1");
 
 	    int i = 0;
 	    while (rs.next()) {
 		actual[i++] = rs.getString(1);
 		actual[i++] = rs.getString(2);
 		actual[i++] = rs.getInt(3);
 		actual[i++] = rs.getInt(4);
 		actual[i++] = rs.getString(5);
 	    }
 	    if (rs != null)
 		rs.close();
 	    if (st != null)
 		st.close();
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 
 	// then
 	Assert.assertTrue(Arrays.deepEquals(actual, exp));
     }
 
     @Test
     public void getNotesShouldRetrieveAllNotesForSpecifiedPosition() {
 	List<Note> exp = new ArrayList<Note>();
 	exp.add(new Note("note text2", new Position(BibleBook.ACTS, 1, 4), NoteType.USER_NOTE));
 	exp.add(new Note("note text3", new Position(BibleBook.ACTS, 1, 4), NoteType.USER_NOTE));
 	exp.add(new Note("note text4", new Position(BibleBook.ACTS, 1, 4), NoteType.COMMENTARY));
 
 	List<Note> retrieved = null;
 
 	try {
 	    // given
 	    bible.initializeStorage();
 	    bible.insertBibleBook(BibleBook.ACTS);
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 2));
 	    bible.insertPosition(new Position(BibleBook.ACTS, 1, 4));
 
 	    bible.insertNote(new Note("note text1", new Position(BibleBook.ACTS, 1, 2), NoteType.USER_NOTE));
 	    bible.insertNote(new Note("note text2", new Position(BibleBook.ACTS, 1, 4), NoteType.USER_NOTE));
 	    bible.insertNote(new Note("note text3", new Position(BibleBook.ACTS, 1, 4), NoteType.USER_NOTE));
 	    bible.insertNote(new Note("note text4", new Position(BibleBook.ACTS, 1, 4), NoteType.COMMENTARY));
 
 	    // when
 	    retrieved = bible.getNotes(new Position(BibleBook.ACTS, 1, 4));
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	// then
 	Assert.assertEquals(retrieved, exp);
     }
 
     @Test
     public void searchVersesForTextShouldReturnAllVersesFound() {
 	List<Verse> exp = new ArrayList<Verse>();
 	exp.add(new Verse("search2 textik1", new Position(BibleBook.JOHN, 1, 6), new BibleVersion("King's James Version", "KJV", "en")));
 	exp.add(new Verse("search2 textik2", new Position(BibleBook.JOHN, 1, 7), new BibleVersion("King's James Version", "KJV", "en")));
 	List<Verse> actual = null;
 
 	try {
 	    // given
 	    bible.initializeStorage();
 	    bible.insertBibleVersion(new BibleVersion("King's James Version", "KJV", "en"));
 	    bible.insertBibleBook(BibleBook.JOHN);
 	    bible.insertPosition(new Position(BibleBook.JOHN, 1, 6));
 	    bible.insertPosition(new Position(BibleBook.JOHN, 1, 7));
 	    bible.insertVerse(new Verse("search1 textik1", new Position(BibleBook.JOHN, 1, 6), new BibleVersion("King's James Version", "KJV", "en")));
 	    bible.insertVerse(new Verse("search1 textik2", new Position(BibleBook.JOHN, 1, 7), new BibleVersion("King's James Version", "KJV", "en")));
 	    bible.insertVerse(new Verse("search2 textik1", new Position(BibleBook.JOHN, 1, 6), new BibleVersion("King's James Version", "KJV", "en")));
 	    bible.insertVerse(new Verse("search2 textik2", new Position(BibleBook.JOHN, 1, 7), new BibleVersion("King's James Version", "KJV", "en")));
 	    bible.insertVerse(new Verse("search3 textik1", new Position(BibleBook.JOHN, 1, 6), new BibleVersion("King's James Version", "KJV", "en")));
 	    bible.insertVerse(new Verse("search3 textik2", new Position(BibleBook.JOHN, 1, 7), new BibleVersion("King's James Version", "KJV", "en")));
 
 	    // when
 	    actual = bible.searchVersesForText("search2");
 
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    Assert.fail();
 	}
 	// then
 	Assert.assertEquals(actual, exp);
     }
 
 }
