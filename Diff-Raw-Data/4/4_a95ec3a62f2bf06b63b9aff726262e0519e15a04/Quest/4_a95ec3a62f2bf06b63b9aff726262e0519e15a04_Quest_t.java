 package rpisdd.rpgme.gamelogic.quests;
 
 import org.joda.time.DateTime;
 
 import rpisdd.rpgme.gamelogic.player.Reward;
 import rpisdd.rpgme.gamelogic.player.StatType;
 
 //The Quest class represents a quest that the player can add.
 public class Quest {
 
 	private final String name;
 	private final String description;
 	private final QuestDifficulty difficulty;
 	private final StatType statType;
 	private boolean isComplete;
 	private final DateTime deadline;
 
 	private final Recurrence recurrence;
 	private DateTime recurCompleteDate; // What datetime was this recurring
 										// quest last completed?
 
 	public boolean getIsComplete() {
 		return isComplete;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public QuestDifficulty getDifficulty() {
 		return difficulty;
 	}
 
 	public Recurrence getRecurrence() {
 		return recurrence;
 	}
 
 	public StatType getStatType() {
 		return statType;
 	}
 
 	public boolean isTimed() {
 		return deadline != null;
 	}
 
 	public DateTime getDeadline() {
 		return deadline;
 	}
 
 	public boolean isFailed() {
 		return !isComplete && isTimed() && DateTime.now().isAfter(deadline);
 	}
 
 	public DateTime getRecCompDate() {
 		return recurCompleteDate;
 	}
 
 	public boolean isRecurring() {
 		return recurrence != Recurrence.NONE;
 	}
 
 	public void tempComplete() {
 		recurCompleteDate = DateTime.now();
 	}
 
 	public boolean isTempComplete() {
 		return recurCompleteDate != null;
 	}
 
 	public void updateTempDone() {
 		System.out.println("Updating temp done");
 		if (recurCompleteDate != null) {
 
 			switch (recurrence) {
 			case DAILY:
 				if (DateHelper.oneDayAgo(recurCompleteDate)) {
 					System.out.println("Quest revived!");
 					recurCompleteDate = null;
 				}
 				break;
 			case WEEKLY:
 				if (DateHelper.oneWeekAgo(recurCompleteDate)) {
 					recurCompleteDate = null;
 				}
 				break;
 			case MONTHLY:
 				if (DateHelper.oneMonthAgo(recurCompleteDate)) {
 					recurCompleteDate = null;
 				}
 				break;
 			default:
 				break;
 			}
 
 		}
 	}
 
 	private Quest(String aname, String adesc, QuestDifficulty adiff,
 			StatType atype, boolean aIsComplete, DateTime adeadline,
 			Recurrence arecurrence, DateTime arecurCompleteDate) {
 		name = aname;
 		description = adesc;
 		difficulty = adiff;
 		statType = atype;
 		deadline = adeadline;
 		isComplete = aIsComplete;
 		recurrence = arecurrence;
 		recurCompleteDate = arecurCompleteDate;
 	}
 
 	// Builder class to streamline quest creation
 	// Usage: Quest myQuest = new
 	// QuestBuilder("Name","Desc",difficulty,type).isComplete(true).deadline(deadline).(and
 	// so on...).getQuest()
 	public static class QuestBuilder {
 
 		String name;
 		String desc;
 		QuestDifficulty difficulty;
 		StatType type;
 		boolean isComplete = false;
 		DateTime deadline = null;
 		Recurrence recurrence = Recurrence.NONE;
 		DateTime recurCompleteDate = null;
 
 		public QuestBuilder(String aname, String adesc, QuestDifficulty adiff,
 				StatType atype, Recurrence arecurrence) {
 			name = aname;
 			desc = adesc;
 			difficulty = adiff;
 			type = atype;
 			recurrence = arecurrence;
 		}
 
 		public QuestBuilder isComplete(boolean aIsComplete) {
 			isComplete = aIsComplete;
 			return this;
 		}
 
 		public QuestBuilder deadline(DateTime aDeadline) {
 			deadline = aDeadline;
 			return this;
 		}
 
 		public QuestBuilder recurCompleteDate(DateTime aDate) {
 			recurCompleteDate = aDate;
 			return this;
 		}
 
 		public Quest build() {
 			return new Quest(name, desc, difficulty, type, isComplete,
 					deadline, recurrence, recurCompleteDate);
 		}
 
 	}
 
 	public Reward completeQuest() {
		if (!isRecurring()) {
			isComplete = true;
		}
 		return Reward.questReward(this);
 	}
 
 }
