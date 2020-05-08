 package rpiplanner.model;
 
 import java.util.ArrayList;
 
 import rpiplanner.SchoolInformation;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamImplicit;
 
 @XStreamAlias("plan")
 public class PlanOfStudy {
 	protected String fullname;
 	protected String school;
 	protected String studentID;
 	protected int startingYear;
 
 	@XStreamImplicit
 	protected ArrayList<Term> terms;
 	
 	public PlanOfStudy(){
 		initializeTerms();
 	}
 
 	public String getFullname() {
 		return fullname;
 	}
 	public void setFullname(String fullname) {
 		this.fullname = fullname;
 	}
 	public String getSchool() {
 		return school;
 	}
 	public void setSchool(String school) {
 		this.school = school;
 	}
 	public String getStudentID() {
 		return studentID;
 	}
 	public void setStudentID(String studentID) {
 		this.studentID = studentID;
 	}
 	public int getStartingYear() {
 		return startingYear;
 	}
 	public void setStartingYear(int startingYear) {
 		this.startingYear = startingYear;
 	}
 	public Term getTerm(int index){
 		return terms.get(index);
 	}
 	public void rebuildTerms(){
 		for(int i = 0; i < SchoolInformation.DEFAULT_NUM_SEMESTERS/2; i++){
			terms.get(i).setYear(startingYear+i);
			terms.get(i+1).setYear(startingYear+i);
 		}
 	}
 	
 	/**
 	 * Initialize terms according to school. For now this is a simple
 	 * prototype that uses RPI's semester-based term system
 	 */
 	private void initializeTerms(){
		terms = new ArrayList<Term>(8);
 		for(int i = 0; i < SchoolInformation.DEFAULT_NUM_SEMESTERS/2; i++){
 			Term fall = new Term();
 			fall.setYear(startingYear+i);
 			fall.setTerm(Term.YearPart.FALL);
 			terms.add(fall);
 			
 			Term spring = new Term();
 			spring.setYear(startingYear+i);
 			spring.setTerm(Term.YearPart.SPRING);
 			terms.add(spring);
 		}
 	}
 }
