 package edu.bu.cs673.AwesomeAlphabet.model;
 
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 
 import edu.bu.cs673.AwesomeAlphabet.model.Letter;
 import edu.bu.cs673.AwesomeAlphabet.model.ThemeManager;
 import edu.bu.cs673.AwesomeAlphabet.view.AlphabetPageView;
 
 import org.easymock.EasyMock;
 import org.junit.*;
 import static org.junit.Assert.*;
 
 /**
  * The class <code>LetterTest</code> contains tests for the class <code>{@link Letter}</code>.
  *
  * @generatedBy CodePro at 2/22/13 1:46 AM
  * @author Levi
  * @version $Revision: 1.0 $
  */
 public class LetterTest {
 	/**
 	 * Run the Letter(char) constructor test.
 	 * Verify a Letter object is created
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 2/22/13 1:46 AM
 	 */
 	@Test
 	public void testLetterObjectIsCreated()	throws Exception {
 		char cLetter = 'a';
 
 		Letter result = new Letter(cLetter, new ThemeManager());
 
 		// add additional test code here
 		assertNotNull(result);
 		/*assertEquals(null, result.getWord());
 		assertEquals('', result.GetUppercaseLetter());
 		assertEquals('', result.GetLetterAsChar());
 		assertEquals(false, result.hasChanged());
 		assertEquals(0, result.countObservers());*/
 	}
 
 	/**
 	 * Run the char GetLetterAsChar() method test.
 	 * Verify the method returns a letter as a lower case character
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 2/22/13 1:46 AM
 	 */
 	@Test
 	public void testGetLetterAsChar() throws Exception {
 		
 		Letter fixture = new Letter('a', new ThemeManager());
 
 		char result = fixture.GetLetterAsChar();
 
 		assertEquals('a', result);
 	}
 
 	/**
 	 * Run the char GetUppercaseLetter() method test.
 	 * Verify the method returns a letter as an upper case character
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 2/22/13 1:46 AM
 	 */
 	@Test
 	public void testGetUppercaseLetter() throws Exception {
 		
 		Letter fixture = new Letter('b', new ThemeManager());
 		
 		char result = fixture.GetUppercaseLetter();
 
 		assertEquals('B', result);
 	}
 
 	/**
 	 * Run the void addResource(String,String,String) method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 2/22/13 1:46 AM
 	 */
 	@Test
 	public void testAddResource() throws Exception {
 		
 		Letter fixture = new Letter('f', new ThemeManager());
 		
 		String imageName = "frog.jpg";
 		String soundName = "frog.wav";
 		String wordText = "Frog";
 
 		fixture.addResource(imageName, soundName, wordText, new Theme(Theme.DEFAULT_THEME_NAME));
 
 	}
 
 	/**
 	 * Run the Icon getIcon(int,int) method test.
 	 * Verify the method returns null for invalid value
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 2/22/13 1:46 AM
 	 */
 	@Test
 	public void testGetIconForInvalidValue() throws Exception {
 		
 		Letter fixture = new Letter('c', new ThemeManager());
 	
 		int width = 1;
 		int height = 1;
 
 		Icon result = fixture.getIcon(width, height);
 
 		assertEquals(null, result);
 	}
 
 	/**
 	 * Run the Icon getIcon(int,int) method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 2/22/13 1:46 AM
 	 */
 	@Test
 	public void testGetIconForValidValue()	throws Exception {
 		
 		Letter fixture = new Letter('c', new ThemeManager());
 		
 		String imageName = "frog.jpg";
 		String soundName = "frog.wav";
 		String wordText = "Frog";
 
		fixture.addResource(imageName, soundName, wordText, new Theme(ThemeManager.DEFAULT_THEME_NAME));
 		
 		int width = 1;
 		int height = 1;
 
 		
 		ImageIcon result = (ImageIcon) fixture.getIcon(width, height);
 
 		assertNotNull(result);
 	}
 
 	/**
 	 * Run the String getWord() method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 2/22/13 1:46 AM
 	 */
 	@Test
 	public void testGetWordForInvalidValue()	throws Exception {
 		
 		Letter fixture = new Letter('t', new ThemeManager());
 
 		String result = fixture.getWord();
 
 		assertEquals(null, result);
 	}
 
 	/**
 	 * Run the String getWord() method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 2/22/13 1:46 AM
 	 */
 	@Test
 	public void testGetWordForValidValue()	throws Exception {
 		
 		Letter fixture = new Letter('c', new ThemeManager());
 		
 		String imageName = "frog.jpg";
 		String soundName = "frog.wav";
 		String wordText = "Frog";
 
		fixture.addResource(imageName, soundName, wordText, new Theme(ThemeManager.DEFAULT_THEME_NAME));
 
 		String result = fixture.getWord();
 
 		assertEquals(wordText, result);
 	}
 
 	/**
 	 * Run the void nextExample() method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 2/22/13 1:46 AM
 	 */
 	@Test
 	public void testNextExampleForInvalidValue()	throws Exception {
 		
 		Letter fixture = new Letter('t', new ThemeManager());
 		
 		// TODO write review code and modify test case, not quite sure how to verify this test
 		fixture.nextExample();
 
 	}
 
 	/**
 	 * Run the void nextExample() method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 2/22/13 1:46 AM
 	 */
 	@Test
 	public void testNextExampleForValidValue()	throws Exception {
 		
 		Letter fixture = new Letter('t', new ThemeManager());
 		fixture.addObserver(new AlphabetPageView(""));
 
 		fixture.nextExample();
 
 		// TODO write review code and modify test case, not quite sure how to verify this test
 	}
 
 	/**
 	 * Run the void playSound() method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 2/22/13 1:46 AM
 	 */
 	@Test
 	public void testPlaySoundForInvalidValue() throws Exception {
 		Letter fixture = new Letter('t', new ThemeManager());
 		//fixture.addObserver(new AlphabetPageView(""));
 
 		fixture.playSound();
 
 		// add additional test code here
 	}
 
 	/**
 	 * Run the void playSound() method test.
 	 *
 	 * @throws Exception
 	 *
 	 * @generatedBy CodePro at 2/22/13 1:46 AM
 	 */
 	@Test
 	public void testPlaySoundForValidValue() throws Exception {
 		Letter fixture = new Letter('f', new ThemeManager());
 		
 		String imageName = "frog.jpg";
 		String soundName = "frog.wav";
 		String wordText = "Frog";
 
		fixture.addResource(imageName, soundName, wordText, new Theme(ThemeManager.DEFAULT_THEME_NAME));
 		fixture.playSound();
 
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
 		new org.junit.runner.JUnitCore().run(LetterTest.class);
 	}
 }
