 package jacs.database.exceed.model;
 
 import java.io.Serializable;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Table;
 

 @Entity
 @Table(name="ProjectList")
 public class Project_eXceed implements Serializable{
 	
	private static final long serialVersionUID = 1L; 
 	@Id
 	//@GeneratedValue(strategy = GenerationType.AUTO)
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private int project_ID;
 	private String project_Name;
 	private int scores;
 	
 	public Project_eXceed()	{
 		
 	}
 	public Project_eXceed(String project_Name) {
 		super();
 		this.project_Name = project_Name;
 		this.scores = 0;
 	}
 
 	public int getProject_ID() {
 		return project_ID;
 	}
 
 	public void setProject_ID(int project_ID) {
 		this.project_ID = project_ID;
 	}
 
 	public String getProject_Name() {
 		return project_Name;
 	}
 
 	public void setProject_Name(String project_Name) {
 		this.project_Name = project_Name;
 	}
 
 	public int getScores() {
 		return scores;
 	}
 	public void setScores(int scores) {
 		this.scores = scores;
 	}
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + project_ID;
 		result = prime * result
 				+ ((project_Name == null) ? 0 : project_Name.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (!(obj instanceof Project_eXceed))
 			return false;
 		Project_eXceed other = (Project_eXceed) obj;
 		if (project_ID != other.project_ID)
 			return false;
 		if (project_Name == null) {
 			if (other.project_Name != null)
 				return false;
 		} else if (!project_Name.equals(other.project_Name))
 			return false;
 		return true;
 	}
 	@Override
 	public String toString() {
 		return "Project_eXceed [project_ID=" + project_ID + ", project_Name="
 				+ project_Name + ", scores=" + scores + "]";
 	}
 
 	
 }
