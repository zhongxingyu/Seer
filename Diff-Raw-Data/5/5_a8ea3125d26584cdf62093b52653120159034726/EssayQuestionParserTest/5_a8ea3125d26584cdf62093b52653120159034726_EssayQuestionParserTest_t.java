 package ups.m2glre.rossf1.parser;
 
 import java.io.File;
 
 import junit.framework.TestCase;
 
 import org.jdom.Document;
 import org.jdom.input.SAXBuilder;
 
 import ups.m2glre.rossf1.question.EssayQuestion;
 
 public class EssayQuestionParserTest extends TestCase {
     private EssayQuestionParser essayQuestionParser;
 
     public void setUp() {
         essayQuestionParser = new EssayQuestionParser();
     }
 
     public void testFile1() {
         try {
             Document document = new SAXBuilder().build(new File("src/test/TestEssayQuestion1.xml"));
             EssayQuestion essayQuestion = (EssayQuestion) essayQuestionParser.
                     parseQuestion(document.getRootElement().getChild("question"));
             checkAnswerFraction(essayQuestion);
             checkFeedbackText(essayQuestion);
         } catch (Exception e) {
             System.out.println(e.getMessage());
             e.printStackTrace();
             fail();
         }
 
     }
     
     public void testFile2() {
         try {
             Document document = new SAXBuilder().build(new File("src/test/TestEssayQuestion2.xml"));
             EssayQuestion essayQuestion = (EssayQuestion) essayQuestionParser.
                     parseQuestion(document.getRootElement().getChild("question"));
             checkAnswerFraction(essayQuestion);
             checkFeedbackText(essayQuestion);
         } catch (Exception e) {
             e.printStackTrace();
             fail();
         }
 
     }
 
 
     private void checkAnswerFraction(EssayQuestion essayQuestion) {
        assertEquals(2, essayQuestion.getAnswerFraction());
     }
 
     private void checkFeedbackText(EssayQuestion essayQuestion) {
        assertEquals("Good job", essayQuestion.getAnswerValue());
     }
 }
