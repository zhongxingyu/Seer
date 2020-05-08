 package c301.AdventureBook.test;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import android.test.AndroidTestCase;
 import c301.AdventureBook.Models.Page;
 import c301.AdventureBook.Models.Story;
 
 /**
  * JUnit test case for the Story model.
  * 
  * @author William
  *
  */
 public class StoryTest extends AndroidTestCase {
 	private Story story;
 	private String title = "C301";
 	private String description = "This is a test";
 	private String uuid = UUID.randomUUID().toString();
 	private String author = "william";
 	private String date = "2013.11.6";
	private String imagePath = "I don't know";
 	private String imageByte = "random imagebyte";
 	public List<Page> pages = new ArrayList<Page>();
 
 	@Before
 	protected void setUp() throws Exception {
		story = new Story(title, description, author, date, imagePath, imageByte);
 	}
 
 	@After
 	protected void tearDown() throws Exception {
 	}
 
 	/*
      * Test that the getter methods give data matching the constructor.
      */
 	@Test
 	public void testGetter() {
		assertEquals(imagePath, story.getImagePath());
 		assertEquals(date, story.getDate());
 		assertEquals(title, story.getTitle());
 		assertEquals(description, story.getDescription());
 		assertEquals(author, story.getAuthor());
 
 	}
 
 	/**
 	 * Test adding and deleting pages.
 	 */
 	@Test
 	public void testAddDeletePages() {
 		Page page1 = new Page(title, description, uuid);
 		String title2 = "this is 2 test";
 		String description2 = "just a test";
 		String uuid2 = UUID.randomUUID().toString();
 		Page page2 = new Page(title2, description2, uuid2);
 		this.pages.add(page1);
 	    assertEquals(pages.get(0), page1);
 	    this.pages.remove(page1);
 	    this.pages.add(page2);
 	    assertEquals(pages.get(0), page2);
 	}
 	
 	/**
 	 * Test that the getFilename() method returns a filename in the correct
 	 * format.
 	 */
 	@Test
 	public void testGetFilename() {
 		String filename = story.getTitle() + "-" + story.getAuthor() + ".sav";
 		assertEquals(filename, story.getFilename());
 	}
 
 }
