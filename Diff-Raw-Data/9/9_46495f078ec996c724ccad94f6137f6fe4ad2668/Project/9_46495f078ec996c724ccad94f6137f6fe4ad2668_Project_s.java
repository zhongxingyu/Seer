 package models;
 
 import java.util.*;
 import javax.persistence.*;
 import play.db.ebean.*;
 
 
 @Entity
 public class Project extends Model {
 
 	@Id
 	public Long id;
 	public String name;
 	public String folder;
 	
 	@ManyToMany(cascade = CascadeType.REMOVE)
 	public List<Uzer> members = new ArrayList<Uzer>();
 	
 	public Project(String name, String folder, Uzer owner) {
 		this.name = name;
 		this.folder = folder;
 		this.members.add(owner);
 	}
 	
 	public static Model.Finder<Long, Project> find = new Model.Finder<Long, Project>(Long.class,  Project.class);
 
	public static Project create(String name, String folder, String email) {
		Project project = new Project(name, folder, Uzer.find.ref(email));
 		project.save();
 		project.saveManyToManyAssociations("members");
 		return project;
 	}
 	
	public static List<Project> findInvolving(String uzerEmail) {
		return find.where().eq("members.email", uzerEmail).findList();
 	}
 	
 }
