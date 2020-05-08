 package c301.AdventureBook.test;
 
 import java.util.ArrayList;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import android.content.Context;
 import android.test.AndroidTestCase;
 import c301.AdventureBook.Controllers.LibraryManager;
 import c301.AdventureBook.Controllers.StoryManager;
 import c301.AdventureBook.Models.Story;
 
 /**
  * JUnit test case for the LibraryManager controller.
  * 
  * @author tyleung
  *
  */
 public class LibraryManagerTest extends AndroidTestCase {
 
 	private LibraryManager lManager;
 	private StoryManager sManager;
 	private Context context;
 	private Story story;
 	
 	@Before
 	protected void setUp() {
 		context = getContext();
 		lManager = LibraryManager.getInstance();
 		lManager.initContext(context);
 		
		story = new Story("Titletest", "Descriptiontest", "Authortest", "Datetest", "Imagebytetest");
 		
 		
 	}
 	
 	@Test
 	public void test() {
 		fail("Not yet implemented");
 	}
 
 }
