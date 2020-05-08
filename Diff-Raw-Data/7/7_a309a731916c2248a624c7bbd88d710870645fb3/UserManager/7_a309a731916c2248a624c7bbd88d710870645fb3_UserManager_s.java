 package net.java.dev.cejug.classifieds.richfaces.security;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 /**
  * In order to run this class, you should create the tables in the database:
  * 
  * <pre>
  * ij
  * connect 'jdbc:derby://localhost:1527/sun-appserv-samples';
  * 
  * create table usertable(login varchar(10) not null, password varchar(32) not null, primary key(login));
 * create table grouptable(userid varchar(10) not null, groupid varchar(20) not null, primary key(userid));
 * alter table grouptable add constraint FK_USERID foreign key(userid) references usertable(login);
  * 
  * ij&gt; describe usertable;
  * COLUMN_NAME         |TYPE_NAME|DEC&amp;|NUM&amp;|COLUM&amp;|COLUMN_DEF|CHAR_OCTE&amp;|IS_NULL&amp;
  * ------------------------------------------------------------------------------
  * LOGIN               |VARCHAR  |NULL|NULL|10    |NULL      |20        |NO      
  * PASSWORD            |VARCHAR  |NULL|NULL|32    |NULL      |64        |NO
  * 
  * ij&gt; describe grouptable;
  * COLUMN_NAME         |TYPE_NAME|DEC&amp;|NUM&amp;|COLUM&amp;|COLUMN_DEF|CHAR_OCTE&amp;|IS_NULL&amp;
  * ------------------------------------------------------------------------------
  * USERID              |VARCHAR  |NULL|NULL|10    |NULL      |20        |NO      
  * GROUPID             |VARCHAR  |NULL|NULL|20    |NULL      |40        |NO
  * 
  * </pre>
  * 
  * @author $Author$
  * @version $Rev$ ($Date$)
  * 
  */
 public class UserManager {
 	private static final String SHOW_ALL = "select login from usertable";
 	private static final String DRIVER = "org.apache.derby.jdbc.ClientDriver";
 	private static final String CREATE_USER = "insert into usertable values(?, ?)";
 	private static final String CREATE_GROUP = "insert into grouptable values(?, ?)";
 
 	private static final char[] HEXADECIMAL = { '0', '1', '2', '3', '4', '5',
 			'6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
 
 	private static String hashPassword(String password)
 			throws NoSuchAlgorithmException {
 		MessageDigest md = MessageDigest.getInstance("MD5");
 		md.reset();
 
 		byte[] bytes = md.digest(password.getBytes());
 		StringBuilder sb = new StringBuilder(2 * bytes.length);
 		for (int i = 0; i < bytes.length; i++) {
 			int low = (int) (bytes[i] & 0x0f);
 			int high = (int) ((bytes[i] & 0xf0) >> 4);
 			sb.append(HEXADECIMAL[high]);
 			sb.append(HEXADECIMAL[low]);
 		}
 		return sb.toString();
 	}
 
 	/** Test database, should be reviewed/replaced. */
	private static final String strUrl = "jdbc:derby://localhost:1527/sun-appserv-samples;user=app;password=adminadmin";
 
 	/** Insert a new user/password in the authentication table. */
 	public void createUser(String login, String password, String group)
 			throws ClassNotFoundException, SQLException,
 			NoSuchAlgorithmException {
 		Connection conn = null;
 		PreparedStatement userStmt = null;
 		PreparedStatement groupStmt = null;
 		try {
 			Class.forName(DRIVER);
 			conn = DriverManager.getConnection(strUrl);
 
 			userStmt = conn.prepareStatement(CREATE_USER);
 			userStmt.setString(1, login);
 			String hPassword = hashPassword(password);
 			userStmt.setString(2, hPassword);
 			userStmt.executeUpdate();
 			userStmt.close();
 
 			groupStmt = conn.prepareStatement(CREATE_GROUP);
 			groupStmt.setString(1, login);
 			groupStmt.setString(2, group);
 			groupStmt.executeUpdate();
 			groupStmt.close();
 		} finally {
 			if (userStmt != null) {
 				userStmt.close();
 			}
 			if (groupStmt != null) {
 				groupStmt.close();
 			}
 			if (conn != null) {
 				conn.close();
 			}
 		}
 	}
 
 	public void showAll() throws SQLException {
 		Connection conn = null;
 		ResultSet set = null;
 		PreparedStatement userStmt = null;
 		try {
 			conn = DriverManager.getConnection(strUrl);
 			userStmt = conn.prepareStatement(SHOW_ALL);
 			set = userStmt.executeQuery();
 			while (set.next()) {
 				System.out.println(set.getString("login"));
 			}
 		} finally {
 			// connection cleanup
 			try {
 				if (set != null) {
 					set.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			try {
 				if (userStmt != null) {
 					userStmt.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			try {
 				if (conn != null) {
 					conn.close();
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 		}
 	}
 
 	public static void main(String args[]) {
 		UserManager manager = null;
 		manager = new UserManager();
 		try {
 			manager.createUser("hendrix", "teste", "customer");
 			manager.createUser("tosh", "teste", "customer");
 			manager.createUser("ozzy", "teste", "customer");
 			manager.createUser("marley", "teste", "admin");
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (SQLException e) {
 			System.out.println("* I guess the users already exist");
 		}
 		try {
 			manager.showAll();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 }
