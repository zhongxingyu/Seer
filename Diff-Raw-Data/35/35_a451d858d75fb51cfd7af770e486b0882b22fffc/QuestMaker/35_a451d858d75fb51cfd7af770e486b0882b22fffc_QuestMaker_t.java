 package quest;
 
//need QuestJournal, QuestGiver, tasks
 //maybe put this in RPGame
 
 public class QuestMaker 
 {
 	public static QuestMaker instance = null;
 	
 	private QuestMaker()
 	{
 		
 	}
 	
 	public static QuestMaker getInstance()
 	{
 		if (instance == null)
 			return new QuestMaker();
 		
 		return instance;
 	}
 	
 }
