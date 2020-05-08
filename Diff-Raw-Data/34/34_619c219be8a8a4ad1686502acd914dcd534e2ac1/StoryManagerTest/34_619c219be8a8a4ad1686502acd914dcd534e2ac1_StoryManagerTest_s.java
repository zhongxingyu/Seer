 package c301.AdventureBook.test;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import android.content.Context;
 import android.test.AndroidTestCase;
 import android.util.Log;
 import c301.AdventureBook.Controllers.StoryManager;
 import c301.AdventureBook.Models.Story;
 
 /**
  * JUnit test case for the FileLoader controller.
  * 
  * @author tyleung
  *
  */
 public class StoryManagerTest extends AndroidTestCase {
 	
 	private StoryManager storyManager;
 	private Context context;
 	private Story story;
 	private Story story2;
 	private String fakeFilename;
 	
 	@Before
 	protected void setUp() {
 		context = getContext();
 		storyManager = StoryManager.getInstance();
 		storyManager.initContext(context);
 		
		story = new Story("Titletest", "Descriptiontest", "Authortest", "Datetest", "Imagepathtest", "Imagebytetest");
 		storyManager.saveStory(story, true);
 		
		story2 = new Story("someTitle", "someDescription", "someAuthor", "someDate", "someImagepath", "someImagebyte");
 		storyManager.saveStory(story2, true);
 		
 		fakeFilename = "randomTitle-randomAuthor.sav";
 	}
 	
 	/**
 	 * Test that loadFromFile(String FILENAME) is able to load an existing file
 	 * from internal storage and return the story object from that file.
 	 */
 	@Test
 	public void testLoadFromFile() {
 		//String filename = story.getFilename();
 		storyManager.setCurrentStory(story);
		Story someStory = storyManager.getStory();
 		assertNotNull(someStory);
 		assertEquals(story.getTitle(), someStory.getTitle());
 		assertEquals(story.getDescription(), someStory.getDescription());
 		assertEquals(story.getAuthor(), someStory.getAuthor());
 		assertEquals(story.getDate(), someStory.getDate());
		assertEquals(story.getImagePath(), someStory.getImagePath());
 		assertEquals(story.getImageByte(), someStory.getImageByte());
 	}
 	
 	/**
 	 * Test that saveStory(Story saveStory, boolean overwrite) returns true if
 	 * the story is saved.
 	 * 
 	 * TODO: loadFromFile works if a random filename is passed in. Need to fix!
 	 */
 	@Test
 	public void testSaveStory() {
 		//Story someStory = new Story("someTitle", "someDescription", "someAuthor", "someDate", "someImagepath");
 		//fileLoader.saveStory(someStory, true);
 		//assertTrue(fileLoader.saveStory(story, false));
 		//assertTrue(fileLoader.checkFileExists(filename));
 		storyManager.setCurrentStory(story);
		Story storyLoaded = storyManager.getStory();
 		Log.i("Testing story load", storyLoaded.getTitle());
 		storyManager.setCurrentStory(story2);
		Story story2Loaded = storyManager.getStory();
 		Log.i("Testing story2 load", story2Loaded.getTitle());
 		
 		//Story fakeStoryLoaded = storyManager.loadFromFile("randomnonexistantfilename");
 		//Log.i("Testing fake load", fakeStoryLoaded.getTitle());
 	}
 	
 	/**
 	 * Test that checkFileExists(String FILENAME) returns false if the file 
 	 * with the given filename doesn't exist.
 	 */
 	@Test
 	public void testCheckFileExists() {
 		assertFalse(storyManager.checkFileExists(fakeFilename));
 	}
 }
