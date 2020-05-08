 package models;
 
 import java.util.List;
 import org.bson.types.ObjectId;
 
 import com.google.code.morphia.annotations.Entity;
 import com.google.code.morphia.annotations.Id;
 import play.modules.morphia.Model;
 
 @Entity
 public class Course extends Model {
 
 	public Course(String courseName, String courseHead,
 			List<String> courseSubjects) {
 		super();
 		this.courseName = courseName;
 		this.courseHead = courseHead;
 		this.courseSubjects = courseSubjects;
 	}
//	@Id
//	public ObjectId id;
 	
 	public String courseName;
 	public String courseHead;
 	public List<String> courseSubjects;
//	String name;
//	
//	//overridden
//	@Override public Object getId() {return name;}
//    @Override protected void setId_(Object id) {name = id.toString();}
//    protected static Object processId_(Object id) {return id.toString();}
 }
