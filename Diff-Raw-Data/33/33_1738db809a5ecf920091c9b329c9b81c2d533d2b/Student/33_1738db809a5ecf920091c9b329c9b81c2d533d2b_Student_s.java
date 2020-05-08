 package models;
 
 import java.util.Date;
 import java.util.List;
 
 import org.bson.types.ObjectId;
 
 import com.google.code.morphia.annotations.Embedded;
 import com.google.code.morphia.annotations.Entity;
 import com.google.code.morphia.annotations.Id;
 import com.google.code.morphia.annotations.Reference;
 
 import play.data.validation.Required;
 import play.modules.morphia.Model;
 
 
 /* this class has Student related details */
 @Entity
 public class Student extends Model {
 	
//	@Id
//	private Long objectId;
 	
 	// User details, user details are fetched as Student.user.userId; 
 	@Required
 	@Embedded
 	public User user;	
 	
 	// personalDetails
 	@Embedded
 	public PersonalDetails personalDetails;
 	
 	// enrolment details
 	@Embedded
 	public EnrolmentDetails enrolmentDetails;
 
 	// scores of the student
 	@Embedded
 	public List<Scores> scores;
 	
 	// Course details - Reference, not embedded
 	@Reference
 	public Course currentCourse;
 	
 
 }
