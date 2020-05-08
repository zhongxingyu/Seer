 package helper;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 /**
  * This class will create methods for the controller to access information
  * from the database using depending java classes
  * @author Jason Beck
  */
 public class CourseDAO {
 
 	/**
 	 * This PreparedStatement will query the database to find all of the classes
 	 * that satisfy a given requirement
 	 */
 	private PreparedStatement classesSatisfyReq;
 	
 	/**
 	 * This PreparedStatement will query the database to find all meetings of a
 	 * given course
 	 */
 	private PreparedStatement meetingsForCourse;
 	
 	/**
 	 * This PreparedStatement will query the database to find the requirement map 
 	 * names
 	 */
 	private PreparedStatement getReqMapNames;
 	
 	/**
 	 * ArrayLists of classes that satisfy the appropriate requirement number
 	 */
 	private ArrayList<Requirement> req1List;
 	private ArrayList<Requirement> req2List;
 	private ArrayList<Requirement> req3List;
 	private ArrayList<Requirement> req4List;
 	private ArrayList<Requirement> req5List;
 	private ArrayList<Requirement> req6List;
 	private ArrayList<Requirement> req7List;
 	private ArrayList<Requirement> req8List;
 	private ArrayList<Requirement> req9List;
 	private ArrayList<Requirement> req10List;
 	private ArrayList<Requirement> req11List;
 	private ArrayList<Requirement> req12List;
 	private ArrayList<Requirement> req13List;
 	private ArrayList<Requirement> req14List;
 	private ArrayList<Requirement> req15List;
 	private ArrayList<Requirement> req16List;
 	private ArrayList<Requirement> req17List;
 	private ArrayList<Requirement> req18List;
 	private ArrayList<Requirement> req19List;
 	
 	
 	/**
 	 * A constructor for a CourseDAO object to connect to the database and prepare the
 	 * PreparedStatements
 	 * @author Jason Beck
 	 */
 	public CourseDAO() {
 		try{
 			Class.forName("com.mysql.jdbc.Driver");
 			System.out.println("Instantiated MySQL driver");
 			Connection conn = DriverManager.getConnection("jdbc:mysql://172.17.152.60/classScheduler", 
 					"classSchedUser", "Thisisit03214380");
 			System.out.println("Connected to MySql");
 			meetingsForCourse = conn.prepareStatement("select * from classScheduler.course course, classScheduler.courseRequirement creq, " +
 					"(Select req.reqId from classScheduler.requirement req where req.reqCourseNumber = ? AND req.reqCoursePrefix = ?) AS reqOut " +
 					"where reqOut.reqId = creq.reqId AND creq.courseId = course.courseUID ORDER BY course.callNumber");
 			classesSatisfyReq = conn.prepareStatement("SELECT course.coursePrefix, course.courseNumber, course.courseTitle FROM classScheduler.course " +
 					"course, classScheduler.courseRequirement creq, (SELECT req.reqId FROM classScheduler.requirement req WHERE req.reqMapId = ?) AS reqOut " +
 					"WHERE reqOut.reqId = creq.reqId AND creq.courseId = course.courseUID GROUP BY course.courseNumber ORDER BY course.coursePrefix, course.courseNumber");
 			getReqMapNames = conn.prepareStatement("SELECT reqMapName FROM classScheduler.requirementMapping");
 
 			req1List = getCoursesForReq(1);
 			req2List = getCoursesForReq(2);
 			req3List = getCoursesForReq(3);
 			req4List = getCoursesForReq(4);
 			req5List = getCoursesForReq(5);
 			req6List = getCoursesForReq(6);
 			req7List = getCoursesForReq(7);
 			req8List = getCoursesForReq(8);
 			req9List = getCoursesForReq(9);
 			req10List = getCoursesForReq(10);
 			req11List = getCoursesForReq(11);
 			req12List = getCoursesForReq(12);
 			req13List = getCoursesForReq(13);
 			req14List = getCoursesForReq(14);
 			req15List = getCoursesForReq(15);
 			req16List = getCoursesForReq(16);
 			req17List = getCoursesForReq(17);
 			req18List = getCoursesForReq(18);
 			req19List = getCoursesForReq(19);
 			
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * This will create an arraylist of courses that will satisfy
 	 * a given requirement
 	 * @param Requirement requirement object
 	 * @return arraylist of class objects
 	 * @author Jason Beck
 	 */
 	private ArrayList<Requirement> getCoursesForReq(int reqMapId){
 		ArrayList<Requirement> list = new ArrayList<Requirement>();
 		try{
 			classesSatisfyReq.setInt(1, reqMapId);
 			ResultSet rs = classesSatisfyReq.executeQuery();
 			while(rs.next()){
 				//creates and adds a requirement object to the list from
 				//the record
 				String reqCoursePrefix = rs.getString("coursePrefix");
 				String reqCourseNumber = rs.getString("courseNumber");
 				String reqCourseTitle = rs.getString("courseTitle");
 				list.add(new Requirement(reqMapId, reqCoursePrefix, reqCourseNumber, reqCourseTitle));
 			}
 		}
 		catch (SQLException e) {
 			System.out.println(e.getClass().getName() + ": " + e.getMessage());
 		}
 		return list;
 	}
 	
 	/**
 	 * This will return all of the sections and meetings wrapped in a CourseListing object
 	 * for any course given as a requirement 
 	 * @param requirement requirement object
 	 * @return CourseListing course listing object
 	 * @author Jason Beck
 	 */
 	public CourseListing getSections(Requirement requirement){
 		try{
 			meetingsForCourse.setString(1,requirement.getReqCourseNumber());
 			meetingsForCourse.setString(2,requirement.getReqCoursePrefix());
 			ResultSet rs = meetingsForCourse.executeQuery();
 			ArrayList<ClassObj> classObjList = new ArrayList<ClassObj>();
 			String coursePrefix="", courseNumber="", courseTitle="";
 			while(rs.next()){
 				//packs the record into a ClassObj object and adds to arraylist
 				int callNumber = rs.getInt("callNumber");
 				coursePrefix = rs.getString("coursePrefix");
 				courseNumber = rs.getString("courseNumber");
 				courseTitle = rs.getString("courseTitle");
 				String days = rs.getString("days");
 				String periodBegin = rs.getString("periodBegin");
 				String periodEnd = rs.getString("periodEnd");
 				String bldg = rs.getString("bldg");
 				String room = rs.getString("room");
 				classObjList.add(new ClassObj(callNumber, coursePrefix, courseNumber, courseTitle,
 						days.trim(), periodBegin, periodEnd, bldg, room));
 				}
 			
 			CourseListing courseListing = new CourseListing(coursePrefix, courseNumber, courseTitle);
 			
 			for(int i=0; i < classObjList.size();){
 				//packages up each ClassObj into appropriate sections and meetings under the CourseListing object
 				ClassObj classes = classObjList.get(i);
 				ClassSection section = new ClassSection(classes.getCallNumber());
 						
 				int tempCallNumber = classes.getCallNumber();
 				int j = i;
 				while(tempCallNumber ==  classes.getCallNumber() && j < classObjList.size()){
 					//packages up a meeting(s) object and adds it to the list of meeting object in the ClassSection object
 										
 					//when there are multiple class meetings under a single line of a class section
 					if(classes.getDays().length() > 1){
 						String day = classes.getDays();
 						while(day.length() >= 1){
 							//packages up meeting objects and adds it to the list of meeting objects in the ClassSection object
 							//(for a single section that have multiple meeting days in it)
 							ClassMeeting meeting = new ClassMeeting(day.substring(0,1), classes.getPeriodBegin(), classes.getPeriodEnd(),
 									classes.getBldg(), classes.getRoom());
 							//adds meeting to section object
 							section.addClassMeetingList(meeting);
 							
 							//still more than 1 day left
 							if(day.length() > 1){
 								day = day.substring(2);
 							}
 							//only 1 day left so break
 							else break;
 						}
 					}
 					//only meets 1 day for this section
 					else{
 						ClassMeeting meeting = new ClassMeeting(classes.getDays(), classes.getPeriodBegin(), classes.getPeriodEnd(),
 								classes.getBldg(), classes.getRoom());
 						//adds meeting to section object
 						section.addClassMeetingList(meeting);
 					}
 					i=++j;
 					//must check to make sure increment will not go out of bounds
 					if(j < classObjList.size())
 						classes = classObjList.get(j);
 				}
 				//adds section to courseListing object
 				courseListing.addClassSectionList(section);	
 			}
 			return courseListing;
 		}
 		catch (SQLException e) {
 			System.out.println(e.getClass().getName() + ": " + e.getMessage());
 		}
 		return null;
 	}
 	
 	/**
 	 * Get a list of classes that satisfy a requirement
 	 * @param reqMapId requirement id
 	 * @return arraylist of classes satisfying the given requirement
 	 * @author Jason Beck
 	 */
 	public ArrayList<Requirement> getReqList(int reqMapId){
 		switch(reqMapId){
 			case 1: return req1List;
 			case 2: return req2List;
 			case 3: return req3List;
 			case 4: return req4List;
 			case 5: return req5List;
 			case 6: return req6List;
 			case 7: return req7List;
 			case 8: return req8List;
 			case 9: return req9List;
 			case 10: return req10List;
 			case 11: return req11List;
 			case 12: return req12List;
 			case 13: return req13List;
 			case 14: return req14List;
 			case 15: return req15List;
 			case 16: return req16List;
 			case 17: return req17List;
 			case 18: return req18List;
 			case 19: return req19List;
 			default: return null;
 		}
 	}
 	
 	/**
 	 * Return an arraylist of strings representing the requirement names
 	 * @return return an arraylist of strings representing the requirement names
 	 * @author Jeffrey Swindle
 	 */
 	public ArrayList<String> getReqMapNames(){
 		//Variable Decs
 		ResultSet rSet = null;
 		ArrayList<String> reqNames = new ArrayList<String>();
 		
 		//Try to get a customer by id with purchases and error if unsuccessful
 		try{
 			//Execute req name query
 			System.out.println("Execute Query");
 			rSet = getReqMapNames.executeQuery();
 			
 			//Run through the result set and add each requirement mapping 
 			//name to the array list of requirement names
 			while(rSet.next()){
 				reqNames.add(rSet.getString("reqMapName"));
 			}
 
 			//Return the array list of requirement mapping names
 			return reqNames;
 	      
 		}
 		catch( SQLException e ){
 			System.out.println("Error with SQL query");
 			e.printStackTrace();
 			System.exit(1);
 		}
 
 		return null;
 	}
 	
 	/**
 	 * This will determine if the arraylist of PendingCourse objects has any
 	 * conflicts in ClassMeetings
 	 * @param pendingCourses Arraylist of PendingCourse objects
 	 * @return true if there is a conflict and false if there is not 
 	 * @author Jason Beck
 	 */
 	public boolean courseConflict(ArrayList<PendingCourse> pendingCourses, PendingCourse newCourse){
 		if(pendingCourses.size() == 0)
 			//no pending courses yet
 			return false;
 		for(int j = 0; j < pendingCourses.size(); j++){
 			//compares two PendingCourse objects to see if they have conflicts
 			PendingCourse course2 = pendingCourses.get(j);
 			if(meetingConflict(newCourse.getClassMeetingsList(), course2.getClassMeetingsList()))
 				return true; //there is a meeting conflict
 		}
 		return false;
 	}
 	
 	/**
 	 * This will compare two ArrayList of ClassMeeting objects and determine if they have 
 	 * any conflicts
 	 * @param meetingList1 ArrayList of ClassMeeting objects
 	 * @param meetingList2 ArrayList of ClassMeeting objects
 	 * @return true if there is a conflict and false if there is not
 	 * @author Jason Beck
 	 */
 	protected boolean meetingConflict(ArrayList<ClassMeeting> meetingList1, ArrayList<ClassMeeting> meetingList2){
 		for(int i = 0; i < meetingList1.size(); i++){
 			//check conflicts with all meetings from meetingList2 against a meetingList1 ClassMeeting
 			ClassMeeting meeting1 = meetingList1.get(i);
 			for(int j = 0; j < meetingList2.size(); j++){
 				//checks conflict of meetingList2 ClassMeeting object with the meetingList1 ClassMeeting object
 				ClassMeeting meeting2 = meetingList2.get(j);
					if(meeting1.getBeginInt() == meeting2.getBeginInt())
 						return true; //meets at the same time
					else if(meeting1.getBeginInt() > meeting2.getBeginInt() && meeting2.getEndInt() > meeting1.getBeginInt() ||
 							meeting1.getBeginInt() < meeting2.getBeginInt() && meeting1.getEndInt() > meeting2.getBeginInt()){
 						//meeting1 starts AFTER meeting2 or meeting1 starts BEFORE meeting2 and the time overlap
 						return true;
 					}
 			}
 		}
 		return false;
 	}
 	
 		
 	public ArrayList<ClassMeeting> getMeetings(Requirement requirement, int callNumber){
 		CourseListing courseListing = getSections(requirement);
 		ArrayList<ClassSection> classSections = courseListing.getClassSectionList();
 		for(int i=0; i < classSections.size(); i++){
 			if(classSections.get(i).getCallNumber() == callNumber){
 				return classSections.get(i).getClassMeetingsList();
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Removes a PendingCourse object from the ArrayList of PendingCourses
 	 * @param pendingCourses ArrayList of PendingCourses
 	 * @param callNumber call number of the source to remove
 	 * @return ArrayList of PendingCourses with the desired class removed
 	 * @author Jason Beck
 	 */
 	public ArrayList<PendingCourse> removePendingCourse(ArrayList<PendingCourse> pendingCourses, int callNumber){
 		for(int i = 0; i < pendingCourses.size(); i++){
 			//check to see if the PendingCourse object matches the given calNumber
 			if(pendingCourses.get(i).getCallNumber() == callNumber){
 				//if the PendingCourse has this call number, remove it
 				pendingCourses.remove(i);
 				break;
 			}
 		}
 		return pendingCourses; 
 	}
 	
 }
