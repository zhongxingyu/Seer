 package src;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 public class Container
 {
 	private java.util.List<Task> tasksList = new ArrayList<Task>();
 	
 	private Task task;
 	
 	public void addTask( String[] strings )
 	{
 		Task task = new Task();
 		if(getTask(strings[0])==null){
 			tasksList.add(task);
 		} else{
 			task = getTask(strings[0]);
 		}
 		task.setName(strings[0]);
 		task.setDuration(Float.parseFloat( strings[1]));
 
 		for(int x = 2; x < strings.length; x = x+1) {
 	         Task pred = new Task();
 	 	     if(getTask(strings[0])==null){
 			 	tasksList.add(pred);
 			 } else{
 			 	pred = getTask(strings[x]);
 			 }
 	         pred.setName(strings[x]);
 	         pred.getSuccessorTask().add(task);
 	         task.getPredcessorTask().add(pred);
 	      }
 	}
 	
 	private Task getTask(String taskName) {
 		for(Task task  : tasksList){
 			if(taskName.equals(task.getName())){
 				return task;
 			}
 		}
 		return null;
 	}
 	public void readPERT( )
 	{
 		try {
 			BufferedReader br = new BufferedReader(new FileReader("example.pert"));
 			 
 			String sCurrentLine;
  
 			while ((sCurrentLine = br.readLine()) != null) {
 				this.addTask(sCurrentLine.split(","));
 				this.printTaskList();
 			}
  
 		} catch (IOException e) {
 			e.printStackTrace();
 		} 
 	}
 	
 	public void writeDOT( )
 	{
 		
 	}
 	public void printTaskList(){
 		for(Task task  : tasksList){
			System.out.println(task);
 		}
 	}
 	public float calculateCriticalPath(){
 		return calculateCriticalPathImpl(findFinalTask(), findFinalTask().getDuration(), new ArrayList<Task>() );
 	}
 	
 	public float calculateCriticalPathImpl(Task task1, Float duration, ArrayList<Task> tasks )
 	{
 		System.out.println(task1.getPredcessorTask().size());
 		float longestWay = duration;
 		if(task1.getPredcessorTask().size()==0){
 			return duration + task1.getDuration();
 		} else{
			int n = 0;
			int index = -1;
 			for(Task task : task1.getPredcessorTask()){
 				float dur = calculateCriticalPathImpl(task, duration + task.getDuration(), tasks);
 				if (dur>longestWay){
 					longestWay = dur;
					index = n;
 				}
				n +=1;
 			}
			System.out.println(index);
			if(index != -1){
				tasks.add(task1.getPredcessorTask().get(index));				
			}
		}
		for(Task task2 : tasks){
			task2.setCritical(true);
 		}
		findFinalTask().setCritical(true);
		findStartTask().setCritical(true);
 		System.out.println(tasks);
 		return longestWay;
 	}
 	
 	public Task findFinalTask(){
 		for(Task task  : tasksList){
 			if(task.getSuccessorTask().size()==0){
 				System.out.println(task);
 				return task;
 			}
 		}
 		return null;
 	}
 	
 	public Task findStartTask(){
 		for(Task task  : tasksList){
 			if(task.getPredcessorTask().size()==0){
 				System.out.println(task);
 				return task;
 			}
 		}
 		return null;
 	}
 	
 }
 
