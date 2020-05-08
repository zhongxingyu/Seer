 import java.util.ArrayList;
 import java.util.List;
 
 
 /**
  * @author KarelPetranek
  * Represents a correct or student's answer (depending on usage).
  */
 public class Answer {
 	private class AnswerField  {
 		List<Integer> variables = new ArrayList<Integer>();
 		
 		public AnswerField(int a, int b)  {
 			variables.add(a);
 			variables.add(b);
 		}
 		
 		public AnswerField(int a, int b, int c)  {
 			variables.add(a);
 			variables.add(b);
 			variables.add(c);
 		}
 		
 		public int getSize() { return variables.size(); }
 		
 		@Override
 		public boolean equals(Object oth)  {
 			if (oth instanceof AnswerField)  {
 				AnswerField o = (AnswerField)oth;
 				if (o.getSize() != getSize())
 					return false;
 				
 				for (int i = 0; i < getSize(); i++)  {
 					boolean f = false;
 					for (int j = 0; j < getSize(); j++)  {
 						if (variables.get(i).equals(o.variables.get(j)))  {
 							f = true;
 							break;
 						}
 					}
 					
 					if (!f)
 						return false;
 				}
 				
 				return true;
 			}
 			
 			return false;
 		}
 	}
 	
 	List<AnswerField> answers = new ArrayList<AnswerField>();
 	int questionId = -1;
 	
 	/**
 	 * Creates an answer from the given description string. Multiple choices are 
 	 * separated by pipes, question variables are separated by commas
 	 * @param input The description string
 	 * @param questionId ID of the question this answer belongs to
 	 */
 	public Answer(String input, int questionId)  {
		this.questionId = 0;
 		
 		// Parse answer in the format a1,b1,c1|a2,b2,c2|...
 		String[] multipleChoices = input.split("\\|");
 		for (int i = 0; i < multipleChoices.length; i++)  {
 			String[] answerNumbers = multipleChoices[i].split(",");
 			switch (answerNumbers.length)  {
 			case 2:  {
 				int a = Integer.valueOf(answerNumbers[0]);
 				int b = Integer.valueOf(answerNumbers[1]);
 				answers.add(new AnswerField(a, b));
 			} break;
 			
 			case 3:  {
 				int a = Integer.valueOf(answerNumbers[0]);
 				int b = Integer.valueOf(answerNumbers[1]);
 				int c = Integer.valueOf(answerNumbers[1]);
 				answers.add(new AnswerField(a, b, c));				
 			} break;
 			
 			default:
 				System.out.println("Warning: answer with an unknown number of variables (" + answerNumbers.length + ")");
 			}
 		}
 	}
 	
 	@Override
 	public boolean equals(Object oth)  {
 		if (oth instanceof Answer)  {
 			Answer a = (Answer)oth;
 			if (a.questionId != questionId)
 				return false;
 			
 			if (a.answers.size() != answers.size())
 				return false;
 			
 			for (int i = 0; i < answers.size(); i++)
 				if (!a.answers.get(i).equals(answers.get(i)))
 					return false;
 			
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * @return True if the answer is empty (student left it blank)
 	 */
 	public boolean isEmpty()  {
 		return answers.isEmpty();
 	}
 	
 	/**
 	 * @return ID of the question corresponding to this answer
 	 */
 	public int getQuestionId()  {
 		return questionId;
 	}
 	
 	/**
 	 * Sets ID of the question that corresponds to this answer
 	 * @param id Question ID
 	 */
 	public void setQuestionId(int id)  {
 		questionId = id;
 	}
 
 	/**
 	 * @return This multichoice answer split to individual choices as an answer
 	 */
 	public List<Answer> getSubAnswers() {
 		List<Answer> res = new ArrayList<Answer>();
 		for (AnswerField af : answers)  {
 			Answer newA = new Answer("", questionId);
 			newA.answers.add(af);
 			res.add(newA);
 		}
 		
 		return res;
 	}
 }
