 package models.json;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import models.State;
 import models.Status;
 
 import com.google.gson.annotations.Expose;
 
 public class Day {
 	@SuppressWarnings("unused")
 	@Expose
 	private int num;
 	@SuppressWarnings("unused")
 	@Expose
 	private String label;
 	@SuppressWarnings("unused")
 	@Expose
 	private long time;
 	@Expose
 	private List<Job> jobs = new ArrayList<Job>();
 	@Expose
 	private List<EnvChange> envChanges = new ArrayList<EnvChange>();
 	
 
 	public Day(int num, String label, Date start) {
 		this.num = num;
 		this.label = label;
 		this.time = Math.min(1440, ((new Date()).getTime()-start.getTime())/(60*1000));
 	}
 
 	public void addJob(long runId, long taskId, Date dayStart, String name, int envnum, Date start, Date end, String description, Status status, State state) {
 		Job j = new Job(runId, taskId, dayStart, name, envnum, start, end, description, status, state);
 		jobs.add(j);
 	}
 	public void addEnvChange(long envId, String name, int envnum, Date dayStart, Date since, Set<EnvironmentChange> changesBefore) {
 		EnvChange e = new EnvChange(envId, name, envnum, dayStart, since, changesBefore);
 		envChanges.add(e);
 	}
 
 
 	class Job {
 		@SuppressWarnings("unused")
 		@Expose
 		private long runId;
 		@SuppressWarnings("unused")
 		@Expose
 		private long taskId;
 		@SuppressWarnings("unused")
 		@Expose
 		private String name;
 		@SuppressWarnings("unused")
 		@Expose
 		private int envnum;
 		@Expose
 		private long start;
 		@Expose
 		private long end;
 		@SuppressWarnings("unused")
 		@Expose
 		private String description;
 		@SuppressWarnings("unused")
 		@Expose
 		private String description2;
 		@SuppressWarnings("unused")
 		@Expose
 		private String durationStr;
 		@SuppressWarnings("unused")
 		@Expose
 		private Status status;
 		@SuppressWarnings("unused")
 		@Expose
 		private State state;
 		
 		public Job(long runId, long taskId, Date dayStart, String name, int envnum, Date start, Date end, String description, Status status, State state) {
 			this.runId = runId; 
 			this.taskId = taskId; 
 			this.name = name; 
 			this.envnum = envnum; 
 			this.start = (start.getTime()-dayStart.getTime())/(60*1000); 
 			if (end == null) {
 				end = new Date();
 			}
 			this.end = (end.getTime()-dayStart.getTime())/(60*1000);  
 			this.description = (description == null ? description : description.replaceFirst("[(].*[)]", ""));  
 			this.description2 = (description == null ? description : description.replaceFirst("[^(]*[(]", "").replaceFirst("[)][^)]*", ""));  
 			long durMin = this.end-this.start;
			this.durationStr = (durMin > (60*24) ? (long)Math.floor(durMin/(60*24))+"d " : "");
 			durMin = durMin - (60*24)*(long)Math.floor(durMin/(60*24));
			this.durationStr += (durMin > 60 ? (long)Math.floor(durMin/60)+"h " : "");  
 			durMin = durMin - (60*(long)Math.floor(durMin/60));
 			this.durationStr += durMin+"m";  
 			this.status = status; 
 			this.state = state;
 			
 		}
 	}
 
 	class EnvChange {
 		@SuppressWarnings("unused")
 		@Expose
 		private long	id;
 		@SuppressWarnings("unused")
 		@Expose
 		private String name;
 		@SuppressWarnings("unused")
 		@Expose
 		private int envnum;
 		@SuppressWarnings("unused")
 		@Expose
 		private long since;
 		@SuppressWarnings("unused")
 		@Expose
 		private Set<EnvironmentChange> changesBefore;
 		
 		public EnvChange(long envId, String name, int envnum, Date dayStart, Date since, Set<EnvironmentChange> changesBefore) {
 			this.id = envId; 
 			this.name = name; 
 			this.envnum = envnum; 
 			this.since = (since.getTime()-dayStart.getTime())/(60*1000);
 			this.changesBefore = changesBefore;
 			
 		}
 	}
 }
