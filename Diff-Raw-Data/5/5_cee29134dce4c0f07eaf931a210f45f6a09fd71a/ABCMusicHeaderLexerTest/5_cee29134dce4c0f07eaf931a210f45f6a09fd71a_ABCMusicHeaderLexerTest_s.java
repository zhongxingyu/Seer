 package grammar;
 
 import static org.junit.Assert.*;
 
 import java.util.List;
 
 import org.antlr.v4.runtime.ANTLRInputStream;
 import org.antlr.v4.runtime.CharStream;
 import org.antlr.v4.runtime.Token;
 import org.junit.Test;
 
 /*
  * TESTING STRATEGY:
  * 
  * -> Empty String
  * -> Individual Element Tests (Composer, Key, Length, Meter, Tempo, Title, Index)
  * -> Comments (empty comment included)
  * -> Full Header
  * -> Empty Elements (i.e. "T:\n")
  */
 
 public class ABCMusicHeaderLexerTest {
 
 	@Test
     //empty test
     public void testEmptyTokens() {
         verifyLexer("", new String[] {});
     }
 	
     @Test
     //Composer Test
     public void testComposerLexer() {
         verifyLexer("C:", new String[] {"C:"});
     }
     
     @Test
     //Key Test
     public void testKeyLexer() {
         verifyLexer("K:", new String[] {"K:"});
     }
     
     @Test
     //Length Test
     public void testLengthLexer() {
         verifyLexer("L:1/4\n", new String[] {"L:","1/4","\n"});
     }
     
     @Test
     //Meter Test
     public void testMeterLexer() {
         verifyLexer("M:4/4\n", new String[] {"M:","4/4","\n"});
     }
     
     @Test
     //Tempo Test
     public void testTempoLexer() {
        verifyLexer("Q:1/4=80\n", new String[] {"Q:","1/4=80","\n"});
     }
     
     @Test
     //Title Test
     public void testTitleLexer() {
         verifyLexer("T:Symphony No.1 Test\n", new String[] {"T:","Symphony No.1 Test", "\n"});
     }
     
     @Test
     //Index Test
     public void testIndexLexer() {
         verifyLexer("X:1\n", new String[] {"X:","1","\n"});
     }
     
     @Test
     //Comment Test
     public void testCommentLexer() {
         verifyLexer("%I'm a comment!!! :)\n", new String[] {"%","I'm a comment!!! :)","\n"});
     }
     
     @Test
     //Header Test
     public void testHeaderLexer() {
         verifyLexer("X:1\n"
         		+ "T:Simple scale\n"
         		+ "C:Unknown\n"
         		+ "M:4/4\n"
         		+ "%Hi\n"
         		+ "L:1/4\n"
         		+ "Q:1/4=120\n"
         		+ "K:C\n",
         		new String[] {
         				"X:","1","\n",
         				"T:","Simple scale", "\n",
         				"C:","Unknown", "\n",
         				"M:","4/4", "\n",
         				"%","Hi","\n",
         				"L:","1/4", "\n",
        				"Q:","1/4=120", "\n",
         				"K:","C","\n"});
     }
     
     @Test
     //Header Test (with empty comment)
     public void testEmptyHeaderLexer() {
         verifyLexer("X:\n"
         		+ "T:\n"
         		+ "C:\n"
         		+ "M:\n"
         		+ "%\n"
         		+ "L:\n"
         		+ "Q:\n"
         		+ "K:\n",
         		new String[] {
         				"X:","\n",
         				"T:", "\n",
         				"C:", "\n",
         				"M:", "\n",
         				"%", "\n",
         				"L:", "\n",
         				"Q:", "\n",
         				"K:", "\n"});
     }
     
 
 	public void verifyLexer(String input, String[] expectedTokens) {
         CharStream stream = new ANTLRInputStream(input);
         ABCMusicHeaderLexer lexer = new ABCMusicHeaderLexer(stream);
         lexer.reportErrorsAsExceptions();
         List<? extends Token> actualTokens = lexer.getAllTokens();
 
         assertEquals(expectedTokens.length, actualTokens.size());
         
         for(int i = 0; i < actualTokens.size(); i++) {
              String actualToken = actualTokens.get(i).getText();
              String expectedToken = expectedTokens[i];
              assertEquals(actualToken, expectedToken);
         }
     }
 }
