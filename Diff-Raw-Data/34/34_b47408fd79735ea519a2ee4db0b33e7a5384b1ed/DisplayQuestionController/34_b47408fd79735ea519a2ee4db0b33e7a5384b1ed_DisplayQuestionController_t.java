 package controllers;
 
 import java.util.ArrayList;
 
 import models.Answer;
 import models.DbManager;
 import models.Post;
 import models.Question;
 import play.libs.Codec;
 import play.mvc.Controller;
 
 /**
  * This Controller manages the view of questions
  */
 public class DisplayQuestionController extends Controller {
 	private static DbManager manager = DbManager.getInstance();
 	
 	public static void questions(String mod) {
 		ArrayList<Question> questions = null;
 		if (mod.equals("recent")) {
 			questions = manager.getRecentQuestionsByNumber(25);
 			render(questions, mod);
 		} else if (mod.equals("all")) {
 			questions = manager.getQuestionsSortedByScore();
 			render(questions, mod);
 		}
 	}
 	
 	public static void showAnswers(String id, String newAnswer) {
 		int intId = Integer.parseInt(id);
 		String randomID = Codec.UUID();
		ArrayList<Answer> answers = manager
				.getAnswersSortedByDateByScore(intId);
 		Question question = manager.getQuestionById(intId);
 
 		if (answers.size() == 0) {
 			render(question, newAnswer, randomID);
 		} else {
 			render(answers, question, newAnswer, randomID);
 		}
 	}
 
 	/**
 	 * 
 	 * @param qid
 	 */
 	public static void showSimilarQuestions(int qid) {
 		Question question = manager.getQuestionById(qid);
 		ArrayList<Post> similar = question.similarQuestions();
 		render(similar);
 	}
 
 	/**
 	 * Renders the preferred amount of newest questions
 	 */
 	public static void showRecentQuestionsByNumber() {
 		ArrayList<Question> recentQuestionsByNumber = manager
 				.getRecentQuestionsByNumber(25);
 
 		render(recentQuestionsByNumber);
 	}
 
 }
