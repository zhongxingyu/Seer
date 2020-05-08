 package ycp.edu.seniordesign.model;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Date;
 
 public class Assignment {
 	int id;
 	int courseId;
 	int studentId;
 	String name;
 	Date dueDate;
 	int gradeWeightType;
 	int earnedPoints;
 	int possiblePoints;
 
 	public Assignment(){
 		
 	}
 	
 	public Assignment(int id, int courseId, int studentId, String name, Date dueDate, int gradeWeightType, int earnedPoints, int possiblePoints) {
 		this.id = id;
 		this.courseId = courseId;
 		this.studentId = studentId;
 		this.name = name;
 		this.dueDate = dueDate;
 		this.gradeWeightType = gradeWeightType;
 		this.earnedPoints = earnedPoints;
 		this.possiblePoints = possiblePoints;
 	}
 	
 	public int getId() {
 		return id;
 	}
 	
 	public void setId(int id) {
 		this.id = id;
 	}
 	
 	public int getCourseId() {
 		return courseId;
 	}
 	
 	public void setCourseId(int courseId) {
 		this.courseId = courseId;
 	}
 	
 	public int getStudentId() {
 		return studentId;
 	}
 
 	public void setStudentId(int studentId) {
 		this.studentId = studentId;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public Date getDueDate() {
 		return dueDate;
 	}
 
 	public void setDueDate(Date dueDate) {
 		this.dueDate = dueDate;
 	}
 	
 	public int getGradeWeightType() {
 		return gradeWeightType;
 	}
 	
 	public void setGradeWeightType(int gradeWeightType) {
 		this.gradeWeightType = gradeWeightType;
 	}
 	
 	public int getEarnedPoints() {
 		return earnedPoints;
 	}
 
 	public void setEarnedPoints(int earnedPoints) {
 		this.earnedPoints = earnedPoints;
 	}
 
 	public int getPossiblePoints() {
 		return possiblePoints;
 	}
 
 	public void setPossiblePoints(int possiblePoints) {
 		this.possiblePoints = possiblePoints;
 	}
 	
 	public boolean isOverdue(){
 		return dueDate.before(new Date(System.currentTimeMillis()));
 	}
 	
 	/**
 	 * This method can be used to load the fields of an assignment from a resultSet to an Assignment object
 	 * @param resultSet the resultSet to load the fields from
 	 * @throws SQLException
 	 * Note: The fields in this method must be set in the same order the columns occur in the database
 	 */
 	public void loadFrom(ResultSet resultSet) throws SQLException {
 		int index = 1;
 		setId(resultSet.getInt(index++));
 		setCourseId(index++);
 		setStudentId(index++);
 		setName(resultSet.getString(index++));
 		setDueDate(resultSet.getDate(index++));
 		setGradeWeightType(resultSet.getInt(index++));
		setEarnedPoints(index++);
		setPossiblePoints(index++);
 	}
 	
 	/**
 	 * This method can be used to store the fields from an assignment object to a prepared statement
 	 * @param statement the PreparedStatement to store the fields to
 	 * @throws SQLException
 	 * Note: The fields in this method must be set in the same order the columns occur in the database
 	 */
 	 
 	 public void storeTo(PreparedStatement statement) throws SQLException {
 		int index = 1;
 		statement.setInt(index++, id);
 		statement.setInt(index++, courseId);
 		statement.setInt(index++, studentId);
 		statement.setString(index++, name);
 		statement.setDate(index++, new java.sql.Date(dueDate.getTime()));
 		statement.setInt(index++, gradeWeightType);
 		statement.setInt(index++, earnedPoints);
 		statement.setInt(index++, possiblePoints);
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null || obj.getClass() != this.getClass()) {
 			return false;
 		}
 		Assignment other = (Assignment) obj;
 		
 		if (dueDate == null) {
 			return id == other.id
 					&& courseId == other.courseId
 					&& studentId == other.studentId
 					&& name.equals(other.name)
 					&& dueDate == other.dueDate
 					&& gradeWeightType == other.gradeWeightType
 					&& earnedPoints == other.earnedPoints
 					&& possiblePoints == other.possiblePoints;
 		} else {		
 			return id == other.id
 					&& courseId == other.courseId
 					&& studentId == other.studentId
 					&& name.equals(other.name)
 					&& dueDate.equals(other.dueDate)
 					&& gradeWeightType == other.gradeWeightType
 					&& earnedPoints == other.earnedPoints
 					&& possiblePoints == other.possiblePoints;
 		}
 	}
 
 
 
 
 }
