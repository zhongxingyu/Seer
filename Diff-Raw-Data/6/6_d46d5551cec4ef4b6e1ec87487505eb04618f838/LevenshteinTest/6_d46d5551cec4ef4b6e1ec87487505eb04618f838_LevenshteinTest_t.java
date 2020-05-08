 package brown.puzzles.breathalyzer;
 
 import junit.framework.TestCase;
 
 
 /**
  * @author Matt Brown
  * @date Feb 23, 2010
  *
  */
 public class LevenshteinTest extends TestCase {
 
 	public void testScore() {
 		assertEquals(3, Levenshtein.score("sitting", "kitten"));
 		assertEquals(3, Levenshtein.score("Saturday", "Sunday"));
		// removed case insensitivity from the score method, more performant to
		// apply upper/lowercase rules at read time
		// assertEquals(0, Levenshtein.score("this", "THIS"));
		// assertEquals(1, Levenshtein.score("goud", "GOUT"));
 
 	}
 }
