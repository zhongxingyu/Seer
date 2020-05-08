 package src.objects;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Date;
 
 import org.apache.catalina.User;
 
 import src.utils.DBManager;
 import java.io.Serializable;
 
 public class Users implements Serializable{
 	private int userID = 0;
 	private String first = "";
 	private String last = "";
 	private String user = "";
 	private String pass = "";
 	private Date created = null;
 	private Date paid = null;
 	private Date next = null;
 	private Date log = null;
 	private int  Ex1Reps= 0;
 	private int  Ex1Act= 0;
 	private int  Ex2Reps= 0;
 	private int  Ex2Act= 0;
 	private int  Ex3Reps= 0;
 	private int  Ex3Act= 0;
 	private int  Ex4Reps= 0;
 	private int  Ex4Act= 0;
 	private int  Ex5Reps= 0;
 	private int  Ex5Act= 0;
 	private int  Ex6Reps= 0;
 	private int  Ex6Act= 0;
 	private int  Ex7Reps= 0;
 	private int  Ex7Act= 0;
 	private int  Ex8Reps= 0;
 	private int  Ex8Act= 0;
 	private int  Ex9Reps= 0;
 	private int  Ex9Act= 0;
 	private int  Ex10Reps= 0;
 	private int  Ex10Act = 0;
 	
 	/* Methods */
	public boolean enterData(Users user, Wod wod){
 		Statement st = null;
 		ResultSet rs = null;
 		Connection conn = null;
 		DBManager db = new DBManager();
 		String query = "INSERT INTO ";
 		String value = " VALUES (";
 		boolean success = false;
 		
 		//if admin, the WOD object already contains all data for insert
 		if(user.getName() == "admin"){
 			query += "wod (`Name`, `Type`, `Rounds`, `Date`, `Ex1ID`, `Ex1Reps`, `Ex1RX`, `Ex2ID`, `Ex2Reps`, `Ex2RX`, `Ex3ID`, `Ex3Reps`, `Ex3RX`, `Ex4ID`, `Ex4Reps`, `Ex4RX`, `Ex5ID`, `Ex5Reps`, `Ex5RX`, `Ex6ID`, `Ex6Reps`, `Ex6RX`, `Ex7ID`, `Ex7Reps`, `Ex7RX`, `Ex8ID`, `Ex8Reps`, `Ex8RX`, `Ex9ID`, `Ex9Reps`, `Ex9RX`, `Ex10ID`, `Ex10Reps`, `Ex10RX`) ";
 			value +="'" + wod.getName() + "', '" + wod.getType()+ "', " + wod.getRounds() + ", '" + wod.getDate() + "', " +
 				wod.getEx1ID() +", "+ wod.getEx1Reps()+", "+ wod.getEx1RX()+ ", " +
 				wod.getEx2ID() +", "+ wod.getEx2Reps()+", "+ wod.getEx2RX()+ ", " +
 				wod.getEx3ID() +", "+ wod.getEx3Reps()+", "+ wod.getEx3RX()+", "+
 				wod.getEx4ID() +", "+ wod.getEx4Reps()+", "+ wod.getEx4RX()+", "+
 				wod.getEx5ID() +", "+ wod.getEx5Reps()+", "+ wod.getEx5RX()+", "+
 				wod.getEx6ID() +", "+ wod.getEx6Reps()+", "+ wod.getEx6RX()+", "+
 				wod.getEx7ID() +", "+ wod.getEx7Reps()+", "+ wod.getEx7RX()+", "+
 				wod.getEx8ID() +", "+ wod.getEx8Reps()+", "+ wod.getEx8RX()+", "+
 				wod.getEx9ID() +", "+ wod.getEx9Reps()+", "+ wod.getEx9RX()+", "+
 				wod.getEx10ID() +", "+ wod.getEx10Reps()+", "+ wod.getEx10RX()+ "');";		
 		}else{
 		//if user, the User object contains their workout data
 			query += "userdata (`UserID`, `LogDate`, `WODID`,`Ex1Reps`,`Ex1Act`,`Ex2Reps`,`Ex2Act`,`Ex3Reps`,`Ex3Act`,`Ex4Reps`,`Ex4Act`,`Ex5Reps`,`Ex5Act`,`Ex6Reps`,`Ex6Act`,`Ex7Reps`,`Ex7Act`,`Ex8Reps`,`Ex8Act`,`Ex9Reps`,`Ex9Act`,`Ex10Reps`,`Ex10Act`) ";
 			value += getUserID() + ", '', " +	wod.getWODID() + ", '" + 
 				getEx1Reps()+", "+ getEx1Act()+", "+
 				getEx2Reps()+", "+ getEx2Act()+", "+
 				getEx3Reps()+", "+ getEx3Act()+", "+
 				getEx4Reps()+", "+ getEx4Act()+", "+
 				getEx5Reps()+", "+ getEx5Act()+", "+
 				getEx6Reps()+", "+ getEx6Act()+", "+
 				getEx7Reps()+", "+ getEx7Act()+", "+
 				getEx8Reps()+", "+ getEx8Act()+", "+
 				getEx9Reps()+", "+ getEx9Act()+", "+
 				getEx10Reps()+", "+ getEx10Act()+ "');";
 		}
 		
 		query += value;
 		
 		try{
 			conn = db.connect();
 			st = conn.createStatement();
 	        success = st.execute(query);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}finally{
 			try {
 				conn.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return success;
 		
 	}
 	
 	
	private String getName() {
		// TODO Auto-generated method stub
		return null;
	}


 	/* Getters */
 	public int getUserID() {
 		return userID;
 	}
 
 	public String getFirst() {
 		return first;
 	}
 
 	public String getLast() {
 		return last;
 	}
 
 	public String getUser() {
 		return user;
 	}
 	
 	public String getPass() {
 		return pass;
 	}
 
 	public Date getCreated() {
 		return created;
 	}
 
 	public Date getPaid() {
 		return paid;
 	}
 	
 	public Date getNext() {
 		return next;
 	}	
 
 	public Date getLog() {
 		return log;
 	}
 	
 	/* Setters */ 
 	public void setUserID(int userID) {
 		this.userID = userID;
 	}
 
 	public void setFirst(String first) {
 		this.first = first;
 	}
 
 	public void setLast(String last) {
 		this.last = last;
 	}
 
 	public void setUser(String user) {
 		this.user = user;
 	}
 
 	public void setPass(String pass) {
 		this.pass = pass;
 	}
 
 
 	public void setCreated(Date created) {
 		this.created = created;
 	}
 
 	public void setPaid(Date paid) {
 		this.paid = paid;
 	}
 
 	public void setNext(Date next) {
 		this.next = next;
 	}
 
 
 	public void setLog(Date log) {
 		this.log = log;
 	}
 
 
 	public int getEx1Reps() {
 		return Ex1Reps;
 	}
 
 
 	public void setEx1Reps(int ex1Reps) {
 		Ex1Reps = ex1Reps;
 	}
 
 
 	public int getEx1Act() {
 		return Ex1Act;
 	}
 
 
 	public void setEx1Act(int ex1Act) {
 		Ex1Act = ex1Act;
 	}
 
 
 	public int getEx2Reps() {
 		return Ex2Reps;
 	}
 
 
 	public void setEx2Reps(int ex2Reps) {
 		Ex2Reps = ex2Reps;
 	}
 
 
 	public int getEx2Act() {
 		return Ex2Act;
 	}
 
 
 	public void setEx2Act(int ex2Act) {
 		Ex2Act = ex2Act;
 	}
 
 
 	public int getEx3Reps() {
 		return Ex3Reps;
 	}
 
 
 	public void setEx3Reps(int ex3Reps) {
 		Ex3Reps = ex3Reps;
 	}
 
 
 	public int getEx3Act() {
 		return Ex3Act;
 	}
 
 
 	public void setEx3Act(int ex3Act) {
 		Ex3Act = ex3Act;
 	}
 
 
 	public int getEx4Reps() {
 		return Ex4Reps;
 	}
 
 
 	public void setEx4Reps(int ex4Reps) {
 		Ex4Reps = ex4Reps;
 	}
 
 
 	public int getEx4Act() {
 		return Ex4Act;
 	}
 
 
 	public void setEx4Act(int ex4Act) {
 		Ex4Act = ex4Act;
 	}
 
 
 	public int getEx5Reps() {
 		return Ex5Reps;
 	}
 
 
 	public void setEx5Reps(int ex5Reps) {
 		Ex5Reps = ex5Reps;
 	}
 
 
 	public int getEx5Act() {
 		return Ex5Act;
 	}
 
 
 	public void setEx5Act(int ex5Act) {
 		Ex5Act = ex5Act;
 	}
 
 
 	public int getEx6Reps() {
 		return Ex6Reps;
 	}
 
 
 	public void setEx6Reps(int ex6Reps) {
 		Ex6Reps = ex6Reps;
 	}
 
 
 	public int getEx6Act() {
 		return Ex6Act;
 	}
 
 
 	public void setEx6Act(int ex6Act) {
 		Ex6Act = ex6Act;
 	}
 
 
 	public int getEx7Reps() {
 		return Ex7Reps;
 	}
 
 
 	public void setEx7Reps(int ex7Reps) {
 		Ex7Reps = ex7Reps;
 	}
 
 
 	public int getEx7Act() {
 		return Ex7Act;
 	}
 
 
 	public void setEx7Act(int ex7Act) {
 		Ex7Act = ex7Act;
 	}
 
 
 	public int getEx8Reps() {
 		return Ex8Reps;
 	}
 
 
 	public void setEx8Reps(int ex8Reps) {
 		Ex8Reps = ex8Reps;
 	}
 
 
 	public int getEx8Act() {
 		return Ex8Act;
 	}
 
 
 	public void setEx8Act(int ex8Act) {
 		Ex8Act = ex8Act;
 	}
 
 
 	public int getEx9Reps() {
 		return Ex9Reps;
 	}
 
 
 	public void setEx9Reps(int ex9Reps) {
 		Ex9Reps = ex9Reps;
 	}
 
 
 	public int getEx9Act() {
 		return Ex9Act;
 	}
 
 
 	public void setEx9Act(int ex9Act) {
 		Ex9Act = ex9Act;
 	}
 
 
 	public int getEx10Reps() {
 		return Ex10Reps;
 	}
 
 
 	public void setEx10Reps(int ex10Reps) {
 		Ex10Reps = ex10Reps;
 	}
 
 
 	public int getEx10Act() {
 		return Ex10Act;
 	}
 
 
 	public void setEx10Act(int ex10Act) {
 		Ex10Act = ex10Act;
 	}
 	
 	
 }
