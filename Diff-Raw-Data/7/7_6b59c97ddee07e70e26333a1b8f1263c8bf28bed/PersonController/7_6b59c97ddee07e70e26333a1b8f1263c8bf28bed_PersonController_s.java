 package person;
 
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.commons.lang3.text.WordUtils;
 import account.CreateAccount;
 import lynx.ITypes.*;
 
 public class PersonController extends lynx.Manager {
 	private static Connection con;
 	private static String SQL;
 	private static ResultSet rs;
 
 	public static final String[] languageList = new String[] { "N/A",
 			"English", "Arabic", "Bengali", "Chinese", "French", "German",
 			"Hindi", "Italian", "Japanese", "Korean", "Malay", "Polish",
 			"Portuguese", "Russian", "Somali", "Spanish", "Thai", "Turkish",
 			"Urdu", "Vietnamese" };
 	public static final String[] ethinicityList = new String[] { "N/A",
 			"White, Non-Hispanic", "Black, Non-Hispanic", "Hispanic",
 			"American Indian or Native Alaskan", "Pacific Islander" };
 
 	public PersonController() throws SQLException {
 
 	}
 
 	public static void addPerson(String fname, String lname, String mname,
 			String suf, int aID, int adID, String gnr, String date,
 			String language, String ethinicity, String password1,
 			String password2, String username, String group)
 			throws SQLException, NoSuchAlgorithmException, IOException,
 			ParseException
 
 	{
 		int test = CreateAccount.getAccountByUserName(username);
 		System.out.println(test);
 		if (checkByID(CreateAccount.getAccountByUserName(username),
 				checkType.ACCOUNT) == 0) {
 			CreateAccount.createAccount(password1, password2, username,
 					Integer.parseInt(group));
 
 			con = cpds.getConnection();
 			con.setAutoCommit(false);
 			SQL = "INSERT INTO person(accountID,addressID,lastName,firstName,middleName,suffix,gender,birthDate, [language], ethinicity) "
 					+ "VALUES(?,?,?,?,?,?,?,?,?,?)";
 			PreparedStatement stmt = con.prepareStatement(SQL,
 					Statement.RETURN_GENERATED_KEYS);
 			stmt.getGeneratedKeys();
 			System.out.println(SQL);
 
 			stmt.setInt(1, CreateAccount.getAccountByUserName(username));
 			stmt.setInt(2, adID);
 			stmt.setString(3, WordUtils.capitalizeFully(lname));
 			stmt.setString(4, WordUtils.capitalizeFully(fname));
 			stmt.setString(5, WordUtils.capitalizeFully(mname));
 			stmt.setString(6, suf);
 			stmt.setString(7, gnr);
 			DateFormat DOB = new SimpleDateFormat("yyyy-MM-dd");
 			java.sql.Date convertedDate = new java.sql.Date(DOB.parse(date)
 					.getTime());
 
 			// java.sql.Date sqlToday = new java.sql.Date(convertedDate);
 			stmt.setDate(8, convertedDate);
 			stmt.setString(9, language);
 			stmt.setString(10, ethinicity);
 			try {
 				stmt.executeUpdate();
 				con.commit();
 				postMod();
 				rs = stmt.getGeneratedKeys();
 				if (group.equals("2")) {
 
 					if (rs.next()) {
 						int personID = rs.getInt(1);
 						linkToStudent(personID);
 						System.out.println(personID);
 					}
 
 				} else if (group.equals("3")) {
 					if (rs.next()) {
 						int personID = rs.getInt(1);
 						addTeacher(personID);
 						System.out.println(personID);
 					}
 				}
 			} finally {
 				con.close();
 				stmt.close();
 				rs.close();
 			}
 
 		}
 	}
 
 	public static void removePerson(int personID) throws SQLException,
 			NoSuchAlgorithmException, IOException
 
 	{
 		if (checkByID(personID, checkType.PERSON) != 0
 				&& CreateAccount.getAccountByPersonID(personID) != 0) {
 			con = cpds.getConnection();
 			con.setAutoCommit(false);
 			SQL = "DELETE FROM person WHERE personID = ? "
 					+ "\nDELETE FROM accounts WHERE personID = ?"
 					+ "\nDELETE FROM teacher WHERE personID = ?"
 					+ "\nDELETE enrollment \r\n" + "FROM enrollment a\r\n"
 					+ "INNER JOIN student s ON s.personID = ?"
 					+ "\nDELETE FROM student WHERE personID = ?";
 
 			PreparedStatement stmt = con.prepareStatement(SQL);
 			System.out.println(SQL);
 			stmt.setInt(1, personID);
 			stmt.setInt(2, personID);
 			stmt.setInt(3, personID);
 			stmt.setInt(4, personID);
 			stmt.setInt(5, personID);
 			try {
 				stmt.executeUpdate();
 				con.commit();
 			} finally {
 				con.close();
 				stmt.close();
 			}
 
 		}
 	}
 
 	public static void editPerson(int personID, String fname, String lname,
 			String mname, String suf, String gnr, String date, String language,
 			String ethinicity, String password1, String password2,
 			String userName, int currentGroup, int group) throws SQLException,
 			NoSuchAlgorithmException, IOException, ParseException
 
 	{
 		if (checkByID(personID, checkType.PERSON) != 0
 				&& CreateAccount.getAccountByPersonID(personID) != 0) {
 			con = cpds.getConnection();
 			con.setAutoCommit(false);
 			SQL = "UPDATE person SET lastName = ?,firstName = ?,middleName = ?,suffix = ?,gender = ?,birthDate = ?, ethinicity = ?, language = ? "
 					+ "WHERE personID = ?";
 			PreparedStatement stmt = con.prepareStatement(SQL);
 			System.out.println(SQL);
 			stmt.setString(1, WordUtils.capitalizeFully(lname));
 			stmt.setString(2, WordUtils.capitalizeFully(fname));
 			stmt.setString(3, WordUtils.capitalizeFully(mname));
 			stmt.setString(4, suf);
 			stmt.setString(5, gnr);
 			DateFormat DOB = new SimpleDateFormat("yyyy-MM-dd");
 			java.sql.Date convertedDate = new java.sql.Date(DOB.parse(date)
 					.getTime());
 			;
 			stmt.setDate(6, convertedDate);
 			stmt.setString(7, ethinicity);
 			stmt.setString(8, language);
 			stmt.setInt(9, personID);
 
 			try {
 				stmt.executeUpdate();
 				if(!(currentGroup == group))
 				{
 					if(currentGroup == 2)
 					{
						unlinkStudent(currentGroup, true);
 					}
 					else if(currentGroup == 3)
 					{
						removeTeacher(currentGroup, true);
 					}
 					if (group == 2) {
 
 						
 							linkToStudent(personID);
 							System.out.println(personID);
 						
 
 					} else if (group == 3) {
 						
 							addTeacher(personID);
 							System.out.println(personID);		
 					}
 				}
 					
 				
 			} finally {
 				con.close();
 				stmt.close();
 			}
 
 		}
 	}
 
 	private static void postMod() throws SQLException {
 		con = cpds.getConnection();
 		con.setAutoCommit(false);
 		SQL = "UPDATE accounts\r\n" + "SET accounts.personID = p.personID \n"
 				+ "FROM accounts a\r\n"
 				+ "INNER JOIN person p ON  p.accountID = a.accountID ";
 		PreparedStatement stmt = con.prepareStatement(SQL);
 		try {
 			stmt.executeUpdate();
 			con.commit();
 		} finally {
 			con.close();
 			stmt.close();
 		}
 
 	}
 
 	private static void linkAccount() throws SQLException {
 		con = cpds.getConnection();
 		con.setAutoCommit(false);
 		SQL = "UPDATE acc";
 		PreparedStatement stmt = con.prepareStatement(SQL);
 		try {
 			stmt.executeUpdate();
 			con.commit();
 		} finally {
 			con.close();
 			stmt.close();
 		}
 
 	}
 
 	public static List<String> getSelectList(String type) throws SQLException {
 		con = cpds.getConnection();
 		con.setAutoCommit(false);
 		SQL = "SELECT data FROM metaData WHERE [type] = ?";
 		List<String> list = new ArrayList<String>();
 		PreparedStatement stmt = con.prepareStatement(SQL,
 				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		stmt.setString(1, type);
 		try {
 			rs = stmt.executeQuery();
 		} finally {
 			if (!rs.isBeforeFirst()) {
 				stmt.close();
 				con.close();
 			} else {
 				rs.beforeFirst();
 				while (rs.next()) {
 					list.add(rs.getString("data"));
 				}
 				stmt.close();
 				con.close();
 				rs.close();
 
 			}
 		}
 		return list;
 
 	}
 
 	public static String validateData(int fname, int lname, int mname,
 			String suf, String gen, String date, String language,
 			String country, String ethinicity) throws ParseException,
 			SQLException {
 
 		if (fname <= 50 && lname <= 50 && mname < 50 && fname > 0 && lname > 0
 				&& mname > 0 && isValidDate(date.replace("-", ""))
 				&& validateMeta(language, "language")
 				&& validateMeta(country, "country")
 				&& validateMeta(ethinicity, "ethinicity")
 				&& validateMeta(suf, "suffix") && validateMeta(gen, "gender")) {
 			return "true";
 		}
 		return "The demographic infomation is invalid. ";
 	}
 
 	public static boolean isValidLanguage(String lang) {
 		if (Arrays.asList(languageList).contains(lang))
 			return true;
 
 		return false;
 
 	}
 
 	public static boolean isValidEthinicity(String ethinicity) {
 		if (Arrays.asList(ethinicityList).contains(ethinicity))
 			return true;
 		return false;
 	}
 
 	public static boolean isValidCountry(String country) throws SQLException
 
 	{
 		con = cpds.getConnection();
 		con.setAutoCommit(false);
 		SQL = "SELECT data FROM metaData WHERE data = ? AND [type] = ?";
 		PreparedStatement stmt = con.prepareStatement(SQL,
 				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		System.out.println(SQL);
 		stmt.setString(1, country);
 		stmt.setString(2, "country");
 		try {
 			rs = stmt.executeQuery();
 		} finally {
 			if (!rs.isBeforeFirst()) {
 				stmt.close();
 				con.close();
 
 				return false;
 			} else {
 				stmt.close();
 				con.close();
 				rs.close();
 
 			}
 		}
 		return true;
 	}
 
 	public static boolean validateMeta(String data, String type)
 			throws SQLException
 
 	{
 		con = cpds.getConnection();
 		con.setAutoCommit(false);
 		SQL = "SELECT data FROM metaData WHERE data = ? AND [type] = ?";
 		PreparedStatement stmt = con.prepareStatement(SQL,
 				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		System.out.println(SQL);
 		stmt.setString(1, data);
 		stmt.setString(2, type);
 		try {
 			rs = stmt.executeQuery();
 		} finally {
 			if (!rs.isBeforeFirst()) {
 				stmt.close();
 				con.close();
 
 				return false;
 			} else {
 				stmt.close();
 				con.close();
 				rs.close();
 
 			}
 		}
 		return true;
 	}
 
 	public static boolean isValidDate(String dateString) {
 		System.out.println(dateString);
 		if (dateString == null || dateString.length() != "yyyyMMdd".length()) {
 			System.out.println("invalid");
 			return false;
 		}
 
 		int date;
 		try {
 			date = Integer.parseInt(dateString);
 		} catch (NumberFormatException e) {
 			return false;
 		}
 
 		int year = date / 10000;
 		int month = (date % 10000) / 100;
 		int day = date % 100;
 
 		// leap years calculation not valid before 1581
 		boolean yearOk = (year >= 1581) && (year <= 2500);
 		boolean monthOk = (month >= 1) && (month <= 12);
 		boolean dayOk = (day >= 1) && (day <= daysInMonth(year, month));
 
 		return (yearOk && monthOk && dayOk);
 	}
 
 	private static int daysInMonth(int year, int month) {
 		int daysInMonth;
 		switch (month) {
 		case 1: // fall through
 		case 3: // fall through
 		case 5: // fall through
 		case 7: // fall through
 		case 8: // fall through
 		case 10: // fall through
 		case 12:
 			daysInMonth = 31;
 			break;
 		case 2:
 			if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) {
 				daysInMonth = 29;
 			} else {
 				daysInMonth = 28;
 			}
 			break;
 		default:
 
 			daysInMonth = 30;
 		}
 		return daysInMonth;
 	}
 
 	private static int checkByID(int id, checkType type) throws SQLException {
 		SQL = null;
 		con = cpds.getConnection();
 		con.setAutoCommit(false);
 		if (type == checkType.ACCOUNT) {
 			SQL = "SELECT count(*) over (partition by 1) total_rows FROM person WHERE accountID = ?";
 		} else if (type == checkType.PERSON) {
 			SQL = "SELECT count(*) over (partition by 1) total_rows FROM person WHERE personID = ?";
 		} else if (type == checkType.STUDENT) {
 			SQL = "SELECT count(*) over (partition by 1) total_rows FROM student WHERE studentID = ?";
 		} else if (type == checkType.TEACHER) {
 			SQL = "SELECT count(*) over (partition by 1) total_rows FROM teacher WHERE teacherID = ?";
 		}
 
 		PreparedStatement stmt = con.prepareStatement(SQL);
 		System.out.println(SQL);
 		stmt.setInt(1, id);
 		try {
 			rs = stmt.executeQuery();
 
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
 
 	public static int getCount(countType type) throws SQLException {
 		con = cpds.getConnection();
 		if (type == countType.PEOPLE) {
 			SQL = "SELECT COUNT(*) as total_rows FROM person";
 		} else if (type == countType.STUDENTS) {
 			SQL = "SELECT COUNT(*) as total_rows FROM student";
 		} else if (type == countType.ENROLLED) {
 			SQL = "SELECT COUNT(*) as total_rows FROM student \r\n"
 					+ "WHERE EXISTS (SELECT enrollment.studentID FROM enrollment WHERE student.studentID = enrollment.studentID)";
 		} else if (type == countType.STUDENT_GRADES) {
 			SQL = "SELECT COUNT(*) AS total_rows  FROM student s\r\n"
 					+ "INNER JOIN person p ON p.personID = s.personID\r\n"
 					+ "INNER JOIN enrollment e ON e.studentID = s.studentID\r\n"
 					+ "WHERE EXISTS (SELECT enrollment.studentID FROM enrollment WHERE enrollment.studentID = s.studentID)\r\n"
 					+ "AND EXISTS(SELECT grade.enrollmentID FROM grade WHERE grade.enrollmentID = e.enrollmentID)";
 		} else if (type == countType.TEACHERS) {
 			SQL = "SELECT COUNT(*) as total_rows FROM teacher";
 		} else if (type == countType.AVAILABLE_PEOPLE) {
 			SQL = "SELECT COUNT(*) as total_rows FROM person p\r\n"
 					+ "WHERE NOT EXISTS (SELECT personID FROM student s WHERE s.personID = p.personID)\r\n"
 					+ "AND NOT EXISTS (SELECT personID FROM teacher t WHERE t.personID = p.personID)";
 		} else if (type == countType.ACCOUNTS) {
 			SQL = "SELECT COUNT(*) as total_rows FROM accounts";
 		}
 
 		System.out.println(SQL);
 		PreparedStatement stmt = con.prepareStatement(SQL,
 				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
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
 
 	public static Person[] getPeople() throws SQLException {
 		int totalPeople = getCount(countType.PEOPLE);
 		System.out.println(totalPeople);
 		Person[] people = new Person[totalPeople];
 		Connection con = cpds.getConnection();
 
 		SQL = "SELECT personID,firstName,lastName FROM person";
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
 					people[i] = new Person(rs.getString("firstName"),
 							rs.getString("lastName"), rs.getInt("personID"));
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
 
 	public static Person[] getPeopleWithoutStudent() throws SQLException {
 		int totalPeople = getCount(countType.AVAILABLE_PEOPLE);
 		if (totalPeople == 0) {
 			return null;
 		}
 		System.out.println(totalPeople);
 		Person[] people = new Person[totalPeople];
 		Connection con = cpds.getConnection();
 
 		SQL = "SELECT p.personID,p.firstName,p.lastName FROM person p \r\n"
 				+ "				WHERE NOT EXISTS (SELECT personID FROM student s WHERE s.personID = p.personID)\r\n"
 				+ "AND NOT EXISTS (SELECT personID FROM teacher t WHERE t.personID = p.personID)";
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
 					people[i] = new Person(rs.getString("firstName"),
 							rs.getString("lastName"), rs.getInt("personID"));
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
 
 	public static Student[] getStudents() throws SQLException {
 		int totalPeople = getCount(countType.STUDENTS);
 		System.out.println(totalPeople);
 		Student[] stu = new Student[totalPeople];
 		Connection con = cpds.getConnection();
 
 		SQL = "SELECT s.studentID,s.personID, s.accountID, p.firstName, p.lastName  FROM student s\r\n"
 				+ "INNER JOIN person p ON p.personID = s.personID\r\n"
 				+ "ORDER BY p.lastName";
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
 
 					stu[i] = new Student(rs.getInt("studentID"),
 							rs.getInt("personID"), rs.getInt("accountID"),
 							rs.getString("firstName"), rs.getString("lastName"));
 
 					System.out.println(stu[i].getName());
 					i++;
 				}
 
 				return stu;
 
 			}
 		} finally {
 			stmt.close();
 			con.close();
 			rs.close();
 
 		}
 
 	}
 
 	public static Student[] getStudentsWithEnrollment() throws SQLException {
 		int totalPeople = getCount(countType.ENROLLED);
 		System.out.println(totalPeople);
 		Student[] stu = new Student[totalPeople];
 		Connection con = cpds.getConnection();
 
 		SQL = "SELECT s.studentID,s.personID, s.accountID, p.firstName, p.lastName  FROM student s\r\n"
 				+ "				INNER JOIN person p ON p.personID = s.personID\r\n"
 				+ "				WHERE EXISTS (SELECT enrollment.studentID FROM enrollment WHERE enrollment.studentID = s.studentID)\r\n"
 				+ "				ORDER BY p.lastName";
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
 
 					stu[i] = new Student(rs.getInt("studentID"),
 							rs.getInt("personID"), rs.getInt("accountID"),
 							rs.getString("firstName"), rs.getString("lastName"));
 
 					System.out.println(stu[i].getName());
 					i++;
 				}
 
 				return stu;
 
 			}
 		} finally {
 			stmt.close();
 			con.close();
 			rs.close();
 
 		}
 
 	}
 
 	public static Student[] getStudentWithGrades() throws SQLException {
 		int totalPeople = getCount(countType.STUDENT_GRADES);
 		System.out.println(totalPeople);
 		Student[] stu = new Student[totalPeople];
 		Connection con = cpds.getConnection();
 
 		SQL = "SELECT s.studentID,s.personID, s.accountID, p.firstName, p.lastName  FROM student s\r\n"
 				+ "								INNER JOIN person p ON p.personID = s.personID\r\n"
 				+ "								INNER JOIN enrollment e ON e.studentID = s.studentID\r\n"
 				+ "							WHERE EXISTS (SELECT enrollment.studentID FROM enrollment WHERE enrollment.studentID = s.studentID)\r\n"
 				+ "							AND EXISTS(SELECT grade.enrollmentID FROM grade WHERE grade.enrollmentID = e.enrollmentID)\r\n"
 				+ "								ORDER BY p.lastName";
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
 
 					stu[i] = new Student(rs.getInt("studentID"),
 							rs.getInt("personID"), rs.getInt("accountID"),
 							rs.getString("firstName"), rs.getString("lastName"));
 
 					System.out.println(stu[i].getName());
 					i++;
 				}
 
 				return stu;
 
 			}
 		} finally {
 			stmt.close();
 			con.close();
 			rs.close();
 
 		}
 
 	}
 
 	public static Teacher[] getTeachers() throws SQLException {
 		int totalPeople = getCount(countType.TEACHERS);
 		System.out.println(totalPeople);
 		Teacher[] tc = new Teacher[totalPeople];
 		Connection con = cpds.getConnection();
 
 		SQL = "SELECT t.teacherID,t.personID, t.accountID, p.firstName, p.lastName  FROM teacher t\r\n"
 				+ "INNER JOIN person p ON p.personID = t.personID";
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
 
 					tc[i] = new Teacher(rs.getInt("teacherID"),
 							rs.getInt("personID"), rs.getString("lastName"),
 							rs.getString("firstName"));
 					i++;
 				}
 
 				return tc;
 
 			}
 		} finally {
 			stmt.close();
 			con.close();
 			rs.close();
 
 		}
 
 	}
 
 	public static Person getPerson(int personID) throws SQLException {
 		Connection con = cpds.getConnection();
 		con.setAutoCommit(false);
 		SQL = "SELECT person.personID, person.accountID,firstName,lastName,middleName,suffix,gender,birthDate, ethinicity, language, a.username, person.addressID, a.usergroupID FROM person \r\n"
 				+ "INNER JOIN accounts a ON a.personID = person.personID\r\n"
 				+ "WHERE person.personID = ?";
 		PreparedStatement stmt = con.prepareStatement(SQL,
 				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		System.out.println(SQL);
 		stmt.setInt(1, personID);
 		try {
 			rs = stmt.executeQuery();
 			con.commit();
 			if (!rs.isBeforeFirst()) {
 				return null;
 
 			} else {
 				rs.first();
 				return new Person(rs.getString("firstName"),
 						rs.getString("lastName"), rs.getString("middleName"),
 						rs.getInt("personID"), rs.getString("suffix"),
 						rs.getInt("accountID"), rs.getInt("addressID"),
 						rs.getString("gender"), rs.getString("birthDate"),
 						rs.getString("username"), rs.getString("language"),
 						rs.getString("ethinicity"), rs.getString("usergroupID"));
 
 			}
 		} finally {
 			stmt.close();
 			con.close();
 			rs.close();
 
 		}
 
 	}
 
 	public static void linkToStudent(int personID) throws SQLException,
 			NoSuchAlgorithmException, IOException
 
 	{
 		if (checkByID(personID, checkType.PERSON) != 0
 				&& CreateAccount.getAccountByPersonID(personID) != 0) {
 			con = cpds.getConnection();
 			con.setAutoCommit(false);
 			SQL = "INSERT INTO student (personID,accountID) \n"
 					+ "SELECT personID, accountID \n" + "FROM   person \n"
 					+ "WHERE personID = ?";
 			PreparedStatement stmt = con.prepareStatement(SQL);
 			System.out.println(SQL);
 			stmt.setInt(1, personID);
 			try {
 				stmt.executeUpdate();
 				con.commit();
 			} finally {
 				con.close();
 				stmt.close();
 			}
 
 		}
 	}
 
 	public static void unlinkStudent(int studentID, boolean usePerson) throws SQLException,
 			NoSuchAlgorithmException, IOException
 
 	{
 		if (checkByID(studentID, checkType.STUDENT) != 0) {
 			con = cpds.getConnection();
 			con.setAutoCommit(false);
 			if(usePerson)
 				SQL = "DELETE FROM student WHERE personID = ?";
 			else
 				SQL = "DELETE FROM student WHERE studentID = ?";
 			PreparedStatement stmt = con.prepareStatement(SQL);
 			System.out.println(SQL);
 			stmt.setInt(1, studentID);
 			try {
 				stmt.executeUpdate();
 				con.commit();
 			} finally {
 				con.close();
 				stmt.close();
 			}
 
 		}
 	}
 	
 
 	public static void updateLink(int studentID, int personID)
 			throws SQLException, NoSuchAlgorithmException, IOException
 
 	{
 		if (checkByID(studentID, checkType.STUDENT) != 0
 				&& checkByID(personID, checkType.PERSON) != 0) {
 			con = cpds.getConnection();
 			con.setAutoCommit(false);
 			SQL = "UPDATE student  SET student.personID = ?, student.accountID = p.accountID\r\n"
 					+ "FROM student\r\n"
 					+ "INNER JOIN person p ON p.personID = student.personID\r\n"
 					+ "WHERE student.studentID = ?";
 			PreparedStatement stmt = con.prepareStatement(SQL);
 			System.out.println(SQL);
 			stmt.setInt(1, personID);
 			stmt.setInt(2, studentID);
 			try {
 				stmt.executeUpdate();
 				con.commit();
 			} finally {
 				con.close();
 				stmt.close();
 			}
 
 		}
 	}
 
 	public static void addTeacher(int personID) throws SQLException,
 			NoSuchAlgorithmException, IOException
 
 	{
 		if (checkByID(personID, checkType.PERSON) != 0
 				&& CreateAccount.getAccountByPersonID(personID) != 0) {
 			con = cpds.getConnection();
 			con.setAutoCommit(false);
 			SQL = "INSERT INTO teacher (personID,accountID) \n"
 					+ "SELECT personID, accountID \n" + "FROM   person \n"
 					+ "WHERE personID = ?";
 			PreparedStatement stmt = con.prepareStatement(SQL);
 			System.out.println(SQL);
 			System.out.println(personID);
 			stmt.setInt(1, personID);
 			try {
 				stmt.executeUpdate();
 				con.commit();
 			} finally {
 				con.close();
 				stmt.close();
 			}
 
 		}
 	}
 
 	public static void removeTeacher(int teacherID, boolean usePerson) throws SQLException,
 			NoSuchAlgorithmException, IOException
 
 	{
 		if (checkByID(teacherID, checkType.TEACHER) != 0) {
 			con = cpds.getConnection();
 			con.setAutoCommit(false);
 			if(usePerson)
 				SQL = "DELETE FROM teacher WHERE personID = ?";
 			else
 				SQL = "DELETE FROM teacher WHERE teacherID = ?";
 			PreparedStatement stmt = con.prepareStatement(SQL);
 			System.out.println(SQL);
 			stmt.setInt(1, teacherID);
 			try {
 				stmt.executeUpdate();
 				con.commit();
 			} finally {
 				con.close();
 				stmt.close();
 			}
 
 		}
 	}
 
 	public static void editTeacher(int teacherID, int personID)
 			throws SQLException, NoSuchAlgorithmException, IOException
 
 	{
 		if (checkByID(teacherID, checkType.TEACHER) != 0
 				&& checkByID(personID, checkType.PERSON) != 0) {
 			con = cpds.getConnection();
 			con.setAutoCommit(false);
 			SQL = "UPDATE teacher SET teacher.personID = ?, teacher.accountID = p.accountID\r\n"
 					+ "FROM teacher\r\n"
 					+ "INNER JOIN person p ON p.personID = teacher.personID\r\n"
 					+ "WHERE teacher.teacherID = ?";
 			PreparedStatement stmt = con.prepareStatement(SQL);
 			System.out.println(SQL);
 			stmt.setInt(1, personID);
 			stmt.setInt(2, teacherID);
 			try {
 				stmt.executeUpdate();
 				con.commit();
 			} finally {
 				con.close();
 				stmt.close();
 			}
 
 		}
 	}
 }
