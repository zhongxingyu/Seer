 package edu.bu.cs673.AwesomeAlphabet.model;
 
 import javax.swing.ImageIcon;
 import org.junit.*;
 
 import edu.bu.cs673.AwesomeAlphabet.model.WordPictureSound;
 import static org.junit.Assert.*;
 
 /**
  * The class <code>WordPictureSoundTest</code> contains tests for the class <code>{@link WordPictureSound}</code>.
  *
  * @generatedBy CodePro at 2/22/13 1:46 AM
  * @author Levi
  * @version $Revision: 1.0 $
  */
 public class WordPictureSoundTest {
 	/**
 	 * Run the WordPictureSound(String,String,String) constructor test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 2/22/13 1:46 AM
 	 */
 	@Test
 	public void testWordPictureSound_1()
 		throws Exception {
 		String word = "";
 		String imageFile = "";
 		String soundFile = "";
 		char letter = 'c';
 
		WordPictureSound result = new WordPictureSound(letter, word, imageFile, soundFile, new Theme(Theme.DEFAULT_THEME_NAME));
 
 		// add additional test code here
 		assertNotNull(result);
 		assertEquals("", result.GetWordString());
 	}
 
 	/**
 	 * Run the ImageIcon GetWordImage(int,int) method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 2/22/13 1:46 AM
 	 */
 	@Test
 	public void testGetWordImage_1()
 		throws Exception {
 		WordPictureSound fixture = new WordPictureSound('c', "", "", "", new Theme(ThemeManager.DEFAULT_THEME_NAME));
 		int width = 1;
 		int height = 1;
 
 		ImageIcon result = fixture.GetWordImage(width, height);
 
 		// add additional test code here
 		assertEquals(null, result);
 	}
 
 	/**
 	 * Run the ImageIcon GetWordImage(int,int) method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 2/22/13 1:46 AM
 	 */
 	@Test
 	public void testGetWordImage_2()
 		throws Exception {
 		WordPictureSound fixture = new WordPictureSound('c', "", "", "", new Theme(ThemeManager.DEFAULT_THEME_NAME));
 		int width = 1;
 		int height = 1;
 
 		ImageIcon result = fixture.GetWordImage(width, height);
 
 		// add additional test code here
 		assertEquals(null, result);
 	}
 
 	/**
 	 * Run the String GetWordString() method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 2/22/13 1:46 AM
 	 */
 	@Test
 	public void testGetWordString_1()
 		throws Exception {
 		WordPictureSound fixture = new WordPictureSound('c', "", "", "", new Theme(ThemeManager.DEFAULT_THEME_NAME));
 
 		String result = fixture.GetWordString();
 
 		// add additional test code here
 		assertEquals("", result);
 	}
 
 	/**
 	 * Run the void PlaySound() method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 2/22/13 1:46 AM
 	 */
 	@Test
 	public void testPlaySound_1()
 		throws Exception {
 		WordPictureSound fixture = new WordPictureSound('c',"", "", "", new Theme(ThemeManager.DEFAULT_THEME_NAME));
 
 		fixture.PlaySound();
 
 		// add additional test code here
 	}
 
 	/**
 	 * Perform pre-test initialization.
 	 *
 	 * @throws Exception
 	 *         if the initialization fails for some reason
 	 *
 	 * @generatedBy CodePro at 2/22/13 1:46 AM
 	 */
 	@Before
 	public void setUp()
 		throws Exception {
 		// add additional set up code here
 	}
 
 	/**
 	 * Perform post-test clean-up.
 	 *
 	 * @throws Exception
 	 *         if the clean-up fails for some reason
 	 *
 	 * @generatedBy CodePro at 2/22/13 1:46 AM
 	 */
 	@After
 	public void tearDown()
 		throws Exception {
 		// Add additional tear down code here
 	}
 
 	/**
 	 * Launch the test.
 	 *
 	 * @param args the command line arguments
 	 *
 	 * @generatedBy CodePro at 2/22/13 1:46 AM
 	 */
 	public static void main(String[] args) {
 		new org.junit.runner.JUnitCore().run(WordPictureSoundTest.class);
 	}
 }
