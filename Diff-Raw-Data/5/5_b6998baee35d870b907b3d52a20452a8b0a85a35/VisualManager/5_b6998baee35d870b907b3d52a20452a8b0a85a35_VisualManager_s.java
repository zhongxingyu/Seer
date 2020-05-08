 import java.util.ArrayList;
 import java.util.Collections;
 
 //Singleton instantiation.
 public class VisualManager{
 
 	private static VisualManager visualManager = null;
 	private static Question[] questionArray;
 	private Question currentQue;
 	private VisualManager(){}
 	
 	private VisualManager(Question[] questionArray){
 		VisualManager.questionArray = questionArray;
 	}
 	public static VisualManager instantiate()
 	{	
 		if(visualManager == null)
 		{
 			return null;
 		}
 		return visualManager;
 	}
 
 	public static VisualManager instantiate(Question[] questionArray)
 	{	
 		if(visualManager == null)
 		{
 			visualManager = new VisualManager(questionArray);
 		}
 		return visualManager;
 	}
 	public void setNextQue(int i)
 	{
 		currentQue = VisualManager.questionArray[i];
 		System.out.println(currentQue.getQue());
 	}
 	public Question getCurrentQue()
 	{
 		System.out.println(currentQue.con().movieName);
 		return currentQue;
 	}
 	public ArrayList<String> getAnswers()
 	{		
 		ArrayList<String> answers = new ArrayList<String>();
 		answers.add(currentQue.getQueAns());
 		while(answers.size() < 3)
 		{
 			Question tempQue = questionArray[(int)(Math.random() * questionArray.length)];
 			String tempString = new String();
			switch(tempQue.tOQ())
 			{
 			case MovieQuestion:
 				
				switch((MovieQuestionType)tempQue.tOQ().getValue())
 				{
 				case directed:
 					tempString = tempQue.con().directorName;
 					break;
 					
 				case date:
 					tempString = tempQue.con().releaseDate;
 					break;
 				}
 				
 				break;
 			}
 			if(!answers.contains(tempString))
 			{
 				answers.add(tempString);
 			}
 		}
 		
 		Collections.shuffle(answers);
 		return answers;
 	}
 }
