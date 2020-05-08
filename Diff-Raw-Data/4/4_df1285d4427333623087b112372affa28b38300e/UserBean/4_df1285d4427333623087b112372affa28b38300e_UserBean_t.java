 package group;
 
 import java.io.UnsupportedEncodingException;
 import java.security.NoSuchAlgorithmException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Hashtable;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import exceptions.ServiceLocatorException;
 import jdbc.DBConnectionFactory;
 import jdbc.DBConnectionFactory;
 
 public class UserBean {
 	private Hashtable<String, String> errors= new Hashtable<String, String>();
 	private int userid;
 	private String username = "";
 	private String userpwd = "";
 	private String useremail= "";
 	private String fname = "";
 	private String lname = "";
 	private String yearofbirth = "";
 	private String fulladdress = "";
 	private String creditcard = "";
 	private int status = 0;
 	private boolean confirmed = false;
 	private String namemd5 = "";
 	public int getUserid() {
 		return userid;
 	}
 	public void setUserid(int userid) {
 		this.userid = userid;
 	}
 	public String getUsername() {
 		return username;
 	}
 	public void setUsername(String username) {
 		this.username = (username != null) ? username : "";
 	}
 	public String getUserpwd() {
 		return userpwd;
 	}
 	public void setUserpwd(String userpwd) {
 		this.userpwd = (userpwd != null) ? userpwd : "";
 	}
 	public String getUseremail() {
 		return useremail;
 	}
 	public void setUseremail(String useremail) {
 		this.useremail = (useremail != null) ? useremail : "";
 	}
 	public String getFname() {
 		return fname;
 	}
 	public void setFname(String fname) {
 		this.fname = (fname != null) ? fname : "";
 	}
 	public String getLname() {
 		return lname;
 	}
 	public void setLname(String lname) {
 		this.lname = (lname != null) ? lname : "";
 	}
 	public String getYearofbirth() {
 		return yearofbirth;
 	}
 	public void setYearofbirth(String yearofbrirth) {
 		this.yearofbirth = (yearofbrirth != null) ? yearofbrirth : "";
 	}
 	public String getFulladdress() {
 		return fulladdress;
 	}
 	public void setFulladdress(String fulladdress) {
 		this.fulladdress = (fulladdress != null) ? fulladdress : "";
 	}
 	public String getCreditcard() {
 		return creditcard;
 	}
 	public void setCreditcard(String creditcard) {
 		this.creditcard = (creditcard != null) ? creditcard : "";
 	}
 	public int getStatus() {
 		return status;
 	}
 	public void setStatus(int status) {
 		this.status = status;
 	}
 	public boolean getConfirmed() {
 		return confirmed;
 	}
 	public void setConfirmed(boolean confirmed) {
 		this.confirmed = confirmed;
 	}
 	public String getNameMd5() {
 		return namemd5;
 	}
 	public void setNameMd5(String namemd5) {
 		this.namemd5 = (namemd5 != null) ? namemd5 : "";
 	}
 	public void Initialize(String Username) {
 
 		Connection conn = null;
 		PreparedStatement st = null;
 		ResultSet rs = null;
 		try {
 			try {
 				conn = DBConnectionFactory.getConnection();
 				if(conn!=null) System.out.println("connected");
 				String sqlQuery = "SELECT username, password, email, status, "
 						+ "confirmed, namemd5, yearofbirth, fname, "
 						+ "lname, fulladdress, creditcard "
 						+ "FROM Users where username = ?";
 				st = conn.prepareStatement(sqlQuery);
 				st.setString(1, Username);
 				rs = st.executeQuery();
 				if(rs.next()){
 		            username = rs.getString(1);
 		            userpwd = rs.getString(2);
 		            useremail = rs.getString(3);
 		            status = rs.getInt(4);
 		            confirmed = rs.getBoolean(5);
 		            namemd5 = rs.getString(6);
 		            yearofbirth = rs.getString(7);
 		            if(yearofbirth==null) yearofbirth = "";
 		            fname = rs.getString(8);
 		            if(fname==null) fname = "";
 		            lname = rs.getString(9);
 		            if(lname==null) lname = "";
 		            fulladdress = rs.getString(10);
 		            if(fulladdress==null) fulladdress = "";
 		            creditcard = rs.getString(11); 
 		            if(creditcard==null) creditcard = "";
 				}
 			}catch  (Exception e) {
 				e.printStackTrace();
 			} finally {
 				st.close();
 				rs.close();
 				conn.close();
 			}
 		} catch (SQLException e) {
 				e.printStackTrace();
 		}
 	}
 	public static UserBean initializeFromUsername(Connection conn, String username) throws SQLException {
 		PreparedStatement st = null;
 		ResultSet rs = null;
 		UserBean user = new UserBean();
 		try {
 			String sqlQuery = "SELECT * FROM Users WHERE username = ?";
 			st = conn.prepareStatement(sqlQuery);
 			st.setString(1, username);
 			rs = st.executeQuery();
 			if (rs.next()){
 				user = makeUser(rs);
 			} else {
 				System.out.println("Username "+username+" not found.");
 				throw new SQLException();
 			}
 		} finally {
 			if (st != null)
 				st.close();
 			if (rs != null)
 				rs.close();
 		}
 		return user;
 	}
 	public static UserBean initializeFromId(Connection conn, int id) throws SQLException {
 		PreparedStatement st = null;
 		ResultSet rs = null;
 		UserBean user = new UserBean();
 		try {
 			String sqlQuery = "SELECT * FROM Users WHERE id = ?";
 			st = conn.prepareStatement(sqlQuery);
 			st.setInt(1, id);
 			rs = st.executeQuery();
 			if (rs.next()){
 				user = makeUser(rs);
 			} else {
 				System.out.println("Username "+id+" not found.");
 				throw new SQLException();
 			}
 		} finally {
 			if (st != null)
 				st.close();
 			if (rs != null)
 				rs.close();
 		}
 		return user;
 	}
 	private static UserBean makeUser(ResultSet rs) throws SQLException {
 		UserBean user = new UserBean();
 		user.setUserid(rs.getInt(1));
 		user.setUsername(rs.getString(2));
 		user.setUserpwd(rs.getString(3));
 		user.setUseremail(rs.getString(4));
 		user.setStatus(rs.getInt(5));
 		user.setConfirmed(rs.getBoolean(6));
 		user.setNameMd5(rs.getString(7));
 		user.setYearofbirth(rs.getString(8));
 		user.setFname(rs.getString(9));
 		user.setLname(rs.getString(10));
 		user.setFulladdress(rs.getString(11));
 		user.setCreditcard(rs.getString(12));
 		return user;
 	}
 
 	public boolean validateForRegistration() {
 		boolean okAll = true;
 		if (username.length()>10 || username.length()<6) {
 			errors.put("name", "The length of username must be between 6 and 10.");
 			okAll = false;
 		}
 		if(!username.matches("[a-zA-Z0-9_-]+")) {
 			errors.put("name", "The content of username must be numbers, alphabets or mixing above.");
 			okAll = false;
 		}
 		if (userpwd.length() > 10 ||userpwd.length() < 6) {
 		    errors.put("password","The length of password must be between 6 and 10");
 		    okAll = false;
 		}
 		if (!userpwd.matches("[a-zA-Z0-9_-]+")) {
 			errors.put("password","The content of password must be numbers, alphabets or mixing above.");
 			okAll = false;
 			}
 		
 		if(!useremail.matches("[a-z0-9_-]+@[a-z0-9_-]+(\\.[a-z0-9_-]+)+")){
 		    errors.put("email", "Invalide email address, all the alphabets must be lower case.");
 		    okAll = false;
 		}
 		return okAll;
 	}
 
 	public boolean validate() {
 		boolean okAll = true;
 		if (userpwd.length() > 10 ||userpwd.length() < 6) {
 		    errors.put("password","The length of password must be between 6 and 10");
 		    okAll = false;
 		}
 		if (!userpwd.matches("[a-zA-Z0-9_-]+")) {
 			errors.put("password","The content of password must be numbers, alphabets or mixing above.");
 			okAll = false;
 			}
 
 		if(!useremail.matches("[a-z0-9_-]+@[a-z0-9_-]+(\\.[a-z0-9_-]+)+")){
 		    errors.put("email", "Invalide email address, all the alphabets must be lower case.");
 		    okAll = false;
 		   }
 		if(!fname.matches("[a-zA-Z-]+")) {
 			errors.put("fname", "invalid first name.");
 			okAll = false;
 		}
 		if(!lname.matches("[a-zA-Z-]+")) {
 			errors.put("lname", "invalid last name.");
 			okAll = false;
 		}
 		if(yearofbirth.length()!=4||!yearofbirth.matches("[0-9-]+")) {
 			errors.put("yearofbirth", "invalid, Year of birth must be 4 numbers like : 1949");
 			okAll = false;
 		}
 		if(creditcard.length()!=16||!creditcard.matches("[0-9-]+")) {
 			errors.put("creditcard", "invalid credit card number.");
 			okAll = false;
 		}
 		if(!fulladdress.matches("[0-9a-zA-Z ,-]+")){
 			errors.put("address", "invalid address.");
 			okAll = false;			
 		}
 		return okAll;
 	}
 	public void setErrorMsg(String err,String errMsg) {
 		if (err != null && errMsg !=null) {
 			errors.put(err, errMsg);
 		}
 	}
 
 	public String getErrorMsg(String err) {
 		String message = errors.get(err);
 		return (message == null) ? "" : message;
 	}
 	
 	public boolean canUpdateEmail(Connection conn, String useremail) throws SQLException {
 		PreparedStatement st = null;
 		ResultSet rs = null;
 		try {
 			String sqlQuery = "select username from Users where email = ?";
 			st = conn.prepareStatement(sqlQuery);
 			st.setString(1, useremail);
 			rs = st.executeQuery();
 			if (rs.next()) {
 				String un = rs.getString(1);
 				System.out.println(un);
 				System.out.println(username);
 				if (!un.equals(username)) {
 					setErrorMsg("email", "Sorry. This email address is already in use");
 					System.out.println("invalid");
 					return false;
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (st != null)
 				st.close();
 			if (rs != null)
 				rs.close();
 		}
 		return true;
 	}
 		
 	public boolean isUnique(Connection conn) throws SQLException {
 		PreparedStatement st = null;
 		ResultSet rs = null;
 		try {
 			// check for unique usernames
 			st = conn.prepareStatement("select * from Users where username = ?");
 			st.setString(1, username);
 			rs = st.executeQuery();
 			if(rs.next()){
 				setErrorMsg("name", "Sorry. This username is already in use");
 				System.out.println("invalid");
 				return false;
 			}
 			st.close();
 			rs.close();
 			
 			//check for unique emails
 			st = conn.prepareStatement("select * from Users where email = ?");
 			st.setString(1, useremail);
 			rs = st.executeQuery();
 			if(rs.next()){
 				setErrorMsg("email", "Sorry. This email address is already in use");
 				System.out.println("invalid");
 				return false;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 
 		} finally {
 			if (st != null)
 				st.close();
 			if (rs != null)
 				rs.close();
 		}
 		return true;
 	}
 	
 	public void calculateNameMd5() {
 		if (namemd5.equals("") || (namemd5 == null)) {
 			try {
 				Md5 md5 = new Md5();
 				namemd5 = md5.getMD5Str(username);
 			} catch (NoSuchAlgorithmException e) {
 				e.printStackTrace();
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public void insertDatabase(Connection conn) throws SQLException {
 		PreparedStatement st = null;
 		ResultSet rs = null;
 		calculateNameMd5();
 		try {
 			st = conn.prepareStatement("INSERT INTO Users (username, password, email, nameMd5, status, confirmed) VALUES (?, ?, ?, ?, '0', false);");
 			st.setString(1, username);
 			st.setString(2, userpwd);
 			st.setString(3, useremail);
 			st.setString(4, namemd5);
 			st.executeUpdate();
 		} catch (Exception e) {
 			e.printStackTrace();
 
 		} finally {
 			if (st != null)
 				st.close();
 			if (rs != null)
 				rs.close();
 		}
 	}
 	
 	public void updateDatabase(Connection conn) throws SQLException {
 		PreparedStatement st = null;
 		calculateNameMd5();
 		try {
 			String sqlQuery = "UPDATE Users SET password=?, email=?,fname=?, lname=?, yearofbirth=?, fulladdress=?, creditcard=? WHERE username=?";
 			st = conn.prepareStatement(sqlQuery);
 			st.setString(1, userpwd);
 			st.setString(2, useremail);
 			st.setString(3, fname);
 			st.setString(4, lname);
 			st.setString(5, yearofbirth);
 			st.setString(6, fulladdress);
 			st.setString(7, creditcard);
 			st.setString(8, username);
 			st.executeUpdate();
 			st.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (st != null)
 				st.close();
 		}
 	}
 
 	public static boolean login(Connection conn, HttpServletRequest request, String un, String pw) throws SQLException {
 		ResultSet rs = null;
 		PreparedStatement st = null;
 		try {
 			String sqlQuery = "select id, username from Users where username = ? and password = ?";
 			st = conn.prepareStatement(sqlQuery);
 			st.setString(1, un);
 			st.setString(2, pw);
 			rs = st.executeQuery();
 			if(rs.next()) {
 				int uid = rs.getInt(1);
 				String username = rs.getString(2);
				//HttpSession session = request.getSession();
				//session.setAttribute("username", username);
 				UserBean user = new UserBean();
 				user.Initialize(username);
 				request.setAttribute("UserBean",user);
 				System.out.println("SUCCESS");
 				return true;
 			} else {
 				request.setAttribute("msg", "invalid username or password!");
 				return false;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (st != null)
 				st.close();
 			if (rs != null)
 				rs.close();
 		}
 		return false;
 	}
 	
 	public static boolean confirmRegistration(Connection conn, HttpServletRequest request, String usermd5) throws SQLException {
 		PreparedStatement st = null;
 		ResultSet rs = null;
 		try {
 			conn = DBConnectionFactory.getConnection();
 			st = conn.prepareStatement("select id from Users where namemd5 = ?");
 			st.setString(1, usermd5);
 			rs = st.executeQuery();
 			int uid = 0;
 			if(rs.next()){
 				uid = rs.getInt(1);
 			} else {
 				request.setAttribute("errorMsg", "Invalid confirmation string.");
 				return false;
 			}
 			st.close();
 			rs.close();
 			st = conn.prepareStatement("UPDATE Users SET confirmed=TRUE where id=?");
 			st.setInt(1, uid);
 			st.executeUpdate();
 		} catch (ServiceLocatorException e) {
 			e.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			if (st != null)
 				st.close();
 			if (rs != null)
 				rs.close();
 		}
 		return true;
 	}
 	public static int getstatus(Connection conn, String username, HttpServletRequest request){
 		PreparedStatement st = null;
 		ResultSet rs = null;
 		int s = 0;
 		try{
 			//conn = DBConnectionFactory.getConnection();
 			st = conn.prepareStatement("select status from Users where username = ?");
 			st.setString(1, username);
 			rs = st.executeQuery();
 			if(rs.next()){
 			s = rs.getInt(1);
 			}
 			st.close();
 			rs.close();
 			} catch (Exception e) {
 			e.printStackTrace();
 		} 
 		return s;
 	}
 	public static boolean BanUser(Connection conn, int id) {
 		PreparedStatement st = null;
 		try{
 			//conn = DBConnectionFactory.getConnection();
 			st = conn.prepareStatement("UPDATE users SET status = '1' where id = ?");
 			st.setInt(1, id);
 	        st.executeUpdate();
 			st.close();
 			return true;
 			} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		} 
 
 	}
 	public static boolean getconfirmation(Connection conn,String username) {
 		boolean confirmed = false;
 		PreparedStatement st = null;
 		ResultSet rs = null;
 		try{
 			conn = DBConnectionFactory.getConnection();
 			st = conn.prepareStatement("select confirmed from Users where username = ?");
 			st.setString(1, username);
 			rs = st.executeQuery();
 			if(rs.next()){
 			confirmed = rs.getBoolean(1);
 			}
 			st.close();
 			rs.close();
 			} catch (Exception e) {
 			e.printStackTrace();
 		} 
 		return confirmed;
 	}
 	
 }
