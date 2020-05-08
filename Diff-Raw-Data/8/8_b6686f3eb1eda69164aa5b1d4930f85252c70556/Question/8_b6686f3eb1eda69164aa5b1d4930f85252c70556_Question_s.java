 import java.util.ArrayList;
 import java.util.List;
 
 
 /**
  * @author KarelPetranek
  *
  * A class representing one question in the quiz. It contains a list of valid answers
  * and their corresponding mental models
  */
 public class Question {
 	int id = -1;
 	
 	List<ModelsAndAnswer> answerModels;
 	
 	/**
 	 * Creates a new question and fills it with correct answers
 	 * @param id Question id
 	 * @param answerModels Models for correct answers
 	 */
 	public Question(int id, List<ModelsAndAnswer> answerModels)  {
 		this.answerModels = answerModels;
 		for (ModelsAndAnswer ma : answerModels)
 			ma.answer.setQuestionId(id);
 	}
 	
 	/**
 	 * Question ID
 	 * @return Question ID
 	 */
 	public int getId()  {
 		return id;
 	}
 	
 	private List<Model> modelsForSingleAnswer(Answer answer)  {
 		List<Model> result = new ArrayList<Model>();
 		
 		for (ModelsAndAnswer m : answerModels)  {
 			if (m.answer.equals(answer))  {
 				if (result != null)
 					System.out.println("Warning: duplicate answer for question " + id);
 				result = m.models;
 			}
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Returns list of models that correspond to the given answer.
 	 * @param answer The student answer
 	 * @return Corresponding models or an empty list if no models match the answer
 	 */
 	public List<Model> modelsForAnswer(Answer answer)  {
 	
 		if (answer.getQuestionId() != id)  {
 			System.out.println("Warning: the given answer with id=" + answer.getQuestionId() + " does not match the question id " + id);
 			return null;
 		}
 			
 		List<Model> result = modelsForSingleAnswer(answer);
 		
 		// No model found, split the multi choice answer to a list of simple answers
 		// and assign them the models
 		if (result.size() == 0)  {
 			List<Answer> subAnswers = answer.getSubAnswers();
 			for (Answer a : subAnswers)  {
 				result.addAll(modelsForSingleAnswer(a));
 			}
 		}
 		
 		return result;
 	}
 }
