 package scheduleGenerator;
 
 import java.util.ArrayList;
 
 import scheduleGenerator.Constants.Season;
 
 public class StudentSchedule {
 	private ArrayList<SelectedCourse> selectedCourses;
 	private Season season;
 	
 	
 	public StudentSchedule(Season season) {
 		this.season = season;
 	}
 	
 	
 	public void setSelectedCourses(ArrayList<SelectedCourse> selectedCourses) {
 		this.selectedCourses = selectedCourses;
 	}
 	
 	
 	public void addSelectedCourse(SelectedCourse selectedCourse) {
 		selectedCourses.add(selectedCourse);
 	}
 	
	public void addSelectedCourse(CourseId courseId, Section section) {
		SelectedCourse selectedCourse = new SelectedCourse(courseId, section);
		selectedCourses.add(selectedCourse);
	}
	
 	
 	public ArrayList<SelectedCourse> getSelectedCourses(){
 		return selectedCourses;
 	}
 	
 	static public StudentSchedule getFakeSchedule(){
 		StudentSchedule sched = new StudentSchedule(Constants.Season.FALL);
 		//course 1
 		CourseId courseID = new CourseId("SOEN","341");
 		Section sect = new Section("ty");
 		TimeSlot ts = new TimeSlot("14:00", "15:15", "akdjflk",
 				"efawef", Constants.CourseType.LECT, Constants.Day.MONDAY,
 				"TY");
 		TimeSlot ts2 = new TimeSlot("14:00", "15:15", "akdjflk",
 				"efawef", Constants.CourseType.TUT, Constants.Day.WEDNESDAY,
 				"TY");
 		sect.timeSlots.add(ts);
 		sect.timeSlots.add(ts2);
 		sched.addSelectedCourse(courseID, sect);
 		
 		//course2
 		CourseId courseID2 = new CourseId("COEN","490");
 		Section sect2 = new Section("fg");
 		TimeSlot ts21 = new TimeSlot("16:00", "17:15", "akdjflk",
 				"efawef", Constants.CourseType.LECT, Constants.Day.MONDAY,
 				"fg");
 		TimeSlot ts22 = new TimeSlot("14:00", "15:15", "akdjflk",
 				"efawef", Constants.CourseType.TUT, Constants.Day.FRIDAY,
 				"fg");
 		sect.timeSlots.add(ts21);
 		sect.timeSlots.add(ts22);
 		sched.addSelectedCourse(courseID2, sect2);
 		
 		return sched;
 	}
 }
