 package planning;
 
 import java.util.*;
 
 import faction.Faction;
 
 //Todo: Make the task check for stuff work as follows:
 //Check to see that every task looks like it has finished (this will have to be personalized to each task
 //Check to see that we do the first task in our list that looks unfinished (i.e., it doesn't look completed)
 public abstract class Task {
 	//Tasks should 
 	//protected ArrayList<Task> tasks = new ArrayList<Task>();
 	protected boolean isBaseTask;
 	protected String name;
 	protected Task parent;
 	protected boolean didFinish = false;
 	protected Random random = new Random();
 
 	public Task(boolean isBaseTask, String taskName, Task parent) {
 		this.isBaseTask = isBaseTask;
 		this.name = taskName;
 		this.parent = parent;
 	}
 	
 	public abstract int stepsToCompletion(Faction faction);
 	
 	public abstract Task getNextStep(Faction faction);
 	
 	public abstract boolean isCompleted(Faction faction);
 	
 	public abstract boolean canPerform(Faction faction);
 	
 	public void perform(Faction faction) {
 		System.out.println("Doing " + name);
 		getNextStep(faction).perform(faction);
 	}
 	
 	public void reportFinished(Task child) {
 		this.didFinish = true;
 	}
 	
 	//Think of way to have this work generically
 	//Flavors are war, science, diplomacy
 	public abstract double getFlavorMatch(Faction faction);
 	
 	protected List<Task> getTaskList(Faction faction){return null;}
 
 	protected Task getFlavorMatchTask(Faction faction) {
 		List<Task> taskList = getTaskList(faction);
 		Task bestMatch = null;
 		double totalFlavor = 0;		
 		
 		for(Task task : taskList)
 		{
 			totalFlavor += task.getFlavorMatch(faction);
 		}
 		
 		double flavorPick = random.nextDouble() * totalFlavor;
 		
 		for(Task task : taskList)
 		{
 			flavorPick -= task.getFlavorMatch(faction);
 			
 			if(flavorPick <= 0)
 				return task;
 		}
 		
 		return null;
 	}
 	
 	public String toString() {
 		return name.substring(0, name.length() - 5);
 	}
 	public boolean isBaseTask() {
 		return isBaseTask;
 	}
 	
 	
 }
