 package course;
 
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.ParseException;
 
 import course.Subject;
 
 import lynx.ITypes.countType;
 import lynx.Manager;
 
 import account.CreateAccount;
 
 public class CourseController extends lynx.Manager {
 	private static Connection con;
 	private static String SQL;
 	private static ResultSet rs;
 
 	public static void addSubject(String name, int calendarID)
 			throws SQLException {
 
 		con = cpds.getConnection();
 		con.setAutoCommit(false);
 		SQL = "INSERT INTO [subject] (name,calendarID)\r\n" + "VALUES (?,?)";
 		PreparedStatement stmt = con.prepareStatement(SQL);
 		System.out.println(SQL);
 		stmt.setString(1, name);
 		stmt.setInt(2, calendarID);
 		try {
 			stmt.executeUpdate();
 			con.commit();
 		} finally {
 			con.close();
 			stmt.close();
 		}
 
 	}
 
 	public static int validateSubject(int name) {
 		if (name > 0 && name <= 50) {
 			return 1;
 		}
 		return 0;
 	}
 
 	public static int validateGrade(int grade) {
 		if (grade > 0 && grade <= 1) {
 			return 1;
 		}
 		return 0;
 	}
 
 	public static void removeSubject(int subjectID) throws SQLException,
 			ParseException {
 
 		if (checkSubjectByID(subjectID) != 0) {
 			con = cpds.getConnection();
 			con.setAutoCommit(false);
 			SQL = "DELETE FROM [subject] WHERE subjectID = ?";
 			PreparedStatement stmt = con.prepareStatement(SQL);
 			System.out.println(SQL);
 			stmt.setInt(1, subjectID);
 			try {
 				stmt.executeUpdate();
 				con.commit();
 				removeCourseBySubject(subjectID);
 			} finally {
 				con.close();
 				stmt.close();
 			}
 		}
 
 	}
 
 	public static void removeSubjectFromCalendar(int calendarID)
 			throws SQLException, ParseException {
 
 		con = cpds.getConnection();
 		con.setAutoCommit(false);
 		SQL = "DELETE FROM [subject] WHERE calendarID = ?\r\n"
 				+ "DELETE FROM [course] WHERE calendarID = ?";
 		PreparedStatement stmt = con.prepareStatement(SQL);
 		System.out.println(SQL);
 		stmt.setInt(1, calendarID);
 		stmt.setInt(2, calendarID);
 		try {
 			stmt.executeUpdate();
 			con.commit();
 
 		} finally {
 			con.close();
 			stmt.close();
 		}
 
 	}
 
 	public static void removeCourseBySubject(int subjectID) throws SQLException {
 		con = cpds.getConnection();
 		con.setAutoCommit(false);
 		SQL = "DELETE FROM course WHERE subjectID = ?";
 		PreparedStatement stmt = con.prepareStatement(SQL);
 		System.out.println(SQL);
 		stmt.setInt(1, subjectID);
 		try {
 			stmt.executeUpdate();
 			con.commit();
 
 		} finally {
 			con.close();
 			stmt.close();
 		}
 	}
 
 	public static int getCount(countType type, int id, int sid)
 			throws SQLException {
 		con = cpds.getConnection();
 		if (type == countType.SUBJECTS) {
 			SQL = "SELECT COUNT(*) as total_rows FROM [subject]";
 		} else if (type == countType.COURSES) {
 			SQL = "SELECT COUNT(*) as total_rows FROM Course";
 		} else if (type == countType.SUBJECT_CALENDAR) {
 			SQL = "SELECT COUNT(*) as total_rows FROM [subject] WHERE calendarID = ?";
 		} else if (type == countType.COURSES_CALENDAR) {
 			SQL = "SELECT COUNT(*) as total_rows FROM Course WHERE calendarID = ?";
 		} else if (type == countType.STUDENT_ENROLLMENT_COUNT) {
 			SQL = "SELECT COUNT(*) as total_rows FROM enrollment WHERE studentID = ?";
 		} else if (type == countType.COURSES_FROM_SUBJECTS) {
 			SQL = "SELECT COUNT(*) as total_rows FROM course WHERE subjectID = ?";
 		} else if (type == countType.COURSES_CALENDAR_STUDENT) {
 			SQL = "SELECT COUNT(*) as total_rows FROM enrollment WHERE studentID = ? AND calendarID = ?";
 		}
 		System.out.println(SQL);
 		PreparedStatement stmt = con.prepareStatement(SQL,
 				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		if (type == countType.SUBJECT_CALENDAR
 				|| type == countType.COURSES_CALENDAR
 				|| type == countType.STUDENT_ENROLLMENT_COUNT
 				|| type == countType.COURSE_ENROLLMENT_COUNT
 				|| type == countType.COURSES_FROM_SUBJECTS) {
 			stmt.setInt(1, id);
 		} else if (type == countType.COURSES_CALENDAR_STUDENT) {
 			stmt.setInt(1, id);
 			stmt.setInt(2, sid);
 		}
 
 		try {
 			rs = stmt.executeQuery();
 
 			if (!rs.isBeforeFirst()) {
 				return 0;
 
 			} else {
 				rs.first();
 				return rs.getInt("total_rows");
 			}
 
 		} finally {
 			con.close();
 			stmt.close();
 		}
 	}
 
 	public static int validateCourseDate(int name, int sname) {
 		if (name <= 50 && name > 0 && sname > 0 & sname <= 10) {
 			return 1;
 		}
 		return 0;
 	}
 
 	public static String getCourse(int enrollID) throws SQLException {
 		con = cpds.getConnection();
 
 		SQL = "SELECT c.name, c.shortName FROM enrollment e\r\n"
 				+ "INNER JOIN course c ON c.courseID = e.courseID\r\n"
 				+ "WHERE enrollmentID = ?";
 		System.out.println(SQL);
 		System.out.println(enrollID);
 		PreparedStatement stmt = con.prepareStatement(SQL,
 				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		stmt.setInt(1, enrollID);
 		try {
 			rs = stmt.executeQuery();
 
 			if (!rs.isBeforeFirst()) {
 				return null;
 
 			} else {
 				rs.first();
 				return rs.getString("name") + " (" + rs.getString("shortName")
 						+ ")";
 			}
 
 		} finally {
 			con.close();
 			stmt.close();
 		}
 
 	}
 
 	public static Subject[] getSubjects() throws SQLException {
 		int totalPeople = getCount(countType.SUBJECTS, 0, 0);
 		System.out.println(totalPeople);
 		Subject[] people = new Subject[totalPeople];
 		Connection con = cpds.getConnection();
 
 		SQL = "SELECT subjectID,calendarID,name FROM subject";
 		PreparedStatement stmt = con.prepareStatement(SQL,
 				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		System.out.println(SQL);
 		try {
 			rs = stmt.executeQuery();
 
 			if (!rs.isBeforeFirst()) {
 				return null;
 
 			} else {
 				rs.beforeFirst();
 
 				int i = 0;
 				while (rs.next()) {
 					people[i] = new Subject(rs.getString("name"),
 							rs.getInt("subjectID"), rs.getInt("calendarID"));
 					i++;
 				}
 				return people;
 
 			}
 		} finally {
 			stmt.close();
 			con.close();
 			rs.close();
 
 		}
 
 	}
 
 	public static Subject[] getSubjects(int calendarID) throws SQLException {
 		int totalPeople = getCount(countType.SUBJECT_CALENDAR, calendarID, 0);
 		System.out.println(totalPeople);
 		Subject[] people = new Subject[totalPeople];
 		Connection con = cpds.getConnection();
 
 		SQL = "SELECT subjectID,calendarID,name FROM subject WHERE calendarID = ?";
 		PreparedStatement stmt = con.prepareStatement(SQL,
 				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		stmt.setInt(1, calendarID);
 		System.out.println(SQL);
 		try {
 			rs = stmt.executeQuery();
 
 			if (!rs.isBeforeFirst()) {
 				return null;
 
 			} else {
 				rs.beforeFirst();
 
 				int i = 0;
 				while (rs.next()) {
 					people[i] = new Subject(rs.getString("name"),
 							rs.getInt("subjectID"), rs.getInt("calendarID"));
 					i++;
 				}
 				return people;
 
 			}
 		} finally {
 			stmt.close();
 			con.close();
 			rs.close();
 
 		}
 
 	}
 
 	public static Course[] getCourses() throws SQLException {
 		int totalPeople = getCount(countType.COURSES, 0, 0);
 		System.out.println(totalPeople);
 		Course[] people = new Course[totalPeople];
 		Connection con = cpds.getConnection();
 
 		SQL = "SELECT c.courseID,c.subjectID,c.teacherID,c.name,c.shortName, s.name AS subjectName\r\n"
 				+ "FROM course c\r\n"
 				+ "INNER JOIN [subject] s ON s.subjectID = c.subjectID";
 		PreparedStatement stmt = con.prepareStatement(SQL,
 				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		System.out.println(SQL);
 		try {
 			rs = stmt.executeQuery();
 
 			if (!rs.isBeforeFirst()) {
 				return null;
 
 			} else {
 				rs.beforeFirst();
 
 				int i = 0;
 				while (rs.next()) {
 					people[i] = new Course(rs.getInt("courseID"),
 							rs.getInt("subjectID"), rs.getString("name"),
 							rs.getString("shortName"), rs.getInt("teacherID"),
 							rs.getString("subjectName"));
 					i++;
 				}
 				return people;
 
 			}
 		} finally {
 			stmt.close();
 			con.close();
 			rs.close();
 
 		}
 
 	}
 
 	public static Course[] getCourses(int calendarID) throws SQLException {
 		int totalPeople = getCount(countType.COURSES_CALENDAR, calendarID, 0);
 		System.out.println(totalPeople);
 		Course[] people = new Course[totalPeople];
 		Connection con = cpds.getConnection();
 
 		SQL = "SELECT c.courseID,c.subjectID,c.teacherID,c.name,c.shortName, s.name AS subjectName, ca.name AS calendarName\r\n"
 				+ "FROM course c\r\n"
 				+ "INNER JOIN [subject] s ON s.subjectID = c.subjectID \n" +
 				"INNER JOIN calendar ca ON ca.calendarID = c.calendarID \n"
 				+ "WHERE c.calendarID = ?";
 		PreparedStatement stmt = con.prepareStatement(SQL,
 				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		stmt.setInt(1, calendarID);
 		System.out.println(SQL);
 		try {
 			rs = stmt.executeQuery();
 
 			if (!rs.isBeforeFirst()) {
 				return null;
 
 			} else {
 				rs.beforeFirst();
 
 				int i = 0;
 				while (rs.next()) {
 					people[i] = new Course(rs.getInt("courseID"),
 							rs.getInt("subjectID"), rs.getString("name"),
 							rs.getString("shortName"), rs.getInt("teacherID"),
 							rs.getString("subjectName"), rs.getString("calendarName"));
 					i++;
 				}
 				return people;
 
 			}
 		} finally {
 			stmt.close();
 			con.close();
 			rs.close();
 
 		}
 
 	}
 
 	public static Course[] getCoursesBySubject(int subjectID)
 			throws SQLException {
 		int totalPeople = getCount(countType.COURSES_FROM_SUBJECTS, subjectID,
 				0);
 		System.out.println(totalPeople);
 		Course[] people = new Course[totalPeople];
 		Connection con = cpds.getConnection();
 
		SQL = "SELECT  c.courseID,c.name, c.shortName, c.subjectID  FROM course c\r\n"
 				+ "WHERE c.subjectID = ?";
 		PreparedStatement stmt = con.prepareStatement(SQL,
 				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		stmt.setInt(1, subjectID);
 		System.out.println(SQL);
 		try {
 			rs = stmt.executeQuery();
 
 			if (!rs.isBeforeFirst()) {
 				return null;
 
 			} else {
 				rs.beforeFirst();
 
 				int i = 0;
 				while (rs.next()) {
 					people[i] = new Course(rs.getInt("courseID"),
 							rs.getInt("subjectID"), rs.getString("name"),
							rs.getString("shortName"));
 					i++;
 				}
 				return people;
 
 			}
 		} finally {
 			stmt.close();
 			con.close();
 			rs.close();
 
 		}
 
 	}
 
 	public static Course[] getCoursesByStudent(int studentID, int calendarID)
 			throws SQLException {
 		int totalPeople = getCount(countType.COURSES_CALENDAR_STUDENT,
 				studentID, calendarID);
 		System.out.println(totalPeople);
 		Course[] people = new Course[totalPeople];
 		Connection con = cpds.getConnection();
 
 		SQL = "SELECT c.courseID, c.name, c.shortName, c.subjectID, e.enrollmentID FROM course c\r\n"
 				+ "INNER JOIN enrollment e ON e.courseID = c.courseID\r\n"
 				+ "WHERE e.studentID = ? AND e.calendarID = ?";
 		PreparedStatement stmt = con.prepareStatement(SQL,
 				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		stmt.setInt(1, studentID);
 		stmt.setInt(2, calendarID);
 		System.out.println(SQL);
 		try {
 			rs = stmt.executeQuery();
 
 			if (!rs.isBeforeFirst()) {
 				return null;
 
 			} else {
 				rs.beforeFirst();
 
 				int i = 0;
 				while (rs.next()) {
 					people[i] = new Course(rs.getInt("courseID"),
 							rs.getInt("subjectID"), rs.getString("name"),
 							rs.getString("shortName"),
 							rs.getString("enrollmentID"));
 					i++;
 				}
 				return people;
 
 			}
 		} finally {
 			stmt.close();
 			con.close();
 			rs.close();
 
 		}
 
 	}
 
 	public static Course[] getCoursesNotEnrolled(int studentID)
 			throws SQLException {
 		int totalPeople = getCount(countType.COURSES, 0, 0);
 		System.out.println(totalPeople);
 		Course[] people = new Course[totalPeople];
 		Connection con = cpds.getConnection();
 
 		SQL = "SELECT c.courseID,c.subjectID,c.teacherID,c.name,c.shortName, s.name AS subjectName \r\n"
 				+ "FROM course c\r\n"
 				+ "INNER JOIN [subject] s ON s.subjectID = c.subjectID\r\n"
 				+ "WHERE NOT EXISTS (SELECT enrollment.courseID, enrollment.studentID FROM enrollment WHERE enrollment.courseID = c.courseID AND enrollment.studentID = ?)";
 		PreparedStatement stmt = con.prepareStatement(SQL,
 				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		stmt.setInt(1, studentID);
 		System.out.println(SQL);
 		try {
 			rs = stmt.executeQuery();
 
 			if (!rs.isBeforeFirst()) {
 				return null;
 
 			} else {
 				rs.beforeFirst();
 
 				int i = 0;
 				while (rs.next()) {
 					people[i] = new Course(rs.getInt("courseID"),
 							rs.getInt("subjectID"), rs.getString("name"),
 							rs.getString("shortName"), rs.getInt("teacherID"),
 							rs.getString("subjectName"));
 					i++;
 				}
 				return people;
 
 			}
 		} finally {
 			stmt.close();
 			con.close();
 			rs.close();
 
 		}
 
 	}
 
 	public static Enrollment[] getCoursesIsEnrolled(int studentID)
 			throws SQLException {
 		int totalPeople = getCount(countType.STUDENT_ENROLLMENT_COUNT,
 				studentID, 0);
 		System.out.println(totalPeople);
 		Enrollment[] people = new Enrollment[totalPeople];
 		Connection con = cpds.getConnection();
 
 		SQL = "SELECT c.courseID,c.subjectID,c.teacherID,c.name,c.shortName, s.name AS subjectName, e.enrollmentID, e.studentID, e.calendarID\r\n"
 				+ "FROM course c\r\n"
 				+ "INNER JOIN [subject] s ON s.subjectID = c.subjectID \r\n"
 				+ "INNER JOIN enrollment e ON e.courseID = c.courseID\r\n"
 				+ "WHERE EXISTS (SELECT enrollment.courseID, enrollment.studentID FROM enrollment WHERE enrollment.courseID = c.courseID) \r\n"
 				+ "AND NOT EXISTS(SELECT grade.enrollmentID FROM grade WHERE grade.enrollmentID=e.enrollmentID)\r\n"
 				+ "AND e.studentID = ?";
 		PreparedStatement stmt = con.prepareStatement(SQL,
 				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		stmt.setInt(1, studentID);
 		System.out.println(SQL);
 		try {
 			rs = stmt.executeQuery();
 
 			if (!rs.isBeforeFirst()) {
 				return null;
 
 			} else {
 				rs.beforeFirst();
 
 				int i = 0;
 				while (rs.next()) {
 					people[i] = new Enrollment(rs.getInt("enrollmentID"),
 							rs.getInt("courseID"), rs.getInt("studentID"),
 							rs.getInt("calendarID"), rs.getString("name"),
 							rs.getString("shortName"));
 					i++;
 				}
 				return people;
 
 			}
 		} finally {
 			stmt.close();
 			con.close();
 			rs.close();
 
 		}
 
 	}
 
 	public static Enrollment[] getCoursesWithGrades(int studentID)
 			throws SQLException {
 		int totalPeople = getCount(countType.STUDENT_ENROLLMENT_COUNT,
 				studentID, 0);
 		System.out.println(totalPeople);
 		Enrollment[] people = new Enrollment[totalPeople];
 		Connection con = cpds.getConnection();
 
 		SQL = "SELECT c.courseID,c.subjectID,c.teacherID,c.name,c.shortName, s.name AS subjectName, e.enrollmentID, e.studentID, e.calendarID, g.gradeID   \r\n"
 				+ "															FROM course c\r\n"
 				+ "														INNER JOIN [subject] s ON s.subjectID = c.subjectID \r\n"
 				+ "															INNER JOIN enrollment e ON e.courseID = c.courseID\r\n"
 				+ "															INNER JOIN grade g ON g.enrollmentID = e.enrollmentID\r\n"
 				+ "																WHERE EXISTS (SELECT enrollment.courseID, enrollment.studentID FROM enrollment WHERE enrollment.courseID = c.courseID) AND e.studentID = ?\r\n"
 				+ "																AND EXISTS (SELECT grade.enrollmentID FROM grade WHERE grade.enrollmentID = e.enrollmentID)";
 		PreparedStatement stmt = con.prepareStatement(SQL,
 				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		stmt.setInt(1, studentID);
 		System.out.println(SQL);
 		try {
 			rs = stmt.executeQuery();
 
 			if (!rs.isBeforeFirst()) {
 				return null;
 
 			} else {
 				rs.beforeFirst();
 
 				int i = 0;
 				while (rs.next()) {
 					people[i] = new Enrollment(rs.getInt("enrollmentID"),
 							rs.getInt("courseID"), rs.getInt("studentID"),
 							rs.getInt("calendarID"), rs.getString("name"),
 							rs.getString("shortName"), rs.getInt("gradeID"));
 					i++;
 				}
 				return people;
 
 			}
 		} finally {
 			stmt.close();
 			con.close();
 			rs.close();
 
 		}
 
 	}
 
 	private static int checkSubjectByID(int subjectID) throws SQLException {
 
 		con = cpds.getConnection();
 		con.setAutoCommit(false);
 		SQL = "SELECT count(*) over (partition by 1) total_rows FROM [subject] WHERE subjectID = ?";
 		PreparedStatement stmt = con.prepareStatement(SQL);
 		System.out.println(SQL);
 		stmt.setInt(1, subjectID);
 		try {
 			rs = stmt.executeQuery();
 			con.commit();
 		} finally {
 			if (!rs.isBeforeFirst()) {
 				stmt.close();
 				con.close();
 				return 0;
 			} else {
 				stmt.close();
 				con.close();
 				rs.close();
 			}
 
 		}
 
 		return 1;
 	}
 
 	private static int checkCourseByID(int courseID) throws SQLException {
 
 		con = cpds.getConnection();
 		con.setAutoCommit(false);
 		SQL = "SELECT count(*) over (partition by 1) total_rows FROM course WHERE courseID = ?";
 		PreparedStatement stmt = con.prepareStatement(SQL);
 		System.out.println(SQL);
 		stmt.setInt(1, courseID);
 		try {
 			rs = stmt.executeQuery();
 			con.commit();
 		} finally {
 			if (!rs.isBeforeFirst()) {
 				stmt.close();
 				con.close();
 				return 0;
 			} else {
 				stmt.close();
 				con.close();
 				rs.close();
 			}
 
 		}
 
 		return 1;
 	}
 
 	public static void editSubject(int subjectID, String name)
 			throws SQLException, NoSuchAlgorithmException, IOException
 
 	{
 		if (checkSubjectByID(subjectID) != 0) {
 			con = cpds.getConnection();
 			con.setAutoCommit(false);
 			SQL = "UPDATE [subject] SET name = ?\r\n" + "WHERE subjectID = ?";
 			PreparedStatement stmt = con.prepareStatement(SQL);
 			System.out.println(SQL);
 			stmt.setString(1, name);
 			stmt.setInt(2, subjectID);
 			try {
 				stmt.executeUpdate();
 				con.commit();
 			} finally {
 				con.close();
 				stmt.close();
 			}
 
 		}
 	}
 
 	public static void createCourse(String name, String shortName,
 			int subjectID, int teacherID, int calendarID) throws SQLException {
 
 		con = cpds.getConnection();
 		con.setAutoCommit(false);
 		SQL = "INSERT INTO course (name,shortName,subjectID,teacherID, calendarID)\r\n"
 				+ "VALUES (?,?,?,?,?)";
 		PreparedStatement stmt = con.prepareStatement(SQL);
 		System.out.println(SQL);
 		stmt.setString(1, name);
 		stmt.setString(2, shortName);
 		stmt.setInt(3, subjectID);
 		stmt.setInt(4, teacherID);
 		stmt.setInt(5, calendarID);
 		try {
 			stmt.executeUpdate();
 			con.commit();
 		} finally {
 			con.close();
 			stmt.close();
 		}
 
 	}
 
 	public static void removeCourse(int courseID) throws SQLException,
 			ParseException {
 
 		if (checkCourseByID(courseID) != 0) {
 			con = cpds.getConnection();
 			con.setAutoCommit(false);
 			SQL = "DELETE FROM course WHERE courseID = ?"
 					+ "DELETE FROM enrollment WHERE courseID = ?";
 
 			PreparedStatement stmt = con.prepareStatement(SQL);
 			System.out.println(SQL);
 			stmt.setInt(1, courseID);
 			stmt.setInt(2, courseID);
 			try {
 				stmt.executeUpdate();
 				con.commit();
 			} finally {
 				con.close();
 				stmt.close();
 			}
 		}
 
 	}
 
 	public static void editCourse(int courseID, String name, String shortName,
 			int subjectID, int teacherID, int calendarID) throws SQLException,
 			NoSuchAlgorithmException, IOException
 
 	{
 		if (checkCourseByID(courseID) != 0 && checkSubjectByID(subjectID) != 0) {
 			con = cpds.getConnection();
 			con.setAutoCommit(false);
 			SQL = "UPDATE COURSE SET subjectID = ?, teacherID = ?, name = ?, shortName = ?, calendarID = ?\r\n"
 					+ "WHERE courseID = ?";
 			PreparedStatement stmt = con.prepareStatement(SQL);
 			System.out.println(SQL);
 			stmt.setInt(1, subjectID);
 			stmt.setInt(2, teacherID);
 			stmt.setString(3, name);
 			stmt.setString(4, shortName);
 			stmt.setInt(5, calendarID);
 			stmt.setInt(6, courseID);
 			try {
 				stmt.executeUpdate();
 				con.commit();
 			} finally {
 				con.close();
 				stmt.close();
 			}
 
 		}
 	}
 	
 	public static int checkSubject(boolean byName, int subjectID, String name, int calendarID) throws SQLException
 	{
 		con = cpds.getConnection();
 		con.setAutoCommit(false);
 		if(byName)
 			SQL = "SELECT subjectID,name FROM subject WHERE name = ? AND calendarID = ?";		
 		else
 			SQL = "SELECT subjectID,name FROM subject WHERE subjectID = ? AND calendarID = ?";
 		PreparedStatement stmt = con.prepareStatement(SQL,
 				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		System.out.println(SQL);
 		if(byName)
 			stmt.setString(1, name);
 		else
 			stmt.setInt(1, subjectID);
 			
 		stmt.setInt(2, calendarID);
 		try {
 			rs = stmt.executeQuery();
 		} finally {
 			if (!rs.isBeforeFirst()) {
 				stmt.close();
 				con.close();	
 				rs.close();
 			} else {
 				rs.first();
 				int subject = rs.getInt("subjectID");
 				stmt.close();
 				con.close();
 				rs.close();
 				return subject;
 
 			}
 		}
 		return -1;
 		
 	}
 	
 	public static boolean checkCourse(boolean byName, int courseID, String name, int calendarID, int subjectID) throws SQLException
 	{
 		con = cpds.getConnection();
 		con.setAutoCommit(false);
 		if(byName)
 			SQL = "SELECT courseID,name, subjectID FROM course WHERE name = ? AND calendarID = ? AND subjectID = ?";		
 		else
 			SQL = "SELECT courseID,name, subjectID FROM course WHERE courseID = ? AND calendarID = ? AND subjectID = ?";
 		PreparedStatement stmt = con.prepareStatement(SQL,
 				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		System.out.println(SQL);
 		if(byName)
 			stmt.setString(1, name);
 		else
 			stmt.setInt(1, courseID);
 			
 		stmt.setInt(2, calendarID);
 		stmt.setInt(3, subjectID);
 		try {
 			rs = stmt.executeQuery();
 		} finally {
 			if (!rs.isBeforeFirst()) {
 				stmt.close();
 				con.close();	
 				rs.close();
 				return false;
 			} else {
 				rs.first();
 				stmt.close();
 				con.close();
 				rs.close();
 
 			}
 		}
 		return true;
 		
 	}
 	
 	public static void transferCourse(int courseID, int calendarID, int subjectTransfer, boolean duplicate) throws SQLException
 	{
 		con = cpds.getConnection();
 		con.setAutoCommit(false);
 		SQL = "SELECT c.courseID, c.subjectID, c.name, s.name as subjectNAME, c.shortName, c.teacherID FROM course c\r\n" + 
 				"INNER JOIN subject s ON s.subjectID = c.subjectID\r\n" + 
 				"WHERE courseID = ?\r\n";
 		PreparedStatement stmt = con.prepareStatement(SQL,
 				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		stmt.setInt(1, courseID);
 		try {
 			rs = stmt.executeQuery();
 		} finally {
 			if (!rs.isBeforeFirst()) {
 				stmt.close();
 				con.close();
 				rs.close();
 				
 			} else {
 				rs.first();
 				Course course = new Course(rs.getInt("courseID"), rs.getInt("subjectID"), rs.getString("name"), rs.getString("shortName"), rs.getInt("teacherID"), rs.getString("subjectName"));
 				int subjectID = checkSubject(false, subjectTransfer,course.getSubjectName(), calendarID);
 				boolean courseCheck = checkCourse(true, course.courseID, course.name,calendarID, subjectTransfer);
 				if (!duplicate) {
 					if (subjectID != -1) {
 						if (!courseCheck)
 							createCourse(course.name, course.shortName,
 									subjectID, course.teacherID, calendarID);
 
 					}
 
 					else {
 						System.out.println("no subject name");
 						addSubject(course.subjectName, calendarID);
 						subjectID = checkSubject(false, subjectTransfer,
 								course.getSubjectName(), calendarID);
 						if (subjectID != -1 && !courseCheck) {
 							createCourse(course.name, course.shortName,
 									subjectID, course.teacherID, calendarID);
 						} else {
 							System.out.println("no subject error");
 						}
 
 					}
 				}
 				else
 				{
 					subjectID = checkSubject(true, subjectTransfer,course.getSubjectName(), calendarID);
 					courseCheck = checkCourse(true, course.courseID, course.name,calendarID, subjectID);
 					if (subjectID != -1) {
 						if (!courseCheck)
 							createCourse(course.name, course.shortName,
 									subjectID, course.teacherID, calendarID);
 
 					}
 					else
 					{
 						System.out.println("no subject name");
 						addSubject(course.subjectName, calendarID);
 						subjectID = checkSubject(true, subjectTransfer,
 								course.getSubjectName(), calendarID);
 						courseCheck = checkCourse(true, course.courseID, course.name,calendarID, subjectID);
 						if (subjectID != -1 && !courseCheck) {
 							createCourse(course.name, course.shortName,
 									subjectID, course.teacherID, calendarID);
 						} else {
 							System.out.println("no subject error");
 						}
 					}
 					
 				}
 				stmt.close();
 				con.close();
 				
 				rs.close();
 				
 			}
 
 		}
 		
 	}
 
 }
