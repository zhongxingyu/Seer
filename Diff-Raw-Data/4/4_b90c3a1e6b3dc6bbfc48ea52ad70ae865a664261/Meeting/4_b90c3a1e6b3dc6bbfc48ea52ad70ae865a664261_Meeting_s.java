 package edu.brown.cs32.scheddar;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 public class Meeting {
 	private String name;  // a name for the event
 	private boolean decided; // true if the meeting time is finalized
 	private ScheddarTime timeForFinalizing; // the time at which the meeting time must be finalized
 	
 	private ScheddarTime finalTime; // the block of time when the meeting will occur
 	private List<ScheddarTime> proposedTimes; // proposed times for the meeting to occur
 	private HashMap<Integer,Double> indexToScore; // maps proposed time indices to the weighted score for the meeting
 	
 	public List<String> dummyGroupsInvolved;
 	private List<Group> groupsInvolved; // a list of the names of groups involved in this meeting
 	private String description; // a description of this event
 	
 	private UsefulMethods methods = new UsefulMethods();
 	
 	// The number of hours in advance that a meeting will be finalized
 	
 	private int hoursBeforeMeetingFinalized = 72;
 	
 	// A Hashmap of individuals to importance for a meeting that are not in groups involved with the meeting
 	// Maps the Person to their importance in this meeting
 	
 	private HashMap<Person, Double> extraPeopleToImportance;
 	
 	//TODO : Add extraPeopleToImportance to the XML file for Meetings
 	
 	/**
 	 * XML Constructor
 	 */
 	
 	Meeting(String name, Boolean decided, ScheddarTime timeForFinalizing, ScheddarTime finalTime,
 			List<ScheddarTime> proposedTimes, HashMap<Integer, Double> indexToScore, 
 			List<String> dummyGroupsInvolved, String description){
 		this.name = name;
 		this.decided = decided;
 		this.timeForFinalizing = timeForFinalizing;
 		this.finalTime = finalTime;
 		this.proposedTimes = proposedTimes;
 		this.indexToScore = indexToScore;
 		this.dummyGroupsInvolved = dummyGroupsInvolved;
 		this.description = description;

 	}
 	
 	/**
 	 * Constructor for default time before scheduling
 	 **/
 	
 	public Meeting(String name, List<ScheddarTime> times,List<Group> groupsInvolved,String description,List<Person> peopleAttending){
 		this.name = name;
 		this.proposedTimes = times;
 		this.groupsInvolved = groupsInvolved;
 		this.description = description;
 		this.decided = false;
 		this.indexToScore = new HashMap<Integer,Double>();
 		this.extraPeopleToImportance = new HashMap<Person,Double>();
 		
 		// If there is only one proposed time, then the meeting will definitely occur then,
 		// so we set decided to true and the final time to that time
 		
 		if(this.proposedTimes.size()==1){
 			this.decided = true;
 			this.finalTime = this.proposedTimes.get(0);
 		}
 		
 		// Else we calculate the time by which we must decide on a final meeting time
 		
 		else{
 			ScheddarTime earliestTime = Collections.min(this.proposedTimes);
 			int newDay = earliestTime.getDay();
 			int newMonth = earliestTime.getMonth();
 			int newYear = earliestTime.getYear();
 			int newHour = earliestTime.getStartHour() - this.hoursBeforeMeetingFinalized;
 			int newMinutes = earliestTime.getStartMinutes();
 			
 			while(newHour < 0){
 				newHour += 24;
 				newDay = newDay - 1;
 			}
 			
 			if(newDay <= 0){
 				newMonth--;
 				if(newMonth<0){ 
 					newMonth = 11; // if was january and backed up, go to december
 					newYear = newYear--;
 				}
 				newDay = methods.daysInMonth(newMonth,newYear) + newDay;
 			}
 			
 			ScheddarTime timeToFinalize = new ScheddarTime(newHour,newMinutes,0,0,newDay,newMonth,newYear,false);
 			this.timeForFinalizing = timeToFinalize;
 		}
 	}
 	
 	/**
 	 * Constructor for explicit time given before scheduling
 	 */
 	
 	public Meeting(String name, List<ScheddarTime> times,List<Group> groupsInvolved,String description,List<Person> peopleAttending, int hoursBeforeScheduling){
 		this.name = name;
 		this.proposedTimes = times;
 		this.groupsInvolved = groupsInvolved;
 		this.description = description;
 		this.decided = false;
 		this.indexToScore = new HashMap<Integer,Double>();
 		this.extraPeopleToImportance = new HashMap<Person,Double>();
 		
 		// If there is only one proposed time, then the meeting will definitely occur then,
 		// so we set decided to true and the final time to that time
 		
 		if(this.proposedTimes.size()==1){
 			this.decided = true;
 			this.finalTime = this.proposedTimes.get(0);
 		}
 		
 		// Else we calculate the time by which we must decide on a final meeting time
 		
 		else{
 			ScheddarTime earliestTime = Collections.min(this.proposedTimes);
 			int newDay = earliestTime.getDay();
 			int newMonth = earliestTime.getMonth();
 			int newYear = earliestTime.getYear();
 			int newHour = earliestTime.getStartHour() - hoursBeforeScheduling;
 			int newMinutes = earliestTime.getStartMinutes();
 			
 			while(newHour < 0){
 				newHour += 24;
 				newDay = newDay - 1;
 			}
 			
 			if(newDay <= 0){
 				newMonth--;
 				if(newMonth<0){ 
 					newMonth = 11; // if was january and backed up, go to december
 					newYear = newYear--;
 				}
 				newDay = methods.daysInMonth(newMonth,newYear) + newDay;
 			}
 			
 			ScheddarTime timeToFinalize = new ScheddarTime(newHour,newMinutes,0,0,newDay,newMonth,newYear,false);
 			this.timeForFinalizing = timeToFinalize;
 		}
 	}
 	
 	/**
 	 * Empty constructor for when a new meeting pane is created
 	 */
 	public Meeting() {
 		this.name = "";
 		this.decided = false;
 		this.timeForFinalizing = null;
 		this.finalTime = null;
 		this.proposedTimes = new ArrayList<ScheddarTime>();
 		this.indexToScore = new HashMap<Integer,Double>();
 		this.dummyGroupsInvolved = new ArrayList<String>();
 		this.groupsInvolved = new ArrayList<Group>();
 		this.description = "";
 	}
 	
 	
 	/**
 	 * Getter Functions
 	 */
 	
 	public String getName(){
 		return this.name;
 	}
 	
 	public boolean isDecided(){
 		return this.decided;
 	}
 	
 	public boolean getDecided(){
 		return decided;
 	}
 	
 	public ScheddarTime getTimeForFinalizing(){
 		return timeForFinalizing;
 	}
 	
 	public List<ScheddarTime> getProposedTimes(){
 		return this.proposedTimes;
 	}
 	
 	public HashMap<Integer,Double> getIndexToScore(){
 		return this.indexToScore;
 	}
 	
 	// This will be null if a final time has yet to be decided
 	
 	public ScheddarTime getFinalTime(){
 		return this.finalTime;
 	}
 	
 	public List<Group> getGroupsInvolved(){
 		return this.groupsInvolved;
 	}
 	
 	public String getDescription(){
 		return this.description;
 	}
 	
 	public HashMap<Person,Double> getExtraPeopleToImportance(){
 		return this.extraPeopleToImportance;
 	}
 
 	
 	/**
 	 * Setter Functions (to completely reset values)
 	 */
 	
 	public void setName(String name){
 		this.name = name;
 	}
 	
 	public void setGroupsInvolved(List<Group> newGroupList){
 		this.groupsInvolved = newGroupList;
 	}
 	
 	public void setDescription(String newDescription){
 		this.description = newDescription;
 	}
 	
 	public void setProposedTimes(List<ScheddarTime> timeList){
 		this.proposedTimes = timeList;
 	}
 	
 	// THIS IS FOR TESTING PURPOSES ONLY. FINALTIME SHOULD
 	// NOT BE SET USING THIS METHOD!
 	
 	public void setFinalTime(ScheddarTime time){
 		decided = true;
 		this.finalTime = time;
 	}
 	
 	/**
 	 * More convenient setters
 	 */
 	
 	public void addGroupInvolved(Group group){
 		this.groupsInvolved.add(group);
 	}
 	
 	public void removeGroupInvolved(Group group){
 		this.groupsInvolved.remove(group);
 	}
 	
 	public void addProposedTime(ScheddarTime time){
 		this.proposedTimes.add(time);
 	}
 	
 	public void removeProposedTime(ScheddarTime time){
 		this.proposedTimes.remove(time);
 	}
 	
 	public void addExtraPerson(Person p, double importance){
 		this.extraPeopleToImportance.put(p,importance);
 	}
 	
 	/**
 	 * Returns a Set containing the emails of each person in a Group involved
 	 * in this meeting (Set used to prevent emailing a person multiple times
 	 * if they belong to more than one of the groups involved)
 	 */
 	
 	public Set<String> getAllEmails(){
 		Set<String> allEmails = new TreeSet<String>();
 		for(Group g : groupsInvolved){
 			for(Person p : g.getMembers()){
 				allEmails.add(p.getEmail());
 			}
 		}
 		Set<Person> keys = this.extraPeopleToImportance.keySet();
 		for(Person person : keys){
 			allEmails.add(person.getEmail());
 		}
 		return allEmails;
 	}
 
 	
 	
 	public Set<String> getAllNames(){
 		Set<String> allNames = new TreeSet<String>();
 		for(Group g : groupsInvolved){
 			for(Person p : g.getMembers()){
 				allNames.add(p.getFullName());
 			}
 		}
 		Set<Person> keys = this.extraPeopleToImportance.keySet();
 		for(Person person : keys){
 			allNames.add(person.getFullName());
 		}
 		return allNames;
 	}
 	
 	
 	/**
 	 * Finds the best time to hold the meeting out of the proposed times.
 	 * To be called at a given period of time before the first proposed meeting
 	 * time as described elsewhere
 	 */
 	
 	public ScheddarTime getBestTime(){
 		double bestScore = Collections.max(indexToScore.values());
 		for(int i : indexToScore.keySet()){
 			if(indexToScore.get(i)==bestScore){
 				return proposedTimes.get(i);
 			}
 		}
 		return null; // shouldnt happen
 	}
 	
 	/**
 	 * Get a person's importance to a meeting based on their importance in the groups
 	 * of the meeting. Will also find a Person's importance if they are in the extraPeople
 	 * Hashmap.
 	 * 
 	 * @param p the Person to get the importance of
 	 * @return the double representing that person's importance
 	 */
 	
 	public double getPersonImportance(Person p){
 		double maxImportance = 1.0;
 		for(Group g : this.getGroupsInvolved()){
 			if(g.getMemberRanking(p)>maxImportance){
 				maxImportance = g.getMemberRanking(p);
 			}
 		}
 		if(this.extraPeopleToImportance.containsKey(p)){
 			maxImportance = extraPeopleToImportance.get(p);
 		}
 		return maxImportance;
 	}
 	
 	/**
 	 * Update a score in the Hashmap of proposed time indices to scores
 	 */
 	
 	public void updateScore(int index, double score){
 		double prevScore = this.indexToScore.get(index);
 		this.indexToScore.put(index,prevScore + score);
 	}
 	
 	/**
 	 * Ranks all potential ScheddarTimes that a meeting could occur in
 	 * (broken into 30 minute intervals) based on recurring conflicts,
 	 * and returns a HashMap of scores to ScheddarTimes
 	 * 
 	 * @param timeRange a list of ScheddarTimes representing time ranges that the
 	 * meeting could occur in (ranges will be in multiples of 30 minutes)
 	 * @param duration the duration of the meeting (will be a multiple of 30)
 	 * @return 
 	 */
 	
 	public HashMap<Double,ScheddarTime> recommendMeetingTimes(ScheddarTime time, int duration){
 		HashMap<Double,ScheddarTime> toRet = new HashMap<Double,ScheddarTime>();
 		
 		List<ScheddarTime> potentialTimes = new LinkedList<ScheddarTime>();
 		
 		// Generate all potential meeting times within the ranges that were given
 		
 		
 		int day = time.getDay();
 		int month = time.getMonth();
 		int year = time.getYear();
 		int dayOfWeek = time.getDayOfWeek();
 		int startTime = time.getStartHour() * 60 + time.getStartMinutes(); // the start time in minutes
 		ScheddarTime endScheddar = time.getEndTime();
 		int endTime = endScheddar.getStartHour() * 60 + endScheddar.getStartMinutes(); // the end time in minutes
 		while(startTime+duration<=endTime){
 			int hour = startTime % 60;
 			int minutes = startTime - (hour * 60);
 			potentialTimes.add(new ScheddarTime(hour,minutes,duration,dayOfWeek,day,month,year,false));
 			startTime += duration;
 		}
 		
 		
 		// Generate rankings based on recurring conflicts
 		
 		for(ScheddarTime t : potentialTimes){
 			double currScore = 0.0; // keep track of weights of people with conflicts
 			double totalWeight = 0.0; // keep track of the total weight of people in the groups for normalizing results
 			for(Group g : this.getGroupsInvolved()){
 				for(Person p : g.getMembers()){
 					totalWeight += g.getMemberRanking(p);
 					List<ScheddarTime> conflicts = p.getConflicts();
 					for(ScheddarTime c : conflicts){
 						if(c.getDayOfWeek()== t.getDayOfWeek()){
 							c.setDay(t.getDay());
 							c.setMonth(t.getMonth());
 							c.setYear(t.getYear());
 							if(UsefulMethods.doTimesConflict(c,t)){
 								currScore += g.getMemberRanking(p);
 								break;
 							}
 						}
 					}
 				}
 			}
 			
 			HashMap<Person,Double> otherPeople = this.getExtraPeopleToImportance();
 			Set<Person> personSet = otherPeople.keySet();
 			for(Person p : personSet){
 				totalWeight+=otherPeople.get(p);
 				List<ScheddarTime> conflicts = p.getConflicts();
 				for(ScheddarTime conflict: conflicts){
 					if(conflict.getDayOfWeek()==t.getDayOfWeek()){
 						conflict.setDay(t.getDay());
 						conflict.setMonth(t.getMonth());
 						conflict.setYear(t.getYear());
 						if(UsefulMethods.doTimesConflict(t,conflict)){
 							currScore += otherPeople.get(p);
 							break;
 						}
 					}
 				}
 			}
 			toRet.put(((totalWeight - currScore) / totalWeight) * 100, t);
 		}
 		return toRet;
 	}
 }
