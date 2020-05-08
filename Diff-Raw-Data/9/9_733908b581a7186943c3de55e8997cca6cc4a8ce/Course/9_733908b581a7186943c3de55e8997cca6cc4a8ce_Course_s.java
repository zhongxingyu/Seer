package edu.mines.Schedule;
 
 import java.util.ArrayList;
 import java.lang.Double;
 
 /**
  * Represents a course for the purpose of scheduling.
  */
 class Course {
 	private final String courseNumber;
 	private final String courseName;
 	private final double numCredits;
 	private final ArrayList<String> textBooks;
 	private final ArrayList<Course> preReqs;
 	private final Department department;
 
 	public Course(String courseNumber, String courseName, Department department, double numCredits,
 			ArrayList<String> textBooks, ArrayList<Course> preReqs) {
 		this.courseNumber = courseNumber;
 		this.courseName = courseName;
 		this.department = department;
 		if(numCredits >= 0)
 			this.numCredits = numCredits;
 		else
 			throw new IllegalArgumentException("Cannot have negative number of credits");
 		this.textBooks = new ArrayList<String>(textBooks);
 		this.preReqs = new ArrayList<Course>(preReqs);
 	}
 
 	public String getCourseNumber() {
 		return courseNumber;
 	}
 
 	public String getCourseName() {
 		return courseName;
 	}
 
 	public Department getDepartment() {
 		return department;
 	}
 
 	public double getNumCredits() {
 		return numCredits;
 	}
 
 	public ArrayList<String> getTextBooks() {
 		return new ArrayList<String>(textBooks);
 	}
 
 	public ArrayList<Course> getPreReqs() {
 		return new ArrayList<Course>(preReqs);
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		builder.append(department.getCode());
 		builder.append(courseNumber);
 		builder.append(" ");
 		builder.append(courseName);
 		return builder.toString();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		Course c = (Course) obj;
 
 		if (this.courseName != c.getCourseName())
 			return false;
 		else if (this.courseNumber != c.getCourseNumber())
 			return false;
 		else if (this.numCredits != c.getNumCredits())
 			return false;
 		else if (this.department != c.getDepartment())
 			return false;
 		else if (!this.textBooks.containsAll(c.getTextBooks())
 				|| this.textBooks.size() != c.getTextBooks().size())
 			return false;
 
 		return true;
 	}
 	
 	@Override
   public int hashCode() {
     int hash=7;
     
     hash = 31*hash + courseName.hashCode();
     hash = 31*hash + courseNumber.hashCode();
     hash = 31*hash + new Double(numCredits).hashCode();
     hash = 31*hash + department.hashCode();
     hash = 31*hash + textBooks.hashCode();
     
     return hash;
   }
 
 }
