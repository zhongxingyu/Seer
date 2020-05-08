 package org.kisst.gft.task;
 
 
 public class FileBasedTask implements Task {
 	private final TaskDefinition definition;
 	private Object data;
 	private Task.Status status=null;
 	private Exception lastError=null;
 	
 	public FileBasedTask(TaskDefinition definition, Object data) {
 		this.definition=definition;
 		this.data=data;
 	}
 	
 	public Object getData() { return data; }
 	public TaskDefinition getDefinition() { return definition; }
 	public void run() { definition.run(this); }
 	public void save() {  throw new RuntimeException("save not implemented yet"); }
 	public Status getStatus() { return status; }
 	public boolean isDone() { return status==DONE; }
 	public void setStatus(Status status) { this.status=status;}
 
 	public Exception getLastError() { return lastError; }
	public void setLastError(Exception e) {
		this.lastError=e;
	}
 	
 }
