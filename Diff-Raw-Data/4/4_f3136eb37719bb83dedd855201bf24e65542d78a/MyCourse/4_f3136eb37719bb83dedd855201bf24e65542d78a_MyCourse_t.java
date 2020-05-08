 package objects;
 
 import java.util.ArrayList;
 import java.util.List;
 /**
  * Course defines a actual teacher's course, with name, course ID, course number, section number,
  * building, room number, meeting time, an ArrayList of Students, an ArrayList of AssignmentCategories,
  * and a gradeBook object to hold all of the students grades as they will appear on the final html5 document
  * screen in the application
  * 
  * @Jesse W. Milburn
  * @date 01 October, 2013
  */
 
 public class MyCourse {
     //course data kept private so only method calls can change them
     private String courseName;
     private String courseID;
     private int courseNumber;
     private String section;
     private String building;
     private String roomID;
     private String meetingTime;
     private List<Student> students = new ArrayList<Student>();
     private List<AssignmentCategory> categories = new ArrayList<AssignmentCategory>();
     private List<GhostStudent> ghostStudents = new ArrayList<GhostStudent>();
     private CourseGrades gradebook = new CourseGrades();
     //private PseudoNameGenerator psNames = new PseudoNameGenerator();
     
     /**
      * Constructs a new MyCourse object, note there is no 'empty' constructor
      * 
      * @param   cn  string denoting what the callee has named the course
      */
     public MyCourse(String cn) {
         courseName = cn;
     }
     
     /**
      * Set the name of the course.
      * 
      * @param   cn  set the courseName
      */
     public void setName(String cn) {
         courseName = cn;
     }
     
     /**
      * Set the course ID of the course
      * 
      * @param   cid set the courseID
      */
     public void setCourseID(String cid) {
         courseID = cid;
     }
     
     /**
      * Set the course number of the course
      * 
      * @param   cnum    set the course number
      */
     public void setCourseNumber(int cnum) {
         courseNumber = cnum;
     }
     
     /**
      * Set the section identification of the course.
      * 
      * @param   sec    set the section identification
      */
     public void setSection(String sec) {
         section = sec;
     }
     
     /**
      * Set the building name
      * 
      * @param   bn    set the building name
      */
     public void setBuilding(String bn) {
         building = bn;
     }
     
     /**
      * Set the room number identification
      * 
      * @param   rn    set the room identification
      */
     public void setRoomID(String rn) {
         roomID = rn;
     }
     
     /**
      * Set the meeting time 
      * 
      * @return      meeting time
      */
     public void setMeetingTime(String mt) {
         meetingTime = mt;
     }
     
     /**
      * Get the name of the course
      * 
      * @return      get the name of the course
      */
     public String getName() {
         return courseName;
     }
     
     /**
      * Get the course identification
      * 
      * @return      course identification
      */
     public String getCourseID() {
         return courseID;
     }
     
     /**
      * Get the course number
      * 
      * @return      course number
      */
     public int getCourseNumber() {
         return courseNumber;
     }
     
     /**
      * Get the section identification of the course
      * 
      * @return      section identification
      */
     public String getSection() {
         return section;
     }
     
     /**
      * Get the name of the building where the course is held
      * 
      * @return      building name
      */
     public String getBuilding() {
         return building;
     }
     
     /**
      * Get the room number of the course
      * 
      * @return      room number
      */
     public String getRoomID() {
         return roomID;
     }
     
     /**
      * Get meeting time of the course
      * 
      * @return      meeting time
      */
     public String getMeetingTime() {
         return meetingTime;
     }
     
     /**
      * Returns a string of all of the information about a course
      * 
      * @return        complete course overview of description, location, time
      */
     public String toString() {
         return (courseName + ": " + courseID + " " + courseNumber + "-" + section + ", " + building +
                 " " + roomID + ", " + meetingTime);
     }
     
     /**
      * Contstucts a new AssignmentCategory object and adds it into the categegories
      * ArrayList structure.
      * 
      * @param   ac  The name of the assignment category
      */
     public void addAssignmentCategory(String ac) {
         categories.add(new AssignmentCategory(ac));
     }
     
     /**
      * Returns the AssignmentCategory object from the index in the categories ArrayList
      * 
      * @return      the requested AssignmentCategory object
      */    
     public AssignmentCategory getAssignmentCategory(int index) {
         return categories.get(index);
     }
     
     /**
      * Returns the index of the AssignmentCategory in the categories arrayList
      * 
      * @param   name    the name of the AssignmentCategory
      * @return          the index of the AssignmentCategory object
      */
     public int getAssignmentCategoryIndex(String name) {
         //iterates through AssignmentCategor objects and performs name checking, 
         //returns index if successful else returns -1
         for (int i = 0; i < categories.size(); i++) {
             if (name == categories.get(i).getName()) return i;
         }
         
         return -1;
     }
     
    public int getNumberOfAssignmentCategories() {
    	return categories.size();
    }
    
     /**
      * Removes the AssignmentCategory object from the categories ArrayList
      * 
      * @param   name    the string name of the object
      * @return          the AssignmentCategory object removed
      */
     public AssignmentCategory removeAssignmentCategory(String name) {
         try {
             return categories.remove(getAssignmentCategoryIndex(name));
         } catch (ArrayIndexOutOfBoundsException e) {
             return null;
         }
     }
     
     /**
      * Constructs a new Student object and adds it into the students ArrayList structure
      * 
      * @param   sn  students actual name
      * @param   pn  students pseudo-name
      */
     public void addStudent(String fn, String ln, String pn) {
         students.add(new Student(fn, ln, pn));
     }
         
     /**
      * Returns the Studend object at the specified index
      * 
      * @param   index   the index of a Student object
      * @return          the Student object
      */
     public Student getStudent(int index) {
         return students.get(index);
     }
     
     /**
      * Returns the index of the Student in the student arrayList
      * 
      * @param   name    the name of the student
      * @return          the index of the Student object
      */
     public int getStudentIndex(String name) {
         //iterates through Student objects and performs name checking, returns index if successful else returns -1
         for (int i = 0; i < students.size(); i++) {
             if (name == students.get(i).getFullName()) return i;
         }
         
         return -1;
     }
     
     /**
      * Removes a Student object from the students ArrayList by a String name param
      * 
      * @param   name    the String name of the student in the Student object
      * @return          the Student object removed, null on the object was not in the list
      */
     public Student removeStudent(String name) {
         try {
             return students.remove(getStudentIndex(name));
         } catch (ArrayIndexOutOfBoundsException e) {
             return null;
         }
     }
     
     /**
      * Removes a Student object from the students arrayList by the index number
      * 
      * @param   index   the integer index of the Student object in the students ArrayList
      * @return          the Student object removed, null if the index passed is out of bounds
      */
     public Student removeStudent(int index) {
         try {
             return students.remove(index);
         } catch (IndexOutOfBoundsException e) {
             return null;
         }
     }
     
     public int getNumberOfStudents() {
     	return students.size();
     }
     
     /**
      * Constructs a new GhostStudent object and adds it into the fakeStudents ArrayList structure
      * 
      * @param   pn  ghost students pseudo-name
      */
     public void addGhostStudent(String pn) {
         ghostStudents.add(new GhostStudent(pn));
     }
         
     /**
      * Returns the GhostStudent object at the specified index
      * 
      * @param   index   the index of a GhostStudent object
      * @return          the GhostStudent object
      */
     public GhostStudent getGhostStudent(int index) {
         return ghostStudents.get(index);
     }
     
     /**
      * Returns the index of the GhostStudent in the student arrayList
      * 
      * @param   name    the name of the ghost student
      * @return          the index of the GhostStudent object
      */
     public int getGhostStudentIndex(String name) {
         //iterates through GhostStudent objects and performs name checking, 
     	//returns index if successful else returns -1
         for (int i = 0; i < ghostStudents.size(); i++) {
             if (name == ghostStudents.get(i).getPseudoName()) return i;
         }
         return -1;
     }
     
     /**
      * Removes a GhostStudent object from the students ArrayList
      * 
      * @param   name    the String name of the ghost student in the GhostStudent object
      * @return          the GhostStudent object removed, null on the object was not in the list
      */
     public GhostStudent removeGhostStudent(String name) {
         try {
             return ghostStudents.remove(getGhostStudentIndex(name));
         } catch (ArrayIndexOutOfBoundsException e) {
             return null;
         }
     }
     
     /**
      * Removes a GhostStudent object from the students arrayList by the index number
      * 
      * @param   index   the integer index of the GhostStudent object in the ghostStudents ArrayList
      * @return          the GhostStudent object removed, null if the index passed is out of bounds
      */
     public GhostStudent removeGhostStudent(int index) {
         try {
             return ghostStudents.remove(index);
         } catch (IndexOutOfBoundsException e) {
             return null;
         }
     }
     
     public int getNumberOfGhostStudents() {
    		return ghostStudents.size();
     }
     
     public int getTotalStudents() {
     	return ghostStudents.size() + students.size();
     }
 
     /**
      * Constructs a new CourseGrades object from gradebook
      * @return 
      */
 /*    public void createGradebook() {
         gradebook = new CourseGrades();
     }*/
     
     public CourseGrades getGradeBook() {
     	return gradebook;
     }
 }
