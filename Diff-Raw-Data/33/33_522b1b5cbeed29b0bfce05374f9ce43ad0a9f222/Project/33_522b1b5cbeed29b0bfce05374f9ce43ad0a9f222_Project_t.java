 package model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import persistency.Database;
 
 public class Project extends DatabaseObject {
 
 	private String name;
 	private int hourBudget;
 	private long deadline;
 	private Developer manager;
 	private List<Activity> activities;
 	
 	/**
 	 * @param id
 	 * @param name
 	 * @param hourBudget
 	 * @param deadline
 	 * @param manager
 	 * @param activities
 	 */
 	public Project(Database db, int id, String name, int hourBudget, 
 			long deadline, Developer manager) {
 		super(id,db);
 		
 		this.name = name;
 		this.hourBudget = hourBudget;
 		this.deadline = deadline;
 		this.manager = manager;
 		this.activities = new ArrayList<Activity>();
 	}
 	
 	public Project(Database db, int id, String name, int hourBudget, 
 			long deadline) {
 		super(id,db);
 		
 		this.name = name;
 		this.hourBudget = hourBudget;
 		this.deadline = deadline;
 		this.activities = new ArrayList<Activity>();
 	}
 
 	@Override
 	protected void save() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	protected void delete() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public String[] getValueArray() {
 		return new String[]{this.name, String.valueOf(hourBudget), 
 				String.valueOf(deadline), 
				this.manager != null ? String.valueOf(this.manager.getId()) : "-1"};
 	}
 
 	public String getName() {
 		return name;
 	}
 	
 	public void setName(String name){
 		this.name = name;
 		this.db.project().update(this);
 	}
 
 	public int getHourBudget() {
 		return hourBudget;
 	}
 	
 	public void setHourBudget(int i) {
 		this.hourBudget = i;
 		this.db.project().update(this);
 	}
 
 	public long getDeadline() {
 		return deadline;
 	}
 	
 	public void setDeadline(int deadline){
 		this.deadline = deadline;
 		this.db.project().update(this);
 	}
 
 	public Developer getManager() {
 		return manager;
 	}
 	
 	public void setManager(Developer manager){
 		this.manager = manager;
 		this.db.project().update(this);
 	}
 
 	public List<Activity> getActivities() {
 		return activities;
 	}
 
 }
