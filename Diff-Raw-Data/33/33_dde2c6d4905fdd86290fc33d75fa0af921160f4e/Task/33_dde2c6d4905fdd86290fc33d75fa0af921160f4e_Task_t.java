 package org.kisst.gft.task;
 
 

 public interface Task {
 	public static class Status {
 		private final String name;
 		public Status(String name) { this.name=name; }
 		public String getName() {return name; }
 	}
 	public static Status DONE=new Status("DONE");
 	
 	public Object getData();
 	public TaskDefinition getDefinition();
 	public void run();
 	public void save();
 	public void setStatus(Status status);
 	public Status getStatus();
 	public boolean isDone();
 	public void setLastError(Exception e);
 	public Exception getLastError();
	public String getLastAction();
	public void setLastAction(String name);
 }
