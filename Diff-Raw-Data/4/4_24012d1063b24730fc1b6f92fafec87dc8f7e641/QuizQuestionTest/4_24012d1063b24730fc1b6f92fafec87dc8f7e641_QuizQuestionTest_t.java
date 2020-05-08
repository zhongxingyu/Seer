 package epfl.sweng.test;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.json.JSONException;
 
 import epfl.sweng.quizquestions.QuizQuestion;
 import epfl.sweng.servercomm.SwengHttpClientFactory;
 import epfl.sweng.tasks.IQuizServerCallback;
 import epfl.sweng.tasks.LoadRandomQuestion;
 
 import junit.framework.TestCase;
 
 /**
  * Unit test for JSON methods in QuizQuestion 
  */
 public class QuizQuestionTest extends TestCase {
     public static final String VALID_QUESTION_JSON = "{"
             + "question: 'What is the answer to life, the universe and everything?', "
             + "answers: ['42', '27'],"
             + "solutionIndex: 0,"
             + "tags : ['h2g2', 'trivia'],"
             + "owner : 'anonymous',"
             + "id : '123'"
             + "}";
 
     public void testQuestionOK() throws JSONException {
         String json = VALID_QUESTION_JSON;
         QuizQuestion result = new QuizQuestion(json);
         assertNotNull(result);
         
         String q = "The Question...";
         List<String> answers = new ArrayList<String>();
         answers.add("Answer 0");
         Set<String> tags = new HashSet<String>();
         tags.add("Tag");
         final int id=13;
         final int rightAnswer=3;
         String owner = "Anonymous";
         QuizQuestion result2 = new QuizQuestion(q, answers, rightAnswer, tags, id, owner);
         
         result2.addAnswerAtIndex("Answer 1", rightAnswer-2);
         result2.addAnswerAtIndex("Answer 2", rightAnswer-1);
         result2.addAnswerAtIndex("Answer 3", rightAnswer);
         result2.addAnswerAtIndex("Answer 4", rightAnswer+1);
         result2.addAnswerAtIndex("Answer 5", rightAnswer+2);
         
         String newAnswer = "Modififed Answer";
         result2.addAnswerAtIndex(newAnswer, 2);
         assertEquals(newAnswer, result2.getAnswers()[2]);
         
         assertEquals(owner, result2.getOwner());
         result2.removeAnswerAtIndex(rightAnswer);
         assertEquals(result2.getSolutionIndex(), -1);
         result2.setSolutionIndex(2);
         result2.removeAnswerAtIndex(answers.size()-1);
         result2.removeAnswerAtIndex(0);
         assertEquals(result2.getSolutionIndex(), 1);
         
         int currentSize = result2.getAnswers().length;
        final int nbrOfNewAnswers = 3;
        int newLastIndex = currentSize+nbrOfNewAnswers;
         result2.addAnswerAtIndex(newAnswer, currentSize+2);
         assertEquals(newLastIndex, result2.getAnswers().length);
         
         
         SwengHttpClientFactory.setInstance(null);
         new LoadRandomQuestion(new IQuizServerCallback() {
         	public void onSuccess(QuizQuestion question) {
         	}
         	public void onError(Exception except) {
         	}
         }).execute();
     }
 }
