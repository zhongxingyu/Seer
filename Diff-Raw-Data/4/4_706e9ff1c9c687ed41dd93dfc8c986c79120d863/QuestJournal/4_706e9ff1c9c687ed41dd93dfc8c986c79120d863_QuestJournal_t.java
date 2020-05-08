 package net.jeebiss.questmanager.quests;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class QuestJournal {
 	private	Map<String,Quest>	quests;
 
 	/**
 	 * Create a new quest journal.
 	 */
 	public QuestJournal() {
 		quests = new HashMap<String,Quest>  ();
 	}
 
 	/**
 	 * Returns the map of Quest Names to quest Objects.
 	 * 
 	 * @return	The map of quest names to quest objects.
 	 */
 	public Map<String,Quest> getQuests () {
 		// TODO:  This should be made immutable.
 		return this.quests;
 	}
 	
 	/**
 	 * Adds a new quest to the journal and returns the quest object.
 	 * 
 	 * @param questName	The name of the quest to add.
 	 * @param questScriptName	The quest's script name to add
 	 * 
 	 * @return	The newly created quest object.
 	 */
	public Quest addQuest (String questName, String questScriptName, String questDescription) {
		Quest	quest = new Quest (questName, questScriptName, questDescription);
 		this.quests.put (questName, quest);
 		return quest;
 	}
 }
